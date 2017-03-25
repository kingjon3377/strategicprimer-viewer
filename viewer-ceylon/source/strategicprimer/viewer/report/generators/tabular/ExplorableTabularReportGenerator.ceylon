import lovelace.util.common {
    DelayedRemovalMap
}
import lovelace.util.jvm {
    ceylonComparator
}
import model.map.fixtures {
    TextFixture
}
import strategicprimer.viewer.model {
    DistanceComparator
}
import model.map {
    Player,
    Point,
    IFixture
}
import model.map.fixtures.explorable {
    Portal,
    ExplorableFixture,
    Cave,
    Battlefield,
    AdventureFixture
}
"A tabular report generator for things that can be explored and are not covered elsewhere:
  caves, battlefields, adventure hooks, and portals."
shared class ExplorableTabularReportGenerator(Player player, Point hq)
        satisfies ITableGenerator<ExplorableFixture|TextFixture> {
    "The header row for the table."
    shared actual [String+] headerRow = ["Distance", "Location", "Brief Description",
        "Claimed By", "Long Description"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "explorables";
    "Produce a report line about the given fixture."
    shared actual Boolean produce(Anything(String) ostream,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            ExplorableFixture|TextFixture item, Point loc) {
        String brief;
        String owner;
        String longDesc;
        switch (item)
        case (is TextFixture) {
            if (item.turn >= 0) {
                brief = "Text Note (``item.turn``)";
            } else {
                brief = "Text Note";
            }
            owner = "---";
            longDesc = item.text;
        }
        case (is Battlefield) {
            brief = "ancient battlefield";
            owner = "---";
            longDesc = "";
        }
        case (is Cave) {
            brief = "caves nearby";
            owner = "---";
            longDesc = "";
        }
        case (is Portal) {
            if (item.destinationCoordinates.valid) {
                brief = "portal to world ``item.destinationWorld``";
            } else {
                brief = "portal to another world";
            }
            owner = "---"; // TODO: report owner?
            longDesc = "";
        }
        case (is AdventureFixture) {
            brief = item.briefDescription;
            if (player == item.owner) {
                owner = "You";
            } else if (item.owner.independent) {
                owner = "No-one";
            } else {
                owner = ownerString(player, item.owner);
            }
            longDesc = item.fullDescription;
        }
        else {
            return false;
        }
        writeRow(ostream, distanceString(loc, hq), loc.string, brief, owner, longDesc);
        return true;
    }
    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs([Point, ExplorableFixture|TextFixture] one,
            [Point, ExplorableFixture|TextFixture] two) {
        Comparison cmp = DistanceComparator(hq).compare(one.first, two.first);
        if (cmp == equal) {
            return one.rest.first.string.compare(two.rest.first.string);
        } else {
            return cmp;
        }
    }
}
