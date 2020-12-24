import strategicprimer.drivers.common {
    DriverFactory,
    DriverUsage,
    IDriverModel,
    IDriverUsage,
    ModelDriver,
    ModelDriverFactory,
    ParamCount,
    SPOptions
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.model.common.map {
    IMutableMapNG
}

"A factory for a driver to fix ID mismatches between forests and Ground in the main and
 player maps."
service(`interface DriverFactory`)
shared class ForestFixerFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage(false, "fix-forests",
        ParamCount.atLeastTwo, "Fix forest IDs",
        "Make sure that forest IDs in submaps match the main map", false, false);

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is UtilityDriverModel model) {
            return ForestFixerDriver(cli, options, model);
        } else {
            return createDriver(cli, options, UtilityDriverModel.copyConstructor(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map) => UtilityDriverModel(map);
}

