import lovelace.util.common {
    DelayedRemovalMap,
    comparingOn
}

import strategicprimer.model {
    DistanceComparator
}
import strategicprimer.model.map {
    Player,
    IFixture,
    Point,
    MapDimensions
}
import strategicprimer.model.map.fixtures.towns {
    Village
}
"A tabular report generator for villages."
shared class VillageTabularReportGenerator(Player player, Point hq,
        MapDimensions dimensions) satisfies ITableGenerator<Village> {
    "The header of this table."
    shared actual [String+] headerRow = ["Distance", "Location", "Owner", "Name"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "villages";
    "Create a GUI table row representing the village."
    shared actual {{String+}+} produce(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, Village item,
            Integer key, Point loc, Map<Integer, Integer> parentMap) {
        fixtures.remove(key);
        return [[distanceString(loc, hq, dimensions), loc.string,
            ownerString(player, item.owner), item.name]];
    }
    "Compare two location-and-village pairs."
    shared actual Comparison comparePairs([Point, Village] one,
            [Point, Village] two) {
        return comparing(
            comparingOn(Tuple<Point|Village, Point, [Village]>.first,
                DistanceComparator(hq, dimensions).compare),
            comparingOn(Tuple<Point|Village, Point, [Village]>.rest,
                comparingOn(Tuple<Village, Village, []>.first,
                    comparing(comparingOn(Village.owner, increasing<Player>),
                        comparingOn(Village.name, increasing<String>)))))(one, two);
    }
}
