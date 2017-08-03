import ceylon.logging {
    logger,
    Logger
}

import java.nio.file {
    JPath=Path
}

import javax.swing {
    SwingUtilities
}

import strategicprimer.drivers.common {
    IMultiMapModel,
    IDriverModel,
    IDriverUsage,
    DriverUsage,
    SimpleDriver,
    SPOptions,
    ParamCount,
    DriverFailedException
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.map {
    MapDimensions,
    Point
}
import strategicprimer.viewer.about {
    aboutDialog
}
import strategicprimer.viewer.drivers {
    IOHandler,
    MenuBroker,
    FileChooser
}
import strategicprimer.drivers.gui.common {
	SPFrame
}
"A logger."
Logger log = logger(`module strategicprimer.viewer`);
"A driver to start the map viewer."
shared object viewerGUI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        shortOption = "-m";
        longOption = "--map";
        paramsWanted = ParamCount.one;
        shortDescription = "Map viewer";
        longDescription = "Look at the map visually. This is probably the app you want.";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    suppressWarnings("expressionTypeNothing")
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IViewerModel model) {
            MenuBroker menuHandler = MenuBroker();
            menuHandler.register(IOHandler(model, options, cli), "load", "save",
                "save as", "new", "load secondary", "save all", "open in map viewer",
                "open secondary map in map viewer");
            menuHandler.register((event) => process.exit(0), "quit");
            menuHandler.register((event) => model.zoomIn(), "zoom in");
            menuHandler.register((event) => model.zoomOut(), "zoom out");
            menuHandler.register((event) => model.resetZoom(), "reset zoom");
            menuHandler.register((event) {
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
            }, "center");
            SwingUtilities.invokeLater(() {
                SPFrame&IViewerFrame frame = viewerFrame(model,
                    menuHandler.actionPerformed);
                menuHandler.register((event) => frame.dispose(), "close");
                menuHandler.register((event) =>
                selectTileDialog(frame, model).setVisible(true), "go to tile");
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
                menuHandler.register((event) => getFindDialog().setVisible(true),
                    "find a fixture");
                menuHandler.register((event) => getFindDialog().search(), "find next");
                menuHandler.register((event) =>
                aboutDialog(frame, frame.windowName).setVisible(true), "about");
                frame.setVisible(true);
            });
        } else if (is IMultiMapModel model) {
            for (map in model.allMaps) {
                startDriverOnModel(cli, options.copy(), ViewerModel.fromPair(map));
            }
        } else {
            startDriverOnModel(cli, options, ViewerModel(model.map,
                model.mapFile));
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
