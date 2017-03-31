import ceylon.collection {
    MutableSet,
    HashSet
}
import ceylon.file {
    Directory,
    parsePath
}

import java.lang {
    IllegalStateException
}

import lovelace.util.common {
    todo
}

import strategicprimer.viewer.model {
    IDriverModel
}

import strategicprimer.viewer.drivers {
    SimpleCLIDriver,
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions,
    ICLIHelper
}

"""A driver to help debug "exploration tables", which were the second "exploration results" framework
   I implemented."""
object tableDebugger satisfies SimpleCLIDriver {
    ExplorationRunner runner = ExplorationRunner();
    if (is Directory directory = parsePath("tables").resource) {
        loadAllTables(directory, runner);
    } else {
        throw IllegalStateException("Table debugger requires a tables directory");
    }
    shared actual IDriverUsage usage = DriverUsage(false, "-T", "--table-debug", ParamCount.none,
        "Debug old-model encounter tables",
        "See whether old-model encounter tables refer to a nonexistent table");
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options, IDriverModel model) {
        log.warn("tableDebugger doesn't need a driver model");
        startDriverNoArgs();
    }
    "Print all possible results from a table."
    void debugSingleTable(
            "The string to print before each result (passed from the calling table)"
            String before,
            "The string to print after each result (passed from the calling table)"
            String after,
            "The table to debug"
            EncounterTable table,
            "The name of that table"
            String tableName,
            "The stream to write to"
            Anything(String) ostream,
            "The set of tables already on the stack, to prevent infinite recursion"
            todo("Use plain {EncounterTable*} instead of a Set?")
            MutableSet<EncounterTable> set) {
        if (set.contains(table)) {
            ostream("table ``tableName`` is already on the stack, skipping ...");
            ostream("The cause was: ``before``#``tableName``#``after``");
            return;
        }
        set.add(table);
        for (item in table.allEvents) {
            if (item.contains("#")) {
                {String+} parsed = item.split('#'.equals, true, false, 3);
                assert (exists callee = parsed.rest.first);
                debugSingleTable("``before````parsed.first``",
                    "``parsed.rest.rest.first else ""````after``",
                    runner.getTable(callee), callee,
                    ostream, set);
            } else {
                ostream("``before````item````after``");
            }
        }
        set.remove(table);
    }
    todo("If a CLIHelper was passed in, write to it")
    shared actual void startDriverNoArgs() {
        runner.verboseGlobalRecursiveCheck((String line) => process.writeLine(line));
        EncounterTable mainTable = runner.getTable("main");
        debugSingleTable("", "", mainTable, "main",
                    (string) => process.writeLine(string), HashSet<EncounterTable>());
    }
}
