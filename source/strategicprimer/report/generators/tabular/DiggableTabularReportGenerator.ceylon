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
import strategicprimer.model.common.map.fixtures {
    Ground,
    MineralFixture
}
import strategicprimer.model.common.map.fixtures.resources {
    MineralVein,
    Mine,
    StoneDeposit
}

"A tabular report generator for resources that can be mined---mines, mineral veins, stone
 deposits, and Ground."
shared class DiggableTabularReportGenerator(Point? hq, MapDimensions dimensions)
        extends AbstractTableGenerator<MineralFixture>()
        satisfies ITableGenerator<MineralFixture> {
    "The header row for the table."
    shared actual [String+] headerRow = ["Distance", "Location", "Kind", "Product",
        "Status"];

    "The file-name to (by default) write this table to."
    shared actual String tableName = "minerals";

    Comparison(Point, Point) distanceComparator;
    if (exists hq) {
        distanceComparator = DistanceComparator(hq, dimensions).compare;
    } else {
        distanceComparator = (Point one, Point two) => equal;
    }

    "Create a GUI table row representing a fixture."
    shared actual {{String+}*} produce(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, MineralFixture item,
            Integer key, Point loc, Map<Integer, Integer> parentMap) {
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
            return [];
        }
        fixtures.remove(key);
        return [[distanceString(loc, hq, dimensions), locationString(loc), classField,
            item.kind, statusField]];
    }

    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs([Point, MineralFixture] one,
            [Point, MineralFixture] two) =>
        comparing(byIncreasing(compose(MineralFixture.kind, pairFixture)),
            comparingOn(pairPoint, distanceComparator),
            byIncreasing(compose(Object.hash, pairFixture)))(one, two);
}
