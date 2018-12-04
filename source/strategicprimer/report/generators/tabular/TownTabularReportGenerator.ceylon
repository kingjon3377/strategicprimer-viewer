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
    AbstractTown
}

"A tabular report generator for towns."
shared class TownTabularReportGenerator(Player player, Point hq, MapDimensions dimensions)
        extends AbstractTableGenerator<AbstractTown>()
        satisfies ITableGenerator<AbstractTown> {
    "The file-name to (by default) write this table to"
    shared actual String tableName = "towns";

    "The header row for this table."
    shared actual [String+] headerRow = ["Distance", "Location", "Owner", "Kind", "Size",
        "Status", "Name"];

    "Create a GUI table row representing a town."
    shared actual {{String+}+} produce(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            AbstractTown item, Integer key, Point loc, Map<Integer, Integer> parentMap) {
        fixtures.remove(key);
        return [[distanceString(loc, hq, dimensions), locationString(loc),
                ownerString(player, item.owner), item.kind, item.townSize.string,
                item.status.string, item.name]];
    }

    "Compare two location-town pairs. We partially reimplement
     [[townComparators.compareTowns]] because there we want to have all active communities
     together, and so on, while here we want all fortifications together, and so on."
    shared actual Comparison comparePairs([Point, AbstractTown] one,
            [Point, AbstractTown] two) =>
        comparing(comparingOn(pairFixture, townComparators.compareTownKind),
            comparingOn(pairPoint, DistanceComparator(hq, dimensions).compare),
            comparingOn(pairFixture,
                comparing(byDecreasing(AbstractTown.townSize),
                    byIncreasing(AbstractTown.status),
                    byIncreasing(AbstractTown.name))))(one, two);
}
