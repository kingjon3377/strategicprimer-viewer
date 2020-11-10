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
    Point,
    TileFixture
}

import strategicprimer.model.common.map.fixtures {
    TextFixture
}

import strategicprimer.model.common.map.fixtures.explorable {
    ExplorableFixture,
    Cave,
    Portal,
    AdventureFixture,
    Battlefield
}

"A tabular report generator for things that can be explored and are not covered elsewhere:
  caves, battlefields, adventure hooks, and portals."
shared class ExplorableTabularReportGenerator(Player player, Point? hq,
        MapDimensions dimensions)
        extends AbstractTableGenerator<ExplorableFixture|TextFixture>()
        satisfies ITableGenerator<ExplorableFixture|TextFixture> {
    "The header row for the table."
    shared actual [String+] headerRow = ["Distance", "Location", "Brief Description",
        "Claimed By", "Long Description"];

    "The file-name to (by default) write this table to."
    shared actual String tableName = "explorables";

    Comparison(Point, Point) distanceComparator;
    if (exists hq) {
        distanceComparator = DistanceComparator(hq, dimensions).compare;
    } else {
        distanceComparator = (Point one, Point two) => equal;
    }

    "Create a GUI table row representing the given fixture."
    shared actual [{[String(), Anything(String)?]+}+] produce(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            ExplorableFixture|TextFixture item, Integer key, Point loc,
            Map<Integer, Integer> parentMap) {
        String() briefGetter;
        Anything(String)? briefSetter;
        String() ownerGetter;
        Anything(String)? ownerSetter;
        String() longDescGetter;
        Anything(String)? longDescSetter;
        switch (item)
        case (is TextFixture) {
            briefGetter = () => (item.turn >= 0) then "Text Note (``item.turn``)" else "Text Note";
            briefSetter = null; // TODO: implement a setter?
            ownerGetter = invalidGetter;
            ownerSetter = null;
            longDescGetter = () => item.text;
            longDescSetter = (String str) => item.text = str;
        }
        case (is Battlefield) {
            briefGetter = () => "ancient battlefield";
            briefSetter = null;
            ownerGetter = invalidGetter;
            ownerSetter = null;
            longDescGetter = invalidGetter;
            longDescSetter = null;
        }
        case (is Cave) {
            briefGetter = () => "caves nearby";
            briefSetter = null;
            ownerGetter = invalidGetter;
            ownerSetter = null;
            longDescGetter = invalidGetter;
            longDescSetter = null;
        }
        case (is Portal) {
            briefGetter = () => (item.destinationCoordinates.valid) then
                "portal to world ``item.destinationWorld``" else "portal to another world";
            briefSetter = null; // TODO: implement a setter?
            ownerGetter = invalidGetter;
            ownerSetter = null;
            longDescGetter = invalidGetter;
            longDescSetter = null;
        }
        case (is AdventureFixture) {
            briefGetter = () => item.briefDescription;
            briefSetter = (String str) => item.briefDescription = str;
            ownerGetter = () {
                if (player == item.owner) {
                    return "You";
                } else if (item.owner.independent) {
                    return "No-one";
                } else {
                    return ownerString(player, item.owner);
                }
            };
            ownerSetter = null; // TODO: implement?
            longDescGetter = () => item.fullDescription;
            longDescSetter = (String str) => item.fullDescription = str;
        }
        else {
            // TODO: We should log a warning here, right?
            return [];
        }
        fixtures.remove(key);
        return [[[() => distanceString(loc, hq, dimensions), null], locationEntry(loc),
            [briefGetter, briefSetter], [ownerGetter, ownerSetter], [longDescGetter, longDescSetter]]];
    }

    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs([Point, ExplorableFixture|TextFixture] one,
            [Point, ExplorableFixture|TextFixture] two) =>
        comparing(comparingOn(pairPoint, distanceComparator),
            byIncreasing(compose(TileFixture.string, pairFixture)))(one, two);
}
