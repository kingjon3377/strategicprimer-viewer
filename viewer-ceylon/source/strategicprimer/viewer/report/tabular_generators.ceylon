import model.map.fixtures.towns {
    Fortress,
    AbstractTown,
    Village
}
import model.map {
    Player,
    Point,
    IFixture,
    DistanceComparator,
    TileFixture
}
import util {
    LineEnd
}
import model.map.fixtures.mobile {
    IWorker,
    Animal,
    Immortal,
    IUnit
}
import model.map.fixtures.mobile.worker {
    WorkerStats
}
import lovelace.util.common {
    todo,
    DelayedRemovalMap
}
import model.map.fixtures.resources {
    Grove,
    Meadow,
    Shrub,
    Mine,
    StoneDeposit,
    MineralVein,
    CacheFixture
}
import model.map.fixtures.terrain {
    Forest
}
import lovelace.util.jvm {
    ceylonComparator
}
import ceylon.language.meta {
    typeOf=type
}
import model.map.fixtures {
    MineralFixture,
    Ground,
    TextFixture,
    ResourcePile,
    Implement
}
import model.map.fixtures.explorable {
    ExplorableFixture,
    Battlefield,
    Portal,
    Cave,
    AdventureFixture
}
import ceylon.collection {
    MutableMap,
    HashMap,
    MutableList,
    ArrayList
}
import controller.map.misc {
    TownComparator
}
import ceylon.regex {
    Regex,
    regex
}
import ceylon.math.float {
    sqrt
}
"A regular expression to mtch quote characters."
Regex quotePattern = regex("\"", true);
"An interface for tabular-report generators. It's expected that implementers will take the
 current player and the location of his or her HQ as constructor parameters."
interface ITableGenerator<T> given T satisfies IFixture {
    "Produce a tabular report on a particular category of fixtures in the map, and remove
      all fixtures covered in the table from the collection."
    shared default void produceTable(Anything(String) ostream, DelayedRemovalMap<Integer,
            [Point, IFixture]> fixtures) {
        MutableList<[Integer, [Point, T]]> temp =
                ArrayList<[Integer, [Point, T]]>();
        for (key->val in fixtures) {
            if (is T fixture = val.rest.first) {
                temp.add([key, [val.first, fixture]]);
            }
        }
        {[Integer, [Point, T]]*} values = temp
            .sort(comparingOn(([Integer, [Point, T]] pair) => pair.rest.first,
            comparePairs));
        writeRow(ostream, headerRow.first, *headerRow.rest);
        for ([num, [loc, item]] in values) {
            if (produce(ostream, fixtures, item,
                    loc)) {
                fixtures.remove(num);
            }
        }
        fixtures.coalesce();
    }
    "Produce a single line of the tabular report. Return whether to remove this item from
     the collection."
    shared formal Boolean produce(
        "The stream to write the row to."
        Anything(String) ostream,
        "The set of fixtures."
        DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
        "The item to base this line on."
        T item,
        "The location of this item in the map."
        Point loc);
    "Given two points, return a number sufficiently proportional to the distance between
     them for ordering points based on distance from a base. The default implementation
     returns the *square* of the distance, for efficiency."
    todo("Reflect the toroidal topology of the map")
    shared default Integer distance(Point first, Point second) =>
            ((first.col - second.col) * (first.col - second.col)) +
            ((first.row - second.row) * (first.row - second.row));
    "A String showing the distance between two points, suitable to be displayed, rounded
     to a tenth of a tile. This default implementation just takes the square root of
     [[distance]] and formats it."
    shared default String distanceString(Point first, Point second) =>
            Float.format(sqrt(distance(first, second).float), 1, 1);
    "The CSV header row to print at the top of the report, not including the newline."
    shared formal [String+] headerRow;
    "Compare two Point-fixture pairs."
    shared formal Comparison comparePairs([Point, T] one, [Point, T] two);
    """"A String representing the owner of a fixture: "You" if equal to currentPlayer,
       "Independent" if an independent player, or otherwise the player's name."""
    shared default String ownerString(Player currentPlayer, Player owner) {
        if (currentPlayer == owner) {
            return "You";
        } else if (owner.independent) {
            return "Independent";
        } else {
            return owner.name;
        }
    }
    """"The field delimiter; provided to limit "magic character" warnings and allow us to
       change it."""
    shared default Character fieldDelimiter => ',';
    """The row delimiter; used to limit "magic character" warnings and allow us to change
       it."""
    shared default String rowDelimiter => LineEnd.lineSep;
    "Write multiple fields to a row, quoting as necessary, separated by the field
     delimiter, with the last field followed by the row delimiter."
    shared default void writeRow(Anything(String) ostream, String firstField,
            String* fields) {
        void writeField(Anything(String) ostream, String field) {
            String quotesQuoted = quotePattern.replace(field, "\"\"");
            if ({"\"", fieldDelimiter.string, rowDelimiter, " "}
                    .any(quotesQuoted.contains)) {
                ostream("\"``quotesQuoted``\"");
            } else {
                ostream(quotesQuoted);
            }
        }
        writeField(ostream, firstField);
        for (field in fields) {
            ostream(fieldDelimiter.string);
            writeField(ostream, field);
        }
        ostream(rowDelimiter);
    }
    "The file-name to (by default) write this table to."
    shared formal String tableName;
}
"A tabular report generator for fortresses."
class FortressTabularReportGenerator(Player player, Point hq)
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
        Comparison(Point, Point) comparator = ceylonComparator(DistanceComparator(hq));
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
                return ceylonComparator((Player one, Player two) => one.compareTo(two))
                    (first.owner, second.owner);
            } else {
                return nameCmp;
            }
        } else {
            return cmp;
        }
    }
}
"A report generator for workers. We do not cover Jobs or Skills; see the main report for
 that."
class WorkerTabularReportGenerator(Point hq) satisfies ITableGenerator<IWorker> {
    "The header row of the table."
    shared actual [String+] headerRow = ["Distance", "Location", "HP", "Max HP", "Str",
        "Dex", "Con", "Int", "Wis", "Cha"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "workers";
    "Produce a table line representing a worker."
    shared actual Boolean produce(Anything(String) ostream,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, IWorker item,
            Point loc) {
        if (exists stats = item.stats) {
            writeRow(ostream, distanceString(loc, hq), loc.string, item.name,
                stats.hitPoints.string, stats.maxHitPoints.string,
                 for (stat in { stats.strength, stats.dexterity, stats.constitution,
                    stats.intelligence, stats.wisdom, stats.charisma })
                        WorkerStats.getModifierString(stat) );
        } else {
            writeRow(ostream, distanceString(loc, hq), loc.string, item.name,
                *(0..9).map((num) => "---"));
        }
        return true;
    }
    "Compare two worker-location pairs."
    shared actual Comparison comparePairs([Point, IWorker] one,
            [Point, IWorker] two) {
        Comparison(Point, Point) comparator = ceylonComparator(DistanceComparator(hq));
        IWorker first = one.rest.first;
        IWorker second = two.rest.first;
        Comparison cmp = comparator(one.first, two.first);
        if (cmp == equal) {
            return (first.name.compare(second.name));
        } else {
            return cmp;
        }
    }
}
"A tabular report generator for crops---forests, groves, orchards, fields, meadows, and
 shrubs"
class CropTabularReportGenerator(Point hq)
        satisfies ITableGenerator<Forest|Shrub|Meadow|Grove> {
    "The header row for the table."
    shared actual [String+] headerRow = ["Distance", "Location", "Kind", "Cultivation",
        "Status", "Crop"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "crops";
    "Produce the report line for a fixture."
    shared actual Boolean produce(Anything(String) ostream,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            Forest|Shrub|Meadow|Grove item, Point loc) {
        String kind;
        String cultivation;
        String status;
        String crop = item.kind;
        if (is Forest item) {
            kind = (item.rows) then "rows" else "forest";
            cultivation = "---";
            status = "---";
        } else if (is Shrub item) {
            kind = "shrub";
            cultivation = "---";
            status = "---";
        } else if (is Meadow item) {
            kind = (item.field) then "field" else "meadow";
            cultivation = (item.cultivated) then "cultivated" else "wild";
            status = item.status.string;
        } else if (is Grove item) {
            kind = (item.orchard) then "orchard" else "grove";
            cultivation = (item.cultivated) then "cultivated" else "wild";
            status = "---";
        } else {
            return false;
        }
        writeRow(ostream, distanceString(loc, hq), loc.string, kind, cultivation, status,
            crop);
        return true;
    }
    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs([Point, Forest|Shrub|Meadow|Grove] one,
            [Point, Forest|Shrub|Meadow|Grove] two) {
        Forest|Shrub|Meadow|Grove first = one.rest.first;
        Forest|Shrub|Meadow|Grove second = two.rest.first;
        Comparison cropCmp = first.kind.compare(second.kind);
        if (cropCmp == equal) {
            Comparison cmp = ceylonComparator(DistanceComparator(hq))(
                one.first, two.first);
            if (cmp == equal) {
                return comparing(byIncreasing<TileFixture, Integer>(
                        (fix) => typeOf(fix).hash), byIncreasing(TileFixture.hash))(
                    first, second);
            } else {
                return cmp;
            }
        } else {
            return cropCmp;
        }
    }
}
"A tabular report generator for resources that can be mined---mines, mineral veins, stone
 deposits, and Ground."
class DiggableTabularReportGenerator(Point hq) satisfies ITableGenerator<MineralFixture> {
    "The header row for the table."
    shared actual [String+] headerRow = ["Distance", "Location", "Kind", "Product",
        "Status"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "minerals";
    "Produce the report line for a fixture."
    shared actual Boolean produce(Anything(String) ostream,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, MineralFixture item,
            Point loc) {
        String classField;
        String statusField;
        switch (item)
        case (is Ground) {
            classField = "ground";
            statusField = (item.exposed) then "exposed" else "not exposed";
        }
        case (is Mine) {
            classField = "mine";
            statusField = item.status.string;
        }
        case (is StoneDeposit ) {
            classField = "deposit";
            statusField = "exposed";
        }
        case (is MineralVein) {
            classField = "vein";
            statusField = (item.exposed) then "exposed" else "not exposed";
        }
        else {
            return false;
        }
        writeRow(ostream, distanceString(loc, hq), loc.string, classField, item.kind,
            statusField);
        return true;
    }
    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs([Point, MineralFixture] one,
            [Point, MineralFixture] two) {
        return comparing(
            byIncreasing(([Point, MineralFixture] pair) => pair.rest.first.kind),
            ([Point, MineralFixture] first, [Point, MineralFixture] second) =>
                ceylonComparator(DistanceComparator(hq))(first.first, second.first),
            byIncreasing(([Point, MineralFixture] pair) => pair.rest.first.hash))
            (one, two);
    }
}
"A report generator for sightings of animals."
class AnimalTabularReportGenerator(Point hq) satisfies ITableGenerator<Animal> {
    "The header row for the table."
    shared actual [String+] headerRow = ["Distance", "Location", "Kind"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "animals";
    "Produce a single line of the tabular report on animals."
    shared actual Boolean produce(Anything(String) ostream,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            Animal item, Point loc) {
        String kind;
        if (item.traces) {
            kind = "tracks or traces of ``item.kind``";
        } else if (item.talking) {
            kind = "talking ``item.kind``";
        } else if ("wild" != item.status) {
            kind = "``item.status`` ``item.kind``";
        } else {
            kind = item.kind;
        }
        writeRow(ostream, distanceString(loc, hq), loc.string, kind);
        return true;
    }
    "Compare two pairs of Animals and locations."
    shared actual Comparison comparePairs([Point, Animal] one, [Point, Animal] two) {
        Comparison cmp = ceylonComparator(DistanceComparator(hq))(one.first, two.first);
        if (cmp == equal) {
            Comparison(Animal, Animal) compareBools(Boolean(Animal) func) {
                Comparison retval(Boolean first, Boolean second) {
                    if (first == second) {
                        return equal;
                    } else if (first) {
                        return larger;
                    } else {
                        return smaller;
                    }
                }
                return (Animal first, Animal second) => retval(func(first), func(second));
            }
            return comparing(compareBools(Animal.talking),
                    compareBools((animal) => !animal.traces), byIncreasing(Animal.kind))(
                one.rest.first, two.rest.first);
        } else {
            return cmp;
        }
    }
}
"A tabular report generator for things that can be explored and are not covered elsewhere:
  caves, battlefields, adventure hooks, and portals."
class ExplorableTabularReportGenerator(Player player, Point hq)
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
        Comparison cmp = ceylonComparator(DistanceComparator(hq))(one.first, two.first);
        if (cmp == equal) {
            return one.rest.first.string.compare(two.rest.first.string);
        } else {
            return cmp;
        }
    }
}
"Compare an object with another object based on one field in particular."
todo("Move to lovelace.util")
Comparison(BaseType, BaseType) comparingOn<BaseType, FieldType>(
        "The field as a function of the object."
        FieldType(BaseType) extractor,
        "How to compare the corresponding field instances"
        Comparison(FieldType, FieldType) comparator) {
    return (BaseType first, BaseType second) =>
        comparator(extractor(first), extractor(second));
}
"""A tabular report generator for "immortals.""""
class ImmortalsTabularReportGenerator(Point hq) satisfies ITableGenerator<Immortal> {
    "The header row for this table."
    shared actual [String+] headerRow = ["Distance", "Location", "Immortal"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "immortals";
    "Produce a table row for the given fixture."
    shared actual Boolean produce(Anything(String) ostream,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, Immortal item,
            Point loc) {
        writeRow(ostream, distanceString(loc, hq), loc.string, item.string);
        return true;
    }
    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs([Point, Immortal] one,
            [Point, Immortal] two) {
        return comparing(comparingOn(
                ([Point, Immortal] pair) => pair.first,
                ceylonComparator(DistanceComparator(hq))),
            comparingOn<[Point, Immortal], Integer>(
                ([Point, Immortal] pair) => pair.rest.first.hash, increasing),
            comparingOn<[Point, Immortal], Integer>((pair) => pair.rest.first.hash,
                increasing))(one, two);
    }
}
"A tabular report generator for resources, including caches, resource piles, and
 implements (equipment)."
class ResourceTabularReportGenerator()
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
                        => pile.quantity.number.doubleValue()))(first, second);
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
        value values = { for (key->item in fixtures)
            if (is CacheFixture|Implement|ResourcePile resource = item.rest.first)
                [key, [item.first, resource]]}
            .sort(comparingOn(
                ([Integer, [Point, CacheFixture|Implement|ResourcePile]] pair) =>
                    pair.rest.first, comparePairs));
        writeRow(ostream, headerRow.first, *headerRow.rest);
        MutableMap<String, Integer> implementCounts = HashMap<String, Integer>();
        for ([key, [loc, fixture]] in values) {
            if (is Implement fixture) {
                Integer num;
                if (exists temp = implementCounts.get(fixture.kind)) {
                    num = temp;
                } else {
                    num = 0;
                }
                implementCounts.put(fixture.kind, num + 1);
                fixtures.remove(key);
            } else if (produce(ostream, fixtures, fixture, loc)) {
                fixtures.remove(key);
            }
        }
        for (key->count in implementCounts) {
            writeRow(ostream, "equipment", count.string, key);
        }
        fixtures.coalesce();
    }
}
"A tabular report generator for towns."
class TownTabularReportGenerator(Player player, Point hq)
        satisfies ITableGenerator<AbstractTown> {
    "The file-name to (by default) write this table to"
    shared actual String tableName = "towns";
    Comparison([Point, AbstractTown], [Point, AbstractTown]) comparator =
            comparing(
                comparingOn(([Point, AbstractTown] pair) => pair.rest.first,
                    ceylonComparator(TownComparator.compareTownKind)),
                comparingOn(([Point, AbstractTown] pair) => pair.first,
                    ceylonComparator(DistanceComparator(hq))),
                comparingOn(([Point, AbstractTown] pair) => pair.rest.first.size(),
                    ceylonComparator(TownComparator.compareTownSize)),
                comparingOn(([Point, AbstractTown] pair) => pair.rest.first.status(),
                    ceylonComparator(TownComparator.compareTownStatus)),
                comparingOn(([Point, AbstractTown] pair) => pair.rest.first.name,
                    increasing<String>));
    "The header row for this table."
    shared actual [String+] headerRow = ["Distance", "Location", "Owner", "Kind", "Size",
        "Status", "Name"];
    "Produce a table line representing a town."
    shared actual Boolean produce(Anything(String) ostream,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            AbstractTown item, Point loc) {
        writeRow(ostream, distanceString(loc, hq), loc.string,
            ownerString(player, item.owner), item.kind(), item.size().string,
            item.status().string, item.name);
        return true;
    }
    "Compare two location-town pairs."
    shared actual Comparison comparePairs([Point, AbstractTown] one,
            [Point, AbstractTown] two) {
        return comparator(one, two);
    }
}
"A tabular report generator for units."
class UnitTabularReportGenerator(Player player, Point hq)
        satisfies ITableGenerator<IUnit> {
    "The header row for this table."
    shared actual [String+] headerRow =
            ["Distance", "Location", "Owner", "Kind/Category", "Name", "Orders"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "units";
    "Write a table row representing a unit."
    shared actual Boolean produce(Anything(String) ostream,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, IUnit item,
            Point loc) {
        writeRow(ostream, distanceString(loc, hq), loc.string,
            ownerString(player, item.owner), item.kind, item.name,
            item.allOrders.lastEntry().\ivalue.string else "");
        for (member in item) {
            if (is Animal item) {
                // We don't want animals inside a unit showing up in the wild-animal
                // report
                fixtures.remove(item.id);
            } else if (player != item.owner) {
                // A player shouldn't be able to see the details of another player's
                // units.
                fixtures.remove(item.id);
            }
        }
        return true;
    }
    "Compare two location-unit pairs."
    shared actual Comparison comparePairs([Point, IUnit] one, [Point, IUnit] two) {
        return comparing(
            comparingOn(([Point, IUnit] pair) => pair.first,
                ceylonComparator(DistanceComparator(hq))),
            comparingOn(([Point, IUnit] pair) =>
                pair.rest.first.owner, ceylonComparator((Player first, Player second) =>
                    first.compareTo(second))),
            comparingOn(([Point, IUnit] pair) => pair.rest.first.kind,
                increasing<String>),
            comparingOn(([Point, IUnit] pair) => pair.rest.first.name,
                increasing<String>))(one, two);
    }
}
"A tabular report generator for villages."
class VillageTabularReportGenerator(Player player, Point hq)
        satisfies ITableGenerator<Village> {
    "The header of this table."
    shared actual [String+] headerRow = ["Distance", "Location", "Owner", "Name"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "villages";
    shared actual Boolean produce(Anything(String) ostream,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, Village item,
            Point loc) {
        writeRow(ostream, distanceString(loc, hq), loc.string,
            ownerString(player, item.owner), item.name);
        return true;
    }
    "Compare two location-and-village pairs."
    shared actual Comparison comparePairs([Point, Village] one,
            [Point, Village] two) {
        return comparing(
            comparingOn(([Point, Village] pair) => pair.first,
                ceylonComparator(DistanceComparator(hq))),
            comparingOn(([Point, Village] pair) => pair.rest.first.owner,
                ceylonComparator((Player first, Player second) =>
                first.compareTo(second))),
            comparingOn(([Point, Village] pair) => pair.rest.first.name,
                increasing<String>))(one, two);
    }
}