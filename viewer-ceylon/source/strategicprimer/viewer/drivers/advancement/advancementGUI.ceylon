import javax.swing {
    SwingUtilities
}
import strategicprimer.viewer.model {
    IDriverModel
}
import strategicprimer.viewer.drivers.worker_mgmt {
    WorkerModel,
    IWorkerModel
}
import strategicprimer.viewer.about {
    aboutDialog
}
import strategicprimer.viewer.drivers {
    ICLIHelper,
    SPFrame,
    DriverUsage,
    PlayerChangeMenuListener,
    ParamCount,
    IOHandler,
    IDriverUsage,
    MenuBroker,
    SPOptions,
    SimpleDriver
}
import model.listeners {
    PlayerChangeListener
}
"The worker-advancement GUI driver."
shared object advancementGUI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        shortOption = "-a";
        longOption = "--adv";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "View a player's workers and manage their advancement";
        longDescription = """View a player's units, the workers in those units, each
                             worker's Jobs, and his or her level in each Skill in each Job.""";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        IWorkerModel workerModel;
        if (is IWorkerModel model) {
            workerModel = model;
        } else {
            workerModel = WorkerModel.copyConstructor(model);
        }
        MenuBroker menuHandler = MenuBroker();
        menuHandler.register(IOHandler(workerModel, options, cli), "load", "save",
            "save as", "new", "load secondary", "save all", "open in map viewer",
            "open secondary map in map viewer");
        PlayerChangeMenuListener pcml = PlayerChangeMenuListener(workerModel);
        menuHandler.register(pcml, "change current player");
        menuHandler.register((event) => process.exit(0), "quit");
        SwingUtilities.invokeLater(() {
            SPFrame&PlayerChangeListener frame = advancementFrame(workerModel, menuHandler);
            pcml.addPlayerChangeListener(frame);
            menuHandler.register((event) =>
            frame.playerChanged(model.map.currentPlayer, model.map.currentPlayer),
                "reload tree");
            menuHandler.register((event) => frame.dispose(), "close");
            menuHandler.register((event) =>
            aboutDialog(frame, frame.windowName).setVisible(true), "about");
            frame.setVisible(true);
        });
    }
}