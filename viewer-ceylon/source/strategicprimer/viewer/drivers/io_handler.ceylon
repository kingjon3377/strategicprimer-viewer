import javax.swing {
    JFileChooser,
    SwingUtilities
}
import model.misc {
    IDriverModel,
    IMultiMapModel
}
import controller.map.misc {
    ICLIHelper
}
import lovelace.util.common {
    todo
}
import java.awt.event {
    ActionListener,
    ActionEvent
}
import java.awt {
    Component
}
import javax.xml.stream {
    XMLStreamException
}
import java.io {
    FileNotFoundException,
    IOException
}
import java.nio.file {
    NoSuchFileException, JPath = Path
}
import controller.map.formatexceptions {
    SPFormatException
}
import java.util {
    JOptional = Optional
}
import util {
    Warning
}
import model.viewer {
    ViewerModel
}
import model.map {
    PlayerCollection
}
import ceylon.interop.java {
    CeylonIterable
}
import strategicprimer.viewer.xmlio {
    readMap,
    writeMap
}
import java.lang.reflect {
    InvocationTargetException
}
import ceylon.language.meta.model {
    InvocationException
}
import java.lang {
    InterruptedException
}
import lovelace.util.jvm {
    showErrorDialog
}
import javax.swing.filechooser {
    FileNameExtensionFilter,
    FileFilter
}
import strategicprimer.viewer.drivers.map_viewer {
    viewerGUI
}
FileFilter mapExtensionsFilter = FileNameExtensionFilter(
    "Strategic Primer world map files", "map", "xml");
"A factory method for [[JFileChooser]] taking a [[FileFilter]] to apply in the same
 operation."
JFileChooser filteredFileChooser(
        "The current directory."
        String current = ".",
        "The filter to apply."
        FileFilter filter = mapExtensionsFilter) {
    JFileChooser retval = JFileChooser(current);
    retval.fileFilter = filter;
    return retval;
}
"""A handler for "open" and "save" menu items (and a few others)"""
todo("Further splitting up", "Fix circular dependency between this and viewerGUI")
shared class IOHandler(IDriverModel mapModel, SPOptions options, ICLIHelper cli,
        JFileChooser fileChooser = filteredFileChooser()) satisfies ActionListener {
    shared actual void actionPerformed(ActionEvent event) {
        value temp = event.source;
        Component? source;
        if (is Component temp) {
            source = temp;
        } else {
            source = null;
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
            // TODO: somehow make this more specific to the particular app
            showErrorDialog(source, "Strategic Primer Assistive Programs", message);
        }
        switch (event.actionCommand.lowercased)
        case ("load") {
            FileChooser.open(null).call((path) {
                try {
                    mapModel.setMap(readMap(path, Warning.default),
                        JOptional.\iof<JPath>(path));
                } catch (IOException|SPFormatException|XMLStreamException except) {
                    handleError(except, path.string);
                }
            });
        }
        case ("save") {
            JOptional<JPath> givenFile = mapModel.mapFile;
            if (givenFile.present) {
                try {
                    writeMap(givenFile.get(), mapModel.map);
                } catch (IOException except) {
                    showErrorDialog(source, "Strategic Primer Assistive Programs",
                        "I/O error writing to ``givenFile.get()``");
                    log.error("I/O error writing XML", except);
                }
            } else {
                actionPerformed(ActionEvent(event.source, event.id, "save as", event.when,
                    event.modifiers));
            }
        }
        case ("save as") {
            FileChooser.save(null, fileChooser).call((path) {
                try {
                    writeMap(path, mapModel.map);
                } catch (IOException except) {
                    showErrorDialog(source, "Strategic Primer Assistive Programs",
                        "I/O error writing to ``path``");
                    log.error("I/O error writing XML", except);
                }
            });
        }
        case ("new") {
            viewerGUI.startDriverOnModel(cli, options, ViewerModel(ConstructorWrapper.map(
                    mapModel.mapDimensions, PlayerCollection(), mapModel.map.currentTurn),
                JOptional.empty<JPath>()));
        }
        case ("load secondary") {
            if (is IMultiMapModel mapModel) {
                FileChooser.open(null).call((path) {
                    try {
                        mapModel.addSubordinateMap(readMap(path, Warning.default),
                            JOptional.\iof<JPath>(path));
                    } catch (IOException|SPFormatException|XMLStreamException except) {
                        handleError(except, path.string);
                    }
                });
            }
        }
        case ("save all") {
            if (is IMultiMapModel mapModel) {
                for (pair in mapModel.allMaps) {
                    JOptional<JPath> file = pair.second();
                    if (file.present) {
                        try {
                            writeMap(file.get(), pair.first());
                        } catch (IOException except) {
                            showErrorDialog(source, "Strategic Primer Assistive Programs",
                                "I/O error writing to ``file.get()``");
                            log.error("I/O error writing XML", except);
                        }
                    }
                }
            } else {
                FileChooser.save(null, fileChooser).call((path) {
                    try {
                        writeMap(path, mapModel.map);
                    } catch (IOException except) {
                        showErrorDialog(source, "Strategic Primer Assistive Programs",
                            "I/O error writing to ``path``");
                        log.error("I/O error writing XML", except);
                    }
                });
            }
        }
        case ("open in map viewer") {
            viewerGUI.startDriverOnModel(cli, options, ViewerModel(mapModel));
        }
        case ("open secondary map in map viewer") {
            if (is IMultiMapModel mapModel,
                    exists mapPair = CeylonIterable(mapModel.subordinateMaps).first) {
                viewerGUI.startDriverOnModel(cli, options, ViewerModel(mapPair));
            }
        }
        else {
            log.info("Unhandled command ``event.actionCommand`` in IOHandler");
        }
    }
}
class FileChooser {
    shared static class ChoiceInterruptedException(Throwable? cause = null)
            extends Exception(
                (cause exists) then "Choice of a file was iterrupted by an exception:"
                else "No file was selected", cause) { }
    Integer(Component?) chooserFunction;
    variable JPath? storedFile;
    JFileChooser chooser;
    shared new open(JPath? loc = null,
            JFileChooser fileChooser = filteredFileChooser()) {
        chooserFunction = fileChooser.showOpenDialog;
        storedFile = loc;
        chooser = fileChooser;
    }
    shared new save(JPath? loc, JFileChooser fileChooser = filteredFileChooser()) {
        chooserFunction = fileChooser.showSaveDialog;
        storedFile = loc;
        chooser = fileChooser;
    }
    shared new custom(JPath? loc, String approveText,
            JFileChooser fileChooser = filteredFileChooser()) {
        chooserFunction = (Component? component) =>
                fileChooser.showDialog(component, approveText);
        storedFile = loc;
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
    "If a valid filename was passed in to the constructor, return it; otherwise,
     show a dialog for the user to select one and return the filename the user selected.
     Throws an exception if the choice is interrupted or the user declines to choose."
    shared JPath file {
        if (exists temp = storedFile) {
            return temp;
        } else if (SwingUtilities.eventDispatchThread) {
            Integer status = chooserFunction(null);
            if (status == JFileChooser.approveOption) {
                assert (exists temp = chooser.selectedFile);
                return temp.toPath();
            } else {
                log.info("Chooser function returned ``status``");
            }
        } else {
            invoke(() {
                Integer status = chooserFunction(null);
                if (status == JFileChooser.approveOption) {
                    assert (exists temp = chooser.selectedFile);
                    storedFile = temp.toPath();
                } else {
                    log.info("Chooser function returned ``status``");
                }
            });
        }
        if (exists temp = storedFile) {
            return temp;
        } else {
            throw ChoiceInterruptedException();
        }
    }
    assign file {
        storedFile = file;
    }
    "Allow the user to choose a file, if necessary, and pass that file to the given
     consumer. If the operation is canceled, do nothing."
    shared void call(Anything(JPath) consumer) {
        try {
            consumer(file);
        } catch (ChoiceInterruptedException exception) {
            log.info("Choice interrupted or user failed to choose", exception);
        }
    }
}