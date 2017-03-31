import lovelace.util.common {
    DelayedRemovalMap
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    Immortal
}
import strategicprimer.viewer.model {
    DistanceComparator
}
import strategicprimer.viewer.model.map {
    IFixture,
    Point
}
"""A tabular report generator for "immortals.""""
shared class ImmortalsTabularReportGenerator(Point hq) satisfies ITableGenerator<Immortal> {
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
            DistanceComparator(hq).compare),
            comparingOn<[Point, Immortal], Integer>(
                        ([Point, Immortal] pair) => pair.rest.first.hash, increasing),
            comparingOn<[Point, Immortal], Integer>((pair) => pair.rest.first.hash,
                increasing))(one, two);
    }
}
