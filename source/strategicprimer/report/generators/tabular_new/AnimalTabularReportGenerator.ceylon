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
    shared actual [String+] headerRow = ["Distance", "Location", "Number", "Domestication Status", "Kind", "Age"];

    "The file-name to (by default) write this table to."
    shared actual String tableName = "animals";

    "Create a GUI table row representing the given animal."
    shared actual {{[String(), Anything(String)?]+}+} produce(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            Animal|AnimalTracks item, Integer key, Point loc,
            Map<Integer, Integer> parentMap) {
        String() kindGetter;
        Anything(String)? kindSetter;
        String() ageGetter;
        Anything(String)? ageSetter;
        String() statusGetter;
        Anything(String)? statusSetter;
        String() populationGetter;
        Anything(String)? populationSetter;
        if (is AnimalTracks item) {
            kindGetter = () => "tracks or traces of ``item.kind``";
            kindSetter = null;
            ageGetter = dummyGetter;
            ageSetter = null;
            statusGetter = dummyGetter;
            statusSetter = null;
            populationGetter = dummyGetter;
            populationSetter = null;
        } else if (item.talking) {
            kindGetter = () => "talking ``item.kind``";
            kindSetter = (String str) => item.kind = str.removeInitial("talking ");
            ageGetter = dummyGetter;
            ageSetter = null;
            statusGetter = dummyGetter;
            statusSetter = null;
            populationGetter = dummyGetter;
            populationSetter = null;
        } else {
            kindGetter = () => item.kind;
            kindSetter = (String str) => item.kind = str;
            ageGetter = () {
                if (item.status == "wild" {
                    return "---";
                } else if (item.born < 0) {
                    return "adult";
                } else if (item.born > currentTurn) {
                    return "unborn";
                } else if (item.born == currentTurn) {
                    return "newborn";
                } else if (existsMaturityAge = maturityModel.maturityAges[item.kind],
                        maturityAge <= (currentTurn - item.born)) {
                    return "adult";
                } else {
                    return "``currentTurn - item.born`` turns";
                }
            };
            ageSetter = (String str) => nothing; // FIXME: Implement, probably as separate method
            statusGetter = () => item.status;
            statusSetter = (String str) => item.status = str;
            populationGetter = () => (item.status == "wild") then "---" else item.population.string;
            populationSetter = (String str) {
                if (is Integer num = Integer.parse(str)) {
                    item.population = num;
                } else {
                    log.warn("Given non-numeric animal population value");
                }
            };
        }
        fixtures.remove(key);
        return [[[() => distanceString(loc, hq, dimensions), null], locationElement(key),
            [populationGetter, populationSetter], [statusGetter, statusSetter],
            [kindGetter, kindSetter], [ageGetter, ageSetter]]];
    }

    // TODO: Move to ITableGenerator, if not lovelace.util
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
