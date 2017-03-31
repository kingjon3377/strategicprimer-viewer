import lovelace.util.common {
    DelayedRemovalMap
}
import strategicprimer.viewer.model.map {
    IFixture,
    Point
}
import strategicprimer.viewer.model.map.fixtures {
    Implement,
    ResourcePile
}
import ceylon.collection {
    MutableMap,
    HashMap
}
import strategicprimer.viewer.model.map.fixtures.resources {
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
    "Write a table row based on the given fixture."
    shared actual Boolean produce(Anything(String) ostream,
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
        writeRow(ostream, kind, quantity, specifics);
        return true;
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
                    // TODO: Total comparison of Quantity, as in Java compareTo().
                    byDecreasing((ResourcePile pile)
                    => pile.quantity.floatNumber))(first, second);
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
                            ([Integer, [Point, CacheFixture|Implement|ResourcePile]] pair) =>
                    pair.rest.first, comparePairs));
        writeRow(ostream, headerRow.first, *headerRow.rest);
        MutableMap<String, Integer> implementCounts = HashMap<String, Integer>();
        for ([key, [loc, fixture]] in values) {
            switch (fixture)
            case (is Implement) {
                Integer num;
                if (exists temp = implementCounts.get(fixture.kind)) {
                    num = temp;
                } else {
                    num = 0;
                }
                implementCounts.put(fixture.kind, num + 1);
                fixtures.remove(key);
            } case (is CacheFixture) {
                // FIXME: combine with ResourcePile case once compiler accepts it
                if (produce(ostream, fixtures, fixture, loc)) {
                    fixtures.remove(key);
                }
            } case (is ResourcePile) {
                if (produce(ostream, fixtures, fixture, loc)) {
                    fixtures.remove(key);
                }
            }
        }
        for (key->count in implementCounts) {
            writeRow(ostream, "equipment", count.string, key);
        }
        fixtures.coalesce();
    }
}
