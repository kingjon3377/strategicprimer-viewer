import strategicprimer.viewer.drivers {
    SPOptions
}
import model.map.fixtures.mobile.worker {
    IJob
}
import model.map {
    Player,
    HasName
}
import java.lang {
    IllegalStateException
}
import model.map.fixtures {
    UnitMember
}
import ceylon.collection {
    ArrayList,
    MutableMap,
    MutableList,
    HashMap
}
import ceylon.interop.java {
    CeylonIterable
}
import model.listeners {
    PlayerChangeListener
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    IUnit
}
import model.map.fixtures.mobile {
    IWorker
}
import ceylon.file {
    Resource,
    File,
    Writer,
    Nil
}
"A class to write a proto-strategy to file."
class StrategyExporter(IWorkerModel model, SPOptions options) satisfies PlayerChangeListener {
    variable Player currentPlayer = model.map.currentPlayer;
    shared actual void playerChanged(Player? old, Player newPlayer) =>
            currentPlayer = newPlayer;
    void writeMember(Writer writer, UnitMember? member) {
        if (is IWorker member) {
            writer.write(member.name);
            Iterable<IJob> iter = CeylonIterable(member);
            if (exists first = iter.first) {
                writer.write(" (``first.name`` ``first.level``");
                for (job in iter.rest) {
                    writer.write(", ``job.name`` ``job.level``");
                }
                writer.write(")");
            }
        } else if (exists member) {
            writer.write(member.string);
        }
    }
    shared void writeStrategy(Resource path, {UnitMember*} dismissed) {
        File file;
        if (is Nil path) {
            file = path.createFile();
        } else if (is File path) {
            file = path;
        } else {
            throw IllegalStateException("Can't write to a directory or link");
        }
        try (writer = file.Overwriter()) {
            String playerName = currentPlayer.name;
            Integer turn = model.map.currentTurn;
            {IUnit*} units = model.getUnits(currentPlayer);
            MutableMap<String, MutableList<IUnit>> unitsByKind =
                    HashMap<String, MutableList<IUnit>>();
            for (unit in units) {
                if (unit.empty, "false" == options.getArgument("--print-empty")) {
                    continue;
                }
                if (exists list = unitsByKind.get(unit.kind)) {
                    list.add(unit);
                } else {
                    MutableList<IUnit> list = ArrayList<IUnit>();
                    list.add(unit);
                    unitsByKind.put(unit.kind, list);
                }
            }
            MutableMap<IUnit, String> orders = HashMap<IUnit, String>();
            for (kind->list in unitsByKind) {
                for (unit in list) {
                    String unitOrders = unit.getLatestOrders(turn);
                    if (unitOrders == unit.getOrders(turn)) {
                        orders.put(unit, unitOrders);
                    } else {
                        orders.put(unit, "(From turn #``unit
                            .getOrdersTurn(unitOrders)``) ``unitOrders``");
                    }
                }
            }
            writer.writeLine("[``playerName``");
            writer.writeLine("Turn ``turn``]");
            writer.writeLine();
            writer.writeLine();
            writer.writeLine("Inventions: TODO: any?");
            writer.writeLine();
            if (!dismissed.empty) {
                writer.write("Dismissed workers etc.: ``dismissed
                    .first else ""``");
                for (member in dismissed.rest) {
                    writer.write(", ");
                    if (is HasName member) {
                        writer.write(member.name);
                    } else {
                        writer.write(member.string);
                    }
                }
                writer.writeLine();
                writer.writeLine();
            }
            writer.write("Workers:");
            for (kind->list in unitsByKind) {
                writer.writeLine("* ``kind``:");
                for (unit in list) {
                    // TODO: inline
                    Iterable<UnitMember> iter = unit;
                    writer.write("  - ``unit.name``");
                    if (!iter.empty) {
                        writer.write(" [");
                        writeMember(writer, iter.first);
                        for (member in iter.rest) {
                            writer.write(", ");
                            writeMember(writer, member);
                        }
                        writer.write("]");
                    }
                    writer.writeLine(":");
                    writer.writeLine();
                    if (exists unitOrders = orders.get(unit), !unitOrders.empty) {
                        writer.writeLine(unitOrders);
                    } else {
                        writer.writeLine("TODO");
                    }
                    writer.writeLine();
                    writer.writeLine();
                }
            }
        }
    }
}
