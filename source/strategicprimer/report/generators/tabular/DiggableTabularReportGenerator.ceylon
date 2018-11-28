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
shared class DiggableTabularReportGenerator(Point hq, MapDimensions dimensions)
        satisfies ITableGenerator<MineralFixture> {
    "The header row for the table."
    shared actual [String+] headerRow = ["Distance", "Location", "Kind", "Product",
        "Status"];

    "The file-name to (by default) write this table to."
    shared actual String tableName = "minerals";

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
        return [[distanceString(loc, hq, dimensions), loc.string, classField, item.kind,
            statusField]];
    }

    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs([Point, MineralFixture] one,
            [Point, MineralFixture] two) => comparing(
            comparingOn(compose(compose(MineralFixture.kind,
                        Tuple<MineralFixture, MineralFixture, []>.first),
                    Tuple<Point|MineralFixture, Point, MineralFixture[1]>.rest),
                increasing<String>),
            comparingOn(Tuple<Point|MineralFixture, Point, MineralFixture[1]>.first,
                DistanceComparator(hq, dimensions).compare),
            comparingOn(compose(compose(Object.hash,
                        Tuple<MineralFixture, MineralFixture, []>.first),
                    Tuple<Point|MineralFixture, Point, MineralFixture[1]>.rest),
                increasing<Integer>))(one, two);
}
