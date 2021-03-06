import strategicprimer.model.common.map {
    IMutableMapNG
}

import strategicprimer.drivers.common {
    IDriverModel,
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions,
    DriverFactory,
    ModelDriverFactory,
    ModelDriver
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

"A factory for a driver to generate new workers."
service(`interface DriverFactory`)
shared class StatGeneratingCLIFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocation = "generate-stats";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Generate new workers.";
        longDescription = "Generate new workers with random stats and experience.";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptions = [ "--current-turn=NN" ];
    };

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is PopulationGeneratingModel model) {
            return StatGeneratingCLI(cli, model);
        } else {
            return createDriver(cli, options, PopulationGeneratingModel.copyConstructor(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map) => PopulationGeneratingModel(map);
}
