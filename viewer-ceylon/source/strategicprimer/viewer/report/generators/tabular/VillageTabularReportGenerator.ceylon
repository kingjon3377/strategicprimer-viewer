import lovelace.util.common {
    DelayedRemovalMap
}
import lovelace.util.jvm {
    ceylonComparator
}

import strategicprimer.viewer.model {
    DistanceComparator
}
import strategicprimer.viewer.model.map {
    Player,
    IFixture
}
import model.map {
    Point
}

import strategicprimer.viewer.model.map.fixtures.towns {
    Village
}
"A tabular report generator for villages."
shared class VillageTabularReportGenerator(Player player, Point hq)
        satisfies ITableGenerator<Village> {
    "The header of this table."
    shared actual [String+] headerRow = ["Distance", "Location", "Owner", "Name"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "villages";
    shared actual Boolean produce(Anything(String) ostream,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, Village item,
            Point loc) {
        writeRow(ostream, distanceString(loc, hq), loc.string,
            ownerString(player, item.owner), item.name);
        return true;
    }
    "Compare two location-and-village pairs."
    shared actual Comparison comparePairs([Point, Village] one,
            [Point, Village] two) {
        return comparing(
            comparingOn(([Point, Village] pair) => pair.first,
                DistanceComparator(hq).compare),
            comparingOn(([Point, Village] pair) => pair.rest.first.owner,
                increasing<Player>),
            comparingOn(([Point, Village] pair) => pair.rest.first.name,
                increasing<String>))(one, two);
    }
}