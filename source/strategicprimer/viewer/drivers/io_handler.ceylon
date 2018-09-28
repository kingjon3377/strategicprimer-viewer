import java.awt {
    Component,
    JFileDialog=FileDialog,
    Frame
}
import java.awt.event {
    ActionListener,
    ActionEvent
}
import java.io {
    FileNotFoundException,
    IOException,
    FilenameFilter,
    JFile=File
}
import java.nio.file {
    NoSuchFileException,
    JPath=Path
}

import javax.swing {
    JFileChooser,
    JOptionPane
}
import javax.swing.filechooser {
    FileNameExtensionFilter,
    FileFilter
}
import javax.xml.stream {
    XMLStreamException
}

import lovelace.util.common {
    todo,
    as,
    defer
}
import lovelace.util.jvm {
    showErrorDialog,
    platform,
    FileChooser
}

import strategicprimer.model.impl.map {
    SPMapNG,
    PlayerCollection,
    IMutableMapNG
}
import strategicprimer.model.impl.xmlio {
    mapIOHelper,
    warningLevels,
    SPFormatException
}
import strategicprimer.viewer.drivers.map_viewer {
    ViewerGUI,
    ViewerModel
}
import strategicprimer.drivers.common {
    IMultiMapModel,
    IDriverModel,
    SPOptions
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import ceylon.file {
    parsePath
}
import strategicprimer.drivers.gui.common {
    ISPWindow
}

"""A handler for "open" and "save" menu items (and a few others)"""
todo("Further splitting up", "Fix circular dependency between this and viewerGUI")
shared class IOHandler
        satisfies ActionListener {
    suppressWarnings("expressionTypeNothing")
    static void defaultQuitHandler() => process.exit(0); // TODO: remove these once eclipse/ceylon#7396 fixed
    shared static variable Anything() quitHandler = defaultQuitHandler;
    static FileFilter mapExtensionsFilter = FileNameExtensionFilter(
        "Strategic Primer world map files", "map", "xml", "db");
    "A factory method for [[JFileChooser]] (or AWT [[FileDialog|JFileDialog]] taking a
     [[FileFilter]] to apply in the same operation."
    todo("Move functionality into FileChooser somehow?")
    shared static JFileChooser|JFileDialog filteredFileChooser(
            "Whether to allow multi-selection."
            Boolean allowMultiple,
            "The current directory."
            String current = ".",
            "The filter to apply."
            FileFilter? filter = mapExtensionsFilter) {
        if (platform.systemIsMac) {
            JFileDialog retval = JFileDialog(null of Frame?);
            if (exists filter) {
                retval.filenameFilter = object satisfies FilenameFilter {
                    shared actual Boolean accept(JFile dir, String name) =>
                            filter.accept(JFile(dir, name));
                };
            }
            return retval;
        } else {
            JFileChooser retval = JFileChooser(current);
            if (exists filter) {
                retval.fileFilter = filter;
            }
            return retval;
        }
    }
    IDriverModel mapModel;
    SPOptions options;
    ICLIHelper cli;
    shared new (IDriverModel mapModel, SPOptions options, ICLIHelper cli) {
        this.mapModel = mapModel;
        this.options = options;
        this.cli = cli;
    }
    "If any files are marked as modified, ask the user whether to save them before
     closing/quitting."
    void maybeSave(String verb, Frame? window, Component? source,
            Anything() ifNotCanceled) {
        if (mapModel.mapModified) {
            String prompt;
            if (is IMultiMapModel mapModel, !mapModel.subordinateMaps.empty) {
                prompt = "Save changes to main map before ``verb``?";
            } else {
                prompt = "Save changes to map before ``verb``?";
            }
            Integer answer = JOptionPane.showConfirmDialog(window, prompt,
                "Save Changes?", JOptionPane.yesNoCancelOption,
                JOptionPane.questionMessage);
            if (answer == JOptionPane.cancelOption) {
                return;
            } else if (answer == JOptionPane.yesOption) {
                actionPerformed(ActionEvent(source, ActionEvent.actionFirst, "save"));
            }
        }
        if (is IMultiMapModel mapModel, mapModel.allMaps.map(Entry.item)
            .map(Tuple.last).coalesced.any(true.equals)) {
            Integer answer = JOptionPane.showConfirmDialog(window,
                "Subordinate map(s) have unsaved changes. Save all before ``verb``?",
                "Save Changes?", JOptionPane.yesNoCancelOption,
                JOptionPane.questionMessage);
            if (answer == JOptionPane.cancelOption) {
                return;
            } else if (answer == JOptionPane.yesOption) {
                actionPerformed(ActionEvent(source, ActionEvent.actionFirst, "save all"));
            }
        }
        ifNotCanceled();
    }
    void handleError(Exception except, String filename, Component? source,
            String errorTitle, String verb) {
        String message;
        if (is XMLStreamException except) {
            message = "Malformed XML in ``filename``";
        } else if (is FileNotFoundException|NoSuchFileException except) {
            message = "File ``filename`` not found";
        } else if (is IOException except) {
            message = "I/O error ``verb`` file ``filename``";
        } else if (is SPFormatException except) {
            message = "SP map format error in ``filename``";
        } else {
            message = except.message;
        }
        log.error(message, except);
        showErrorDialog(source, errorTitle, message);
    }
    void loadHandlerImpl(Anything(IMutableMapNG, JPath) handler, Component? source,
            String errorTitle)(JPath path) {
        try {
            handler(mapIOHelper.readMap(path, warningLevels.default), path);
        } catch (IOException|SPFormatException|XMLStreamException except) {
            handleError(except, path.string, source, errorTitle, "reading");
        }
    }
    void loadHandler(Component? source, String errorTitle) =>
            SPFileChooser.open(null).call(loadHandlerImpl(mapModel.setMap, source,
                errorTitle));
    shared actual void actionPerformed(ActionEvent event) {
        Component? source = as<Component>(event.source);
        variable String errorTitle = "Strategic Primer Assistive Programs";
        variable Component? iter = source;
        while (exists local = iter) {
            if (is ISPWindow local) {
                errorTitle = local.windowName;
                break;
            } else {
                iter = local.parent;
            }
        }
        switch (event.actionCommand.lowercased)
        case ("load") { // TODO: Open in another window if modified flag set instead of prompting before overwriting
            maybeSave("loading another map", as<Frame>(iter), source,
                defer(loadHandler, [source, errorTitle]));
        }
        case ("save") {
            if (exists givenFile = mapModel.mapFile) {
                try {
                    mapIOHelper.writeMap(parsePath(givenFile.string), mapModel.map);
                    mapModel.mapModified = false;
                } catch (IOException except) {
                    handleError(except, givenFile.string, source, errorTitle,
                        "writing to");
                }
            } else {
                actionPerformed(ActionEvent(event.source, event.id, "save as", event.when,
                    event.modifiers));
            }
        }
        case ("save as") {
            FileChooser.save(null, filteredFileChooser(false)).call((path) {
                try {
                    mapIOHelper.writeMap(parsePath(path.string), mapModel.map);
                    mapModel.mapFile = path;
                    mapModel.mapModified = false;
                } catch (IOException except) {
                    handleError(except, path.string, source, errorTitle, "writing to");
                }
            });
        }
        case ("new") {
            ViewerGUI().startDriverOnModel(cli, options, ViewerModel(SPMapNG(
                    mapModel.mapDimensions, PlayerCollection(), mapModel.map.currentTurn),
                null));
        }
        case ("load secondary") { // TODO: Investigate how various apps handle transitioning between no secondaries and one secondary map.
            if (is IMultiMapModel mapModel) {
                SPFileChooser.open(null).call(loadHandlerImpl(mapModel.addSubordinateMap,
                    source, errorTitle));
            }
        }
        case ("save all") {
            if (is IMultiMapModel mapModel) {
                for (map->[file, _] in mapModel.allMaps) {
                    if (exists file) {
                        try {
                            mapIOHelper.writeMap(parsePath(file.string), map);
                            mapModel.setModifiedFlag(map, false);
                        } catch (IOException except) {
                            handleError(except, file.string, source, errorTitle,
                                "writing to");
                        }
                    }
                }
            } else {
                actionPerformed(ActionEvent(event.source, event.id, "save", event.when,
                    event.modifiers));
            }
        }
        case ("open in map viewer") {
            ViewerGUI().startDriverOnModel(cli, options,
                ViewerModel.copyConstructor(mapModel));
        }
        case ("open secondary map in map viewer") {
            if (is IMultiMapModel mapModel,
                    exists mapEntry = mapModel.subordinateMaps.first) {
                ViewerGUI().startDriverOnModel(cli, options,
                    ViewerModel.fromEntry(mapEntry));
            }
        }
        case ("close") {
            if (is Frame local = iter) {
                maybeSave("closing", local, source, local.dispose);
            }
        }
        case ("quit") {
            maybeSave("quitting", as<Frame>(iter), source, quitHandler);
//            maybeSave("quitting", as<Frame>(iter), source, SPMenu.defaultQuit()); // TODO: switch to this once eclipse/ceylon#7396 fixed
        }
        else {
            log.info("Unhandled command ``event.actionCommand`` in IOHandler");
        }
    }
}
shared class SPFileChooser extends FileChooser {
    shared new open(JPath? loc = null,
            JFileChooser|JFileDialog fileChooser = IOHandler.filteredFileChooser(true))
            extends FileChooser.open(fileChooser, loc) {}
    shared new save(JPath? loc,
            JFileChooser|JFileDialog fileChooser = IOHandler.filteredFileChooser(false))
            extends FileChooser.save(loc, fileChooser) {}
    shared new custom(JPath? loc, String approveText,
            JFileChooser|JFileDialog fileChooser = IOHandler.filteredFileChooser(false))
            extends FileChooser.custom(loc, approveText, fileChooser) {}
}
