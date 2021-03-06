import lovelace.util.common {
    DelayedRemovalMap,
    comparingOn
}

import strategicprimer.model.common {
    DistanceComparator
}
import strategicprimer.model.common.map {
    IFixture,
    MapDimensions,
    Point
}
import strategicprimer.model.common.map.fixtures.mobile {
    Animal,
    maturityModel,
    AnimalTracks
}

"A report generator for [[animal populations|Animal]] and
 [[sightings of animals|AnimalTracks]]."
shared class AnimalTabularReportGenerator(Point? hq, MapDimensions dimensions,
        Integer currentTurn) satisfies ITableGenerator<Animal|AnimalTracks> {
    "The header row for the table."
    shared actual [String+] headerRow = ["Distance", "Location", "Number", "Kind", "Age"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "animals";
    "Create a GUI table row representing the given animal."
    shared actual {{String+}+} produce(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            Animal|AnimalTracks item, Integer key, Point loc,
            Map<Integer, Integer> parentMap) {
        String kind;
        String age;
        String population;
        if (is AnimalTracks item) {
            kind = "tracks or traces of ``item.kind``";
            age = "---";
            population = "---";
        } else if (item.talking) {
            kind = "talking ``item.kind``";
            age = "---";
            population = "---";
        } else if ("wild" != item.status) {
            kind = "``item.status`` ``item.kind``";
            population = item.population.string;
            if (item.born >= 0) {
                if (item.born > currentTurn) {
                    age = "unborn";
                } else if (item.born == currentTurn) {
                    age = "newborn";
                } else if (exists maturityAge = maturityModel.maturityAges[item.kind],
                        maturityAge <= (currentTurn - item.born)) {
                    age = "adult";
                } else {
                    age = "``currentTurn - item.born`` turns";
                }
            } else {
                age = "adult";
            }
        } else {
            kind = item.kind;
            age = "---";
            population = "---";
        }
        fixtures.remove(key);
        return [[distanceString(loc, hq, dimensions), locationString(loc), population,
            kind, age]];
    }

    Comparison compareBools(Boolean first, Boolean second) {
        if (first == second) {
            return equal;
        } else if (first) {
            return larger;
        } else {
            return smaller;
        }
    }

    "Compare two pairs of Animals and locations."
    shared actual Comparison comparePairs([Point, Animal|AnimalTracks] one,
            [Point, Animal|AnimalTracks] two) {
        Comparison cmp;
        if (exists hq) {
            cmp = DistanceComparator(hq, dimensions).compare(one.first, two.first);
        } else {
            cmp = equal;
        }
        if (cmp == equal) {
            // We'd like to extract the comparison on type to a function, which
            // we would pass in to comparing() with the distance function above
            // to reduce nesting, but there's too much here that can only be applied
            // if both are [[Animal]]s or both are [[AnimalTracks]].
            if (is Animal first = one.rest.first) {
                if (is Animal second = two.rest.first) {
                    return comparing(comparingOn(Animal.talking, compareBools),
                        byIncreasing(Animal.kind), byDecreasing(Animal.population),
                        byIncreasing(Animal.born))(first, second);
                } else {
                    return larger;
                }
            } else if (is Animal second = two.rest.first) {
                return smaller;
            } else {
                return one.rest.first.kind <=> two.rest.first.kind;
            }
        } else {
            return cmp;
        }
    }
}
