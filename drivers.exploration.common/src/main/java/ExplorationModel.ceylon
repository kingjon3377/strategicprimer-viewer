import ceylon.collection {
    ArrayList,
    MutableList
}
import ceylon.numeric.float {
    ceiling
}

import strategicprimer.drivers.common {
    SimpleMultiMapModel,
    IDriverModel,
    SelectionChangeListener
}
import strategicprimer.model.common.map {
    FakeFixture,
    IFixture,
    Player,
    HasKind,
    HasMutableKind,
    HasMutableName,
    HasMutableOwner,
    HasOwner,
    MapDimensions,
    Point,
    River,
    TileFixture,
    TileType,
    IMutableMapNG,
    Direction,
    IMapNG
}
import strategicprimer.model.common.map.fixtures {
    Ground,
    MineralFixture,
    UnitMember
}
import strategicprimer.model.common.map.fixtures.mobile {
    Animal,
    AnimalTracks,
    IMutableUnit,
    IUnit,
    ProxyUnit,
    MobileFixture
}
import strategicprimer.model.common.map.fixtures.resources {
    CacheFixture,
    Grove,
    Meadow,
    Mine,
    MineralVein
}
import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}
import strategicprimer.model.common.map.fixtures.towns {
    IMutableFortress,
    Village,
    IFortress
}
import ceylon.random {
    randomize
}
import lovelace.util.common {
    anythingEqual,
    matchingValue
}
import com.vasileff.ceylon.structures {
    Multimap
}

// TODO: Make sure all methods are still used; at least one driver now uses a different model interface.
"A model for exploration apps."
shared class ExplorationModel extends SimpleMultiMapModel satisfies IExplorationModel {
    """A fixture is "diggable" if it is a [[MineralFixture]] or a [[Mine]]."""
    static Boolean isDiggable(TileFixture fixture) => fixture is MineralFixture|Mine;

    static {IFixture*} flattenIncluding(IFixture fixture) {
        if (is {IFixture*} fixture) {
            return fixture.follow(fixture);
        } else {
            return Singleton(fixture);
        }
    }

    "If the item in the entry is a [[fortress|IFortress]], return a stream of its
     contents paired with its location; otherwise, return a [[Singleton]] of
     the argument."
    static {<Point->IFixture>*} flattenEntries(Point->IFixture entry) {
        if (is IFortress item = entry.item) {
            return item.map((each) => entry.key->each);
        } else {
            return Singleton(entry);
        }
    }

    """Check whether two fixtures are "equal enough" for the purposes of updating a map
       after digging. This method is needed because equals() in
       [[strategicprimer.model.common.map.fixtures.resources::StoneDeposit]] and
       [[strategicprimer.model.common.map.fixtures.resources::MineralVein]] compares
       DCs."""
    static Boolean areDiggablesEqual(IFixture firstFixture, IFixture secondFixture) =>
            firstFixture == secondFixture || firstFixture.copy(true) == secondFixture
                .copy(true);

    "If a unit's motion could be observed by someone allied to another (non-independent)
     player (which at present means the unit is moving *to* a tile two or fewer tiles away
     from the watcher), print a message saying so to stdout."
    static void checkAllNearbyWatchers(IMapNG map, IUnit unit, Point dest) {
        MapDimensions dimensions = map.dimensions;
        String description;
        if (unit.owner.independent) {
            description = "``unit.shortDescription`` (ID #``unit.id``)";
        } else {
            description = unit.shortDescription;
        }
        for (point in surroundingPointIterable(dest, dimensions).distinct) {
//            for (fixture in map.fixtures[point].narrow<HasOwner>()) { // TODO: syntax sugar once compiler bug fixed
            for (fixture in map.fixtures.get(point).narrow<HasOwner>()) {
                if (!fixture.owner.independent, fixture.owner != unit.owner) {
                    process.writeLine( // FIXME: Make a new interface for reporting this, and write to UI in a listener
                        "Motion of ``description`` to ``dest`` could be observed by ``
                            fixture.shortDescription`` at ``point``");
                }
            }
        }
    }

    "Remove a unit from a location, even if it's in a fortress."
    static void removeImpl(IMutableMapNG map, Point point, IUnit unit) {
        variable Boolean outside = false;
//        for (fixture in map.fixtures[point]) { // TODO: syntax sugar once compiler bug fixed
        for (fixture in map.fixtures.get(point)) {
            if (unit == fixture) {
                outside = true;
                break;
            } else if (is IMutableFortress fixture, exists item = fixture.find(unit.equals)) {
                fixture.removeMember(item);
                return;
            }
        }
        if (outside) {
            map.removeFixture(point, unit);
        }
    }

    "Ensure that a given map has at least terrain information for the specified location."
    static void ensureTerrain(IMapNG mainMap, IMutableMapNG map, Point point) {
        if (!map.baseTerrain[point] exists) {
            map.baseTerrain[point] = mainMap.baseTerrain[point];
        }
//        if (mainMap.mountainous[point]) { // TODO: syntax sugar once compiler bug fixed
        if (mainMap.mountainous.get(point)) {
            map.mountainous[point] = true;
        }
        //map.addRivers(point, *mainMap.rivers[point]); // TODO: syntax sugar
        map.addRivers(point, *mainMap.rivers.get(point));
        // TODO: Should we copy roads here?
    }

    "Whether the given fixture is contained in the given stream."
    static Boolean doesStreamContainFixture({IFixture*} stream, IFixture fixture) {
        for (member in stream) {
            if (member == fixture) {
                return true;
            } else if (is {IFixture*} member, doesStreamContainFixture(member, fixture)) {
                return true;
            }
        }
        return false;
    }

    "Whether the given fixture is at the given location in the given map."
    static Boolean doesLocationHaveFixture(IMapNG map, Point point, TileFixture fixture)
            => doesStreamContainFixture(map.fixtures.get(point), fixture); // TODO: syntax sugar

    """A "plus one" method with a configurable, low "overflow"."""
    static Integer increment(
            "The number to increment"
            Integer number,
            "The maximum number we want to return"
            Integer max) => if (number >= max) then 0 else number + 1;

    """A "minus one" method that "underflows" after 0 to a configurable, low value."""
    static Integer decrement(
            "The number to decrement"
            Integer number,
            """The number to "underflow" to"""
            Integer max) => if (number <= 0) then max else number - 1;

    "The intersection of two sets; here so it can be passed as a method reference rather
     than a lambda in [[playerChoices]]."
    static Set<T> intersection<T>(Set<T> one, Set<T> two) given T satisfies Object =>
            one.intersection(two);

    "If [[fixture]] is a [[fortress|IFortress]], return it; otherwise, return a Singleton
     containing it. This is intended to be used in [[Iterable.flatMap]]."
    static {IFixture*} unflattenNonFortresses(TileFixture fixture) {
        if (is IFortress fixture) {
            return fixture;
        } else {
            return Singleton(fixture);
        }
    }

    MutableList<MovementCostListener> mcListeners = ArrayList<MovementCostListener>();
    MutableList<SelectionChangeListener> scListeners =
            ArrayList<SelectionChangeListener>();

    "The currently selected unit and its location."
    variable [Point, IUnit?] selection = [Point.invalidPoint, null];

    shared new (IMutableMapNG map) extends SimpleMultiMapModel(map) {}
    shared new copyConstructor(IDriverModel model)
            extends SimpleMultiMapModel.copyConstructor(model) {}

    "All the players shared by all the maps." // TODO: Move to IMultiMapModel?
    shared actual {Player*} playerChoices => allMaps.map(IMapNG.players)
        .map(set).fold(set(map.players))(intersection);

    "Collect all the units in the main map belonging to the specified player."
    shared actual {IUnit*} getUnits(Player player) =>
            map.fixtures.items.flatMap(unflattenNonFortresses)
                .narrow<IUnit>().filter(matchingValue(player, HasOwner.owner));

    "Tell listeners that the selected point changed."
    void fireSelectionChange(Point old, Point newSelection) {
        for (listener in scListeners) {
            log.trace("Notifying a listener of selected-point change");
            listener.selectedPointChanged(old, newSelection);
        }
    }

    "Tell listeners that the selected unit changed."
    void fireSelectedUnitChange(IUnit? old, IUnit? newSelection) {
        for (listener in scListeners) {
            log.trace("Notifying a listener of selected-unit change");
            listener.selectedUnitChanged(old, newSelection);
        }
    }

    "Tell listeners to deduct a cost from their movement-point totals."
    void fireMovementCost(Integer cost) {
        for (listener in mcListeners) {
            listener.deduct(cost);
        }
    }

    "Get the location one tile in the given direction from the given point."
    shared actual Point getDestination(Point point, Direction direction) {
        MapDimensions dims = mapDimensions;
        Integer maxColumn = dims.columns - 1;
        Integer maxRow = dims.rows - 1;
        Integer row = point.row;
        Integer column = point.column;
        switch (direction)
        case (Direction.east) { return Point(row, increment(column, maxColumn)); }
        case (Direction.north) { return Point(decrement(row, maxRow), column); }
        case (Direction.northeast) {
            return Point(decrement(row, maxRow), increment(column, maxColumn));
        }
        case (Direction.northwest) {
            return Point(decrement(row, maxRow), decrement(column, maxColumn));
        }
        case (Direction.south) { return Point(increment(row, maxRow), column); }
        case (Direction.southeast) {
            return Point(increment(row, maxRow), increment(column, maxColumn));
        }
        case (Direction.southwest) {
            return Point(increment(row, maxRow), decrement(column, maxColumn));
        }
        case (Direction.west) { return Point(row, decrement(column, maxColumn)); }
        case (Direction.nowhere) { return point; }
    }

    void fixMovedUnits(Point base) {
        {<Point->TileFixture>*} localFind(IMapNG mapParam, TileFixture target) =>
                mapParam.fixtures
                    .filter(matchingValue(target, Entry<Point, TileFixture>.item));
        // TODO: Unit vision range
        {Point*} points = surroundingPointIterable(base, map.dimensions, 2);
        for (submap in restrictedSubordinateMaps) { // TODO: Can we limit use of mutability to a narrower critical section?
            for (point in points) {
                for (fixture in submap.fixtures.get(point).narrow<MobileFixture>()) { // TODO: syntax sugar once bug fixed
                    for (innerPoint->match in localFind(submap, fixture)) {
                        if (innerPoint != point,
                                !map.fixtures.get(innerPoint).contains(match)) {// TODO: syntax sugar
                            submap.removeFixture(innerPoint, match);
                            submap.modified = true;
                        }
                    }
                }
            }
        }
    }

    "Move the currently selected unit from its current location one tile in the specified
     direction. Moves the unit in all maps where the unit *was* in that tile, copying
     terrain information if the tile didn't exist in a subordinate map. If movement in the
     specified direction is impossible, we update all subordinate maps with the terrain
     information showing that, then re-throw the exception; callers should deduct a
     minimal MP cost (though we notify listeners of that cost). We return the cost of the
     move in MP, which we also tell listeners about."
    throws(`class TraversalImpossibleException`,
        "if movement in the specified direction is impossible")
    shared actual Integer move(
            "The direction to move"
            Direction direction,
            "How hastily the explorer is moving"
            Speed speed) {
        [Point, IUnit?] local = selection;
        Point point = local.first;
        assert (exists unit = local.rest.first);
        Point dest = getDestination(point, direction);
        if (exists terrain = map.baseTerrain[dest],
                exists startingTerrain = map.baseTerrain[point],
                    ((simpleMovementModel.landMovementPossible(terrain) &&
                            startingTerrain != TileType.ocean) ||
                        (startingTerrain == TileType.ocean &&
                            terrain == TileType.ocean))) {
            Integer base;
            if (dest == point) {
                base = 1;
            } else {
//                {TileFixture*} fixtures = map.fixtures[dest]; // TODO: syntax sugar once compiler bug fixed
                {TileFixture*} fixtures = map.fixtures.get(dest);
//                base = movementCost(map.baseTerrain[dest],
                base = simpleMovementModel.movementCost(map.baseTerrain.get(dest),
                    map.fixtures[dest]?.narrow<Forest>()?.first exists,
//                    map.mountainous[dest],
                    map.mountainous.get(dest),
                        simpleMovementModel.riversSpeedTravel(direction,
//                          map.rivers[point],
                            map.rivers.get(point),
//                        map.rivers[dest]), fixtures);
                        map.rivers.get(dest)), fixtures);
            }
            Integer retval = (ceiling(base * speed.mpMultiplier) + 0.1).integer;
            removeImpl(restrictedMap, point, unit);
            restrictedMap.addFixture(dest, unit);
            mapModified = true;
            for (subMap in restrictedSubordinateMaps) { // FIXME: Use copyToSubMaps()
                if (doesLocationHaveFixture(subMap, point, unit)) {
                    ensureTerrain(map, subMap, dest);
                    removeImpl(subMap, point, unit);
                    subMap.addFixture(dest, unit);
                    subMap.modified = true;
                }
            }
            selection = [dest, unit];
            fireSelectionChange(point, dest);
            fireMovementCost(retval);
            checkAllNearbyWatchers(map, unit, dest);
            fixMovedUnits(dest);
            return retval;
        } else {
            if (!map.baseTerrain[point] exists) {
                log.trace("Started outside explored territory in main map");
            } else if (!map.baseTerrain[dest] exists) {
                log.trace("Main map doesn't have terrain for destination");
            } else {
                assert (exists terrain = map.baseTerrain[dest],
                    exists startingTerrain = map.baseTerrain[point]);
                if (simpleMovementModel.landMovementPossible(terrain) &&
                        startingTerrain == TileType.ocean) {
                    log.trace("Starting in ocean, trying to get to ``terrain``");
                } else if (startingTerrain == TileType.ocean, terrain != TileType.ocean) {
                    log.trace("Land movement not possible from ocean to ``terrain``");
                } else if (startingTerrain != TileType.ocean, terrain == TileType.ocean) {
                    log.trace("Starting in ``startingTerrain``, trying to get to ocean");
                } else {
                    log.trace("Unknown reason for movement-impossible condition");
                }
            }
            for (subMap in restrictedSubordinateMaps) {
                ensureTerrain(map, subMap, dest);
                subMap.modified = true;
            }
            fireMovementCost(1);
            throw TraversalImpossibleException();
        }
    }

    """Search the main map for the given fixture. Returns the first location found (search
       order is not defined) containing a fixture "equal to" the specified one."""
    shared actual Point find(TileFixture fixture) {
        for (point in map.locations) {
            if (doesLocationHaveFixture(map, point, fixture)) {
                return point;
            }
        } else {
            return Point.invalidPoint;
        }
    }

    Boolean mapsAgreeOnLocation(IUnit unit) {
        if (is ProxyUnit unit) {
            if (exists first = unit.proxied.first) {
                return mapsAgreeOnLocation(first);
            } else {
                return false;
            }
        }
        Point mainLoc = find(unit);
        if (!mainLoc.valid) {
            return false;
        }
        for (subMap in subordinateMaps) {
            for (point in subMap.locations) {
                if (doesLocationHaveFixture(subMap, point, unit)) {
                    if (point != mainLoc) {
                        return false;
                    } else {
                        break;
                    }
                }
            }
        }
        return true;
    }

    "The currently selected unit."
    shared actual IUnit? selectedUnit => selection.rest.first;
    "Select the given unit."
    assign selectedUnit {
        Point oldLoc = selection.first;
        IUnit? oldSelection = selection.rest.first;
        Point loc;
        if (exists selectedUnit) {
            if (!mapsAgreeOnLocation(selectedUnit)) {
                log.warn("Maps containing that unit don't all agree on its location");
            }
            log.trace("Setting a newly selected unit");
            loc = find(selectedUnit);
            if (loc.valid) {
                log.trace("Found at ``loc``");
            } else {
                log.trace("Not found using our 'find' method");
            }
        } else {
            log.trace("Unsetting currently-selected-unit property");
            loc = Point.invalidPoint;
        }
        selection = [loc, selectedUnit];
        fireSelectionChange(oldLoc, loc);
        fireSelectedUnitChange(oldSelection, selectedUnit);
    }

    "The location of the currently selected unit."
    shared actual Point selectedUnitLocation => selection.first;

    "Add a selection-change listener."
    shared actual void addSelectionChangeListener(SelectionChangeListener listener) =>
            scListeners.add(listener);
    "Remove a selection-change listener."
    shared actual void removeSelectionChangeListener(SelectionChangeListener listener) =>
            scListeners.remove(listener);

    "Add a movement-cost listener."
    shared actual void addMovementCostListener(MovementCostListener listener) =>
            mcListeners.add(listener);
    "Remove a movement-cost listener."
    shared actual void removeMovementCostListener(MovementCostListener listener) =>
            mcListeners.remove(listener);

    "If there is a currently selected unit, make any independent villages at its location
     change to be owned by the owner of the currently selected unit. This costs MP."
    shared actual void swearVillages() {
        [Point, IUnit?] localSelection = selection;
        Point currentPoint = localSelection.first;
        if (exists unit = localSelection.rest.first) {
            Player owner = unit.owner;
            {Village*} villages = allMaps
                .flatMap(shuffle(compose(Multimap<Point, TileFixture>.get,
                    IMapNG.fixtures))(currentPoint))
                .narrow<Village>().filter(compose(Player.independent, Village.owner));
            if (!villages.empty) {
                variable Boolean subordinate = false;
                for (village in villages) {
                    village.owner = owner;
                    for (subMap in restrictedAllMaps) {
                        subMap.addFixture(currentPoint, village.copy(subordinate));
                        subordinate = true;
                        subMap.modified = true;
                    }
                }
                IMapNG mainMap = map;
                {Point*} surroundingPoints =
                        surroundingPointIterable(currentPoint, mapDimensions, 1);
                for (point in surroundingPoints) {
                    for (subMap in restrictedSubordinateMaps) {
                        ensureTerrain(mainMap, subMap, point);
                        Forest? subForest =
                                subMap.fixtures[point]?.narrow<Forest>()?.first;
                        if (exists forest = map.fixtures[point]?.narrow<Forest>()?.first,
                                !subForest exists) {
                            subMap.addFixture(point, forest);
                        }
                    }
                }
                {[Point, TileFixture]*} surroundingFixtures = surroundingPoints
//                            .flatMap((point) => mainMap.fixtures[point] // TODO: syntax sugar once compiler bug fixed
                            .flatMap((point) => mainMap.fixtures.get(point)
                                .map((fixture) => [point, fixture]));
                [Point, TileFixture]? vegetation = surroundingFixtures
                        .narrow<[Point, Meadow|Grove]>().first;
                [Point, TileFixture]? animal = surroundingFixtures
                        .narrow<[Point, Animal]>().first;
                for (subMap in restrictedSubordinateMaps) {
                    if (exists vegetation) {
                        subMap.addFixture(vegetation.first,
                            vegetation.rest.first.copy(true));
                    }
                    if (exists animal) {
                        subMap.addFixture(animal.first, animal.rest.first.copy(true));
                    }
                }
            }
            fireMovementCost(5);
        }
    }

    "If there is a currently selected unit, change one [[Ground]],
     [[strategicprimer.model.common.map.fixtures.resources::StoneDeposit]], or
     [[MineralVein]] at the location of that unit from unexposed to exposed (and discover
     it). This costs MP."
    shared actual void dig() {
        Point currentPoint = selection.first;
        if (currentPoint.valid) {
            IMutableMapNG mainMap = restrictedMap;
            variable {TileFixture*} diggables =
//                    mainMap.fixtures[currentPoint].filter(isDiggable); // TODO: syntax sugar once compiler bug fixed
                    mainMap.fixtures.get(currentPoint).filter(isDiggable);
            if (diggables.empty) {
                return;
            }
            variable Integer i = 0;
            variable Boolean first = true;
            while (first || (i < 4 && !diggables.first is Ground)) {
                diggables = randomize(diggables);
                first = false;
                i++;
            }
            assert (exists oldFixture = diggables.first);
            TileFixture newFixture = oldFixture.copy(false);
            if (is Ground newFixture) { // TODO: Extract an interface for this field so we only have to do one test
                newFixture.exposed = true;
            } else if (is MineralVein newFixture) {
                newFixture.exposed = true;
            }
            void addToMap(IMutableMapNG map, Boolean condition) {
//                if (map.fixtures[currentPoint] // TODO: syntax sugar once compiler bug fixed
                if (map.fixtures.get(currentPoint)
                        .any(curry(areDiggablesEqual)(oldFixture))) {
                    map.replace(currentPoint, oldFixture, newFixture.copy(condition));
                } else {
                    map.addFixture(currentPoint, newFixture.copy(condition));
                }
            }
            variable Boolean subsequent = false;
            for (subMap in restrictedAllMaps) {
                addToMap(subMap, subsequent);
                subsequent = true;
                subMap.modified = true;
            }
            fireMovementCost(4);
        }
    }

    "Add the given [[unit]] at the given [[location]]."
    shared actual void addUnitAtLocation(IUnit unit, Point location) { // TODO: If more than one map, return a proxy for the units; otherwise, return the unit
        for (indivMap in restrictedAllMaps) {
            indivMap.addFixture(location, unit); // FIXME: Check for existing matching unit there already
            indivMap.modified = true;
        }
    }

    "Copy the given fixture from the main map to subordinate maps. (It is found
     in the main map by ID, rather than trusting the input.) If it is a cache,
     remove it from the main map. If [[zero]], remove sensitive information
     from the copies."
    shared actual Boolean copyToSubMaps(Point location, TileFixture fixture, Boolean zero) {
        TileFixture? matching;
        variable Boolean retval = false;
        if (is FakeFixture fixture) {
            // Skip it! It'll corrupt the output XML!
            return false;
        } else if (is AnimalTracks fixture) {
            matching = fixture;
        } else {
            matching = map.fixtures.get(location).find(matchingValue(fixture.id, TileFixture.id));
        }
        if (exists matching) {
            for (subMap in restrictedSubordinateMaps) {
                retval = subMap.addFixture(location, matching.copy(zero)) || retval;
                // We do *not* use the return value because it returns false if an existing fixture was *replaced*
                subMap.modified = true;
            }

            if (is CacheFixture matching) {
                restrictedMap.removeFixture(location, matching); // TODO: make removeFixture() return Boolean, true if anything was removed
                retval = true;
                restrictedMap.modified = true;
            }
        } else {
            log.warn("Skipping because not in the main map");
        }
        return retval;
    }

    "Copy any terrain, mountain, rivers, and roads from the main map to subordinate maps."
    shared actual void copyTerrainToSubMaps(Point location) {
        for (subMap in restrictedSubordinateMaps) {
            if (map.mountainous.get(location), !subMap.mountainous.get(location)) { // TODO: syntax sugar
                subMap.mountainous[location] = true;
                subMap.modified = true;
            }
            if (exists terrain = map.baseTerrain[location], !anythingEqual(terrain, subMap.baseTerrain[location])) {
                subMap.baseTerrain[location] = terrain;
                subMap.modified = true;
            }
            if (!map.rivers.get(location).every(subMap.rivers.get(location).contains)) { // TODO: syntax sugar
                subMap.addRivers(location, *map.rivers.get(location)); // TODO: syntax sugar
                subMap.modified = true;
            }
            value subRoads = subMap.roads[location] else emptyMap;
            if (exists roads = map.roads[location], !roads.empty) {
                for (road->quality in roads) {
                    if ((subRoads[road] else -1) < quality) {
                        subMap.setRoadLevel(location, road, quality);
                        subMap.modified = true;
                    }
                }
            }
        }
    }

    "Set sub-map terrain at the given location to the given type."
    deprecated("Can we redesign the fixture list to not need this for the exploration GUI?")
    shared actual void setSubMapTerrain(Point location, TileType? terrain) {
        for (subMap in restrictedSubordinateMaps) {
            subMap.baseTerrain[location] = terrain;
            subMap.modified = true;
        }
    }

    "Copy the given rivers to sub-maps, if they are present in the main map."
    shared actual void copyRiversToSubMaps(Point location, River* rivers) {
        {River*} actualRivers = rivers.filter(map.rivers.get(location).contains); // TODO: syntax sugar
        for (subMap in restrictedSubordinateMaps) {
            subMap.addRivers(location, *actualRivers); // TODO: Make it return Boolean if this was a change, and only set modified flag in that case
            subMap.modified = true;
        }
    }

    "Remove the given rivers from sub-maps."
    deprecated("Can we redesign the fixture list to not need this for the exploration GUI?")
    shared actual void removeRiversFromSubMaps(Point location, River* rivers) {
        for (subMap in restrictedSubordinateMaps) {
            subMap.removeRivers(location, *rivers); // TODO: Make it return Boolean if this was a change, and only set modified flag in that case
            subMap.modified = true;
        }
    }

    "Remove the given fixture from sub-maps."
    deprecated("Can we redesign the fixture list to not need this for the exploration GUI?")
    shared actual void removeFixtureFromSubMaps(Point location, TileFixture fixture) {
        for (subMap in restrictedSubordinateMaps) {
            subMap.removeFixture(location, fixture); // TODO: Make it return Boolean if this was a change, and only set modified flag in that case
            subMap.modified = true;
        }
    }

    "Set whether sub-maps have a mountain at the given location."
    deprecated("Can we redesign the fixture list to not need this for the exploration GUI?")
    shared actual void setMountainousInSubMap(Point location, Boolean mountainous) {
        for (subMap in restrictedSubordinateMaps) {
            if (subMap.mountainous.get(location) != mountainous) { // TODO: syntax sugar
                subMap.mountainous[location] = mountainous;
                subMap.modified = true;
            }
        }
    }

    "Move a unit-member from one unit to another."
    shared actual void moveMember(UnitMember member, IUnit old, IUnit newOwner) {
        for (map in restrictedAllMaps) {
            if (exists matchingOld = map.fixtures.items.flatMap(unflattenNonFortresses)
                        .narrow<IMutableUnit>()
                        .filter(matchingValue(old.owner, HasOwner.owner))
                        .filter(matchingValue(old.kind, IUnit.kind))
                        .filter(matchingValue(old.name, IUnit.name))
                        .find(matchingValue(old.id, IUnit.id)),
                    exists matchingMember = matchingOld.find(member.equals), // TODO: equals() isn't ideal for finding a matching member ...
                    exists matchingNew = map.fixtures.items.flatMap(unflattenNonFortresses)
                        .narrow<IMutableUnit>()
                        .filter(matchingValue(newOwner.owner, HasOwner.owner))
                        .filter(matchingValue(newOwner.kind, IUnit.kind))
                        .filter(matchingValue(newOwner.name, IUnit.name))
                        .find(matchingValue(newOwner.id, IUnit.id))) {
                matchingOld.removeMember(matchingMember);
                matchingNew.addMember(matchingMember);
                map.modified = true;
            }
        }
    }

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
        log.trace("In ExplorationModel.removeUnit()");
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
                for (fort in map.fixtures.get(location).narrow<IMutableFortress>()) { // TODO: syntax sugar
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

    shared actual void addUnitMember(IUnit unit, UnitMember member) {
        for (map in restrictedAllMaps) {
            if (exists matching = map.fixtures.items.flatMap(unflattenNonFortresses)
                    .narrow<IMutableUnit>()
                    .filter(matchingValue(unit.owner, HasOwner.owner))
                    .filter(matchingValue(unit.name, IUnit.name))
                    .filter(matchingValue(unit.kind, IUnit.kind))
                    .find(matchingValue(unit.id, IUnit.id))) {
                matching.addMember(member.copy(false));
                map.modified = true;
                continue;
            }
        }
    }

    Boolean matchingPlayer(HasOwner fixture) {
        value [unit, currentPlayer] = selection;
        if (exists currentPlayer) {
            return fixture.owner == currentPlayer;
        } else {
            return false;
        }
    }

    shared actual Boolean renameItem(HasMutableName item, String newName) {
        variable Boolean any = false;
        if (is IUnit item) {
            for (map in restrictedAllMaps) {
                if (is HasMutableName matching = map.fixtures.items.flatMap(unflattenNonFortresses)
                        .narrow<IUnit>()
                        .filter(matchingValue(item.owner, HasOwner.owner))
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
                if (exists matching = map.fixtures.items.flatMap(unflattenNonFortresses)
                        .narrow<IUnit>()
                        .filter(matchingPlayer)
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
                if (is HasMutableKind matching = map.fixtures.items.flatMap(unflattenNonFortresses)
                        .narrow<IUnit>()
                        .filter(matchingValue(item.owner, HasOwner.owner))
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
                if (exists matching = map.fixtures.items.flatMap(unflattenNonFortresses)
                        .narrow<IUnit>()
                        .filter(matchingPlayer)
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

    // TODO: Keep a list of dismissed members
    shared actual void dismissUnitMember(UnitMember member) {
        for (map in restrictedAllMaps) {
            for (unit in map.fixtures.items.flatMap(unflattenNonFortresses)
                    .narrow<IMutableUnit>().filter(matchingPlayer)) {
                if (exists matching = unit.find(member.equals)) { // FIXME: equals() will really not do here ...
                    unit.removeMember(matching);
                    map.modified = true;
                    break;
                }
            }
        }
    }

    shared actual Boolean addSibling(UnitMember existing, UnitMember sibling) {
        variable Boolean any = false;
        for (map in restrictedAllMaps) {
            for (unit in map.fixtures.items.flatMap(unflattenNonFortresses)
                    .narrow<IMutableUnit>().filter(matchingPlayer)) {
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

    shared actual Boolean sortFixtureContents(IUnit fixture) {
        variable Boolean any = false;
        for (map in restrictedAllMaps) {
            if (exists matching = map.fixtures.items.flatMap(unflattenNonFortresses)
                    .narrow<IMutableUnit>().filter(matchingPlayer)
                    .filter(matchingValue(fixture.name, IUnit.name))
                    .filter(matchingValue(fixture.kind, IUnit.kind))
                    .find(matchingValue(fixture.id, IUnit.id))) {
                matching.sortMembers();
                map.modified = true;
                any = true;
            }
        }
        return any;
    }

    shared actual void addUnit(IUnit unit) {
        variable Point hqLoc = Point.invalidPoint;
        for (location in map.locations) {
            if (exists fortress = map.fixtures.get(location).narrow<IFortress>()
                    .find(matchingValue(unit.owner, HasOwner.owner))) {
                if (fortress.name == "HQ") {
                    hqLoc = location;
                    break;
                } else if (!hqLoc.valid) {
                    hqLoc = location;
                }
            }
        }
        addUnitAtLocation(unit, hqLoc);
    }
}
