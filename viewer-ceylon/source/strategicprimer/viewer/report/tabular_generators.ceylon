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
    DistanceComparator
}
import java.lang {
    JAppendable=Appendable, JInteger=Integer, JClass=Class
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
}