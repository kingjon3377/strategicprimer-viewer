import ceylon.collection {
    ArrayList,
    MutableMap,
    MutableList,
    HashMap
}
import ceylon.file {
    Resource,
    File,
    Writer,
    Nil
}

import java.lang {
    IllegalStateException
}

import strategicprimer.drivers.common {
    SPOptions,
    PlayerChangeListener
}
import strategicprimer.drivers.worker.common {
    IWorkerModel
}
import strategicprimer.model.map {
    Player,
    HasName
}
import strategicprimer.model.map.fixtures {
    UnitMember
}
import strategicprimer.model.map.fixtures.mobile {
    IUnit,
    IWorker
}
import strategicprimer.model.map.fixtures.mobile.worker {
    IJob
}
"A class to write a proto-strategy to file."
class StrategyExporter(IWorkerModel model, SPOptions options)
        satisfies PlayerChangeListener {
    variable Player currentPlayer = model.map.currentPlayer;
    shared actual void playerChanged(Player? old, Player newPlayer) =>
            currentPlayer = newPlayer;
    void writeMember(Writer writer, UnitMember? member) {
        if (is IWorker member) {
            writer.write(member.name);
            {IJob*} jobs = member.filter((job) => !job.emptyJob);
            if (exists first = jobs.first) {
                writer.write(" (``first.name`` ``first.level``");
                for (job in jobs.rest) {
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
                if (exists list = unitsByKind[unit.kind]) {
                    list.add(unit);
                } else {
                    MutableList<IUnit> list = ArrayList<IUnit>();
                    list.add(unit);
                    unitsByKind[unit.kind] = list;
                }
            }
            MutableMap<IUnit, String> orders = HashMap<IUnit, String>();
            for (kind->list in unitsByKind) {
                for (unit in list) {
                    String unitOrders = unit.getLatestOrders(turn);
                    if (unitOrders == unit.getOrders(turn)) {
                        orders[unit] = unitOrders;
                    } else {
                        orders[unit] = "(From turn #``unit
                            .getOrdersTurn(unitOrders)``) ``unitOrders``";
                    }
                }
            }
            writer.writeLine("[``playerName``");
            writer.writeLine("Turn ``turn``]");
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
            writer.writeLine("Workers:");
            writer.writeLine();
            for (kind->list in unitsByKind) {
                writer.writeLine("* ``kind``:");
                for (unit in list) {
                    writer.write("  - ``unit.name``");
                    if (!unit.empty) {
                        writer.write(" [");
                        writeMember(writer, unit.first);
                        for (member in unit.rest) {
                            writer.write(", ");
                            writeMember(writer, member);
                        }
                        writer.write("]");
                    }
                    writer.writeLine(":");
                    writer.writeLine();
                    if (exists unitOrders = orders[unit], !unitOrders.empty) {
                        writer.writeLine(unitOrders);
                    } else {
                        writer.writeLine("TODO");
                    }
                    writer.writeLine();
                }
            }
        }
    }
}
