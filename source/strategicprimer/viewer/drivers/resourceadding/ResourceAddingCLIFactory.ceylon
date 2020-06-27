import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.common.map {
    IMutableMapNG
}
import lovelace.util.common {
    PathWrapper
}
import strategicprimer.drivers.common {
    SPOptions,
    ModelDriver,
    ModelDriverFactory,
    DriverUsage,
    DriverFactory,
    ParamCount,
    IDriverUsage,
    IDriverModel
}

"A factory for a driver to let the user enter a player's resources and equipment."
service(`interface DriverFactory`)
shared class ResourceAddingCLIFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocation = "add-resource";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Add resources to maps";
        longDescription = "Add resources for players to maps.";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptions = [ "--current-turn=NN" ];
    };

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
        IDriverModel model) {
        if (is ResourceManagementDriverModel model) {
            return ResourceAddingCLI(cli, options, model);
        } else {
            return createDriver(cli, options,
                ResourceManagementDriverModel.fromDriverModel(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
        ResourceManagementDriverModel.fromMap(map, path);
}
