import ceylon.collection {
    MutableList,
    ArrayList,
    ListMutator,
    MutableMap,
    TreeMap
}
import ceylon.test {
    test,
    assertEquals
}

import java.nio.file {
    JPath=Path
}

import lovelace.util.common {
    todo
}
import lovelace.util.jvm {
    shuffle
}

import strategicprimer.model.map {
    Point,
    Player,
    TileFixture,
    IMutableMapNG,
    IMapNG,
    PlayerImpl,
    SPMapNG,
    MapDimensionsImpl,
    PlayerCollection
}
import strategicprimer.model.map.fixtures.mobile {
    Animal,
    ProxyFor,
    ProxyUnit,
    IUnit,
    Unit
}
import strategicprimer.model.map.fixtures.terrain {
    Oasis,
    Hill,
    Forest
}
import strategicprimer.model.map.fixtures.towns {
    TownSize,
    Fortress
}
import strategicprimer.drivers.common {
    SimpleMultiMapModel,
    IDriverModel
}
import ceylon.logging {
    Logger,
    logger
}
Logger log = logger(`module strategicprimer.drivers.worker.common`);
"A model to underlie the advancement GUI, etc."
shared class WorkerModel extends SimpleMultiMapModel satisfies IWorkerModel {
    shared new (IMutableMapNG map, JPath? file)
            extends SimpleMultiMapModel(map, file) {}
    shared new copyConstructor(IDriverModel model)
            extends SimpleMultiMapModel.copyConstructor(model) {}
    "Flatten and filter the stream to include only units, and only those owned by the
     given player."
    {IUnit*} getUnitsImpl({Anything*} iter, Player player) {
        value temp = iter.flatMap((item) {
            if (is Fortress item) {
                return item;
            } else {
                return {item};
            }
        });
        return { for (item in temp) if (is IUnit item) if (item.owner == player) item };
    }
    "All the players in all the maps."
    shared actual {Player*} players {
        return allMaps.map(([IMutableMapNG, JPath?] pair) => pair.first)
                .flatMap(IMutableMapNG.players).distinct;
    }
    "Get all the given player's units, or only those of a specified kind."
    shared actual {IUnit*} getUnits(Player player, String? kind) {
        if (exists kind) {
            return { *getUnits(player).filter((unit) => kind == unit.kind) };
        } else if (subordinateMaps.empty) {
            // Just in case I missed something in the proxy implementation, make sure
            // things work correctly when there's only one map.
            return getUnitsImpl(map.locations
                .flatMap((point) => map.getOtherFixtures(point)), player);
        } else {
            value temp = allMaps
                    .map(([IMutableMapNG, JPath?] pair) => pair.first)
                    .flatMap(IMapNG.locations)
                    .flatMap((point) => getUnitsImpl(map.getOtherFixtures(point), player));
            MutableMap<Integer, IUnit&ProxyFor<IUnit>> tempMap =
                    TreeMap<Integer, IUnit&ProxyFor<IUnit>>((x, y) => x<=>y);
            for (unit in temp) {
                Integer key = unit.id;
                ProxyFor<IUnit> proxy;
                if (exists item = tempMap.get(key)) {
                    proxy = item;
                } else {
                    value newProxy = ProxyUnit.fromParallelMaps(key);
                    tempMap.put(key, newProxy);
                    proxy = newProxy;
                }
                proxy.addProxied(unit);
            }
            return tempMap.items;
        }
    }
    """All the "kinds" of units the given player has."""
    shared actual {String*} getUnitKinds(Player player) =>
            { *getUnits(player).map(IUnit.kind).distinct };
    "Add the given unit at the given location in all maps."
    void addUnitAtLocation(IUnit unit, Point location) {
        void impl(IMutableMapNG map) {
            for (fixture in map.getOtherFixtures(location)) {
                if (is Fortress fixture, fixture.owner == unit.owner) {
                    fixture.addMember(unit.copy(false));
                    return;
                }
            } else {
                map.addFixture(location, unit.copy(false));
            }
        }
        if (subordinateMaps.empty) {
            impl(map);
        } else {
            for (pair in allMaps) {
                impl(pair.first);
            }
        }
    }
    "Add a unit to all the maps, at the location of its owner's HQ in the main map."
    shared actual void addUnit(IUnit unit) {
        for (point in map.locations) {
            for (fixture in map.getOtherFixtures(point)) {
                if (is Fortress fixture, "HQ" == fixture.name, fixture.owner == unit.owner) {
                    addUnitAtLocation(unit, point);
                    return;
                }
            } else {
                log.warn("No suitable location found for unit");
            }
        }
    }
    "Get a unit by its owner and ID."
    shared actual IUnit? getUnitByID(Player owner, Integer id) =>
            getUnits(owner).find((unit) => id == unit.id);
}
"Helper method: Flatten any proxies in the list by replacing them with what they are
 proxies for."
todo("Once [[ProxyFor]] is ported to Ceylon, use its reified form instead of assertions")
{T*} filterProxies<T>(T* list) given T satisfies Object {
    MutableList<T> retval = ArrayList<T>();
    for (item in list) {
        if (is ProxyFor<out T> item) {
            retval.addAll(item.proxied);
        } else {
            retval.add(item);
        }
    }
    return {*retval};
}

test
void testGetUnits() {
    "Helper method: Add an item to multiple lists at once."
    void addItem<T>(T item, ListMutator<T>* lists) {
        for (list in lists) {
            list.add(item);
        }
    }
    MutableList<TileFixture> fixtures = ArrayList<TileFixture>();
    fixtures.add(Oasis(14));
    fixtures.add(Animal("animal", false, false, "wild", 1));
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
    value shuffled = shuffle(fixtures);
    IMutableMapNG map = SPMapNG(MapDimensionsImpl(3, 3, 2), PlayerCollection(), -1);
    for ([point, fixture] in zipPairs(map.locations, shuffled)) {
        map.addFixture(point, fixture);
    }
    IWorkerModel model = WorkerModel(map, null);
    Boolean iterableEquality<T>(Anything one, Anything two) given T satisfies Object {
        if (is {T*} one, is {T*} two) {
            return one.containsEvery(two) &&two.containsEvery(one);
        } else if (exists one, exists two) {
            return one == two;
        } else {
            return one exists == two exists;
        }
    }
    assertEquals(filterProxies(*model.getUnits(playerOne)),
        listOne, "Got all units for player 1", iterableEquality<IUnit>);
    assertEquals(filterProxies(*model.getUnits(playerTwo)), listTwo,
        "Got all units for player 2", iterableEquality<IUnit>);
    assertEquals(filterProxies(*model.getUnits(playerThree)), listThree,
        "Got all units for player 3", iterableEquality<IUnit>);
}