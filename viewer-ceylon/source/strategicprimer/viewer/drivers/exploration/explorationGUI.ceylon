import javax.swing {
    SwingUtilities
}
import model.misc {
    IDriverModel
}
import strategicprimer.viewer.drivers {
    SPFrame,
    DriverUsage,
    ParamCount,
    IOHandler,
    IDriverUsage,
    MenuBroker,
    SPOptions,
    SimpleDriver,
    ICLIHelper
}
import strategicprimer.viewer.about {
    aboutDialog
}
"An object to start the exploration GUI."
shared object explorationGUI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        shortOption = "-x";
        longOption = "--explore";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Run exploration.";
        longDescription = "Move a unit around the map, updating the player's map with what it sees.";
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
            "open secondary map in map viewer");
        menuHandler.register((event) => process.exit(0), "quit");
        SwingUtilities.invokeLater(() {
            SPFrame frame = explorationFrame(explorationModel,
                menuHandler.actionPerformed);
            menuHandler.register((event) => frame.dispose(), "close");
            menuHandler.register((event) =>
            aboutDialog(frame, frame.windowName).setVisible(true), "about");
            frame.setVisible(true);
        });
    }
}
