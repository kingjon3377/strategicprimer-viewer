import lovelace.util.common {
    DelayedRemovalMap
}
import strategicprimer.viewer.model.map.fixtures.towns {
    Fortress
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
"A tabular report generator for fortresses."
shared class FortressTabularReportGenerator(Player player, Point hq)
        satisfies ITableGenerator<Fortress> {
    "The header fields are Distance, Location, Owner, and Name."
    shared actual [String+] headerRow = ["Distance", "Location", "Owner", "Name"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "fortresses";
    "Write a table row representing the fortress."
    shared actual Boolean produce(Anything(String) ostream,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            Fortress item, Point loc) {
        writeRow(ostream, distanceString(loc, hq), loc.string,
            ownerString(player, item.owner), item.name);
        // Players shouldn't be able to see the contents of others' fortresses
        // in other tables.
        if (player != item.owner) {
            for (member in item) {
                fixtures.remove(member.id);
            }
        }
        return true;
    }
    "Compare two Point-Fortress pairs."
    shared actual Comparison comparePairs([Point, Fortress] one,
            [Point, Fortress] two) {
        Comparison(Point, Point) comparator =
                DistanceComparator(hq).compare;
        Fortress first = one.rest.first;
        Fortress second = two.rest.first;
        Comparison cmp = comparator(one.first, two.first);
        if (player == first.owner, player != second.owner) {
            return smaller;
        } else if (player != first.owner, player == second.owner) {
            return larger;
        } else if (cmp == equal) {
            Comparison nameCmp = first.name.compare(second.name);
            if ("HQ" == first.name, "HQ" != second.name) {
                return smaller;
            } else if ("HQ" != first.name, "HQ" == second.name) {
                return larger;
            } else if (nameCmp == equal) {
                return first.owner <=> second.owner;
            } else {
                return nameCmp;
            }
        } else {
            return cmp;
        }
    }
}
