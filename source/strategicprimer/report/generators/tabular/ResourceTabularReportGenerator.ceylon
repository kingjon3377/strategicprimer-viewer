import ceylon.collection {
    MutableMap,
    HashMap
}

import lovelace.util.common {
    DelayedRemovalMap,
    comparingOn,
    narrowedStream
}

import strategicprimer.model.common {
    DistanceComparator
}

import strategicprimer.model.common.map {
    MapDimensions,
    IFixture,
    Point
}
import strategicprimer.model.common.map.fixtures {
    Implement,
    ResourcePile
}
import strategicprimer.model.common.map.fixtures.resources {
    CacheFixture
}

"A tabular report generator for resources, including [[caches|CacheFixture]],
 [[resource piles|ResourcePile]], and [[equipment|Implement]]."
shared class ResourceTabularReportGenerator(Point? hq, MapDimensions dimensions)
        extends AbstractTableGenerator<Implement|CacheFixture|ResourcePile>()
        satisfies ITableGenerator<Implement|CacheFixture|ResourcePile> {
    "The file-name to (by default) write this table to."
    shared actual String tableName = "resources";
    "The header row for this table."
    shared actual [String+] headerRow = ["Distance", "Location", "Kind",
        "Quantity", "Specifics"];

    Comparison(Point, Point) distanceComparator;
    if (exists hq) {
        distanceComparator = DistanceComparator(hq, dimensions).compare;
    } else {
        distanceComparator = (Point one, Point two) => equal;
    }

    "Create a GUI table row representing the given fixture."
    shared actual [{String+}+] produce(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            Implement|CacheFixture|ResourcePile item, Integer key, Point loc,
            Map<Integer, Integer> parentMap) {
        String kind;
        String quantity;
        String specifics;
        switch (item)
        case (is ResourcePile) {
            kind = item.kind;
            quantity = item.quantity.string;
            specifics = item.contents;
        }
        case (is Implement) {
            kind = "equipment";
            quantity = item.count.string;
            specifics = item.kind;
        }
        case (is CacheFixture) {
            kind = item.kind;
            quantity = "---";
            specifics = item.contents;
        }
        fixtures.remove(key);
        return [[distanceString(loc, hq, dimensions), locationString(loc),
            kind, quantity, specifics]];
    }

    Comparison compareItems(Implement|CacheFixture|ResourcePile first,
            Implement|CacheFixture|ResourcePile second) {
        switch (first)
        case (is ResourcePile) {
            if (is ResourcePile second) {
                return comparing(byIncreasing(ResourcePile.kind),
                    byIncreasing(ResourcePile.contents),
                    byDecreasing(ResourcePile.quantity))(first, second);
            } else {
                return smaller;
            }
        }
        case (is Implement) {
            if (is Implement second) {
                return comparing(byIncreasing(Implement.kind),
                    byDecreasing(Implement.count))(first, second);
            } else if (is ResourcePile second) {
                return larger;
            } else {
                return smaller;
            }
        }
        case (is CacheFixture) {
            if (is CacheFixture second) {
                return comparing(byIncreasing(CacheFixture.kind),
                    byIncreasing(CacheFixture.contents))(first, second);
            } else {
                return larger;
            }
        }
    }

    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs(
            [Point, Implement|CacheFixture|ResourcePile] one,
            [Point, Implement|CacheFixture|ResourcePile] two) =>
        comparing(comparingOn(pairPoint, distanceComparator),
            comparingOn(pairFixture, compareItems))(one, two);

    "Write rows for equipment, counting multiple identical Implements in one line."
    shared actual void produceTable(Anything(String) ostream,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            Map<Integer, Integer> parentMap) {
        {[Integer, [Point, CacheFixture|Implement|ResourcePile]]*} values =
                narrowedStream<Integer, [Point, CacheFixture|Implement|ResourcePile]>
                    (fixtures).sort(comparingOn(
                        Entry<Integer, [Point, CacheFixture|Implement|ResourcePile]>.item,
                        comparePairs)).map(Entry.pair);
        writeRow(ostream, headerRow.first, *headerRow.rest);
        MutableMap<[Point, String], Integer> implementCounts =
            HashMap<[Point, String], Integer>();
        for ([key, [loc, fixture]] in values) {
            switch (fixture)
            case (is Implement) {
                Integer num;
                if (exists temp = implementCounts[[loc, fixture.kind]]) {
                    num = temp;
                } else {
                    num = 0;
                }
                implementCounts[[loc, fixture.kind]] = num + fixture.count;
                fixtures.remove(key);
            } case (is CacheFixture) {
                // FIXME: combine with ResourcePile case once eclipse/ceylon#7372 fixed
                value [row, *_] = produce(fixtures, fixture, key, loc, parentMap);
                writeRow(ostream, row.first, *row.rest);
                fixtures.remove(key);
            } case (is ResourcePile) {
                value [row, *_] = produce(fixtures, fixture, key, loc, parentMap);
                writeRow(ostream, row.first, *row.rest);
                fixtures.remove(key);
            }
        }
        for ([loc, key]->count in implementCounts) {
            writeRow(ostream, distanceString(loc, hq, dimensions), locationString(loc),
                "equipment", count.string, key);
        }
        fixtures.coalesce();
    }
}
