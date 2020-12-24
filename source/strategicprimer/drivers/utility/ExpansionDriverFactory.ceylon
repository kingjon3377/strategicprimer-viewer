import strategicprimer.model.common.map {
    IMutableMapNG
}

import strategicprimer.drivers.common {
    IDriverModel,
    SPOptions,
    ParamCount,
    IDriverUsage,
    DriverUsage,
    DriverFactory,
    ModelDriverFactory,
    ModelDriver
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.drivers.utility {
    UtilityDriverModel
}


"""A factory for a driver to update a player's map to include a certain minimum distance
   around allied villages."""
service(`interface DriverFactory`)
shared class ExpansionDriverFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocation = "expand";
        paramsWanted = ParamCount.atLeastTwo;
        shortDescription = "Expand a player's map.";
        longDescription = "Ensure a player's map covers all terrain allied villages can
                           see.";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptions = [ "--current-turn=NN" ];
    };

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is UtilityDriverModel model) {
            return ExpansionDriver(cli, options, model);
        } else {
            return createDriver(cli, options, UtilityDriverModel.copyConstructor(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map) => UtilityDriverModel(map);
}

