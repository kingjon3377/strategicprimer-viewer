import lovelace.util.common {
    DelayedRemovalMap
}

import strategicprimer.model {
    DistanceComparator
}
import strategicprimer.model.map {
    IFixture,
    Point,
    MapDimensions
}
import strategicprimer.model.map.fixtures {
    Ground,
    MineralFixture
}
import strategicprimer.model.map.fixtures.resources {
    MineralVein,
    Mine,
    StoneDeposit
}
"A tabular report generator for resources that can be mined---mines, mineral veins, stone
 deposits, and Ground."
shared class DiggableTabularReportGenerator(Point hq, MapDimensions dimensions)
        satisfies ITableGenerator<MineralFixture> {
    "The header row for the table."
    shared actual [String+] headerRow = ["Distance", "Location", "Kind", "Product",
        "Status"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "minerals";
    "Produce the report line for a fixture."
    shared actual Boolean produce(Anything(String) ostream,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, MineralFixture item,
            Point loc) {
        String classField;
        String statusField;
        switch (item)
        case (is Ground) {
            classField = "ground";
            statusField = (item.exposed) then "exposed" else "not exposed";
        }
        case (is Mine) {
            classField = "mine";
            statusField = item.status.string;
        }
        case (is StoneDeposit ) {
            classField = "deposit";
            statusField = "exposed";
        }
        case (is MineralVein) {
            classField = "vein";
            statusField = (item.exposed) then "exposed" else "not exposed";
        }
        else {
            return false;
        }
        writeRow(ostream, distanceString(loc, hq, dimensions), loc.string, classField,
            item.kind, statusField);
        return true;
    }
    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs([Point, MineralFixture] one,
            [Point, MineralFixture] two) {
        return comparing(
            byIncreasing(([Point, MineralFixture] pair) => pair.rest.first.kind),
                    ([Point, MineralFixture] first, [Point, MineralFixture] second) =>
            DistanceComparator(hq, dimensions).compare(first.first, second.first),
            byIncreasing(([Point, MineralFixture] pair) => pair.rest.first.hash))
        (one, two);
    }
}
