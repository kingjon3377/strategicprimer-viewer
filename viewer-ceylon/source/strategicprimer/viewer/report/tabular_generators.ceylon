import controller.map.report.tabular {
    ITableGenerator
}
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
    JAppendable=Appendable, JInteger=Integer, JClass=Class,
    IllegalArgumentException
}
import util {
    PatientMap,
    Pair
}
import java.util {
    JComparator=Comparator
}
import ceylon.interop.java {
    javaClass,
    CeylonMap,
    javaString
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
    HashMap
}
import controller.map.misc {
    TownComparator
}
"A tabular report generator for fortresses."
class FortressTabularReportGenerator(Player player, Point hq)
        satisfies ITableGenerator<Fortress> {
    "Write a table row representing the fortress."
    shared actual Boolean produce(JAppendable ostream,
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
        ostream.append(rowDelimiter);
        return true;
    }
    "The header fields are Distance, Location, Owner, and Name."
    shared actual String headerRow() => "Distance,Location,Owner,Name";
    "Compare two Point-Fortress pairs."
    shared actual Integer comparePairs(Pair<Point, Fortress> one,
            Pair<Point, Fortress> two) {
        JComparator<Point> comparator = DistanceComparator(hq);
        Fortress first = one.second();
        Fortress second = two.second();
        Integer cmp = comparator.compare(one.first(), two.first());
        if (player == first.owner, player != second.owner) {
            return -1;
        } else if (player != first.owner, player == second.owner) {
            return 1;
        } else if (cmp == 0) {
            Comparison nameCmp = first.name.compare(second.name);
            if ("HQ" == first.name, "HQ" != second.name) {
                return -1;
            } else if ("HQ" != first.name, "HQ" == second.name) {
                return 1;
            } else if (nameCmp == equal) {
                return first.owner.compareTo(second.owner);
            } else if (nameCmp == larger) {
                return 1;
            } else {
                return -1;
            }
        } else {
            return cmp;
        }
    }
    "The type of objects we accept. Needed so the default ITableGenerator.produce(
     Appendable, PatientMap) can call the typesafe single-row produce() without
     causing class-cast exceptions or taking a type-object as a parameter."
    shared actual JClass<Fortress> type() => javaClass<Fortress>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "fortresses";
    "Whether we can handle the given fixture."
    shared actual Boolean applies(IFixture obj) => obj is Fortress;
}
"A report generator for workers. We do not cover Jobs or Skills; see the main report for
 that."
class WorkerTabularReportGenerator(Point hq) satisfies ITableGenerator<IWorker> {
    "Produce a table line representing a worker."
    shared actual Boolean produce(JAppendable ostream,
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
        ostream.append(rowDelimiter);
        return true;
    }
    "The header row of the table."
    shared actual String headerRow() =>
            """Distance,Location,HP,"Max HP",Str,Dex,Con,Int,Wis,Cha""";
    "Compare two worker-location pairs."
    shared actual Integer comparePairs(Pair<Point, IWorker> one,
            Pair<Point, IWorker> two) {
        JComparator<Point> comparator = DistanceComparator(hq);
        IWorker first = one.second();
        IWorker second = two.second();
        Integer cmp = comparator.compare(one.first(), two.first());
        if (cmp == 0) {
            switch (first.name.compare(second.name))
            case (equal) { return 0; }
            case (larger) { return 1; }
            case (smaller) { return -1; }
        } else {
            return cmp;
        }
    }
    "The type of the objects we accept."
    shared actual JClass<IWorker> type() => javaClass<IWorker>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "workers";
    "Whether we can handle the given fixture."
    shared actual Boolean applies(IFixture obj) => obj is IWorker;
}
"A tabular report generator for crops---forests, groves, orchards, fields, meadows, and
 shrubs"
todo("Take a union type instead of the too-broad supertype")
class CropTabularReportGenerator(Point hq) satisfies ITableGenerator<TileFixture> {
    "Whether we can handle the given fixture."
    shared actual Boolean applies(IFixture obj) => obj is Forest|Shrub|Meadow|Grove;
    "Produce the report line for a fixture."
    shared actual Boolean produce(JAppendable ostream,
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures, TileFixture item,
            Point loc) {
        String kind;
        String cultivation;
        String status;
        String crop; // TODO: Once we use a union type, assign this here
        if (is Forest item) {
            kind = (item.rows) then "rows" else "forest";
            cultivation = "---";
            status = "---";
            crop = item.kind;
        } else if (is Shrub item) {
            kind = "shrub";
            cultivation = "---";
            status = "---";
            crop = item.kind;
        } else if (is Meadow item) {
            kind = (item.field) then "field" else "meadow";
            cultivation = (item.cultivated) then "cultivated" else "wild";
            status = item.status.string;
            crop = item.kind;
        } else if (is Grove item) {
            kind = (item.orchard) then "orchard" else "grove";
            cultivation = (item.cultivated) then "cultivated" else "wild";
            status = "---";
            crop = item.kind;
        } else {
            return false;
        }
        writeDelimitedField(ostream, distanceString(loc, hq));
        writeDelimitedField(ostream, loc.string);
        writeDelimitedField(ostream, kind);
        writeDelimitedField(ostream, cultivation);
        writeDelimitedField(ostream, status);
        writeField(ostream, crop);
        ostream.append(rowDelimiter);
        return true;
    }
    "The header row for the table."
    shared actual String headerRow() => "Distance,Location,Kind,Cultivation,Status,Crop";
    "Compare two Point-fixture pairs."
    shared actual Integer comparePairs(Pair<Point, TileFixture> one,
            Pair<Point, TileFixture> two) {
        TileFixture first = one.second();
        TileFixture second = two.second();
        if (!applies(first) || !applies(second)) { // TODO: omit once union type parameter
            throw IllegalArgumentException("Unhandleable argument");
        }
        assert (is HasKind first, is HasKind second);
        Comparison cropCmp = first.kind.compare(second.kind);
        switch (cropCmp)
        case (equal) {
            Integer cmp = DistanceComparator(hq).compare(one.first(), two.first());
            if (cmp == 0) {
                return javaComparator(comparing(byIncreasing<TileFixture, Integer>(
                        (fix) => typeOf(fix).hash), byIncreasing(TileFixture.hash)))
                    .compare(first, second);
            } else {
                return cmp;
            }
        }
        case (larger) { return 1; }
        case (smaller) { return -1; }
    }
    "The type of objects we accept."
    shared actual JClass<TileFixture> type() => javaClass<TileFixture>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "crops";
}
"A tabular report generator for resources that can be mined---mines, mineral veins, stone
 deposits, and Ground."
class DiggableTabularReportGenerator(Point hq) satisfies ITableGenerator<MineralFixture> {
    "Whether we can handle the given fixture."
    shared actual Boolean applies(IFixture obj) => obj is MineralFixture;
    "Produce the report line for a fixture."
    shared actual Boolean produce(JAppendable ostream,
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
        ostream.append(rowDelimiter);
        return true;
    }
    "The header row for the table."
    shared actual String headerRow() => "Distance,Location,Kind,Product,Status";
    "Compare two Point-fixture pairs."
    shared actual Integer comparePairs(Pair<Point, MineralFixture> one,
            Pair<Point, MineralFixture> two) {
        JComparator<Point> distComparator = DistanceComparator(hq);
        Comparison distCompare(Pair<Point, MineralFixture> first,
                Pair<Point, MineralFixture> second) {
            Integer temp = distComparator.compare(first.first(), second.first());
            if (temp < 0) {
                return smaller;
            } else if (temp == 0) {
                return equal;
            } else {
                return larger;
            }
        }
        String kindExtractor(Pair<Point, MineralFixture> pair) {
            return pair.second().kind;
        }
        Integer hashExtractor(Pair<Point, MineralFixture> pair) {
            return pair.second().hash;
        }
        return javaComparator(comparing(byIncreasing(kindExtractor), distCompare,
            byIncreasing(hashExtractor))).compare(one, two);
    }
    "The type of objects we accept."
    shared actual JClass<MineralFixture> type() => javaClass<MineralFixture>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "minerals";
}
"A report generator for sightings of animals."
class AnimalTabularReportGenerator(Point hq) satisfies ITableGenerator<Animal> {
    "Produce a single line of the tabular report on animals."
    shared actual Boolean produce(JAppendable ostream,
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
        ostream.append(rowDelimiter);
        return true;
    }
    "The header row for the table."
    shared actual String headerRow() => "Distance,Location,Kind";
    "Whether we can accept the given object."
    shared actual Boolean applies(IFixture obj) => obj is Animal;
    "Compare two pairs of Animals and locations."
    shared actual Integer comparePairs(Pair<Point, Animal> one, Pair<Point, Animal> two) {
        Integer cmp = DistanceComparator(hq).compare(one.first(), two.first());
        if (cmp == 0) {
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
            return javaComparator(comparing(compareBools(Animal.talking),
                    compareBools((animal) => !animal.traces), byIncreasing(Animal.kind)))
                .compare(one.second(), two.second());
        } else {
            return cmp;
        }
    }
    "The type of objects we accept."
    shared actual JClass<Animal> type() => javaClass<Animal>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "animals";
}
"A tabular report generator for things that can be explored and are not covered elsewhere:
  caves, battlefields, adventure hooks, and portals."
class ExplorableTabularReportGenerator(Player player, Point hq)
        satisfies ITableGenerator<ExplorableFixture|TextFixture> {
    "Produce a report line about the given fixture."
    shared actual Boolean produce(JAppendable ostream,
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
        ostream.append(rowDelimiter);
        return true;
    }
    "The header row for the table."
    shared actual String headerRow() =>
            """Distance,Location,"Brief Description","Claimed By","Long Description" """;
    "Compare two Point-fixture pairs."
    shared actual Integer comparePairs(Pair<Point, ExplorableFixture|TextFixture> one,
            Pair<Point, ExplorableFixture|TextFixture> two) {
        Integer cmp = DistanceComparator(hq).compare(one.first(), two.first());
        if (cmp == 0) {
            switch (one.second().string.compare(two.second().string))
            case (equal) { return 0; }
            case (smaller) { return -1; }
            case (larger) { return 1; }
        } else {
            return cmp;
        }
    }
    "Whether we can handle the given fixture."
    shared actual Boolean applies(IFixture obj) => obj is ExplorableFixture|TextFixture;
    "The types of objects we accept."
    shared actual JClass<ExplorableFixture|TextFixture> type() =>
            javaClass<ExplorableFixture|TextFixture>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "explorables";
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
    "Produce a table row for the given fixture."
    shared actual Boolean produce(JAppendable ostream,
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures, Immortal item,
            Point loc) {
        writeDelimitedField(ostream, distanceString(loc, hq));
        writeDelimitedField(ostream, loc.string);
        writeField(ostream, item.string);
        ostream.append(rowDelimiter);
        return true;
    }
    "The header row for this table."
    shared actual String headerRow() => "Distance,Location,Immortal";
    "Compare two Point-fixture pairs."
    shared actual Integer comparePairs(Pair<Point, Immortal> one,
            Pair<Point, Immortal> two) {
        return javaComparator(comparing(comparingOn(
                (Pair<Point, Immortal> pair) => pair.first(),
                ceylonComparator(DistanceComparator(hq))),
            comparingOn<Pair<Point, Immortal>, Integer>(
                (Pair<Point, Immortal> pair) => pair.second().hash, increasing),
            comparingOn<Pair<Point, Immortal>, Integer>((pair) => pair.second().hash,
                increasing)))
            .compare(one, two);
    }
    "The type of objects we accept."
    shared actual JClass<Immortal> type() => javaClass<Immortal>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "immortals";
}
"A tabular report generator for resources, including caches, resource piles, and
 implements (equipment)."
class ResourceTabularReportGenerator()
        satisfies ITableGenerator<Implement|CacheFixture|ResourcePile> {
    "Whether we can handle a given object."
    shared actual Boolean applies(IFixture obj) =>
            obj is CacheFixture|ResourcePile|Implement;
    "Write a table row based on the given fixture."
    shared actual Boolean produce(JAppendable ostream,
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
        ostream.append(rowDelimiter);
        return true;
    }
    "The header row for this table."
    shared actual String headerRow() => "Kind,Quantity,Specifics";
    "Compare two Point-fixture pairs."
    shared actual Integer comparePairs(
            Pair<Point, Implement|CacheFixture|ResourcePile> one,
            Pair<Point, Implement|CacheFixture|ResourcePile> two) {
        value first = one.second();
        value second = two.second();
        switch (first)
        case (is ResourcePile) {
            if (is ResourcePile second) {
                return javaComparator(comparing(byIncreasing(ResourcePile.kind),
                    byIncreasing(ResourcePile.contents),
                    byDecreasing((ResourcePile pile)
                        => pile.quantity.number.doubleValue()))).compare(first, second);
            } else {
                return -1;
            }
        }
        case (is Implement) {
            if (is Implement second) {
                return javaComparator(byIncreasing(Implement.kind))
                    .compare(first, second);
            } else if (is ResourcePile second) {
                return 1;
            } else {
                return -1;
            }
        }
        case (is CacheFixture) {
            if (is CacheFixture second) {
                return javaComparator(comparing(byIncreasing(CacheFixture.kind),
                    byIncreasing(CacheFixture.contents))).compare(first, second);
            } else {
                return 1;
            }
        }
    }
    "Write rows for equipment, counting multiple identical Implements in one line."
    shared actual void produce(JAppendable ostream,
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures) {
        value values = { for (key->item in CeylonMap(fixtures))
            if (is CacheFixture|Implement|ResourcePile resource = item.second())
                Pair<Integer, Pair<Point, IFixture>>.\iof(key.intValue(),
                    Pair<Point, CacheFixture|Implement|ResourcePile>.\iof(item.first(),
                    resource))}
            .sort(comparingOn(
                (Pair<Integer, Pair<Point, CacheFixture|Implement|ResourcePile>> pair) =>
                    pair.second(), ceylonComparator(comparePairs)));
        ostream.append(headerRow());
        ostream.append(rowDelimiter);
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
            ostream.append(rowDelimiter);
        }
        fixtures.coalesce();
    }
    "The type of objects we accept."
    shared actual JClass<Implement|CacheFixture|ResourcePile> type() =>
            javaClass<Implement|CacheFixture|ResourcePile>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "resources";
}
"A tabular report generator for towns."
class TownTabularReportGenerator(Player player, Point hq)
        satisfies ITableGenerator<AbstractTown> {
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
    "Produce a table line representing a town."
    shared actual Boolean produce(JAppendable ostream,
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures,
            AbstractTown item, Point loc) {
        writeDelimitedField(ostream, distanceString(loc, hq));
        writeDelimitedField(ostream, loc.string);
        writeDelimitedField(ostream, getOwnerString(player, item.owner));
        writeDelimitedField(ostream, item.kind());
        writeDelimitedField(ostream, item.size().string);
        writeDelimitedField(ostream, item.status().string);
        writeField(ostream, item.name);
        ostream.append(rowDelimiter);
        return true;
    }
    "The header row for this table."
    shared actual String headerRow() =>
            "Distance,Location,Owner,Kind,Size,Status,Name";
    "Compare two location-town pairs."
    shared actual Integer comparePairs(Pair<Point, AbstractTown> one,
            Pair<Point, AbstractTown> two) {
        return javaComparator(comparator).compare(one, two);
    }
    "The type of objects we accept."
    shared actual JClass<AbstractTown> type() => javaClass<AbstractTown>();
    "The file-name to (by default) write this table to"
    shared actual String tableName = "towns";
}
"A tabular report generator for units."
class UnitTabularReportGenerator(Player player, Point hq)
        satisfies ITableGenerator<IUnit> {
    "Write a table row representing a unit."
    shared actual Boolean produce(JAppendable ostream,
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures, IUnit item,
            Point loc) {
        writeDelimitedField(ostream, distanceString(loc, hq));
        writeDelimitedField(ostream, loc.string);
        writeDelimitedField(ostream, getOwnerString(player, item.owner));
        writeDelimitedField(ostream, item.kind);
        writeDelimitedField(ostream, item.name);
        writeField(ostream, item.allOrders.lastEntry().\ivalue.string else "");
        ostream.append(rowDelimiter);
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
    "The header row for this table."
    shared actual String headerRow() =>
            "Distance,Location,Owner,Kind/Category,Name,Orders";
    "Compare two location-unit pairs."
    shared actual Integer comparePairs(Pair<Point, IUnit> one, Pair<Point, IUnit> two) {
        return javaComparator(comparing(
            comparingOn((Pair<Point, IUnit> pair) => pair.first(),
                ceylonComparator(DistanceComparator(hq))),
            comparingOn((Pair<Point, IUnit> pair) =>
                pair.second().owner, ceylonComparator((Player first, Player second) =>
                    first.compareTo(second))),
            comparingOn((Pair<Point, IUnit> pair) => pair.second().kind, increasing<String>),
            comparingOn((Pair<Point, IUnit> pair) => pair.second().name, increasing<String>)))
            .compare(one, two);
    }
    "The type of objects we accept."
    shared actual JClass<IUnit> type() => javaClass<IUnit>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "units";
}
"A tabular report generator for villages."
class VillageTabularReportGenerator(Player player, Point hq)
        satisfies ITableGenerator<Village> {
    shared actual Boolean produce(JAppendable ostream,
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures, Village item,
            Point loc) {
        writeDelimitedField(ostream, distanceString(loc, hq));
        writeDelimitedField(ostream, loc.string);
        writeDelimitedField(ostream, getOwnerString(player, item.owner));
        writeField(ostream, item.name);
        ostream.append(rowDelimiter);
        return true;
    }
    "The header of this table."
    shared actual String headerRow() => "Distance,Location,Owner,Name";
    "Compare two location-and-village pairs."
    shared actual Integer comparePairs(Pair<Point, Village> one, Pair<Point, Village> two) {
        return javaComparator(comparing(
            comparingOn((Pair<Point, Village> pair) => pair.first(),
                ceylonComparator(DistanceComparator(hq))),
            comparingOn((Pair<Point, Village> pair) => pair.second().owner,
                ceylonComparator((Player first, Player second) =>
                first.compareTo(second))),
            comparingOn((Pair<Point, Village> pair) => pair.second().name,
                increasing<String>))).compare(one, two);
    }
    "The type of objects we accept."
    shared actual JClass<Village> type() => javaClass<Village>();
    "The file-name to (by default) write this table to."
    shared actual String tableName = "villages";
}