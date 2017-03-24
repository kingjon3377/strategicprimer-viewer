import lovelace.util.common {
    DelayedRemovalMap
}
import lovelace.util.jvm {
    ceylonComparator
}

import model.map {
    DistanceComparator,
    Player,
    Point,
    IFixture
}
import strategicprimer.viewer.model.map.fixtures.towns {
    AbstractTown
}
"A tabular report generator for towns."
shared class TownTabularReportGenerator(Player player, Point hq)
        satisfies ITableGenerator<AbstractTown> {
    "The file-name to (by default) write this table to"
    shared actual String tableName = "towns";
    Comparison([Point, AbstractTown], [Point, AbstractTown]) comparator =
            comparing(
                comparingOn(([Point, AbstractTown] pair) => pair.rest.first,
                    compareTownKind),
                comparingOn(([Point, AbstractTown] pair) => pair.first,
                    ceylonComparator(DistanceComparator(hq))),
                comparingOn(([Point, AbstractTown] pair) => pair.rest.first.size,
                    compareTownSize),
                comparingOn(([Point, AbstractTown] pair) => pair.rest.first.status,
                    compareTownStatus),
                comparingOn(([Point, AbstractTown] pair) => pair.rest.first.name,
                    increasing<String>));
    "The header row for this table."
    shared actual [String+] headerRow = ["Distance", "Location", "Owner", "Kind", "Size",
        "Status", "Name"];
    "Produce a table line representing a town."
    shared actual Boolean produce(Anything(String) ostream,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            AbstractTown item, Point loc) {
        writeRow(ostream, distanceString(loc, hq), loc.string,
            ownerString(player, item.owner), item.kind, item.size.string,
            item.status.string, item.name);
        return true;
    }
    "Compare two location-town pairs."
    shared actual Comparison comparePairs([Point, AbstractTown] one,
            [Point, AbstractTown] two) {
        return comparator(one, two);
    }
}
