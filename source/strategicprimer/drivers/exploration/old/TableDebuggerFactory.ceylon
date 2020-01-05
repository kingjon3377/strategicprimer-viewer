import strategicprimer.drivers.common {
    IDriverUsage,
    DriverUsage,
    SPOptions,
    ParamCount,
    UtilityDriver,
    DriverFactory,
    UtilityDriverFactory
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

"""A factory for a driver to help debug "exploration tables", which were the second
   "exploration results" framework I implemented."""
service(`interface DriverFactory`)
shared class TableDebuggerFactory satisfies UtilityDriverFactory {
    shared static IDriverUsage staticUsage = DriverUsage(false, ["table-debug"],
        ParamCount.none, "Debug old-model encounter tables",
        "See whether old-model encounter tables refer to a nonexistent table", false,
        false);

    shared new () {}
    shared actual IDriverUsage usage => staticUsage;
    shared actual UtilityDriver createDriver(ICLIHelper cli, SPOptions options) =>
            TableDebugger(cli.println);
}
