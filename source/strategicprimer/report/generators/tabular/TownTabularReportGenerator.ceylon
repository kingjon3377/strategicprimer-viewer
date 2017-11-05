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
    AbstractTown
}
"A tabular report generator for towns."
shared class TownTabularReportGenerator(Player player, Point hq, MapDimensions dimensions)
        satisfies ITableGenerator<AbstractTown> {
    "The file-name to (by default) write this table to"
    shared actual String tableName = "towns";
    Comparison([Point, AbstractTown], [Point, AbstractTown]) comparator =
            comparing(
                comparingOn(([Point, AbstractTown] pair) => pair.rest.first,
                    townComparators.compareTownKind),
                comparingOn(([Point, AbstractTown] pair) => pair.first,
                    DistanceComparator(hq, dimensions).compare),
                comparingOn(([Point, AbstractTown] pair) => pair.rest.first.townSize,
                    townComparators.compareTownSize),
                comparingOn(([Point, AbstractTown] pair) => pair.rest.first.status,
                    townComparators.compareTownStatus),
                comparingOn(([Point, AbstractTown] pair) => pair.rest.first.name,
                    increasing<String>));
    "The header row for this table."
    shared actual [String+] headerRow = ["Distance", "Location", "Owner", "Kind", "Size",
        "Status", "Name"];
    "Create a GUI table row representing a town."
    shared actual {{String+}+} produce(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            AbstractTown item, Integer key, Point loc) {
        fixtures.remove(key);
        return {{distanceString(loc, hq, dimensions), loc.string,
                ownerString(player, item.owner), item.kind, item.townSize.string,
                item.status.string, item.name}};
    }
    "Compare two location-town pairs."
    shared actual Comparison comparePairs([Point, AbstractTown] one,
            [Point, AbstractTown] two) {
        return comparator(one, two);
    }
}
