import controller.map.report.tabular {
    ITableGenerator
}
import model.map.fixtures.towns {
    Fortress
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
    javaClass
}
import model.map.fixtures.mobile {
    IWorker
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
    MineralVein
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
    Ground
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
        Comparison distCompare(Pair<Point, MineralFixture> first, Pair<Point, MineralFixture> second) {
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