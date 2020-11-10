import lovelace.util.common {
    DelayedRemovalMap,
    comparingOn
}

import strategicprimer.model.common {
    DistanceComparator
}

import strategicprimer.model.common.map {
    IFixture,
    MapDimensions,
    Point
}

import strategicprimer.model.common.map.fixtures.mobile {
    Immortal
}

import ceylon.language.meta {
    type
}

"""A tabular report generator for [["immortals."|Immortal]]"""
shared class ImmortalsTabularReportGenerator(Point? hq, MapDimensions dimensions)
        extends AbstractTableGenerator<Immortal>()
        /*satisfies ITableGenerator<Immortal>*/ {
    "The header row for this table."
    shared actual [String+] headerRow = ["Distance", "Location", "Immortal"];

    "The file-name to (by default) write this table to."
    shared actual String tableName = "immortals";

    Comparison(Point, Point) distanceComparator;
    if (exists hq) {
        distanceComparator = DistanceComparator(hq, dimensions).compare;
    } else {
        distanceComparator = (Point one, Point two) => equal;
    }

    "Create a GUI table row representing the given fixture."
    shared actual [{[String(), Anything(String)?]+}+] produce(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            Immortal item, Integer key, Point loc, Map<Integer, Integer> parentMap) {
        fixtures.remove(key);
        return [[[distanceString(loc, hq, dimensions), null], locationEntry(loc), [item.string, null]]]; // FIXME: kinded immortals should be settable, and ideally others
    }

    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs([Point, Immortal] one,
            [Point, Immortal] two) =>
            comparing(comparingOn(pairPoint, distanceComparator),
                comparingOn(pairFixture,
                    comparing(byIncreasing(compose(Object.hash, type<Immortal>)),
                    byIncreasing(Object.hash))))(one, two);
}
