import strategicprimer.model.common.map {
    HasExtent,
    HasPopulation,
    IMutableMapNG,
    Point,
    TileFixture
}

import strategicprimer.drivers.exploration.common {
    ExplorationModel
}

import lovelace.util.common {
    PathWrapper
}

import strategicprimer.drivers.common {
    IDriverModel
}

import ceylon.decimal {
    Decimal
}

import lovelace.util.jvm {
    decimalize
}

shared class TurnRunningModel extends ExplorationModel satisfies ITurnRunningModel {
    shared new (IMutableMapNG map, PathWrapper? file, Boolean modified = false)
        extends ExplorationModel(map, file, modified) {}
    shared new copyConstructor(IDriverModel model)
        extends ExplorationModel.copyConstructor(model) {}

    "Add a copy of the given fixture to all submaps at the given location iff no fixture
     with the same ID is already there."
    shared actual void addToSubMaps(Point point, TileFixture fixture, Boolean zero) {
        for (map->[file, _] in restrictedSubordinateMaps) {
            if (!map.fixtures.get(point).map(TileFixture.id).any(fixture.id.equals)) {
                map.addFixture(point, fixture.copy(zero));
            }
        }
    }

    "Reduce the population of a group of plants, animals, etc., and copy the reduced form
     into all subordinate maps."
    shared actual void reducePopulation(Point location, HasPopulation<out TileFixture>&TileFixture fixture,
            Boolean zero, Integer reduction) {
        if (reduction.positive) {
            variable Boolean first = false;
            variable Boolean all = false;
            for (map->[file, modified] in restrictedAllMaps) {
                if (exists matching = map.fixtures.get(location).narrow<HasPopulation<out TileFixture>>()
                        .find(shuffle(curry(fixture.isSubset))(noop))) {
                    if (all) {
                        map.removeFixture(location, matching);
                    } else if (matching.population.positive) {
                        Integer remaining = matching.population - reduction;
                        if (remaining.positive) {
                            value addend = matching.reduced(remaining);
                            map.replace(location, matching, addend.copy(first || zero));
                            continue;
                        } else if (first) {
                            all = true;
                        }
                        map.removeFixture(location, matching);
                    } else if (first) {
                        break;
                    } else {
                        map.addFixture(location, fixture.copy(zero));
                    }
                } else if (first) {
                    break;
                }
                first = false;
            }
        }
    }

    "Reduce the acreage of a fixture, and copy the reduced form into all subordinate maps."
    shared actual void reduceExtent(Point location,
            HasExtent<out HasExtent<out Anything>&TileFixture>&TileFixture fixture,
            Boolean zero, Decimal reduction) {
        if (reduction.positive) {
            variable Boolean first = false;
            variable Boolean all = false;
            for (map->[file, modified] in restrictedAllMaps) {
                if (exists matching = map.fixtures.get(location).narrow<HasExtent<out TileFixture>>()
                        .find(shuffle(curry(fixture.isSubset))(noop))) {
                    if (all) {
                        map.removeFixture(location, matching);
                    } else if (matching.acres.positive) {
                        if (decimalize(matching.acres) > reduction) {
                            value addend = matching.reduced(reduction).copy(first || zero);
                            map.replace(location, matching, addend);
                            continue;
                        } else if (first) {
                            all = true;
                        }
                        map.removeFixture(location, matching);
                    } else if (first) {
                        break;
                    } else {
                        map.addFixture(location, fixture.copy(zero));
                    }
                } else if (first) {
                    break;
                }
                first = false;
            }
        }
    }
}
