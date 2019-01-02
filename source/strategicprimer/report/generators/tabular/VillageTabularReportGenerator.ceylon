import lovelace.util.common {
    DelayedRemovalMap,
    comparingOn
}

import strategicprimer.model.common {
    DistanceComparator
}
import strategicprimer.model.common.map {
    Player,
    IFixture,
    MapDimensions,
    Point
}
import strategicprimer.model.common.map.fixtures.towns {
    Village
}

"A tabular report generator for villages."
shared class VillageTabularReportGenerator(Player player, Point hq,
        MapDimensions dimensions) extends AbstractTableGenerator<Village>()
        satisfies ITableGenerator<Village> {
    "The header of this table."
    shared actual [String+] headerRow = ["Distance", "Location", "Owner", "Name"];

    "The file-name to (by default) write this table to."
    shared actual String tableName = "villages";

    "Create a GUI table row representing the village."
    shared actual {{String+}+} produce(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, Village item,
            Integer key, Point loc, Map<Integer, Integer> parentMap) {
        fixtures.remove(key);
        return [[distanceString(loc, hq, dimensions), locationString(loc),
            ownerString(player, item.owner), item.name]];
    }

    "Compare two location-and-village pairs."
    shared actual Comparison comparePairs([Point, Village] one,
            [Point, Village] two) =>
        comparing(comparingOn(pairPoint, DistanceComparator(hq, dimensions).compare),
            comparingOn(pairFixture, comparing(byIncreasing(Village.owner),
                byIncreasing(Village.name))))(one, two);
}
