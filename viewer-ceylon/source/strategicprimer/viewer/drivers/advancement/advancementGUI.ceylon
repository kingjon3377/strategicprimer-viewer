import javax.swing {
    SwingUtilities
}

import strategicprimer.drivers.common {
    ParamCount,
    IDriverUsage,
    SPOptions,
    DriverUsage,
    IDriverModel,
    SimpleDriver,
    ICLIHelper,
    DriverFailedException
}
import java.nio.file {
    JPath=Path
}
import strategicprimer.viewer.about {
    aboutDialog
}
import strategicprimer.viewer.drivers {
    SPFrame,
    PlayerChangeMenuListener,
    IOHandler,
    MenuBroker,
    FileChooser
}
import strategicprimer.viewer.drivers.exploration {
    PlayerChangeListener
}
import strategicprimer.viewer.drivers.worker_mgmt {
    WorkerModel,
    IWorkerModel
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
    "Ask the user to choose a file."
    shared actual JPath askUserForFile() {
        try {
            return FileChooser.open(null).file;
        } catch (FileChooser.ChoiceInterruptedException except) {
            throw DriverFailedException(except,
                "Choice interrupted or user didn't choose");
        }
    }
}