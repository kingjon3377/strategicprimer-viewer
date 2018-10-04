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
    GUIDriver,
    DriverFactory,
    GUIDriverFactory
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
    IMapNG,
    IMutableMapNG
}
import lovelace.util.jvm {
    FileChooser
}
import lovelace.util.common {
    PathWrapper,
    defer,
    silentListener
}

"A factory for the worker-advancemnt GUI app."
service(`interface DriverFactory`)
shared class AdvancementGUIFactory() satisfies GUIDriverFactory {
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
        supportedOptions = [ "--current-turn=NN" ];
    };
    "Ask the user to choose a file or files."
    shared actual {PathWrapper*} askUserForFiles() {
        try {
            return SPFileChooser.open(null).files;
        } catch (FileChooser.ChoiceInterruptedException except) {
            throw DriverFailedException(except,
                "Choice interrupted or user didn't choose");
        }
    }
    shared actual GUIDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        assert (is IWorkerModel model);
        return AdvancementGUI(cli, options, model);
    }

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            WorkerModel(map, path);

}
"The worker-advancement GUI driver."
shared class AdvancementGUI(ICLIHelper cli, SPOptions options, model)
        satisfies GUIDriver {
    shared actual IWorkerModel model;
    void reload(PlayerChangeListener frame) =>
            frame.playerChanged(model.currentPlayer, model.currentPlayer);
    void createWindow(MenuBroker menuHandler, PlayerChangeMenuListener pcml) {
        SPFrame&PlayerChangeListener frame = advancementFrame(model, menuHandler);
        frame.addWindowListener(WindowCloseListener(menuHandler.actionPerformed));
        pcml.addPlayerChangeListener(frame);
        value reloadListener = silentListener(defer(reload, [frame])); // TODO: Inline once eclipse/ceylon#7379 fixed
        menuHandler.register(reloadListener, "reload tree");
        menuHandler.registerWindowShower(aboutDialog(frame, frame.windowName),
            "about");
        if (model.allMaps.map(Entry.key).every(compose(compose(Iterable<IUnit>.empty,
                model.getUnits), IMapNG.currentPlayer))) {
            pcml.actionPerformed(ActionEvent(frame, ActionEvent.actionFirst,
                "change current player"));
        }
        frame.showWindow();
    }
    shared actual void startDriver() {
        MenuBroker menuHandler = MenuBroker();
        menuHandler.register(IOHandler(this, options, cli), "load", "save",
            "save as", "new", "load secondary", "save all", "open in map viewer",
            "open secondary map in map viewer", "close", "quit");
        PlayerChangeMenuListener pcml = PlayerChangeMenuListener(model);
        menuHandler.register(pcml, "change current player");
        SwingUtilities.invokeLater(defer(createWindow, [menuHandler, pcml]));
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
    shared actual void open(IMutableMapNG map, PathWrapper? path) {
        if (model.mapModified) {
            SwingUtilities.invokeLater(defer(compose(AdvancementGUI.startDriver,
                AdvancementGUI), [cli, options, WorkerModel(map, path)]));
        } else {
            model.setMap(map, path);
        }
    }
}
