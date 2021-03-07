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

"A factory for a driver to check every map file in a list for errors and report the
 results in a window."
service(`interface DriverFactory`)
shared class MapCheckerGUIFactory() satisfies UtilityDriverFactory {
    shared actual IDriverUsage usage = DriverUsage(true, "check",
        ParamCount.anyNumber, "Check map for errors",
        "Check a map file for errors, deprecated syntax, etc.", false, true);

    shared actual UtilityDriver createDriver(ICLIHelper cli, SPOptions options) =>
            MapCheckerGUI();
}
