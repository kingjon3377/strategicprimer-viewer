import ceylon.file {
    Directory,
    parsePath
}

import strategicprimer.drivers.common {
    UtilityDriver,
    IncorrectUsageException
}

"""A driver to help debug "exploration tables", which were the second "exploration
   results" framework I implemented."""
shared class TableDebugger(Anything(String) ostream) satisfies UtilityDriver {
    "Print all possible results from a table."
    void debugSingleTable(
            "The exploration-runner to use for testing"
            ExplorationRunner runner,
            "The string to print before each result (passed from the calling table)"
            String before,
            "The string to print after each result (passed from the calling table)"
            String after,
            "The table to debug"
            EncounterTable table,
            "The name of that table"
            String tableName,
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
                debugSingleTable(runner, "``before````parsed.first``",
                    "``parsed.rest.rest.first else ""````after``",
                    runner.getTable(callee), callee,
                    innerState);
            } else {
                ostream("``before````item````after``");
            }
        }
    }

    shared actual void startDriver(String* args) {
        if (args.size.positive) {
            throw IncorrectUsageException(TableDebuggerFactory.staticUsage);
        }
        "Table debugger requires a tables directory"
        assert (is Directory directory = parsePath("tables").resource);
        ExplorationRunner runner = ExplorationRunner();
        runner.loadAllTables(directory);
        runner.verboseGlobalRecursiveCheck(ostream);
        EncounterTable mainTable = runner.getTable("main");
        debugSingleTable(runner, "", "", mainTable, "main", []);
    }
}
