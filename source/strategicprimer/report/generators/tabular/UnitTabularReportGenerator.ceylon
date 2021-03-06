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
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit,
    Animal
}

"A tabular report generator for units."
shared class UnitTabularReportGenerator(Player player, Point? hq, MapDimensions dimensions)
        extends AbstractTableGenerator<IUnit>()
        satisfies ITableGenerator<IUnit> {
    "The header row for this table."
    shared actual [String+] headerRow =
            ["Distance", "Location", "Owner", "Kind/Category", "Name", "Orders", "ID #"];

    "The file-name to (by default) write this table to."
    shared actual String tableName = "units";

    Comparison(Point, Point) distanceComparator;
    if (exists hq) {
        distanceComparator = DistanceComparator(hq, dimensions).compare;
    } else {
        distanceComparator = (Point one, Point two) => equal;
    }

    "Create a GUI table row representing the unit."
    shared actual {{String+}+} produce(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, IUnit item,
            Integer key, Point loc, Map<Integer, Integer> parentMap) {
        {String+} retval = [distanceString(loc, hq, dimensions), locationString(loc),
                ownerString(player, item.owner), item.kind, item.name,
                item.allOrders.last?.item else "",
                (player == item.owner) then item.id.string else "---"];
        for (member in item) {
            if (is Animal member) {
                // We don't want animals inside a unit showing up in the wild-animal
                // report
                fixtures.remove(member.id);
            } else if (player != item.owner) {
                // A player shouldn't be able to see the details of another player's
                // units.
                fixtures.remove(member.id);
            }
        }
        fixtures.remove(key);
        return Singleton(retval);
    }

    "Compare two location-unit pairs."
    shared actual Comparison comparePairs([Point, IUnit] one, [Point, IUnit] two) =>
        comparing(comparingOn(pairPoint, distanceComparator),
            comparingOn(pairFixture, comparing(byIncreasing(IUnit.owner),
                byIncreasing(IUnit.kind), byIncreasing(IUnit.name))))(one, two);
}
