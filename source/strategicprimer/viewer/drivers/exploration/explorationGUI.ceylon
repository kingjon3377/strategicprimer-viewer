import java.nio.file {
    JPath=Path
}

import javax.swing {
    SwingUtilities
}

import strategicprimer.drivers.common {
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions,
    SimpleDriver,
    IDriverModel,
    DriverFailedException,
    ISPDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.gui.common.about {
    aboutDialog
}
import strategicprimer.viewer.drivers {
    IOHandler,
    MenuBroker,
    SPFileChooser
}
import strategicprimer.drivers.exploration.common {
    IExplorationModel,
    ExplorationModel
}
import strategicprimer.drivers.gui.common {
    SPFrame,
    WindowCloseListener
}
import lovelace.util.jvm {
    FileChooser
}
"An object to start the exploration GUI."
service(`interface ISPDriver`)
shared class ExplorationGUI() satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        invocations = ["-x", "--explore"];
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Run exploration.";
        longDescription = "Move a unit around the map, updating the player's map with what
                           it sees.";
        includeInCLIList = false;
        includeInGUIList = true;
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        IExplorationModel explorationModel;
        if (is IExplorationModel model) {
            explorationModel = model;
        } else {
            explorationModel = ExplorationModel.copyConstructor(model);
        }
        MenuBroker menuHandler = MenuBroker();
        menuHandler.register(IOHandler(explorationModel, options, cli), "load", "save",
            "save as", "new", "load secondary", "save all", "open in map viewer",
            "open secondary map in map viewer", "close", "quit");
        SwingUtilities.invokeLater(() {
            SPFrame frame = explorationFrame(explorationModel,
                menuHandler);
            frame.addWindowListener(WindowCloseListener(menuHandler.actionPerformed));
            menuHandler.registerWindowShower(aboutDialog(frame, frame.windowName),
                "about");
            frame.showWindow();
        });
    }
    "Ask the user to choose a file or files."
    shared actual {JPath*} askUserForFiles() {
        try {
            return SPFileChooser.open(null).files;
        } catch (FileChooser.ChoiceInterruptedException except) {
            throw DriverFailedException(except,
                "Choice interrupted or user didn't choose");
        }
    }
}
