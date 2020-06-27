import strategicprimer.drivers.common {
    UtilityDriver,
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions,
    UtilityDriverFactory,
    DriverFactory
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

"A factory for a driver to check every map file in a list for errors."
service(`interface DriverFactory`)
shared class MapCheckerCLIFactory() satisfies UtilityDriverFactory {
    shared actual IDriverUsage usage = DriverUsage(false, "check",
        ParamCount.atLeastOne, "Check map for errors",
        "Check a map file for errors, deprecated syntax, etc.", true, false);

    shared actual UtilityDriver createDriver(ICLIHelper cli, SPOptions options) =>
            MapCheckerCLI(cli.println, cli.println);
}
