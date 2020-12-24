import strategicprimer.model.common.map {
    IMutableMapNG
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.drivers.common {
    SPOptions,
    DriverUsage,
    IDriverUsage,
    IDriverModel,
    ParamCount,
    DriverFactory,
    ModelDriverFactory,
    ModelDriver
}

"A factory for a driver to remove duplicate hills, forests, etc., from the map (to reduce
 the disk space it takes up and the memory and CPU required to deal with it)."
service(`interface DriverFactory`)
shared class DuplicateFixtureRemoverFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocation = "remove-duplicates";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Remove duplicate fixtures";
        longDescription = "Remove duplicate fixtures (identical except ID# and on the
                           same tile) from a map.";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptions = [ "--current-turn=NN" ];
    };

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is UtilityDriverModel model) {
            return DuplicateFixtureRemoverCLI(cli, model);
        } else {
            return createDriver(cli, options, UtilityDriverModel.copyConstructor(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map) => UtilityDriverModel(map);
}
