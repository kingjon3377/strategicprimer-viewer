import ceylon.collection {
    MutableList,
    ArrayList,
    LinkedList,
    ListMutator,
    MutableMap,
    naturalOrderTreeMap,
    Queue
}
import ceylon.test {
    test,
    assertEquals
}

import strategicprimer.model.common.map {
    HasKind,
    HasMutableKind,
    HasMutableName,
    HasMutableOwner,
    IFixture,
    IMapNG,
    IMutableMapNG,
    SPMapNG,
    Point,
    MapDimensionsImpl,
    Player,
    PlayerImpl,
    TileFixture,
    PlayerCollection
}

import strategicprimer.model.common.map.fixtures {
    UnitMember
}

import strategicprimer.model.common.map.fixtures.mobile {
    ProxyFor,
    IUnit,
    Unit,
    ProxyUnit,
    AnimalImpl
}
import strategicprimer.model.common.map.fixtures.terrain {
    Oasis,
    Hill,
    Forest
}
import strategicprimer.model.common.map.fixtures.towns {
    TownSize,
    Fortress
}
import strategicprimer.drivers.common {
    SimpleMultiMapModel,
    IDriverModel,
    IWorkerModel
}
import ceylon.logging {
    Logger,
    logger
}
import ceylon.random {
    randomize
}
import lovelace.util.common {
    anythingEqual,
    matchingValue,
    narrowedStream,
    comparingOn,
    todo
}

"Logger."
Logger log = logger(`module strategicprimer.drivers.worker.common`);

"A model to underlie the advancement GUI, etc."
shared class WorkerModel extends SimpleMultiMapModel satisfies IWorkerModel {
    "If [[the argument|fixture]] is a [[Fortress]], return it; otherwise,
     return a [[Singleton]] of the argument. This allows callers to get a
     flattened stream of units, including those in fortresses."
    static {Anything*} flatten(Anything fixture) {
        if (is Fortress fixture) {
            return fixture;
        } else {
            return Singleton(fixture);
        }
    }

    "If the item in the entry is a [[Fortress]], return a stream of its
     contents paired with its location; otherwise, return a [[Singleton]] of
     the argument."
//    see(`function flatten`)
    static {<Point->IFixture>*} flattenEntries(Point->IFixture entry) {
        if (is Fortress item = entry.item) {
            return item.map((each) => entry.key->each);
        } else {
            return Singleton(entry);
        }
    }

    "Add the given unit at the given location in the given map."
    static void addUnitAtLocationImpl(IUnit unit, Point location, IMutableMapNG map) {
        //if (exists fortress = map.fixtures[location] // TODO: syntax sugar once compiler bug fixed
        if (exists fortress = map.fixtures.get(location)
                .narrow<Fortress>().find(matchingValue(unit.owner, Fortress.owner))) {
            fortress.addMember(unit.copy(false));
        } else {
            map.addFixture(location, unit.copy(false));
        }
    }

    "The current player, subject to change by user action."
    variable Player? currentPlayerImpl = null;

    MutableList<UnitMember> dismissedMembers = ArrayList<UnitMember>();

    shared new (IMutableMapNG map) extends SimpleMultiMapModel(map) {}
    shared new copyConstructor(IDriverModel model)
            extends SimpleMultiMapModel.copyConstructor(model) {}

    "The current player, subject to change by user action."
    shared actual Player currentPlayer {
        if (exists temp = currentPlayerImpl) {
            return temp;
        } else {
            for (localMap in allMaps) {
                Player temp = localMap.currentPlayer;
                if (!getUnits(temp).empty) {
                    currentPlayerImpl = temp;
                    return temp;
                }
            } else {
                currentPlayerImpl = map.currentPlayer;
                return map.currentPlayer;
            }
        }
    }
    // Note we *deliberately* do not pass this change through to the maps; this
    // is a read-only operation as far as the map *files* are concerned.
    assign currentPlayer {
        currentPlayerImpl = currentPlayer;
    }

    "Flatten and filter the stream to include only units, and only those owned by the
     given player."
    {IUnit*} getUnitsImpl({Anything*} iter, Player player) =>
            iter.flatMap(flatten).narrow<IUnit>()
                .filter(compose(matchingValue(player.playerId, Player.playerId),
                    IUnit.owner));

    shared actual {Fortress*} getFortresses(Player player) =>
            map.fixtures.items.narrow<Fortress>().filter(matchingValue(player, Fortress.owner));

    "All the players in all the maps."
    shared actual {Player*} players =>
            allMaps.flatMap(IMapNG.players).distinct;

    "Get all the given player's units, or only those of a specified kind."
    shared actual {IUnit*} getUnits(Player player, String? kind) {
        if (exists kind) {
            return getUnits(player).filter(matchingValue(kind, IUnit.kind));
        } else if (subordinateMaps.empty) {
            // Just in case I missed something in the proxy implementation, make sure
            // things work correctly when there's only one map.
            return getUnitsImpl(map.fixtures.items, player)
                .sort(comparingOn(IUnit.name, byIncreasing(String.lowercased)));
        } else {
            value temp = allMaps.flatMap((indivMap) =>
                        getUnitsImpl(indivMap.fixtures.items, player));
            MutableMap<Integer, IUnit&ProxyFor<IUnit>> tempMap =
                    naturalOrderTreeMap<Integer, IUnit&ProxyFor<IUnit>>([]);
            for (unit in temp) {
                Integer key = unit.id;
                ProxyFor<IUnit> proxy;
                if (exists item = tempMap[key]) {
                    proxy = item;
                } else {
                    value newProxy = ProxyUnit.fromParallelMaps(key);
                    tempMap[key] = newProxy;
                    proxy = newProxy;
                }
                proxy.addProxied(unit);
            }
            return tempMap.items.sort(comparingOn(IUnit.name,
                byIncreasing(String.lowercased)));
        }
    }

    """All the "kinds" of units the given player has."""
    shared actual {String*} getUnitKinds(Player player) =>
            getUnits(player).map(IUnit.kind).distinct
                .sort(byIncreasing(String.lowercased));

    "Add the given unit at the given location in all maps." // FIXME: Should copy into subordinate maps, and return either the unit (in one-map case) or a proxy
    void addUnitAtLocation(IUnit unit, Point location) {
        if (subordinateMaps.empty) {
            addUnitAtLocationImpl(unit, location, restrictedMap);
            mapModified = true;
        } else {
            for (eachMap in restrictedAllMaps) {
                addUnitAtLocationImpl(unit, location, eachMap);
                eachMap.modified = true;
            }
        }
    }

    "Add a unit to all the maps, at the location of its owner's HQ in the main map."
    shared actual void addUnit(IUnit unit) {
        variable [Fortress, Point]? temp = null;
        for (point->fixture in narrowedStream<Point, Fortress>(map.fixtures)
                .filter(compose(matchingValue(unit.owner.playerId, Player.playerId),
                    compose(Fortress.owner, Entry<Point, Fortress>.item)))) {
            if ("HQ" == fixture.name) {
                addUnitAtLocation(unit, point);
                return;
            } else if (!temp exists) {
                temp = [fixture, point];
            }
        } else {
            if (exists [fortress, loc] = temp) {
                log.info("Added unit at fortress ``fortress.name``, not HQ");
                addUnitAtLocation(unit, loc);
                return;
            } else if (!unit.owner.independent) {
                log.warn("No suitable location found for unit ``unit.name``, owned by ``
                    unit.owner``");
            }
        }
    }

    "Get a unit by its owner and ID."
    shared actual IUnit? getUnitByID(Player owner, Integer id) =>
            getUnits(owner).find(matchingValue(id, IUnit.id));

    Boolean unitMatching(IUnit unit)(Point->IFixture entry) {
        value location->fixture = entry;
        if (is IUnit fixture, fixture.id == unit.id, fixture.owner == unit.owner) {
            return true;
        } else {
            return false;
        }
    }

    """Remove the given unit from the map. It must be empty, and may be
       required to be owned by the current player. The operation will also fail
       if "matching" units differ in name or kind from the provided unit.
       Returns [[true]] if the preconditions were met and the unit was removed,
       and [[false]] otherwise. To make an edge case explicit, if there are no
       matching units in any map the method returns [[false]]."""
    shared actual Boolean removeUnit(IUnit unit) {
        log.trace("In WorkerModel.removeUnit()");
        MutableList<IMutableMapNG->[Point, IUnit]> delenda =
            ArrayList<IMutableMapNG->[Point, IUnit]>();
        for (map in restrictedAllMaps) {
            if (exists location->fixture = map.fixtures.flatMap(flattenEntries)
                    .find(unitMatching(unit))) {
                log.trace("Map has matching unit");
                assert (is IUnit fixture);
                if (fixture.kind == unit.kind, fixture.name == unit.name, fixture.empty) {
                    log.trace("Matching unit meets preconditions");
                    delenda.add(map->[location, fixture]);
                } else {
                    log.warn("Matching unit in ``map.filename else "an unsaved map"`` fails preconditions for removal");
                    return false;
                }
            }
        }
        if (delenda.empty) {
            log.trace("No matching units");
            return false;
        }
        for (map->[location, fixture] in delenda) {
            if (fixture in map.fixtures.get(location)) { // TODO: syntax sugar
                map.removeFixture(location, fixture);
            } else {
                for (fort in map.fixtures.get(location).narrow<Fortress>()) { // TODO: syntax sugar
                    if (fixture in fort) {
                        fort.removeMember(fixture);
                        break;
                    }
                } else {
                    log.warn("Failed to find unit to remove that we thought might be in a fortress");
                }
            }
        }
        log.trace("Finished removing matching unit(s) from map(s)");
        return true;
    }

    "Move a unit-member from one unit to another in the presence of proxies,
     i.e. when each unit and unit-member represents corresponding units and
     unit members in multiple maps and the same operations must be applied to
     all of them.

     The proxy code is some of the most difficult and delicate code in the
     entire suite, and I'm *pretty* sure the algorithm this method implements
     is correct ...

     Returns [[true]] if our preconditions were met and so we did the move, and
     [[false]] when preconditions were not met and the caller should fall back
     to the non-proxy algorithm."
    todo("Add a test of this method.")
    Boolean moveProxied(UnitMember&ProxyFor<out UnitMember> member, ProxyUnit old,
            ProxyUnit newOwner) {
        if (old.proxied.size == newOwner.proxied.size,
                old.proxied.size == member.proxied.size) {
            Queue<UnitMember>&{UnitMember*} members = LinkedList<UnitMember>();
            Queue<IUnit>&{IUnit*} newList = LinkedList<IUnit>();
            for ([item, innerOld, innerNew] in
                    zip(member.proxied, zipPairs(old.proxied, newOwner.proxied))) {
                innerOld.removeMember(item);
                members.offer(item);
                newList.offer(innerNew);
            }
            for ([unit, innerMember] in zipPairs(newList, members)) {
                unit.addMember(innerMember);
            }
            for (map in restrictedAllMaps) {
                map.modified = true;
            }
            return true;
        } else {
            return false;
        }
    }

    "Move a unit-member from one unit to another. If all three objects are
     proxies, we use a special algorithm that unwraps the proxies, which was
     extracted as [[moveProxied]]."
    shared actual void moveMember(UnitMember member, IUnit old, IUnit newOwner) {
        if (is ProxyFor<out UnitMember> member, is ProxyUnit old,
                is ProxyUnit newOwner, moveProxied(member, old, newOwner)) {
            return;
        }
        for (map in restrictedAllMaps) {
            if (exists matchingOld = getUnitsImpl(map.fixtures.items, old.owner)
                        .filter(matchingValue(old.kind, IUnit.kind))
                        .filter(matchingValue(old.name, IUnit.name))
                        .find(matchingValue(old.id, IUnit.id)),
                    exists matchingMember = matchingOld.find(member.equals), // TODO: equals() isn't ideal for finding a matching member ...
                    exists matchingNew = getUnitsImpl(map.fixtures.items, newOwner.owner)
                        .filter(matchingValue(newOwner.kind, IUnit.kind))
                        .filter(matchingValue(newOwner.name, IUnit.name))
                        .find(matchingValue(newOwner.id, IUnit.id))) {
                matchingOld.removeMember(matchingMember);
                matchingNew.addMember(matchingMember);
                map.modified = true;
            }
        }
    }

    shared actual void dismissUnitMember(UnitMember member) {
        variable Boolean any = false;
        // TODO: Handle proxies specially?
        for (map in restrictedAllMaps) {
            for (unit in getUnitsImpl(map.fixtures.items, currentPlayer)) {
                if (exists matching = unit.find(member.equals)) { // FIXME: equals() will really not do here ...
                    any = true;
                    unit.removeMember(matching);
                    map.modified = true;
                    break;
                }
            }
        }
        if (any) {
            dismissedMembers.add(member);
        }
    }

    shared actual {UnitMember*} dismissed => dismissedMembers;

    // TODO: Notification events should come from the map, instead of here (as
    // we might add one to this method), so UI could just call this and the
    // tree model could listen to the map---so the worker-mgmt UI would update
    // if a unit were added through the map-viewer UI.
    shared actual void addUnitMember(IUnit unit, UnitMember member) {
        for (map in restrictedAllMaps) {
            if (exists matching = getUnitsImpl(map.fixtures.items, unit.owner)
                    .filter(matchingValue(unit.name, IUnit.name))
                    .filter(matchingValue(unit.kind, IUnit.kind))
                    .find(matchingValue(unit.id, IUnit.id))) {
                matching.addMember(member.copy(false));
                map.modified = true;
                continue;
            }
        }
    }

    shared actual Boolean renameItem(HasMutableName item, String newName) {
        variable Boolean any = false;
        if (is IUnit item) {
            for (map in restrictedAllMaps) {
                if (is HasMutableName matching = getUnitsImpl(map.fixtures.items, item.owner)
                        .filter(matchingValue(item.name, IUnit.name))
                        .filter(matchingValue(item.kind, IUnit.kind))
                        .find(matchingValue(item.id, IUnit.id))) {
                    any = true;
                    matching.name = newName;
                    map.modified = true;
                }
            }
            if (!any) {
                log.warn("Unable to find unit to rename");
            }
            return any;
        } else if (is UnitMember item) {
            for (map in restrictedAllMaps) {
                if (exists matching = getUnitsImpl(map.fixtures.items, currentPlayer)
                        .flatMap(identity).narrow<HasMutableName>()
                        .filter(matchingValue(item.name, HasMutableName.name))
                        .find(matchingValue(item.id, UnitMember.id))) { // FIXME: We should have a firmer identification than just name and ID
                    any = true;
                    matching.name = newName;
                    map.modified = true;
                }
            }
            if (!any) {
                log.warn("Unable to find unit member to rename");
            }
            return any;
        } else {
            log.warn("Unable to find item to rename");
            return false;
        }
    }

    shared actual Boolean changeKind(HasKind item, String newKind) {
        variable Boolean any = false;
        if (is IUnit item) {
            for (map in restrictedAllMaps) {
                if (is HasMutableKind matching = getUnitsImpl(map.fixtures.items, item.owner)
                        .filter(matchingValue(item.name, IUnit.name))
                        .filter(matchingValue(item.kind, IUnit.kind))
                        .find(matchingValue(item.id, IUnit.id))) {
                    any = true;
                    matching.kind = newKind;
                    map.modified = true;
                }
            }
            if (!any) {
                log.warn("Unable to find unit to change kind");
            }
            return any;
        } else if (is UnitMember item) {
            for (map in restrictedAllMaps) {
                if (exists matching = getUnitsImpl(map.fixtures.items, currentPlayer)
                        .flatMap(identity).narrow<HasMutableKind>()
                        .filter(matchingValue(item.kind, HasMutableKind.kind))
                        .find(matchingValue(item.id, UnitMember.id))) { // FIXME: We should have a firmer identification than just kind and ID
                    any = true;
                    matching.kind = newKind;
                    map.modified = true;
                }
            }
            if (!any) {
                log.warn("Unable to find unit member to change kind");
            }
            return any;
        } else {
            log.warn("Unable to find item to change kind");
            return false;
        }
    }

    shared actual Boolean addSibling(UnitMember existing, UnitMember sibling) {
        variable Boolean any = false;
        for (map in restrictedAllMaps) {
            for (unit in getUnitsImpl(map.fixtures.items, currentPlayer)) {
                if (existing in unit) { // TODO: look beyond equals() for matching-in-existing?
                    unit.addMember(sibling.copy(false));
                    any = true;
                    map.modified = true;
                    break;
                }
            }
        }
        return any;
    }

    {IFixture*} flattenIncluding(IFixture fixture) {
        if (is {IFixture*} fixture) {
            return fixture.follow(fixture);
        } else {
            return Singleton(fixture);
        }
    }

    "Change the owner of the given item in all maps. Returns [[true]] if this
     succeeded in any map, [[false]] otherwise."
    shared actual Boolean changeOwner(HasMutableOwner item, Player newOwner) {
        variable Boolean any = false;
        for (map in restrictedAllMaps) {
            if (exists matching = map.fixtures.items.flatMap(flattenIncluding)
                    .flatMap(flattenIncluding).narrow<HasMutableOwner>()
                    .find(item.equals)) { // TODO: equals() is not the best way to find it ...
                if (!newOwner in map.players) {
                    map.addPlayer(newOwner);
                }
                matching.owner = map.players.getPlayer(newOwner.playerId);
                map.modified = true;
                any = true;
            }
        }
        return any;
    }
}

"Test of the worker model."
object workerModelTests {
    "Helper method: Flatten any proxies in the list by replacing them with what they are
     proxies for."
    {T*} filterProxies<T>(T* list) given T satisfies Object {
        MutableList<T> retval = ArrayList<T>();
        for (item in list) {
            if (is ProxyFor<out T> item) {
                retval.addAll(item.proxied);
            } else {
                retval.add(item);
            }
        }
        return retval.sequence();
    }

    "Helper method: Add an item to multiple lists at once."
    void addItem<T>(T item, ListMutator<T>* lists) {
        for (list in lists) {
            list.add(item);
        }
    }

    "Test of the [[IWorkerModel.getUnits]] method."
    test
    shared void testGetUnits() {
        MutableList<TileFixture> fixtures = ArrayList<TileFixture>();
        fixtures.add(Oasis(14));
        fixtures.add(AnimalImpl("animal", false, "wild", 1));
        MutableList<IUnit> listOne = ArrayList<IUnit>();
        Player playerOne = PlayerImpl(0, "player1");
        addItem(Unit(playerOne, "one", "unitOne", 2), fixtures, listOne);
        MutableList<IUnit> listTwo = ArrayList<IUnit>();
        Player playerTwo = PlayerImpl(1, "player2");
        addItem(Unit(playerTwo, "two", "unitTwo", 3), fixtures, listTwo);
        Player playerThree = PlayerImpl(2, "player3");
        Player playerFour = PlayerImpl(3, "player4");
        Fortress fort = Fortress(playerFour, "fort", 4, TownSize.small);
        IUnit unit = Unit(playerThree, "three", "unitThree", 5);
        fort.addMember(unit);
        MutableList<IUnit> listThree = ArrayList<IUnit>();
        listThree.add(unit);
        fixtures.add(fort);
        fixtures.add(Forest("forest", false, 10));
        fixtures.add(Hill(7));
        addItem(Unit(playerOne, "four", "unitFour", 6), fixtures, listOne);
        fixtures.add(Oasis(8));
        value shuffled = randomize(fixtures);
        IMutableMapNG map = SPMapNG(MapDimensionsImpl(3, 3, 2), PlayerCollection(), -1);
        for ([point, fixture] in zipPairs(map.locations, shuffled)) {
            map.addFixture(point, fixture);
        }
        IWorkerModel model = WorkerModel(map);
        Boolean iterableEquality<T>(Anything one, Anything two) given T satisfies Object {
            if (is {T*} one, is {T*} two) {
                return one.containsEvery(two) &&two.containsEvery(one);
            } else {
                return anythingEqual(one, two);
            }
        }
        assertEquals(filterProxies(*model.getUnits(playerOne)),
            listOne, "Got all units for player 1", iterableEquality<IUnit>);
        assertEquals(filterProxies(*model.getUnits(playerTwo)), listTwo,
            "Got all units for player 2", iterableEquality<IUnit>);
        assertEquals(filterProxies(*model.getUnits(playerThree)), listThree,
            "Got all units for player 3", iterableEquality<IUnit>);
    }
}
