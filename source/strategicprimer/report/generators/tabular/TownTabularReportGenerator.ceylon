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
        return [[distanceString(loc, hq, dimensions), loc.string,
                ownerString(player, item.owner), item.kind, item.townSize.string,
                item.status.string, item.name]];
    }

    "Compare two location-town pairs."
    shared actual Comparison comparePairs([Point, AbstractTown] one,
            [Point, AbstractTown] two) => comparing(
                comparingOn(Tuple<Point|AbstractTown, Point, [AbstractTown]>.rest, // TODO: Use compose() instead of nested comparingOn()
                    comparingOn(Tuple<AbstractTown, AbstractTown, []>.first,
                        townComparators.compareTownKind)),
                comparingOn(Tuple<Point|AbstractTown, Point, [AbstractTown]>.first,
                    DistanceComparator(hq, dimensions).compare),
                comparingOn(Tuple<Point|AbstractTown, Point, [AbstractTown]>.rest, // TODO: Can't we just use townComparators.compareTowns()?
                    comparingOn(Tuple<AbstractTown, AbstractTown, []>.first,
                        comparing(comparingOn(AbstractTown.townSize,
                                townComparators.compareTownSize),
                            comparingOn(AbstractTown.status,
                                townComparators.compareTownStatus),
                            comparingOn(AbstractTown.name, increasing<String>)))))(one, two);
}
