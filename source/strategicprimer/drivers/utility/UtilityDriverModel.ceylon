import strategicprimer.model.common.map {
    Direction,
    HasExtent,
    HasPopulation,
    IFixture,
    IMutableMapNG,
    Point,
    Subsettable,
    TileFixture
}

import strategicprimer.drivers.common {
    SimpleMultiMapModel,
    IDriverModel
}

import lovelace.util.common {
    as,
    PathWrapper
}

import ceylon.collection {
    MutableList,
    ArrayList
}

import strategicprimer.model.common.map.fixtures.resources {
    CacheFixture
}

import strategicprimer.model.common.map.fixtures.mobile {
    Animal,
    IUnit
}

import strategicprimer.model.common.map.fixtures.towns {
    Fortress
}

import ceylon.language.meta {
    type
}

import ceylon.language.meta.model {
    ClassOrInterface
}

"A driver model for the various utility drivers."
shared class UtilityDriverModel extends SimpleMultiMapModel {
    static void ifApplicable<Desired, Provided>(Anything(Desired) func)(Provided item) {
        if (is Desired item) {
            func(item);
        }
    }

    static Boolean isSubset(IFixture one, IFixture two) {
        if (is Subsettable<IFixture> one) {
            return one.isSubset(two, noop);
        } else {
            return one == two;
        }
    }

    shared new (IMutableMapNG map, PathWrapper? file, Boolean modified = false)
            extends SimpleMultiMapModel(map, file, modified) {}
    shared new copyConstructor(IDriverModel model)
            extends SimpleMultiMapModel.copyConstructor(model) {}

    "Copy rivers at the given [[location]] missing from subordinate maps, where
     they has other terrain information, from the main map."
    shared void copyRiversAt(Point location) {
        for (subordinateMap->[file, modifiedFlag] in restrictedSubordinateMaps) {
            if (exists mainTerrain = map.baseTerrain[location],
                    exists subTerrain = subordinateMap.baseTerrain[location],
                    mainTerrain == subTerrain,
                    //!super.map.rivers[location].empty, subordinateMap.rivers[location].empty) { // TODO: syntax sugar
                    !super.map.rivers.get(location).empty,
                    subordinateMap.rivers.get(location).empty) {
                //subordinateMap.addRivers(location, *super.map.rivers[location]); // TODO: syntax sugar
                subordinateMap.addRivers(location, *super.map.rivers.get(location));
                if (!modifiedFlag) {
                    setModifiedFlag(subordinateMap, true);
                }
            }
        }
    }

    "Conditionally remove duplicate fixtures. Returns a list of fixtures that
     would be removed and a callback to do the removal; the initial caller of
     this asks the user for approval."
    shared {[Anything(TileFixture), PathWrapper?, TileFixture, {TileFixture+}]*} conditionallyRemoveDuplicates(Point location) {
        MutableList<[Anything(TileFixture), PathWrapper?, TileFixture, {TileFixture+}]> duplicatesList =
            ArrayList<[Anything(TileFixture), PathWrapper?, TileFixture, {TileFixture+}]>();
        MutableList<TileFixture> checked = ArrayList<TileFixture>();
        for (map->[file, modifiedFlag] in restrictedAllMaps) {
            //for (fixture in map.fixtures[location]) { // TODO: syntax sugar once compiler bug fixed
            for (fixture in map.fixtures.get(location)) {
                checked.add(fixture);
                if (is IUnit fixture, fixture.kind.contains("TODO")) {
                    continue;
                } else if (is CacheFixture fixture) {
                    continue;
                } else if (is HasPopulation<out Anything> fixture,
                        fixture.population.positive) {
                    continue;
                } else if (is HasExtent<out Anything> fixture, fixture.acres.positive) {
                    continue;
                }
                //value matching = map.fixtures[location] // TODO: syntax sugar
                value matching = map.fixtures.get(location)
                    .filter((item) => !checked.any((inner) => item === inner))
                    .select(fixture.equalsIgnoringID);
                if (nonempty matching) {
                    duplicatesList.add([(TileFixture item) => map.removeFixture(location, item),
                        file, fixture, matching]);
                }
            }
        }
        return duplicatesList;
    }

    {[Anything(), String, String, {IFixture+}]*} coalesceImpl(String context, {IFixture*} stream,
            Anything(IFixture) add, Anything(IFixture) remove, Anything() setModFlag,
            Map<ClassOrInterface<IFixture>, CoalescedHolder<out IFixture, out Object>> handlers) {
        MutableList<[Anything(), String, String, {IFixture+}]> retval =
            ArrayList<[Anything(), String, String, {IFixture+}]>();
        for (fixture in stream) {
            if (is {IFixture*} fixture) {
                String shortDesc = as<TileFixture>(fixture)?.shortDescription
                    else fixture.string;
                if (is IUnit fixture) {
                    retval.addAll(coalesceImpl("``context``In ``shortDesc``: ", fixture,
                        ifApplicable(fixture.addMember), ifApplicable(fixture.removeMember), setModFlag, handlers));
                } else if (is Fortress fixture) {
                    retval.addAll(coalesceImpl("``context``In ``shortDesc``: ", fixture,
                        ifApplicable(fixture.addMember), ifApplicable(fixture.removeMember), setModFlag, handlers));
                }
            } else if (is Animal fixture) {
                if (fixture.talking) {
                    continue;
                }
                if (exists handler = handlers[`Animal`]) {
                    handler.addIfType(fixture);
                }
            } else if (is HasPopulation<out Anything> fixture, fixture.population < 0) {
                continue;
            } else if (is HasExtent<out Anything> fixture, !fixture.acres.positive) {
                continue;
            } else if (exists handler = handlers[type(fixture)]) {
                handler.addIfType(fixture);
            }
        }

        for (handler in handlers.items) {
            for (list in handler.map(shuffle(List<IFixture>.sequence)())) {
                if (list.empty || list.rest.empty) {
                    continue;
                }
                assert (nonempty list);
                retval.add([() {
                    IFixture combined = handler.combineRaw(list);
                    list.each(remove);
                    add(combined);
                    setModFlag();
                }, context, handler.plural.lowercased, list]);
            }
        }
        return retval;
    }

    "Conditionally coalesce like resources in a location. Returns a list of
     resources that would be combined and a callback to do the operation; the
     initial caller of this ask the user for approval."
    shared {[Anything(), String, String, {IFixture+}]*} conditionallyCoalesceResources(Point location,
            Map<ClassOrInterface<IFixture>, CoalescedHolder<out IFixture, out Object>> handlers) {
        MutableList<[Anything(), String, String, {IFixture+}]> retval =
            ArrayList<[Anything(), String, String, {IFixture+}]>();
        for (map->[file, modifiedFlag] in restrictedAllMaps) {
            variable Boolean modFlag = modifiedFlag;
            retval.addAll(coalesceImpl("In ``file else "a new file"``: At ``location``: ", map.fixtures.get(location), // TODO: syntax sugar
                ifApplicable<TileFixture, IFixture>(shuffle(curry(map.addFixture))),
                ifApplicable<TileFixture, IFixture>(shuffle(curry(map.removeFixture))), () {
                    if (!modFlag) {
                        setModifiedFlag(map, true);
                        modFlag = true;
                     }
                 }, handlers));
        }
        return retval;
    }

    "Remove information in the main map from subordinate maps."
    shared void subtractAtPoint(Point location) {
        for (subMap->[path, modified] in restrictedSubordinateMaps) {
            if (!modified) {
                setModifiedFlag(map, true);
            }
            if (exists terrain = map.baseTerrain[location], exists ours = subMap.baseTerrain[location], terrain == ours) {
                subMap.baseTerrain[location] = null;
            }
            subMap.removeRivers(location, *map.rivers.get(location)); // TODO: syntax sugar
            Map<Direction, Integer> mainRoads = map.roads.getOrDefault(location, emptyMap);
            Map<Direction, Integer> knownRoads = subMap.roads.getOrDefault(location, emptyMap);
            for (direction->road in knownRoads) {
                if (mainRoads.getOrDefault(direction, 0) >= road) {
                    subMap.setRoadLevel(location, direction, 0);
                }
            }
            if (map.mountainous.get(location)) { // TODO: syntax sugar
                subMap.mountainous[location] = false;
            }
            for (fixture in subMap.fixtures.get(location)) { // TODO: syntax sugar
                if (map.fixtures.get(location).any((item) => isSubset(item, fixture))) { // TODO: syntax sugar
                    subMap.removeFixture(location, fixture);
                }
            }
        }
    }
}
