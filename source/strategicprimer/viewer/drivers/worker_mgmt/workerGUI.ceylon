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
    SPFileChooser
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
    ISPDriver
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
import strategicprimer.model.common.map {
    IMapNG
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import lovelace.util.jvm {
    FileChooser
}
import strategicprimer.drivers.gui.common {
    WindowCloseListener
}
"A logger."
Logger log = logger(`module strategicprimer.viewer`);
"A driver to start the worker management GUI."
service(`interface ISPDriver`)
shared class WorkerGUI() satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        invocations = ["-w", "--worker"];
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Unit Orders and Worker Management";
        longDescription = "Organize the members of a player's units.";
        includeInCLIList = false;
        includeInGUIList = true;
        supportedOptionsTemp = [ "--current-turn=NN", "--print-empty",
            "--include-unleveled-jobs", "--summarize-large-units" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IWorkerModel model) {
            MenuBroker menuHandler = MenuBroker();
            menuHandler.register(IOHandler(model, options, cli), "load", "save",
                "save as", "new", "load secondary", "save all", "open in map viewer",
                "open secondary map in map viewer", "close", "quit");
            PlayerChangeMenuListener pcml = PlayerChangeMenuListener(model);
            menuHandler.register(pcml, "change current player");
            SwingUtilities.invokeLater(() {
                log.trace("Inside GUI creation lambda");
                value frame = WorkerMgmtFrame(options, model, menuHandler);
                log.trace("Created worker mgmt frame");
                pcml.addPlayerChangeListener(frame);
                log.trace("Added it as a listener on the PCML");
                frame.addWindowListener(WindowCloseListener(menuHandler.actionPerformed));
                menuHandler.register((event) => frame.playerChanged(
                    model.currentPlayer, model.currentPlayer),
                    "reload tree");
                menuHandler.registerWindowShower(aboutDialog(frame, frame.windowName),
                    "about");
                log.trace("Registered menu handlers");
                if (model.allMaps.map(Entry.key)
                        .every(compose(compose(Iterable<IUnit>.empty,
                        model.getUnits), IMapNG.currentPlayer))) {
                    pcml.actionPerformed(ActionEvent(frame, ActionEvent.actionFirst,
                        "change current player"));
                }
                log.trace("About to show window");
                frame.showWindow();
                log.trace("Window should now be visible");
            });
            log.trace("Worker GUI window should appear any time now");
        } else {
            startDriverOnModel(cli, options, WorkerModel.copyConstructor(model));
        }
    }
    "Ask the user to choose a file or files."
    shared actual {JPath*} askUserForFiles() {
        try {
            return SPFileChooser.open(null).files;
        } catch (FileChooser.ChoiceInterruptedException except) {
            log.debug("Choice interrupted or user didn't choose", except);
            return [];
        }
    }
}
