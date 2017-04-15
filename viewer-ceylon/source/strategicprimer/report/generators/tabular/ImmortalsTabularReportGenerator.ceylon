import lovelace.util.common {
    DelayedRemovalMap,
    comparingOn
}

import strategicprimer.model {
    DistanceComparator
}
import strategicprimer.model.map {
    IFixture,
    Point,
    MapDimensions
}
import strategicprimer.model.map.fixtures.mobile {
    Immortal
}
"""A tabular report generator for "immortals.""""
shared class ImmortalsTabularReportGenerator(Point hq, MapDimensions dimensions)
        satisfies ITableGenerator<Immortal> {
    "The header row for this table."
    shared actual [String+] headerRow = ["Distance", "Location", "Immortal"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "immortals";
    "Produce a table row for the given fixture."
    shared actual Boolean produce(Anything(String) ostream,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, Immortal item,
            Point loc) {
        writeRow(ostream, distanceString(loc, hq), loc.string, item.string);
        return true;
    }
    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs([Point, Immortal] one,
            [Point, Immortal] two) {
        return comparing(comparingOn(
                    ([Point, Immortal] pair) => pair.first,
            DistanceComparator(hq, dimensions).compare),
            comparingOn<[Point, Immortal], Integer>(
                        ([Point, Immortal] pair) => pair.rest.first.hash, increasing),
            comparingOn<[Point, Immortal], Integer>((pair) => pair.rest.first.hash,
                increasing))(one, two);
    }
}
