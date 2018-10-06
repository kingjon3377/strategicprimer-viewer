import javax.swing {
    SwingUtilities
}

import strategicprimer.drivers.common {
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions,
    IDriverModel,
    DriverFailedException,
    GUIDriver,
    DriverFactory,
    GUIDriverFactory,
    MultiMapGUIDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.gui.common.about {
    aboutDialog
}
import strategicprimer.viewer.drivers {
    IOHandler,
    SPFileChooser
}
import strategicprimer.drivers.exploration.common {
    IExplorationModel,
    ExplorationModel
}
import strategicprimer.drivers.gui.common {
    SPFrame,
    WindowCloseListener,
    MenuBroker
}
import lovelace.util.jvm {
    FileChooser
}
import lovelace.util.common {
    PathWrapper,
    defer
}
import strategicprimer.model.common.map {
    IMutableMapNG
}

"An factory for the exploration GUI."
service(`interface DriverFactory`)
shared class ExplorationGUIFactory() satisfies GUIDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        invocations = ["-x", "--explore"];
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Run exploration.";
        longDescription = "Move a unit around the map, updating the player's map with what
                           it sees.";
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
        if (is IExplorationModel model) {
            return ExplorationGUI(cli, options, model);
        } else {
            return createDriver(cli, options, ExplorationModel.copyConstructor(model));
        }
    }
    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            ExplorationModel(map, path);
}
"An object to start the exploration GUI."
class ExplorationGUI(ICLIHelper cli, SPOptions options, model)
        satisfies MultiMapGUIDriver {
    shared actual IExplorationModel model;
    void createWindow(MenuBroker menuHandler) {
        SPFrame frame = explorationFrame(this, menuHandler);
        frame.addWindowListener(WindowCloseListener(menuHandler.actionPerformed));
        menuHandler.registerWindowShower(aboutDialog(frame, frame.windowName),
            "about");
        frame.showWindow();
    }
    shared actual void startDriver() {
        MenuBroker menuHandler = MenuBroker();
        menuHandler.register(IOHandler(this), "load", "save", "save as", "new",
            "load secondary", "save all", "open in map viewer",
            "open secondary map in map viewer", "close", "quit");
        SwingUtilities.invokeLater(defer(createWindow, [menuHandler]));
    }
    "Ask the user to choose a file or files."
    shared actual {PathWrapper*} askUserForFiles() {
        try {
            return SPFileChooser.open(null).files;
        } catch (FileChooser.ChoiceInterruptedException except) {
            throw DriverFailedException(except,
                "Choice interrupted or user didn't choose");
        }
    }
    shared actual void open(IMutableMapNG map, PathWrapper? path) {
        if (model.mapModified) {
            SwingUtilities.invokeLater(defer(compose(ExplorationGUI.startDriver,
                ExplorationGUI), [cli, options, ExplorationModel(map, path)]));
        } else {
            model.setMap(map, path);
        }
    }
}
