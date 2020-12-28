import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import lovelace.util.jvm {
    FileChooser
}
import strategicprimer.model.common.map {
    IMutableMapNG
}
import strategicprimer.drivers.gui.common {
    SPFileChooser
}
import lovelace.util.common {
    PathWrapper
}
import strategicprimer.drivers.common {
    SPOptions,
    GUIDriverFactory,
    DriverUsage,
    DriverFactory,
    ParamCount,
    IDriverUsage,
    IDriverModel,
    DriverFailedException,
    GUIDriver
}

"A factory for the resource-adding GUI app."
service(`interface DriverFactory`)
shared class ResourceAddingGUIFactory() satisfies GUIDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        invocation = "add-resource";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Add resources to maps";
        longDescription = "Add resources for players to maps";
        includeInCLIList = false;
        includeInGUIList = true;
        supportedOptions = [ "--current-turn=NN" ];
    };

    "Ask the user to choose a file or files."
    shared actual {PathWrapper*} askUserForFiles() {
        try {
            return SPFileChooser.open(null).files;
        } catch (FileChooser.ChoiceInterruptedException except) {
            throw DriverFailedException(except,
                "Choice interrupted or user didn't choose");
        }
    }

    shared actual GUIDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is ResourceManagementDriverModel model) {
            return ResourceAddingGUI(cli, options, model);
        } else {
            return createDriver(cli, options,
                ResourceManagementDriverModel.fromDriverModel(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map) =>
        ResourceManagementDriverModel.fromMap(map);
}
