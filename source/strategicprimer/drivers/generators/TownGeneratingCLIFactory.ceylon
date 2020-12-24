import strategicprimer.drivers.common {
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions,
    IDriverModel,
    DriverFactory,
    ModelDriverFactory,
    ModelDriver,
    SimpleMultiMapModel
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.model.common.map {
    IMutableMapNG
}

"A factory for a driver to let the user enter or generate 'stats' for towns."
service(`interface DriverFactory`)
shared class TownGeneratingCLIFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocation = "generate-towns";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Enter or generate stats and contents for towns and villages";
        longDescription = "Enter or generate stats and contents for towns and villages";
        includeInCLIList = true;
        includeInGUIList = false;
    };

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) => TownGeneratingCLI(cli, model);

    shared actual IDriverModel createModel(IMutableMapNG map) => SimpleMultiMapModel(map);
}
