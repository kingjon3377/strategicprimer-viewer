import lovelace.util.common {
    DelayedRemovalMap,
    IntMap,
    matchingValue,
    narrowedStream
}

import strategicprimer.model.common.idreg {
    IDRegistrar,
    createIDFactory
}

import strategicprimer.model.common.map {
    IFixture,
    Player,
    TileFixture,
    Point,
    IMapNG
}

import strategicprimer.model.common.map.fixtures.towns {
    IFortress
}

import ceylon.collection {
    MutableMap,
    HashMap
}

"An encapsulation of helper methods for report generators."
object reportGeneratorHelper {
    "Find the location of the given player's HQ in the given map, or [[null]]
     if not found."
    shared Point? findHQ(IMapNG map, Player player) {
        variable Point? retval = null;
        for (location->fixture in narrowedStream<Point, IFortress>(map.fixtures)
                .filter(compose(matchingValue(player, IFortress.owner),
                    Entry<Point, IFortress>.item))) {
            if ("hq" == fixture.name.lowercased) {
                return location;
            } else if (location.valid, !retval exists) {
                retval = location;
            }
        } else {
            return retval;
        }
    }

    "Create a mapping from ID numbers to Pairs of fixtures and their location for all
     fixtures in the map."
    shared DelayedRemovalMap<Integer, [Point, IFixture]> getFixtures(IMapNG map) {
        DelayedRemovalMap<Integer, [Point, IFixture]> retval =
                IntMap<[Point, IFixture]>();
        IDRegistrar idf = createIDFactory(map);
        Integer checkID(IFixture fixture) {
            if (fixture.id < 0) {
                return idf.createID();
            } else {
                return fixture.id;
            }
        }
        void addToMap(Point location, IFixture fixture) {
            if (fixture is TileFixture || fixture.id >= 0) {
                Integer key = checkID(fixture);
                value val = [location, fixture];
                // We could use `retval[key] = val`, but that would be more confusing
                // here.
                if (exists existing = retval.put(key, val), existing != val) {
                    log.warn("Duplicate key, ``key``, for Pairs ``
                        existing`` and ``val``");
                }
            }
            if (is {IFixture*} fixture) {
                for (inner in fixture) {
                    addToMap(location, inner);
                }
            }
        }
        for (location->fixture in map.fixtures) {
            addToMap(location, fixture);
        }
        return retval;
    }

    void parentMapImpl(MutableMap<Integer, Integer> retval, IFixture parent,
            {IFixture*} stream) {
        for (fixture in stream) {
            retval.put(fixture.id, parent.id);
            if (is {IFixture*} fixture) {
                parentMapImpl(retval, fixture, fixture);
            }
        }
    }

    "Create a mapping from child ID numbers to parent ID numbers."
    shared Map<Integer, Integer> getParentMap(IMapNG map) {
        MutableMap<Integer, Integer> retval = HashMap<Integer, Integer>();
        for (fixture in map.fixtures.items.narrow<{IFixture*}>()) {
            parentMapImpl(retval, fixture, fixture);
        }
        return retval;
    }
}

