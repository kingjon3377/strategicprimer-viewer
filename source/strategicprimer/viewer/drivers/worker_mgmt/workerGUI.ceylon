import javax.swing {
    SwingUtilities
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
import ceylon.logging {
    logger,
    Logger
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.common {
    IDriverUsage,
    ParamCount,
    DriverUsage,
    SPOptions,
    IDriverModel,
    SimpleDriver,
    DriverFailedException
}
import java.nio.file {
    JPath=Path
}
import strategicprimer.drivers.worker.common {
    WorkerModel,
    IWorkerModel
}
import java.awt.event {
    ActionEvent
}
"A logger."
Logger log = logger(`module strategicprimer.viewer`);
"A driver to start the worker management GUI."
shared object workerGUI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        invocations = ["-w", "--worker"];
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Manage a player's workers in units";
        longDescription = "Organize the members of a player's units.";
        supportedOptionsTemp = [ "--current-turn=NN", "--print-empty", "--include-unleveled-jobs",
	        "--summarize-large-units" ];
    };
    suppressWarnings("expressionTypeNothing")
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
                log.trace("Inside GUI creation lambda");
                value frame = workerMgmtFrame(options, model, menuHandler);
                log.trace("Created worker mgmt frame");
                pcml.addPlayerChangeListener(frame);
                log.trace("Added it as a listener on the PCML");
                menuHandler.register((event) => frame.playerChanged(
                    model.currentPlayer, model.currentPlayer),
                    "reload tree");
                menuHandler.register((event) => frame.dispose(), "close");
                menuHandler.register((event) =>
                aboutDialog(frame, frame.windowName).setVisible(true), "about");
                log.trace("Registered menu handlers");
                if (model.allMaps.every(([map, _]) => model.getUnits(map.currentPlayer).empty)) {
                    pcml.actionPerformed(ActionEvent(frame, ActionEvent.actionFirst, "change current player"));
                }
                log.trace("About to show window");
                frame.setVisible(true);
                log.trace("Window should now be visible");
            });
            log.trace("Worker GUI window should appear any time now");
        } else {
            startDriverOnModel(cli, options, WorkerModel.copyConstructor(model));
        }
    }
    "Ask the user to choose a file or files."
    shared actual {JPath+} askUserForFiles() {
        try {
            return FileChooser.open(null).files;
        } catch (FileChooser.ChoiceInterruptedException except) {
            throw DriverFailedException(except,
                "Choice interrupted or user didn't choose");
        }
    }
}
