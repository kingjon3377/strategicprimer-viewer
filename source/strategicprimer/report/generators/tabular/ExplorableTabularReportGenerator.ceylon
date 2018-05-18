import lovelace.util.common {
    DelayedRemovalMap
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
import strategicprimer.model.map.fixtures {
    TextFixture
}
import strategicprimer.model.map.fixtures.explorable {
    ExplorableFixture,
    Cave,
    Portal,
    AdventureFixture,
    Battlefield
}
"A tabular report generator for things that can be explored and are not covered elsewhere:
  caves, battlefields, adventure hooks, and portals."
shared class ExplorableTabularReportGenerator(Player player, Point hq,
        MapDimensions dimensions)
        satisfies ITableGenerator<ExplorableFixture|TextFixture> {
    "The header row for the table."
    shared actual [String+] headerRow = ["Distance", "Location", "Brief Description",
        "Claimed By", "Long Description"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "explorables";
    "Create a GUI table row representing the given fixture."
    shared actual {{String+}*} produce(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            ExplorableFixture|TextFixture item, Integer key, Point loc,
            Map<Integer, Integer> parentMap) {
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
            owner = "---";
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
            return [];
        }
        fixtures.remove(key);
        return [[distanceString(loc, hq, dimensions), loc.string, brief, owner,
            longDesc]];
    }
    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs([Point, ExplorableFixture|TextFixture] one,
            [Point, ExplorableFixture|TextFixture] two) {
        Comparison cmp = DistanceComparator(hq, dimensions).compare(one.first, two.first);
        if (cmp == equal) {
            return one.rest.first.string.compare(two.rest.first.string);
        } else {
            return cmp;
        }
    }
}
