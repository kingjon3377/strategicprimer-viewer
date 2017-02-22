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
    TileFixture,
    HasKind
}
import java.lang {
    JInteger=Integer, JClass=Class,
    IllegalArgumentException
}
import util {
    PatientMap,
    Pair,
    LineEnd
}
import java.util {
    JComparator=Comparator
}
import ceylon.interop.java {
    javaClass,
    CeylonMap
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
    todo
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
    javaComparator,
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
    shared default void produceTable(Anything(String) ostream, PatientMap<JInteger,
            Pair<Point, IFixture>> fixtures) {
        JClass<T> classType = type();
        MutableList<Pair<JInteger, Pair<Point, T>>> temp =
                ArrayList<Pair<JInteger, Pair<Point, T>>>();
        for (entry in fixtures.entrySet()) {
            if (is T fixture = entry.\ivalue.second(), applies(fixture)) {
                temp.add(Pair<JInteger, Pair<Point, T>>
                    .\iof(entry.key, Pair<Point, T>
                    .\iof(entry.\ivalue.first(), fixture)));
            }
        }
        {Pair<JInteger, Pair<Point, T>>*} values = temp
            .sort(comparingOn((Pair<JInteger, Pair<Point, T>> pair) => pair.second(),
            comparePairs));
        if (!headerRow().empty) {
            ostream(headerRow());
            ostream(rowDelimiter);
        }
        for (pair in values) {
            if (produce(ostream, fixtures, pair.second().second(),
                    pair.second().first())) {
                fixtures.remove(pair.first());
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
        PatientMap<JInteger, Pair<Point, IFixture>> fixtures,
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
    todo("Suppot alternate field delimiters")
    shared formal String headerRow(); // TODO: make an attribute
    "Compare two Point-fixture pairs."
    // TODO: take Tuples throughout
    shared formal Comparison comparePairs(Pair<Point, T> one, Pair<Point, T> two);
    """"A String representing the owner of a fixture: "You" if equal to currentPlayer,
       "Independent" if an independent player, or otherwise the player's name."""
    todo("Rename to ownerString()")
    shared default String getOwnerString(Player currentPlayer, Player owner) {
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
    "Write a field to a stream, quoting it if necessary."
    todo("Take multiple fields in one call")
    shared default void writeField(Anything(String) ostream, String field) {
        String quotesQuoted = quotePattern.replace(field, "\"\"");
        if ({"\"", fieldDelimiter.string, rowDelimiter, " "}.any(quotesQuoted.contains)) {
            ostream("\"``quotesQuoted``\"");
        } else {
            ostream(quotesQuoted);
        }
    }
    "Write a field to a stream, quoting it if necessary, and then write the field
     delimiter."
    shared default void writeDelimitedField(Anything(String) ostream, String field) {
        writeField(ostream, field);
        ostream(fieldDelimiter.string);
    }
    "Whether this generator can handle an object. Unlike the original Java implementation,
     the default implementation in Ceylon returns true if the object is within the type
     parameter, not invariably true."
    todo("Is this method necessary since Ceylon has reified generics?")
    shared default Boolean applies(IFixture obj) => obj is T;
    "The type of objects we accept."
    todo("Do we need this in the presence of reified generics?",
        "If so, convert to Ceylon metamodel")
    shared default JClass<T> type() => javaClass<T>();
    "The file-name to (by default) write this table to."
    shared formal String tableName;
}
"A tabular report generator for fortresses."
class FortressTabularReportGenerator(Player player, Point hq)
        satisfies ITableGenerator<Fortress> {
    "The header fields are Distance, Location, Owner, and Name."
    shared actual String headerRow() => "Distance,Location,Owner,Name";
    "The type of objects we accept. Needed so the default ITableGenerator.produce(
     Appendable, PatientMap) can call the typesafe single-row produce() without
     causing class-cast exceptions or taking a type-object as a parameter."
    shared actual JClass<Fortress> type() => javaClass<Fortress>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "fortresses";
    "Whether we can handle the given fixture."
    shared actual Boolean applies(IFixture obj) => obj is Fortress;
    "Write a table row representing the fortress."
    shared actual Boolean produce(Anything(String) ostream,
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures,
            Fortress item, Point loc) {
        writeDelimitedField(ostream, distanceString(loc, hq));
        writeDelimitedField(ostream, loc.string);
        writeDelimitedField(ostream, getOwnerString(player, item.owner));
        writeField(ostream, item.name);
        // Players shouldn't be able to see the contents of others' fortresses
        // in other tables.
        if (player != item.owner) {
            for (member in item) {
                fixtures.remove(JInteger(member.id));
            }
        }
        ostream(rowDelimiter);
        return true;
    }
    "Compare two Point-Fortress pairs."
    shared actual Comparison comparePairs(Pair<Point, Fortress> one,
            Pair<Point, Fortress> two) {
        Comparison(Point, Point) comparator = ceylonComparator(DistanceComparator(hq));
        Fortress first = one.second();
        Fortress second = two.second();
        Comparison cmp = comparator(one.first(), two.first());
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
    shared actual String headerRow() =>
            """Distance,Location,HP,"Max HP",Str,Dex,Con,Int,Wis,Cha""";
    "The type of the objects we accept."
    shared actual JClass<IWorker> type() => javaClass<IWorker>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "workers";
    "Whether we can handle the given fixture."
    shared actual Boolean applies(IFixture obj) => obj is IWorker;
    "Produce a table line representing a worker."
    shared actual Boolean produce(Anything(String) ostream,
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures, IWorker item,
            Point loc) {
        writeDelimitedField(ostream, distanceString(loc, hq));
        writeDelimitedField(ostream, loc.string);
        writeDelimitedField(ostream, item.name);
        if (exists stats = item.stats) {
            writeDelimitedField(ostream, stats.hitPoints.string);
            writeDelimitedField(ostream, stats.maxHitPoints.string);
            for (field in { stats.strength, stats.dexterity, stats.constitution,
                    stats.intelligence, stats.wisdom }) {
                writeDelimitedField(ostream, WorkerStats.getModifierString(field));
            }
            writeField(ostream, WorkerStats.getModifierString(stats.charisma));
        } else {
            for (field in 0..8) {
                writeDelimitedField(ostream, "---");
            }
            writeField(ostream, "---");
        }
        ostream(rowDelimiter);
        return true;
    }
    "Compare two worker-location pairs."
    shared actual Comparison comparePairs(Pair<Point, IWorker> one,
            Pair<Point, IWorker> two) {
        Comparison(Point, Point) comparator = ceylonComparator(DistanceComparator(hq));
        IWorker first = one.second();
        IWorker second = two.second();
        Comparison cmp = comparator(one.first(), two.first());
        if (cmp == equal) {
            return (first.name.compare(second.name));
        } else {
            return cmp;
        }
    }
}
"A tabular report generator for crops---forests, groves, orchards, fields, meadows, and
 shrubs"
todo("Take a union type instead of the too-broad supertype")
class CropTabularReportGenerator(Point hq)
        satisfies ITableGenerator<Forest|Shrub|Meadow|Grove> {
    "The header row for the table."
    shared actual String headerRow() => "Distance,Location,Kind,Cultivation,Status,Crop";
    "Whether we can handle the given fixture."
    shared actual Boolean applies(IFixture obj) => obj is Forest|Shrub|Meadow|Grove;
    "The type of objects we accept."
    shared actual JClass<Forest|Shrub|Meadow|Grove> type() =>
            javaClass<Forest|Shrub|Meadow|Grove>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "crops";
    "Produce the report line for a fixture."
    shared actual Boolean produce(Anything(String) ostream,
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures,
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
        writeDelimitedField(ostream, distanceString(loc, hq));
        writeDelimitedField(ostream, loc.string);
        writeDelimitedField(ostream, kind);
        writeDelimitedField(ostream, cultivation);
        writeDelimitedField(ostream, status);
        writeField(ostream, crop);
        ostream(rowDelimiter);
        return true;
    }
    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs(Pair<Point, Forest|Shrub|Meadow|Grove> one,
            Pair<Point, Forest|Shrub|Meadow|Grove> two) {
        Forest|Shrub|Meadow|Grove first = one.second();
        Forest|Shrub|Meadow|Grove second = two.second();
        Comparison cropCmp = first.kind.compare(second.kind);
        if (cropCmp == equal) {
            Comparison cmp = ceylonComparator(DistanceComparator(hq))(
                one.first(), two.first());
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
    "Whether we can handle the given fixture."
    shared actual Boolean applies(IFixture obj) => obj is MineralFixture;
    "The header row for the table."
    shared actual String headerRow() => "Distance,Location,Kind,Product,Status";
    "The type of objects we accept."
    shared actual JClass<MineralFixture> type() => javaClass<MineralFixture>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "minerals";
    "Produce the report line for a fixture."
    shared actual Boolean produce(Anything(String) ostream,
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures, MineralFixture item,
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
        writeDelimitedField(ostream, distanceString(loc, hq));
        writeDelimitedField(ostream, loc.string);
        writeDelimitedField(ostream, classField);
        writeDelimitedField(ostream, item.kind);
        writeField(ostream, statusField);
        ostream(rowDelimiter);
        return true;
    }
    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs(Pair<Point, MineralFixture> one,
            Pair<Point, MineralFixture> two) {
        return comparing(
            byIncreasing((Pair<Point, MineralFixture> pair) => pair.second().kind),
            (Pair<Point, MineralFixture> first, Pair<Point, MineralFixture> second) =>
                ceylonComparator(DistanceComparator(hq))(first.first(), second.first()),
            byIncreasing((Pair<Point, MineralFixture> pair) => pair.second().hash))
            (one, two);
    }
}
"A report generator for sightings of animals."
class AnimalTabularReportGenerator(Point hq) satisfies ITableGenerator<Animal> {
    "The header row for the table."
    shared actual String headerRow() => "Distance,Location,Kind";
    "Whether we can accept the given object."
    shared actual Boolean applies(IFixture obj) => obj is Animal;
    "The type of objects we accept."
    shared actual JClass<Animal> type() => javaClass<Animal>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "animals";
    "Produce a single line of the tabular report on animals."
    shared actual Boolean produce(Anything(String) ostream,
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures,
            Animal item, Point loc) {
        writeDelimitedField(ostream, distanceString(loc, hq));
        writeDelimitedField(ostream, loc.string);
        if (item.traces) {
            writeField(ostream, "tracks or traces of ``item.kind``");
        } else if (item.talking) {
            writeField(ostream, "talking ``item.kind``");
        } else if ("wild" != item.status) {
            writeField(ostream, "``item.status`` ``item.kind``");
        } else {
            writeField(ostream, item.kind);
        }
        ostream(rowDelimiter);
        return true;
    }
    "Compare two pairs of Animals and locations."
    shared actual Comparison comparePairs(Pair<Point, Animal> one, Pair<Point, Animal> two) {
        Comparison cmp = ceylonComparator(DistanceComparator(hq))(
            one.first(), two.first());
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
                one.second(), two.second());
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
    shared actual String headerRow() =>
            """Distance,Location,"Brief Description","Claimed By","Long Description" """;
    "Whether we can handle the given fixture."
    shared actual Boolean applies(IFixture obj) => obj is ExplorableFixture|TextFixture;
    "The types of objects we accept."
    shared actual JClass<ExplorableFixture|TextFixture> type() =>
            javaClass<ExplorableFixture|TextFixture>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "explorables";
    "Produce a report line about the given fixture."
    shared actual Boolean produce(Anything(String) ostream,
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures,
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
                owner = getOwnerString(player, item.owner);
            }
            longDesc = item.fullDescription;
        }
        else {
            return false;
        }
        writeDelimitedField(ostream, distanceString(loc, hq));
        writeDelimitedField(ostream, loc.string);
        writeDelimitedField(ostream, brief);
        writeDelimitedField(ostream, owner);
        writeField(ostream, longDesc);
        ostream(rowDelimiter);
        return true;
    }
    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs(Pair<Point, ExplorableFixture|TextFixture> one,
            Pair<Point, ExplorableFixture|TextFixture> two) {
        Comparison cmp = ceylonComparator(DistanceComparator(hq))(one.first(),
            two.first());
        if (cmp == equal) {
            return one.second().string.compare(two.second().string);
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
    "Whether we can handle the given object."
    shared actual Boolean applies(IFixture obj) => obj is Immortal;
    "The header row for this table."
    shared actual String headerRow() => "Distance,Location,Immortal";
    "The type of objects we accept."
    shared actual JClass<Immortal> type() => javaClass<Immortal>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "immortals";
    "Produce a table row for the given fixture."
    shared actual Boolean produce(Anything(String) ostream,
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures, Immortal item,
            Point loc) {
        writeDelimitedField(ostream, distanceString(loc, hq));
        writeDelimitedField(ostream, loc.string);
        writeField(ostream, item.string);
        ostream(rowDelimiter);
        return true;
    }
    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs(Pair<Point, Immortal> one,
            Pair<Point, Immortal> two) {
        return comparing(comparingOn(
                (Pair<Point, Immortal> pair) => pair.first(),
                ceylonComparator(DistanceComparator(hq))),
            comparingOn<Pair<Point, Immortal>, Integer>(
                (Pair<Point, Immortal> pair) => pair.second().hash, increasing),
            comparingOn<Pair<Point, Immortal>, Integer>((pair) => pair.second().hash,
                increasing))(one, two);
    }
}
"A tabular report generator for resources, including caches, resource piles, and
 implements (equipment)."
class ResourceTabularReportGenerator()
        satisfies ITableGenerator<Implement|CacheFixture|ResourcePile> {
    "The type of objects we accept."
    shared actual JClass<Implement|CacheFixture|ResourcePile> type() =>
            javaClass<Implement|CacheFixture|ResourcePile>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "resources";
    "Whether we can handle a given object."
    shared actual Boolean applies(IFixture obj) =>
            obj is CacheFixture|ResourcePile|Implement;
    "Write a table row based on the given fixture."
    shared actual Boolean produce(Anything(String) ostream,
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures,
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
        writeDelimitedField(ostream, kind);
        writeDelimitedField(ostream, quantity);
        writeField(ostream, specifics);
        ostream(rowDelimiter);
        return true;
    }
    "The header row for this table."
    shared actual String headerRow() => "Kind,Quantity,Specifics";
    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs(
            Pair<Point, Implement|CacheFixture|ResourcePile> one,
            Pair<Point, Implement|CacheFixture|ResourcePile> two) {
        value first = one.second();
        value second = two.second();
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
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures) {
        value values = { for (key->item in CeylonMap(fixtures))
            if (is CacheFixture|Implement|ResourcePile resource = item.second())
                Pair<Integer, Pair<Point, IFixture>>.\iof(key.intValue(),
                    Pair<Point, CacheFixture|Implement|ResourcePile>.\iof(item.first(),
                    resource))}
            .sort(comparingOn(
                (Pair<Integer, Pair<Point, CacheFixture|Implement|ResourcePile>> pair) =>
                    pair.second(), comparePairs));
        ostream(headerRow());
        ostream(rowDelimiter);
        MutableMap<String, Integer> implementCounts = HashMap<String, Integer>();
        for (pair in values) {
            Pair<Point, Implement|CacheFixture|ResourcePile> inner = pair.second();
            if (is Implement fixture = inner.second()) {
                Integer num;
                if (exists temp = implementCounts.get(fixture.kind)) {
                    num = temp;
                } else {
                    num = 0;
                }
                implementCounts.put(fixture.kind, num + 1);
                fixtures.remove(pair.first());
            } else if (produce(ostream, fixtures, inner.second(), inner.first())) {
                fixtures.remove(pair.first());
            }
        }
        for (key->count in implementCounts) {
            writeDelimitedField(ostream, "equipment");
            writeDelimitedField(ostream, count.string);
            writeField(ostream, key);
            ostream(rowDelimiter);
        }
        fixtures.coalesce();
    }
}
"A tabular report generator for towns."
class TownTabularReportGenerator(Player player, Point hq)
        satisfies ITableGenerator<AbstractTown> {
    "The type of objects we accept."
    shared actual JClass<AbstractTown> type() => javaClass<AbstractTown>();
    "The file-name to (by default) write this table to"
    shared actual String tableName = "towns";
    Comparison(Pair<Point, AbstractTown>, Pair<Point, AbstractTown>) comparator =
            comparing(
                comparingOn((Pair<Point, AbstractTown> pair) => pair.second(),
                    ceylonComparator(TownComparator.compareTownKind)),
                comparingOn((Pair<Point, AbstractTown> pair) => pair.first(),
                    ceylonComparator(DistanceComparator(hq))),
                comparingOn((Pair<Point, AbstractTown> pair) => pair.second().size(),
                    ceylonComparator(TownComparator.compareTownSize)),
                comparingOn((Pair<Point, AbstractTown> pair) => pair.second().status(),
                    ceylonComparator(TownComparator.compareTownStatus)),
                comparingOn((Pair<Point, AbstractTown> pair) => pair.second().name,
                    increasing<String>));
    "The header row for this table."
    shared actual String headerRow() =>
            "Distance,Location,Owner,Kind,Size,Status,Name";
    "Produce a table line representing a town."
    shared actual Boolean produce(Anything(String) ostream,
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures,
            AbstractTown item, Point loc) {
        writeDelimitedField(ostream, distanceString(loc, hq));
        writeDelimitedField(ostream, loc.string);
        writeDelimitedField(ostream, getOwnerString(player, item.owner));
        writeDelimitedField(ostream, item.kind());
        writeDelimitedField(ostream, item.size().string);
        writeDelimitedField(ostream, item.status().string);
        writeField(ostream, item.name);
        ostream(rowDelimiter);
        return true;
    }
    "Compare two location-town pairs."
    shared actual Comparison comparePairs(Pair<Point, AbstractTown> one,
            Pair<Point, AbstractTown> two) {
        return comparator(one, two);
    }
}
"A tabular report generator for units."
class UnitTabularReportGenerator(Player player, Point hq)
        satisfies ITableGenerator<IUnit> {
    "The header row for this table."
    shared actual String headerRow() =>
            "Distance,Location,Owner,Kind/Category,Name,Orders";
    "The type of objects we accept."
    shared actual JClass<IUnit> type() => javaClass<IUnit>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "units";
    "Write a table row representing a unit."
    shared actual Boolean produce(Anything(String) ostream,
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures, IUnit item,
            Point loc) {
        writeDelimitedField(ostream, distanceString(loc, hq));
        writeDelimitedField(ostream, loc.string);
        writeDelimitedField(ostream, getOwnerString(player, item.owner));
        writeDelimitedField(ostream, item.kind);
        writeDelimitedField(ostream, item.name);
        writeField(ostream, item.allOrders.lastEntry().\ivalue.string else "");
        ostream(rowDelimiter);
        for (member in item) {
            if (is Animal item) {
                // We don't want animals inside a unit showing up in the wild-animal
                // report
                fixtures.remove(JInteger(item.id));
            } else if (player != item.owner) {
                // A player shouldn't be able to see the details of another player's
                // units.
                fixtures.remove(JInteger(item.id));
            }
        }
        return true;
    }
    "Compare two location-unit pairs."
    shared actual Comparison comparePairs(Pair<Point, IUnit> one, Pair<Point, IUnit> two) {
        return comparing(
            comparingOn((Pair<Point, IUnit> pair) => pair.first(),
                ceylonComparator(DistanceComparator(hq))),
            comparingOn((Pair<Point, IUnit> pair) =>
                pair.second().owner, ceylonComparator((Player first, Player second) =>
                    first.compareTo(second))),
            comparingOn((Pair<Point, IUnit> pair) => pair.second().kind,
                increasing<String>),
            comparingOn((Pair<Point, IUnit> pair) => pair.second().name,
                increasing<String>))(one, two);
    }
}
"A tabular report generator for villages."
class VillageTabularReportGenerator(Player player, Point hq)
        satisfies ITableGenerator<Village> {
    "The header of this table."
    shared actual String headerRow() => "Distance,Location,Owner,Name";
    "The type of objects we accept."
    shared actual JClass<Village> type() => javaClass<Village>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "villages";
    shared actual Boolean produce(Anything(String) ostream,
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures, Village item,
            Point loc) {
        writeDelimitedField(ostream, distanceString(loc, hq));
        writeDelimitedField(ostream, loc.string);
        writeDelimitedField(ostream, getOwnerString(player, item.owner));
        writeField(ostream, item.name);
        ostream(rowDelimiter);
        return true;
    }
    "Compare two location-and-village pairs."
    shared actual Comparison comparePairs(Pair<Point, Village> one,
            Pair<Point, Village> two) {
        return comparing(
            comparingOn((Pair<Point, Village> pair) => pair.first(),
                ceylonComparator(DistanceComparator(hq))),
            comparingOn((Pair<Point, Village> pair) => pair.second().owner,
                ceylonComparator((Player first, Player second) =>
                first.compareTo(second))),
            comparingOn((Pair<Point, Village> pair) => pair.second().name,
                increasing<String>))(one, two);
    }
}