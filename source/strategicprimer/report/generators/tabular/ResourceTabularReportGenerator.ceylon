import ceylon.collection {
    MutableMap,
    HashMap
}

import lovelace.util.common {
    DelayedRemovalMap,
    comparingOn
}

import strategicprimer.model.map {
    IFixture,
    Point
}
import strategicprimer.model.map.fixtures {
    Implement,
    ResourcePile
}
import strategicprimer.model.map.fixtures.resources {
    CacheFixture
}
"A tabular report generator for resources, including caches, resource piles, and
 implements (equipment)."
shared class ResourceTabularReportGenerator()
        satisfies ITableGenerator<Implement|CacheFixture|ResourcePile> {
    "The file-name to (by default) write this table to."
    shared actual String tableName = "resources";
    "The header row for this table."
    shared actual [String+] headerRow = ["Kind", "Quantity", "Specifics"];
    "Create a GUI table row representing the given fixture."
    shared actual [{String+}+] produce(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            Implement|CacheFixture|ResourcePile item, Point loc) {
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
            quantity = "1";
            specifics = item.kind;
        }
        case (is CacheFixture) {
            kind = item.kind;
            quantity = "---";
            specifics = item.contents;
        }
        return [[kind, quantity, specifics]];
    }
    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs(
            [Point, Implement|CacheFixture|ResourcePile] one,
            [Point, Implement|CacheFixture|ResourcePile] two) {
        value first = one.rest.first;
        value second = two.rest.first;
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
                return first.kind<=>second.kind;
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
    "Write rows for equipment, counting multiple identical Implements in one line."
    shared actual void produceTable(Anything(String) ostream,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures) {
        {[Integer, [Point, CacheFixture|Implement|ResourcePile]]*} values =
                { for (key->item in fixtures)
                if (is CacheFixture|Implement|ResourcePile resource = item.rest.first)
                [key, [item.first, resource]]}
                    .sort(comparingOn(
                            ([Integer,
                                [Point, CacheFixture|Implement|ResourcePile]] pair) =>
                    pair.rest.first, comparePairs));
        writeRow(ostream, headerRow.first, *headerRow.rest);
        MutableMap<String, Integer> implementCounts = HashMap<String, Integer>();
        for ([key, [loc, fixture]] in values) {
            switch (fixture)
            case (is Implement) {
                Integer num;
                if (exists temp = implementCounts[fixture.kind]) {
                    num = temp;
                } else {
                    num = 0;
                }
                implementCounts[fixture.kind] = num + 1;
                fixtures.remove(key);
            } case (is CacheFixture) {
                // FIXME: combine with ResourcePile case once compiler accepts it
                value [row, *_] = produce(fixtures, fixture, loc);
                writeRow(ostream, row.first, *row.rest);
                fixtures.remove(key);
            } case (is ResourcePile) {
                value [row, *_] = produce(fixtures, fixture, loc);
                writeRow(ostream, row.first, *row.rest);
                fixtures.remove(key);
            }
        }
        for (key->count in implementCounts) {
            writeRow(ostream, "equipment", count.string, key);
        }
        fixtures.coalesce();
    }
}
