import strategicprimer.drivers.common {
    SPOptions,
    IDriverModel,
    IDriverUsage,
    DriverUsage,
    ParamCount,
    DriverFactory,
    ModelDriverFactory,
    ModelDriver
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.drivers.exploration.common {
    IExplorationModel,
    ExplorationModel
}

import strategicprimer.model.common.map {
    IMutableMapNG
}

import lovelace.util.common {
    todo
}

"A factory for an app to move independent units around at random."
service(`interface DriverFactory`)
todo("We'd like a GUI for this, perhaps adding customization or limiting the area or something")
shared class RandomMovementFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage(false, "random-move",
        ParamCount.one, "Move independent units at random",
        "Move independent units randomly around the map.", true, false);

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IExplorationModel model) {
            return RandomMovementCLI(cli, options, model);
        } else {
            return createDriver(cli, options, ExplorationModel.copyConstructor(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map) => ExplorationModel(map);
}
