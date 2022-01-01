import strategicprimer.model.common.map {
    IMutableMapNG
}

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

"""A factory for a driver to add some kind of fixture to suitable tiles throughout the
   map."""
service(`interface DriverFactory`)
shared class MapPopulatorFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocation = "populate-map";
        paramsWanted = ParamCount.one;
        shortDescription = "Add missing fixtures to a map";
        longDescription = "Add specified kinds of fixtures to suitable points throughout
                           a map";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptions = [ "--current-turn=NN" ];
    };

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IPopulatorDriverModel model) {
            return MapPopulatorDriver(cli, options, model);
        } else {
            return createDriver(cli, options, PopulatorDriverModel.copyConstructor(model));
        }
    }

    shared actual IPopulatorDriverModel createModel(IMutableMapNG map) =>
            PopulatorDriverModel(map);
}

