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
import strategicprimer.model.map.fixtures.mobile {
    IUnit,
    Animal
}
"A tabular report generator for units."
shared class UnitTabularReportGenerator(Player player, Point hq, MapDimensions dimensions)
        satisfies ITableGenerator<IUnit> {
    "The header row for this table."
    shared actual [String+] headerRow =
            ["Distance", "Location", "Owner", "Kind/Category", "Name", "Orders"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "units";
    "Create a GUI table row representing the unit."
    shared actual {String+} produce(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, IUnit item,
            Point loc) {
        {String+} retval = {distanceString(loc, hq, dimensions), loc.string,
                ownerString(player, item.owner), item.kind, item.name,
                item.allOrders.last?.item else ""};
        for (member in item) {
            if (is Animal item) {
                // We don't want animals inside a unit showing up in the wild-animal
                // report
                fixtures.remove(item.id);
            } else if (player != item.owner) {
                // A player shouldn't be able to see the details of another player's
                // units.
                fixtures.remove(item.id);
            }
        }
        return retval;
    }
    "Compare two location-unit pairs."
    shared actual Comparison comparePairs([Point, IUnit] one, [Point, IUnit] two) {
        return comparing(
            comparingOn(([Point, IUnit] pair) => pair.first,
                DistanceComparator(hq, dimensions).compare),
            comparingOn(([Point, IUnit] pair) =>
                pair.rest.first.owner, increasing<Player>),
            comparingOn(([Point, IUnit] pair) => pair.rest.first.kind,
                increasing<String>),
            comparingOn(([Point, IUnit] pair) => pair.rest.first.name,
                increasing<String>))(one, two);
    }
}
