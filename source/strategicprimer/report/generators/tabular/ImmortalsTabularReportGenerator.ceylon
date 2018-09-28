import lovelace.util.common {
    DelayedRemovalMap,
    comparingOn
}

import strategicprimer.model.impl {
    DistanceComparator
}
import strategicprimer.model.common.map {
    IFixture,
    MapDimensions,
    Point
}
import strategicprimer.model.common.map.fixtures.mobile {
    Immortal
}
import ceylon.language.meta {
    type
}
import ceylon.language.meta.model {
    ClassModel
}
"""A tabular report generator for "immortals.""""
shared class ImmortalsTabularReportGenerator(Point hq, MapDimensions dimensions)
        satisfies ITableGenerator<Immortal> {
    "The header row for this table."
    shared actual [String+] headerRow = ["Distance", "Location", "Immortal"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "immortals";
    "Create a GUI table row representing the given fixture."
    shared actual [{String+}+] produce(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            Immortal item, Integer key, Point loc, Map<Integer, Integer> parentMap) {
        fixtures.remove(key);
        return [[distanceString(loc, hq, dimensions), loc.string, item.string]];
    }
    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs([Point, Immortal] one,
            [Point, Immortal] two) =>
            comparing(comparingOn<[Point, Immortal], Point>(
                    Tuple.first, DistanceComparator(hq, dimensions).compare),
                comparingOn(Tuple<Point|Immortal, Point, [Immortal]>.rest,
                    comparingOn(Tuple<Immortal, Immortal, []>.first,
                        comparing(comparingOn<Immortal, ClassModel<Immortal>>(type,
                            comparingOn(Object.hash, increasing<Integer>)),
                        comparingOn(Object.hash, increasing<Integer>)))))(one, two);
}
