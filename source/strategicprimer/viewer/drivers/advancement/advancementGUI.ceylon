import javax.swing {
    SwingUtilities
}

import strategicprimer.drivers.common {
    ParamCount,
    IDriverUsage,
    SPOptions,
    DriverUsage,
    IDriverModel,
    DriverFailedException,
    PlayerChangeListener,
    ISPDriver,
    GUIDriver
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
    SPFileChooser
}
import strategicprimer.drivers.gui.common {
    SPFrame,
    WindowCloseListener
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import java.awt.event {
    ActionEvent
}
import strategicprimer.model.common.map {
    IMapNG
}
import lovelace.util.jvm {
    FileChooser
}
import lovelace.util.common {
    PathWrapper
}

"The worker-advancement GUI driver."
service(`interface ISPDriver`)
shared class AdvancementGUI() satisfies GUIDriver {
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
            "open secondary map in map viewer", "close", "quit");
        PlayerChangeMenuListener pcml = PlayerChangeMenuListener(workerModel);
        menuHandler.register(pcml, "change current player");
        SwingUtilities.invokeLater(() {
            SPFrame&PlayerChangeListener frame = advancementFrame(workerModel,
                menuHandler);
            frame.addWindowListener(WindowCloseListener(menuHandler.actionPerformed));
            pcml.addPlayerChangeListener(frame);
            menuHandler.register((event) =>
                frame.playerChanged(workerModel.currentPlayer, workerModel.currentPlayer),
                    "reload tree");
            menuHandler.registerWindowShower(aboutDialog(frame, frame.windowName),
                "about");
            if (workerModel.allMaps.map(Entry.key)
                    .every(compose(compose(Iterable<IUnit>.empty,
                    workerModel.getUnits), IMapNG.currentPlayer))) {
                pcml.actionPerformed(ActionEvent(frame, ActionEvent.actionFirst,
                    "change current player"));
            }
            frame.showWindow();
        });
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
}
