import ceylon.logging {
    logger,
    Logger
}

import strategicprimer.drivers.common {
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions,
    SimpleMultiMapModel,
    IMultiMapModel,
    IDriverModel,
    DriverFactory,
    ModelDriverFactory,
    ModelDriver
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.model.common.map {
    IMutableMapNG
}

"A logger."
Logger log = logger(`module strategicprimer.drivers.utility`);

"A factory for a driver to check whether player maps are subsets of the main map."
service(`interface DriverFactory`)
shared class SubsetCLIFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage(false, "subset",
        ParamCount.atLeastTwo, "Check players' maps against master",
        "Check that subordinate maps are subsets of the main map, containing nothing that
         it does not contain in the same place.", true, false);

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IMultiMapModel model) {
            return SubsetCLI(cli, model);
        } else {
            log.warn("Subset checking does nothing with no subordinate maps");
            return createDriver(cli, options, SimpleMultiMapModel.copyConstructor(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map) => SimpleMultiMapModel(map);
}
