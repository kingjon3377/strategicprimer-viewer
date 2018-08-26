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
    todo,
    as
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
import ceylon.language.meta {
    type
}
import strategicprimer.drivers.gui.common {
    ISPWindow
}

"""A handler for "open" and "save" menu items (and a few others)"""
todo("Further splitting up", "Fix circular dependency between this and viewerGUI")
shared class IOHandler
        satisfies ActionListener {
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
                    mapModel.setMap(mapIOHelper
                        .readMap(path, warningLevels.default), path);
                } catch (IOException|SPFormatException|XMLStreamException except) {
                    handleError(except, path.string);
                }
            });
        }
        case ("save") {
            if (exists givenFile = mapModel.mapFile) {
                try {
                    mapIOHelper.writeMap(parsePath(givenFile.string), mapModel.map);
					mapModel.mapModified = false;
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
                    mapIOHelper.writeMap(parsePath(path.string), mapModel.map);
					// TODO: Set the model's filename for the map to this file, and clear modified flag
                } catch (IOException except) {
                    showErrorDialog(source, "Strategic Primer Assistive Programs",
                        "I/O error writing to ``path``");
                    log.error("I/O error writing XML", except);
                }
            });
        }
        case ("new") {
            ViewerGUI().startDriverOnModel(cli, options, ViewerModel(SPMapNG(
                    mapModel.mapDimensions, PlayerCollection(), mapModel.map.currentTurn),
                null));
        }
        case ("load secondary") {
            if (is IMultiMapModel mapModel) {
                FileChooser.open(null).call((path) {
                    try {
                        mapModel.addSubordinateMap(mapIOHelper
                            .readMap(path, warningLevels.default), path);
                    } catch (IOException|SPFormatException|XMLStreamException except) {
                        handleError(except, path.string);
                    }
                });
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
                            showErrorDialog(source, "Strategic Primer Assistive Programs",
                                "I/O error writing to ``file``");
                            log.error("I/O error writing XML", except);
                        }
                    }
                }
            } else {
                FileChooser.save(null, filteredFileChooser(false)).call((path) {
                    try {
                        mapIOHelper.writeMap(parsePath(path.string), mapModel.map);
						mapModel.mapModified = false;
                    } catch (IOException except) {
                        showErrorDialog(source, "Strategic Primer Assistive Programs",
                            "I/O error writing to ``path``");
                        log.error("I/O error writing XML", except);
                    }
                });
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
        else {
            log.info("Unhandled command ``event.actionCommand`` in IOHandler");
        }
    }
}
// TODO: Refactor and move to lovelace.util
shared class FileChooser {
    shared static class ChoiceInterruptedException(Throwable? cause = null)
            extends Exception(
                (cause exists) then "Choice of a file was iterrupted by an exception:"
                else "No file was selected", cause) { }
    static JPath fileToPath(JFile file) => file.toPath();
    Integer(Component?) chooserFunction;
    variable {JPath+}? storedFile;
    JFileChooser|JFileDialog chooser;
    shared new open(JPath? loc = null,
            JFileChooser|JFileDialog fileChooser = IOHandler.filteredFileChooser(true)) {
        log.trace("FileChooser invoked for the Open dialog");
        switch (fileChooser)
        case (is JFileChooser) {
            log.trace("Using Swing JFileChooser");
            chooserFunction = fileChooser.showOpenDialog;
            fileChooser.multiSelectionEnabled = true;
        }
        case (is JFileDialog) {
            log.trace("Using AWT FileDialog");
            fileChooser.mode = JFileDialog.load;
            chooserFunction = (Component? component) {
                fileChooser.setVisible(true);
                return 0;
            };
            fileChooser.multipleMode = true;
        }
        if (exists loc) {
            log.trace("A file was passed in");
            storedFile = Singleton(loc);
        } else {
            log.trace("No file was passed in");
            storedFile = null;
        }
        chooser = fileChooser;
    }
    shared new save(JPath? loc,
            JFileChooser|JFileDialog fileChooser = IOHandler.filteredFileChooser(false)) {
        log.trace("FileChooser invoked for Save dialog");
        switch (fileChooser)
        case (is JFileChooser) {
            log.trace("Using Swing JFileChooser");
            chooserFunction = fileChooser.showSaveDialog;
        }
        case (is JFileDialog) {
            log.trace("Using AWT FileDialog");
            fileChooser.mode = JFileDialog.save;
            chooserFunction = (Component? component) {
                fileChooser.setVisible(true);
                return 0;
            };
        }
        if (exists loc) {
            log.trace("A file was passed in");
            storedFile = Singleton(loc);
        } else {
            log.trace("No file was passed in");
            storedFile = null;
        }
        chooser = fileChooser;
    }
    shared new custom(JPath? loc, String approveText,
            JFileChooser|JFileDialog fileChooser = IOHandler.filteredFileChooser(false)) {
        log.trace("FileChooser invoked for a custom dialog");
        switch (fileChooser)
        case (is JFileChooser) {
            log.trace("Using Swing JFileChooser");
            chooserFunction = (Component? component) =>
                    fileChooser.showDialog(component, approveText);
        }
        case (is JFileDialog) {
            log.trace("Using AWT FileDialog, specifying Save");
            // Unfortunately, it's not possible to use a 'custom' action with the AWT
            // interface.
            fileChooser.mode = JFileDialog.save;
            chooserFunction = (Component? component) {
                fileChooser.setVisible(true);
                return 0;
            };
        }
        if (exists loc) {
            log.trace("A file was passed in");
            storedFile = Singleton(loc);
        } else {
            log.trace("No file was passed in");
            storedFile = null;
        }
        chooser = fileChooser;
    }
    void invoke(Anything() runnable) {
        try {
            log.trace("FileChooser.invoke(): About to invoke the provided function");
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
            log.trace("FileChooser.files: A file was stored, so returning it");
            return temp;
        } else if (SwingUtilities.eventDispatchThread) { // FIXME: Duplicated code between EDT and non-EDT cases.
            log.trace("FileChooser.files: Have to ask the user; on EDT");
            Integer status = chooserFunction(null);
            log.trace("FileChooser: The AWT or Swing chooser returned");
            if (is JFileChooser chooser) {
                if (status == JFileChooser.approveOption) {
                    value retval = chooser.selectedFiles.iterable.coalesced
                        .collect(fileToPath);
                    if (nonempty retval) {
                        log.trace("About to return the file(s) the user chose via Swing");
                        return retval;
                    } else {
                        log.info("User pressed approve but selected no files");
                    }
                } else {
                    log.info("Chooser function returned ``status``");
                }
            } else {
                value retval = chooser.files.iterable.coalesced.collect(fileToPath);
                if (nonempty retval) {
                    log.trace("About to return the file(s) the user chose via AWT");
                    return retval;
                } else {
                    log.info("User failed to choose?");
                    log.info("Returned iterable was ``retval`` (``type(retval)``");
                }
            }
        } else {
            log.trace("FileChooser.files: Have to ask the user; not yet on EDT");
            invoke(() {
                log.trace("Inside lambda on EDT");
                Integer status = chooserFunction(null);
                log.trace("FileChooser: The Swing or AWT chooser returned");
                if (is JFileChooser chooser) {
                    if (status == JFileChooser.approveOption) {
                        value retval = chooser.selectedFiles.iterable.coalesced
                            .collect(fileToPath);
                        if (nonempty retval) {
                            log.trace(
                                "Saving the user's choice(s) from Swing to storedFile");
                            storedFile = retval;
                        } else {
                            log.info("User pressed approve but selected no files");
                        }
                    } else {
                        log.info("Chooser function returned ``status``");
                    }
                } else {
                    value retval = chooser.files.iterable.coalesced
                        .collect(fileToPath);
                    if (nonempty retval) {
                        log.trace("Saving the user's choice(s) from AWT to storedFile");
                        storedFile = retval;
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
            files.each(consumer);
        } catch (ChoiceInterruptedException exception) {
            log.info("Choice interrupted or user failed to choose", exception);
        }
    }
}
