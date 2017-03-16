import javax.swing {
    SwingUtilities
}
import model.misc {
    IDriverModel
}
import strategicprimer.viewer.about {
    aboutDialog
}
import strategicprimer.viewer.drivers {
    ICLIHelper,
    DriverUsage,
    PlayerChangeMenuListener,
    ParamCount,
    IOHandler,
    IDriverUsage,
    MenuBroker,
    SPOptions,
    SimpleDriver
}
import model.workermgmt {
    WorkerModel,
    IWorkerModel
}
import ceylon.logging {
    logger,
    Logger
}
"A logger."
Logger log = logger(`module strategicprimer.viewer`);
"A driver to start the worker management GUI."
shared object workerGUI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        shortOption = "-w";
        longOption = "--worker";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Manage a player's workers in units";
        longDescription = "Organize the members of a player's units.";
        supportedOptionsTemp = [ "--current-turn=NN", "--print-empty" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IWorkerModel model) {
            MenuBroker menuHandler = MenuBroker();
            menuHandler.register(IOHandler(model, options, cli), "load", "save",
                "save as", "new", "load secondary", "save all", "open in map viewer",
                "open secondary map in map viewer");
            PlayerChangeMenuListener pcml = PlayerChangeMenuListener(model);
            menuHandler.register(pcml, "change current player");
            menuHandler.register((event) => process.exit(0), "quit");
            SwingUtilities.invokeLater(() {
                value frame = workerMgmtFrame(options, model, menuHandler);
                pcml.addPlayerChangeListener(frame);
                menuHandler.register((event) => frame.playerChanged(
                    model.map.currentPlayer, model.map.currentPlayer),
                    "reload tree");
                menuHandler.register((event) => frame.dispose(), "close");
                menuHandler.register((event) =>
                aboutDialog(frame, frame.windowName).setVisible(true), "about");
                frame.setVisible(true);
            });
        } else {
            startDriverOnModel(cli, options, WorkerModel(model));
        }
    }
}
