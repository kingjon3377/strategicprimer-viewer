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

import javax.imageio {
    ImageIO
}

import java.io {
    JFile=File
}

"A logger."
Logger log = logger(`module strategicprimer.viewer`);

"A factory for a driver to start the map viewer."
service(`interface DriverFactory`)
shared class ViewerGUIFactory() satisfies GUIDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        invocation = "view-map";
        paramsWanted = ParamCount.one;
        shortDescription = "Map viewer";
        longDescription = "Look at the map visually. This is probably the app you want.";
        includeInCLIList = false;
        includeInGUIList = true;
        supportedOptions = [ "--current-turn=NN", "--background=image.png", "--starting-row=NN --starting-column=NN"];
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
            log.trace("Creating a viewer-GUI instance for a model of the proper type");
            return ViewerGUI(model, options);
        } else {
            log.trace("Creating a viewer-GUI instance after converting its type");
            return createDriver(cli, options, ViewerModel.copyConstructor(model));
        }
    }

    shared actual IViewerModel createModel(IMutableMapNG map, PathWrapper? path) {
        if (exists path) {
            log.trace("Creating a viewer model for path ``path``");
        } else {
            log.trace("Creating a viewer model for a null path");
        }
        return ViewerModel(map, path);
    }
}

"A driver to start the map viewer."
shared class ViewerGUI(model, options) satisfies ViewerDriver {
    shared actual IViewerModel model;
    shared actual SPOptions options;
    log.trace("In ViewerGUI constructor");

    shared actual void center() {
        Point selection = model.selection;
        log.trace("Asked to center on ``selection``.");
        MapDimensions dimensions = model.mapDimensions;
        VisibleDimensions visible = model.visibleDimensions;
        log.trace("Visible area is currently from (``visible.minimumRow``, ``
            visible.minimumColumn``) to (``visible.maximumRow`` to ``
            visible.maximumColumn``), ``visible.height`` rows x ``visible.width`` cols.");
        Integer topRow;
        if (selection.row - (visible.height / 2) <= 0) {
            log.trace("Close enough to the top to go flush to it");
            topRow = 0;
        } else if (selection.row + (visible.height / 2) >= dimensions.rows) {
            log.trace("Close enough to the bottom to go flush to it");
            topRow = dimensions.rows - visible.height;
        } else {
            topRow = selection.row - (visible.height / 2);
            log.trace("Setting top row to ``topRow``");
        }
        Integer leftColumn;
        if (selection.column - (visible.width / 2) <= 0) {
            log.trace("Close enough to left edge to go flush to it");
            leftColumn = 0;
        } else if (selection.column + (visible.width / 2) >= dimensions.columns) {
            log.trace("Close enough to right edge to go flush to it");
            leftColumn = dimensions.columns - visible.width;
        } else {
            leftColumn = selection.column - (visible.width / 2);
            log.trace("Setting left column to ``leftColumn``");
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
        ViewerFrame frame = ViewerFrame(model, menuHandler.actionPerformed, this, options);
        value backgroundFile = options.getArgument("--background");
        if (!backgroundFile.empty && backgroundFile != "false") {
            frame.backgroundImage = ImageIO.read(JFile(backgroundFile));
        }
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
        // TODO: We'd like to have the starting position stored in the map
        // file, in system preferences, or some such, or simply default to HQ.
        if (options.hasOption("--starting-row"), options.hasOption("--starting-column")) {
            value startingRow = Integer.parse(options.getArgument("--starting-row"));
            value startingCol = Integer.parse(options.getArgument("--starting-column"));
            if (is Integer startingRow, is Integer startingCol) {
                value starting = Point(startingRow, startingCol);
                if (starting in model.mapDimensions) {
                    model.selection = starting;
                } else {
                    log.warn("Starting coordinates must be within the map's dimensions");
                }
            } else {
                log.warn("Arguments to --starting-row and --starting-column must be numeric");
            }
        } else if (options.hasOption("--starting-row")) {
            log.warn("--starting-row requires --starting-column");
        } else if (options.hasOption("--starting-column")) {
            log.warn("--starting-column requires --starting-row");
        }
        log.trace("About to show viewer GUI window");
        frame.showWindow();
    }

    shared actual void startDriver() {
        log.trace("In ViewerGUI.startDriver()");
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
                ViewerGUI), [ViewerModel(map, path), options.copy()]));
        } else {
            model.setMap(map, path);
        }
    }
}
