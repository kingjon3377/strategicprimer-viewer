import strategicprimer.drivers.common {
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions,
    UtilityDriver,
    DriverFactory,
    UtilityDriverFactory
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

"A factory for a driver to check whether player maps are subsets of the main map and
 display the results graphically."
service(`interface DriverFactory`)
shared class SubsetGUIFactory satisfies UtilityDriverFactory {
    shared static IDriverUsage staticUsage = DriverUsage(true, ["subset"],
        ParamCount.atLeastOne, "Check players' maps against master",
        "Check that subordinate maps are subsets of the main map, containing nothing that
         it does not contain in the same place.", false, true);

    shared actual IDriverUsage usage => staticUsage;

    shared new () {}

    shared actual UtilityDriver createDriver(ICLIHelper cli, SPOptions options) =>
            SubsetGUI(cli, options);
}
