import strategicprimer.drivers.common {
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions,
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

"A factory for a driver to let the user generate animal and shrub populations, meadow and
 grove sizes, and forest acreages."
service(`interface DriverFactory`)
shared class PopulationGeneratingCLIFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocation = "generate-populations";
        paramsWanted = ParamCount.one;
        shortDescription = "Generate animal populations, etc.";
        longDescription = "Generate animal and shrub populations, meadow and grove sizes, and forest acreages.";
        includeInCLIList = true;
        includeInGUIList = false; // TODO: We'd like a GUI equivalent
    };

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is PopulationGeneratingModel model) {
            return PopulationGeneratingCLI(cli, model);
        } else {
            return createDriver(cli, options, PopulationGeneratingModel.copyConstructor(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map) => PopulationGeneratingModel(map);
}
