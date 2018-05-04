import ceylon.file {
    Directory,
    parsePath
}

import strategicprimer.drivers.common {
    IDriverModel,
    IDriverUsage,
    DriverUsage,
    SPOptions,
    SimpleCLIDriver,
    ParamCount,
	ISPDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import java.lang {
	synchronized
}

"""A driver to help debug "exploration tables", which were the second "exploration
   results" framework I implemented."""
service(`interface ISPDriver`)
shared class TableDebugger() satisfies SimpleCLIDriver {
	ExplorationRunner runner = ExplorationRunner();
	variable Boolean initialized = false;
	synchronized void init() {
		if (initialized) {
			return;
		} else {
			initialized = true;
		}
	    "Table debugger requires a tables directory"
	    assert (is Directory directory = parsePath("tables").resource);
	    loadAllTables(directory, runner);
	}
    shared actual IDriverUsage usage = DriverUsage(false, ["-T", "--table-debug"],
        ParamCount.none, "Debug old-model encounter tables",
        "See whether old-model encounter tables refer to a nonexistent table");
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
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
            {EncounterTable*} set) {
        if (set.contains(table)) {
            ostream("table ``tableName`` is already on the stack, skipping ...");
            ostream("The cause was: ``before``#``tableName``#``after``");
            return;
        }
        {EncounterTable*} innerState = set.follow(table);
        for (item in table.allEvents) {
            if (item.contains("#")) {
                {String+} parsed = item.split('#'.equals, true, false, 3);
                assert (exists callee = parsed.rest.first);
                debugSingleTable("``before````parsed.first``",
                    "``parsed.rest.rest.first else ""````after``",
                    runner.getTable(callee), callee,
                    ostream, innerState);
            } else {
                ostream("``before````item````after``");
            }
        }
    }
    shared actual void startDriverNoArgs(ICLIHelper cli, SPOptions options) {
        init();
        runner.verboseGlobalRecursiveCheck((String line) => cli.println(line)); // TODO: lambda could be method reference, I think
        EncounterTable mainTable = runner.getTable("main");
        debugSingleTable("", "", mainTable, "main",
                    (string) => cli.println(string), []); // TODO: lambda could be method reference, I think
    }
}
