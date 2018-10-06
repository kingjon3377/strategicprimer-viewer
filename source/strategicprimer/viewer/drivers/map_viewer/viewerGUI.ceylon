import ceylon.logging {
    logger,
    Logger
}

import javax.swing {
    SwingUtilities
}

import strategicprimer.drivers.common {
    IDriverModel,
    IDriverUsage,
    DriverUsage,
    SPOptions,
    ParamCount,
    DriverFailedException,
    GUIDriver,
    DriverFactory,
    GUIDriverFactory,
    ViewerDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.common.map {
    MapDimensions,
    Point,
    IMutableMapNG
}
import strategicprimer.drivers.gui.common.about {
    aboutDialog
}
import strategicprimer.viewer.drivers {
    IOHandler
}
import strategicprimer.drivers.gui.common {
    SPFrame,
    WindowCloseListener,
    MenuBroker,
    SPFileChooser
}
import lovelace.util.common {
    silentListener,
    PathWrapper,
    defer
}
import lovelace.util.jvm {
    FileChooser
}

"A logger."
Logger log = logger(`module strategicprimer.viewer`);

"A factory for a driver to start the map viewer."
service(`interface DriverFactory`)
shared class ViewerGUIFactory() satisfies GUIDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        invocations = ["-m", "--map"];
        paramsWanted = ParamCount.one;
        shortDescription = "Map viewer";
        longDescription = "Look at the map visually. This is probably the app you want.";
        includeInCLIList = false;
        includeInGUIList = true;
        supportedOptions = [ "--current-turn=NN" ];
    };
    "Ask the user to choose a file or files."
    shared actual {PathWrapper+} askUserForFiles() {
        try {
            return SPFileChooser.open(null).files;
        } catch (FileChooser.ChoiceInterruptedException except) {
            throw DriverFailedException(except,
                "Choice interrupted or user didn't choose");
        }
    }
    shared actual GUIDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IViewerModel model) {
            return ViewerGUI(model);
        } else {
            return createDriver(cli, options, ViewerModel.copyConstructor(model));
        }
    }

    shared actual IViewerModel createModel(IMutableMapNG map, PathWrapper? path) =>
            ViewerModel(map, path);
}

"A driver to start the map viewer."
shared class ViewerGUI(model) satisfies ViewerDriver {
    shared actual IViewerModel model;
    shared actual void center() {
        Point selection = model.selection;
        MapDimensions dimensions = model.mapDimensions;
        VisibleDimensions visible = model.visibleDimensions;
        Integer topRow;
        if (selection.row - (visible.height / 2) <= 0) {
            topRow = 0;
        } else if (selection.row + (visible.height / 2) >= dimensions.rows) {
            topRow = dimensions.rows - visible.height;
        } else {
            topRow = selection.row - (visible.height / 2);
        }
        Integer leftColumn;
        if (selection.column - (visible.width / 2) <= 0) {
            leftColumn = 0;
        } else if (selection.column + (visible.width / 2) >= dimensions.columns) {
            leftColumn = dimensions.columns - visible.width;
        } else {
            leftColumn = selection.column - (visible.width / 2);
        }
        // Java version had topRow + dimensions.rows and
        // leftColumn + dimensions.columns as max row and column; this seems
        // plainly wrong.
        model.visibleDimensions = VisibleDimensions(topRow,
            topRow + visible.height, leftColumn, leftColumn + visible.width);
    }
    shared actual void zoomIn() => model.zoomIn();
    shared actual void zoomOut() => model.zoomOut();
    shared actual void resetZoom() => model.resetZoom();
    void createWindow(MenuBroker menuHandler) {
        SPFrame&MapGUI frame = ViewerFrame(model, menuHandler.actionPerformed, this);
        frame.addWindowListener(WindowCloseListener(menuHandler.actionPerformed));
        value selectTileDialogInstance = SelectTileDialog(frame, model);
        menuHandler.registerWindowShower(selectTileDialogInstance, "go to tile");
        selectTileDialogInstance.dispose();
        variable FindDialog? finder = null;
        FindDialog getFindDialog() {
            if (exists temp = finder) {
                return temp;
            } else {
                FindDialog local = FindDialog(frame, model);
                finder = local;
                return local;
            }
        }
        menuHandler.registerWindowShower(getFindDialog, "find a fixture");
        menuHandler.register(silentListener(compose(FindDialog.search,
            getFindDialog)()), "find next");
        menuHandler.registerWindowShower(aboutDialog(frame, frame.windowName),
            "about");
        frame.showWindow();
    }
    shared actual void startDriver() {
        MenuBroker menuHandler = MenuBroker();
        menuHandler.register(IOHandler(this), "load", "save", "save as", "new",
            "load secondary", "save all", "open in map viewer",
            "open secondary map in map viewer", "close", "quit");
        menuHandler.register(silentListener(zoomIn), "zoom in");
        menuHandler.register(silentListener(zoomOut), "zoom out");
        menuHandler.register(silentListener(resetZoom), "reset zoom");
        menuHandler.register(silentListener(center), "center");
        SwingUtilities.invokeLater(defer(createWindow, [menuHandler]));
    }
    "Ask the user to choose a file or files."
    shared actual {PathWrapper+} askUserForFiles() {
        try {
            return SPFileChooser.open(null).files;
        } catch (FileChooser.ChoiceInterruptedException except) {
            throw DriverFailedException(except,
                "Choice interrupted or user didn't choose");
        }
    }
    shared actual void open(IMutableMapNG map, PathWrapper? path) {
        if (model.mapModified) {
            SwingUtilities.invokeLater(defer(compose(ViewerGUI.startDriver,
                ViewerGUI), [ViewerModel(map, path)]));
        } else {
            model.setMap(map, path);
        }
    }
}
