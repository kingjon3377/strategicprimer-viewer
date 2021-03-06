import javax.swing {
    SwingUtilities
}
import strategicprimer.drivers.gui.common.about {
    aboutDialog
}
import strategicprimer.viewer.drivers {
    PlayerChangeMenuListener,
    IOHandler
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
    GUIDriver,
    DriverFactory,
    GUIDriverFactory,
    MultiMapGUIDriver,
    IWorkerModel,
    WorkerGUI
}
import strategicprimer.drivers.worker.common {
    WorkerModel
}
import java.awt.event {
    ActionEvent
}
import strategicprimer.model.common.map {
    IMapNG,
    IMutableMapNG
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import lovelace.util.jvm {
    FileChooser
}
import strategicprimer.drivers.gui.common {
    WindowCloseListener,
    MenuBroker,
    SPFileChooser
}
import lovelace.util.common {
    PathWrapper,
    silentListener,
    defer
}

"A logger."
Logger log = logger(`module strategicprimer.viewer`);

"A factory for the worker management GUI."
service(`interface DriverFactory`)
shared class WorkerMgmtGUIFactory() satisfies GUIDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        invocation = "worker-mgmt";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Unit Orders and Worker Management";
        longDescription = "Organize the members of a player's units.";
        includeInCLIList = false;
        includeInGUIList = true;
        supportedOptions = [ "--current-turn=NN", "--print-empty",
            "--include-unleveled-jobs", "--summarize-large-units", "--edit-results" ];
    };

    "Ask the user to choose a file or files."
    shared actual {PathWrapper*} askUserForFiles() {
        try {
            return SPFileChooser.open(null).files;
        } catch (FileChooser.ChoiceInterruptedException except) {
            log.debug("Choice interrupted or user didn't choose", except);
            return [];
        }
    }

    shared actual GUIDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IWorkerModel model) {
            return WorkerMgmtGUI(cli, options, model);
        } else {
            return createDriver(cli, options, WorkerModel.copyConstructor(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map) =>
            WorkerModel(map);
}

"A driver to start the worker management GUI."
shared class WorkerMgmtGUI(ICLIHelper cli, options, model) satisfies MultiMapGUIDriver&WorkerGUI {
    shared actual SPOptions options;
    shared actual IWorkerModel model;

    void createWindow(MenuBroker menuHandler, PlayerChangeMenuListener pcml) {
        log.trace("Inside GUI creation lambda");
        value frame = WorkerMgmtFrame(options, model, menuHandler, this);
        log.trace("Created worker mgmt frame");
        pcml.addPlayerChangeListener(frame);
        log.trace("Added it as a listener on the PCML");
        frame.addWindowListener(WindowCloseListener(menuHandler.actionPerformed));
        value reloadListener = silentListener(defer(reload, [frame])); // TODO: Inline once eclipse/ceylon#7379 fixed
        menuHandler.register(reloadListener, "reload tree");
        menuHandler.registerWindowShower(aboutDialog(frame, frame.windowName),
            "about");
        log.trace("Registered menu handlers");
        if (model.allMaps.every(compose(compose(Iterable<IUnit>.empty,
                model.getUnits), IMapNG.currentPlayer))) {
            pcml.actionPerformed(ActionEvent(frame, ActionEvent.actionFirst,
                "change current player"));
        }

        log.trace("About to show window");
        frame.showWindow();
        log.trace("Window should now be visible");
    }

    void reload(WorkerMgmtFrame frame) =>
            frame.playerChanged(model.currentPlayer, model.currentPlayer);

    shared actual void startDriver() {
        MenuBroker menuHandler = MenuBroker();
        menuHandler.register(IOHandler(this), "load", "save", "save as", "new",
            "load secondary", "save all", "open in map viewer",
            "open secondary map in map viewer", "close", "quit");
        PlayerChangeMenuListener pcml = PlayerChangeMenuListener(model);
        menuHandler.register(pcml, "change current player");
        SwingUtilities.invokeLater(defer(createWindow, [menuHandler, pcml]));
        log.trace("Worker GUI window should appear any time now");
    }

    "Ask the user to choose a file or files."
    shared actual {PathWrapper*} askUserForFiles() {
        try {
            return SPFileChooser.open(null).files;
        } catch (FileChooser.ChoiceInterruptedException except) {
            log.debug("Choice interrupted or user didn't choose", except);
            return [];
        }
    }

    shared actual void open(IMutableMapNG map) {
        if (model.mapModified) {
            SwingUtilities.invokeLater(defer(compose(WorkerMgmtGUI.startDriver,
                WorkerMgmtGUI), [cli, options, WorkerModel(map)]));
        } else {
            model.setMap(map);
        }
    }
}
