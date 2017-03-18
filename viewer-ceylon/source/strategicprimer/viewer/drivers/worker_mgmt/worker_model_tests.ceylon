import ceylon.collection {
    MutableList,
    ArrayList,
    ListMutator
}
import ceylon.interop.java {
    CeylonIterable
}
import java.util {
    JOptional=Optional
}
import java.nio.file {
    JPath=Path
}
import ceylon.test {
    test,
    assertEquals
}

import lovelace.util.common {
    todo
}
import lovelace.util.jvm {
    shuffle
}

import model.map {
    TileFixture,
    Player,
    PlayerImpl,
    IMutableMapNG,
    SPMapNG,
    MapDimensionsImpl,
    PlayerCollection
}
import model.map.fixtures.mobile {
    ProxyFor,
    Animal,
    IUnit,
    Unit
}
import model.map.fixtures.terrain {
    Oasis,
    Forest,
    Hill
}
import model.map.fixtures.towns {
    Fortress,
    TownSize
}
import model.workermgmt {
    IWorkerModel,
    WorkerModel
}
"Helper method: Flatten any proxies in the list by replacing them with what they are
 proxies for."
todo("Once [[ProxyFor]] is ported to Ceylon, use its reified form instead of assertions")
{T*} filterProxies<T>(T* list) given T satisfies Object {
    MutableList<T> retval = ArrayList<T>();
    for (item in list) {
        if (is ProxyFor<out Anything> item) {
            assert (is ProxyFor<out T> item);
            retval.addAll(CeylonIterable(item.proxied));
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
    for ([point, fixture] in zipPairs(CeylonIterable(map.locations()), shuffled)) {
        map.addFixture(point, fixture);
    }
    IWorkerModel model = WorkerModel(map, JOptional.empty<JPath>());
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