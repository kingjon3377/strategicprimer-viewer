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
    FileChooser
}
import strategicprimer.drivers.exploration.common {
    IExplorationModel,
    ExplorationModel
}
import strategicprimer.drivers.gui.common {
    SPFrame
}
import lovelace.util.common {
	silentListener
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
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    suppressWarnings("expressionTypeNothing")
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
            "open secondary map in map viewer");
        menuHandler.register((event) => process.exit(0), "quit");
        SwingUtilities.invokeLater(() {
            SPFrame frame = explorationFrame(explorationModel,
                menuHandler.actionPerformed);
            menuHandler.register(silentListener(frame.dispose), "close");
            menuHandler.registerWindowShower(aboutDialog(frame, frame.windowName), "about");
            frame.setVisible(true);
        });
    }
    "Ask the user to choose a file or files."
    shared actual {JPath*} askUserForFiles() {
        try {
            return FileChooser.open(null).files;
        } catch (FileChooser.ChoiceInterruptedException except) {
            throw DriverFailedException(except,
                "Choice interrupted or user didn't choose");
        }
    }
}
