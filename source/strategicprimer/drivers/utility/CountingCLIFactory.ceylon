import strategicprimer.drivers.common {
    DriverFactory,
    ModelDriverFactory,
    DriverUsage,
    IDriverUsage,
    ParamCount,
    ModelDriver,
    SPOptions,
    IDriverModel,
    SimpleDriverModel
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.model.common.map {
    IMutableMapNG
}

import lovelace.util.common {
    PathWrapper
}

"A factory for an app to report statistics on the contents of the map."
service(`interface DriverFactory`)
shared class CountingCLIFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocation = "count";
        paramsWanted = ParamCount.one;
        shortDescription = "Calculate statistics of map contents";
        longDescription = "Print statistical report of map contents.";
        includeInCLIList = false;
        includeInGUIList = false;
    };

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) => CountingCLI(cli, model);

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            SimpleDriverModel(map, path);
}
