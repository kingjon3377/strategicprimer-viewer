import ceylon.language.meta.model {
    InvocationException
}

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
import java.lang {
    InterruptedException
}
import java.lang.reflect {
    InvocationTargetException
}
import java.nio.file {
    NoSuchFileException,
    JPath=Path
}

import javax.swing {
    JFileChooser,
    SwingUtilities
}
import javax.swing.filechooser {
    FileNameExtensionFilter,
    FileFilter
}
import javax.xml.stream {
    XMLStreamException
}

import lovelace.util.common {
    todo
}
import lovelace.util.jvm {
    showErrorDialog,
    platform
}

import strategicprimer.model.map {
    SPMapNG,
    PlayerCollection
}
import strategicprimer.model.xmlio {
    readMap,
    writeMap,
    warningLevels,
    SPFormatException
}
import strategicprimer.viewer.drivers.map_viewer {
    viewerGUI,
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
import ceylon.language.meta {
    type
}

FileFilter mapExtensionsFilter = FileNameExtensionFilter(
    "Strategic Primer world map files", "map", "xml");
"A factory method for [[JFileChooser]] taking a [[FileFilter]] to apply in the same
 operation."
JFileChooser|JFileDialog filteredFileChooser(
        "Whether to allow multi-selection."
        Boolean allowMultiple,
        "The current directory."
        String current = ".",
        "The filter to apply."
        FileFilter filter = mapExtensionsFilter) {
    if (platform.systemIsMac) {
        JFileDialog retval = JFileDialog(null of Frame?);
        retval.filenameFilter = object satisfies FilenameFilter {
            shared actual Boolean accept(JFile dir, String name) =>
                    filter.accept(JFile(dir, name));
        };
        return retval;
    } else {
        JFileChooser retval = JFileChooser(current);
        retval.fileFilter = filter;
        return retval;
    }
}
"""A handler for "open" and "save" menu items (and a few others)"""
todo("Further splitting up", "Fix circular dependency between this and viewerGUI")
shared class IOHandler(IDriverModel mapModel, SPOptions options, ICLIHelper cli)
        satisfies ActionListener {
    shared actual void actionPerformed(ActionEvent event) {
        value temp = event.source;
        Component? source;
        if (is Component temp) {
            source = temp;
        } else {
            source = null;
        }
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
        void handleError(Exception except, String filename) {
            String message;
            if (is XMLStreamException except) {
                message = "Malformed XML in ``filename``";
            } else if (is FileNotFoundException|NoSuchFileException except) {
                message = "File ``filename`` not found";
            } else if (is IOException except) {
                message = "I/O error reading file ``filename``";
            } else if (is SPFormatException except) {
                message = "SP map format error in ``filename``";
            } else {
                message = except.message;
            }
            log.error(message, except);
            showErrorDialog(source, errorTitle, message);
        }
        switch (event.actionCommand.lowercased)
        case ("load") {
            FileChooser.open(null).call((path) {
                try {
                    mapModel.setMap(readMap(path, warningLevels.default), path);
                } catch (IOException|SPFormatException|XMLStreamException except) {
                    handleError(except, path.string);
                }
            });
        }
        case ("save") {
            if (exists givenFile = mapModel.mapFile) {
                try {
                    writeMap(parsePath(givenFile.string), mapModel.map);
                } catch (IOException except) {
                    showErrorDialog(source, "Strategic Primer Assistive Programs",
                        "I/O error writing to ``givenFile``");
                    log.error("I/O error writing XML", except);
                }
            } else {
                actionPerformed(ActionEvent(event.source, event.id, "save as", event.when,
                    event.modifiers));
            }
        }
        case ("save as") {
            FileChooser.save(null, filteredFileChooser(false)).call((path) {
                try {
                    writeMap(parsePath(path.string), mapModel.map);
                } catch (IOException except) {
                    showErrorDialog(source, "Strategic Primer Assistive Programs",
                        "I/O error writing to ``path``");
                    log.error("I/O error writing XML", except);
                }
            });
        }
        case ("new") {
            viewerGUI.startDriverOnModel(cli, options, ViewerModel(SPMapNG(
                    mapModel.mapDimensions, PlayerCollection(), mapModel.map.currentTurn),
                null));
        }
        case ("load secondary") {
            if (is IMultiMapModel mapModel) {
                FileChooser.open(null).call((path) {
                    try {
                        mapModel.addSubordinateMap(readMap(path, warningLevels.default),
                            path);
                    } catch (IOException|SPFormatException|XMLStreamException except) {
                        handleError(except, path.string);
                    }
                });
            }
        }
        case ("save all") {
            if (is IMultiMapModel mapModel) {
                for ([map, file] in mapModel.allMaps) {
                    if (exists file) {
                        try {
                            writeMap(parsePath(file.string), map);
                        } catch (IOException except) {
                            showErrorDialog(source, "Strategic Primer Assistive Programs",
                                "I/O error writing to ``file``");
                            log.error("I/O error writing XML", except);
                        }
                    }
                }
            } else {
                FileChooser.save(null, filteredFileChooser(false)).call((path) {
                    try {
                        writeMap(parsePath(path.string), mapModel.map);
                    } catch (IOException except) {
                        showErrorDialog(source, "Strategic Primer Assistive Programs",
                            "I/O error writing to ``path``");
                        log.error("I/O error writing XML", except);
                    }
                });
            }
        }
        case ("open in map viewer") {
            viewerGUI.startDriverOnModel(cli, options,
                ViewerModel.copyConstructor(mapModel));
        }
        case ("open secondary map in map viewer") {
            if (is IMultiMapModel mapModel,
                    exists mapPair = mapModel.subordinateMaps.first) {
                viewerGUI.startDriverOnModel(cli, options, ViewerModel.fromPair(mapPair));
            }
        }
        else {
            log.info("Unhandled command ``event.actionCommand`` in IOHandler");
        }
    }
}
shared class FileChooser {
    shared static class ChoiceInterruptedException(Throwable? cause = null)
            extends Exception(
                (cause exists) then "Choice of a file was iterrupted by an exception:"
                else "No file was selected", cause) { }
    Integer(Component?) chooserFunction;
    variable {JPath+}? storedFile;
    JFileChooser|JFileDialog chooser;
    shared new open(JPath? loc = null,
            JFileChooser|JFileDialog fileChooser = filteredFileChooser(true)) {
        switch (fileChooser)
        case (is JFileChooser) {
            chooserFunction = fileChooser.showOpenDialog;
            fileChooser.multiSelectionEnabled = true;
        }
        case (is JFileDialog) {
            fileChooser.mode = JFileDialog.load;
            chooserFunction = (Component? component) {
                fileChooser.setVisible(true);
                return 0;
            };
            fileChooser.multipleMode = true;
        }
        if (exists loc) {
            storedFile = {loc};
        } else {
            storedFile = null;
        }
        chooser = fileChooser;
    }
    shared new save(JPath? loc,
            JFileChooser|JFileDialog fileChooser = filteredFileChooser(false)) {
        switch (fileChooser)
        case (is JFileChooser) {
            chooserFunction = fileChooser.showSaveDialog;
        }
        case (is JFileDialog) {
            fileChooser.mode = JFileDialog.save;
            chooserFunction = (Component? component) {
                fileChooser.setVisible(true);
                return 0;
            };
        }
        if (exists loc) {
            storedFile = {loc};
        } else {
            storedFile = null;
        }
        chooser = fileChooser;
    }
    shared new custom(JPath? loc, String approveText,
            JFileChooser|JFileDialog fileChooser = filteredFileChooser(false)) {
        switch (fileChooser)
        case (is JFileChooser) {
            chooserFunction = (Component? component) =>
                    fileChooser.showDialog(component, approveText);
        }
        case (is JFileDialog) {
            // Unfortunately, it's not possible to use a 'custom' action with the AWT
            // interface.
            fileChooser.mode = JFileDialog.save;
            chooserFunction = (Component? component) {
                fileChooser.setVisible(true);
                return 0;
            };
        }
        if (exists loc) {
            storedFile = {loc};
        } else {
            storedFile = null;
        }
        chooser = fileChooser;
    }
    void invoke(Anything() runnable) {
        try {
            SwingUtilities.invokeAndWait(runnable);
        } catch (InvocationTargetException|InvocationException except) {
            if (exists cause = except.cause) {
                throw ChoiceInterruptedException(cause);
            } else {
                throw ChoiceInterruptedException(except);
            }
        } catch (InterruptedException except) {
            throw ChoiceInterruptedException(except);
        }
    }
    "If a valid filename was, or multiple filenames were, passed in to the constructor,
     return an iterable containing it or them; otherwise, show a dialog for the user to
     select one or more filenames and return the filename(s) the user selected. Throws an
     exception if the choice is interrupted or the user declines to choose."
    shared {JPath+} files {
        if (exists temp = storedFile) {
            return temp;
        } else if (SwingUtilities.eventDispatchThread) {
            Integer status = chooserFunction(null);
            if (is JFileChooser chooser) {
                if (status == JFileChooser.approveOption) {
                    value retval = chooser.selectedFiles.iterable.coalesced
                        .map((file) => file.toPath());
                    if (exists first = retval.first) {
                        return { first, *retval.rest };
                    } else {
                        log.info("User pressed approve but selected no files");
                    }
                } else {
                    log.info("Chooser function returned ``status``");
                }
            } else {
                value retval = chooser.files.iterable.coalesced
                    .map((file) => file.toPath());
                if (exists first = retval.first) {
                    return { first, *retval.rest };
                } else {
                    log.info("User failed to choose?");
                    log.info("Returned iterable was ``retval`` (``type(retval)``");
                }
            }
        } else {
            invoke(() {
                Integer status = chooserFunction(null);
                if (is JFileChooser chooser) {
                    if (status == JFileChooser.approveOption) {
                        value retval = chooser.selectedFiles.iterable.coalesced
                            .map((file) => file.toPath());
                        if (exists first = retval.first) {
                            storedFile = { first, *retval.rest };
                        } else {
                            log.info("User pressed approve but selected no files");
                        }
                    } else {
                        log.info("Chooser function returned ``status``");
                    }
                } else {
                    value retval = chooser.files.iterable.coalesced
                        .map((file) => file.toPath());
                    if (exists first = retval.first) {
                        storedFile = { first, *retval.rest };
                    } else {
                        log.info("User failed to choose?");
                        log.info("Returned iterable was ``retval`` (``type(retval)``");
                    }
                }
            });
        }
        if (exists temp = storedFile) {
            return temp;
        } else {
            throw ChoiceInterruptedException();
        }
    }
    assign files {
        storedFile = files;
    }
    "Allow the user to choose a file or files, if necessary, and pass each file to the
     given consumer. If the operation is canceled, do nothing."
    shared void call(Anything(JPath) consumer) {
        try {
            for (file in files) {
                consumer(file);
            }
        } catch (ChoiceInterruptedException exception) {
            log.info("Choice interrupted or user failed to choose", exception);
        }
    }
}