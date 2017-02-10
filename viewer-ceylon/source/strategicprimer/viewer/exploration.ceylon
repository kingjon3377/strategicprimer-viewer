import controller.map.drivers {
    SimpleCLIDriver,
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions,
    DriverFailedException,
    SimpleDriver
}
import controller.map.misc {
    ICLIHelper,
    MenuBroker,
    IOHandler,
    WindowCloser
}
import model.misc {
    IDriverModel
}
import model.exploration {
    IExplorationModel,
    ExplorationModel
}
import java.io {
    IOException
}
import view.exploration {
    ExplorationCLI,
    ExplorationFrame
}
import javax.swing {
    SwingUtilities
}
import view.util {
    AboutDialog
}
"A CLI to help running exploration."
object explorationCLI satisfies SimpleCLIDriver {
    DriverUsage usageObject = DriverUsage(false, "-x", "--explore", ParamCount.atLeastOne,
        "Run exploration.",
        "Move a unit around the map, updating the player's map with what it sees.");
    usageObject.addSupportedOption("--current-turn=NN");
    shared actual IDriverUsage usage() => usageObject;
    shared actual void startDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
        IExplorationModel explorationModel;
        if (is IExplorationModel model) {
            explorationModel = model;
        } else {
            explorationModel = ExplorationModel(model);
        }
        try {
            ExplorationCLI eCLI = ExplorationCLI(explorationModel, cli);
            if (exists player = eCLI.choosePlayer(), exists unit = eCLI.chooseUnit(player)) {
                explorationModel.selectUnit(unit);
                eCLI.moveUntilDone();
            }
        } catch (IOException except) {
            throw DriverFailedException("I/O error interacting with user", except);
        }
    }
}
"An object to start the exploration GUI."
object explorationGUI satisfies SimpleDriver {
    DriverUsage usageObject = DriverUsage(true, "-x", "--explore", ParamCount.atLeastOne,
        "Run exploration.",
        "Move a unit around the map, updating the player's map with what it sees.");
    usageObject.addSupportedOption("--current-turn=NN");
    shared actual IDriverUsage usage() => usageObject;
    shared actual void startDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
        IExplorationModel explorationModel;
        if (is IExplorationModel model) {
            explorationModel = model;
        } else {
            explorationModel = ExplorationModel(model);
        }
        MenuBroker menuHandler = MenuBroker();
        menuHandler.register(IOHandler(explorationModel, options, cli), "load", "save",
            "save as", "new", "load secondary", "save all", "open in map viewer",
            "open secondary map in map viewer");
        menuHandler.register((event) => process.exit(0), "quit");
        SwingUtilities.invokeLater(() {
            ExplorationFrame frame = ExplorationFrame(explorationModel, menuHandler);
            menuHandler.register(WindowCloser(frame), "close");
            menuHandler.register((event) =>
                AboutDialog(frame, frame.windowName).setVisible(true), "about");
            frame.setVisible(true);
        });
    }
}