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
    Fortress
}

"A tabular report generator for fortresses."
shared class FortressTabularReportGenerator(Player player, Point? hq,
        MapDimensions dimensions) extends AbstractTableGenerator<Fortress>() {
    "The header fields are Distance, Location, Owner, and Name."
    shared actual [String+] headerRow = ["Distance", "Location", "Owner", "Name"];

    "The file-name to (by default) write this table to."
    shared actual String tableName = "fortresses";

    Comparison(Point, Point) distanceComparator;
    if (exists hq) {
        distanceComparator = DistanceComparator(hq, dimensions).compare;
    } else {
        distanceComparator = (Point one, Point two) => equal;
    }

    "Create a GUI table row representing the fortress."
    shared actual [{String+}+] produce(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            Fortress item, Integer key, Point loc, Map<Integer, Integer> parentMap) {
        {String+} retval = [distanceString(loc, hq, dimensions), locationString(loc),
            ownerString(player, item.owner), item.name];
        // Players shouldn't be able to see the contents of others' fortresses
        // in other tables.
        if (player != item.owner) {
            fixtures.removeAll(item.map(IFixture.id));
        }
        fixtures.remove(key);
        return [retval];
    }

    "Compare two fortresses based on whether they are owned by the player for whom the
     report is being produced."
    Comparison compareOwners(Fortress one, Fortress two) {
        if (player == one.owner, player != two.owner) {
            return smaller;
        } else if (player == two.owner, player != one.owner) {
            return larger;
        } else {
            return equal;
        }
    }

    "Compare two fortresses' names, with a special case so HQ goes at the top."
    Comparison compareNames(Fortress one, Fortress two) {
        if ("HQ" == one.name, "HQ" != two.name) {
            return smaller;
        } else if ("HQ" == two.name, "HQ" != one.name) {
            return larger;
        } else {
            return one.name <=> two.name;
        }
    }

    "Compare two Point-Fortress pairs."
    shared actual Comparison comparePairs([Point, Fortress] one, [Point, Fortress] two) =>
        comparing(comparingOn(pairFixture, compareOwners),
                comparingOn(pairPoint, distanceComparator),
                comparingOn(pairFixture, compareNames),
                byIncreasing(compose(Fortress.owner, pairFixture)))
            (one, two);
}
