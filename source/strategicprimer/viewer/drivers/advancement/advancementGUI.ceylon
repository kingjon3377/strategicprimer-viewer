import java.nio.file {
    JPath=Path
}

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
    DriverFailedException,
    PlayerChangeListener,
	ISPDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.worker.common {
    WorkerModel,
    IWorkerModel
}
import strategicprimer.drivers.gui.common.about {
    aboutDialog
}
import strategicprimer.viewer.drivers {
    PlayerChangeMenuListener,
    IOHandler,
    MenuBroker,
    FileChooser
}
import strategicprimer.drivers.gui.common {
    SPFrame
}
import lovelace.util.common {
	silentListener
}
"The worker-advancement GUI driver."
service(`interface ISPDriver`)
shared class AdvancementGUI() satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        invocations = ["-a", "--adv"];
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Worker Skill Advancement";
        longDescription = """View a player's units, the workers in those units, each
                             worker's Jobs, and his or her level in each Skill in each
                             Job.""";
        includeInCLIList = false;
        includeInGUIList = true;
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    suppressWarnings("expressionTypeNothing")
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
            SPFrame&PlayerChangeListener frame = advancementFrame(workerModel,
                menuHandler);
            pcml.addPlayerChangeListener(frame);
            menuHandler.register((event) =>
            frame.playerChanged(workerModel.currentPlayer, workerModel.currentPlayer),
                "reload tree");
            menuHandler.register(silentListener(frame.dispose), "close");
            menuHandler.registerWindowShower(aboutDialog(frame, frame.windowName), "about");
            frame.showWindow();
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
