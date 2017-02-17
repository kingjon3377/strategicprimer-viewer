import javax.swing {
    JFileChooser
}
import model.misc {
    IDriverModel,
    IMultiMapModel
}
import controller.map.misc {
    ICLIHelper,
    FileChooser
}
import lovelace.util.common {
    todo
}
import java.awt.event {
    ActionListener,
    ActionEvent
}
import view.util {
    FilteredFileChooser,
    ErrorShower
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
"""A handler for "open" and "save" menu items (and a few others)"""
todo("Further splitting up", "Fix circular dependency between this and viewerGUI")
class IOHandler(IDriverModel mapModel, SPOptions options, ICLIHelper cli,
        JFileChooser fileChooser = FilteredFileChooser()) satisfies ActionListener {
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
            ErrorShower.showErrorDialog(source, message);
        }
        switch (event.actionCommand.lowercased)
        case ("load") {
            FileChooser(JOptional.empty<JPath>()).call((path) {
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
                    ErrorShower.showErrorDialog(source,
                        "I/O error writing to ``givenFile.get()``");
                    log.error("I/O error writing XML", except);
                }
            } else {
                actionPerformed(ActionEvent(event.source, event.id, "save as", event.when,
                    event.modifiers));
            }
        }
        case ("save as") {
            FileChooser(JOptional.empty<JPath>(), fileChooser,
                    FileChooser.FileChooserOperation.save).call((path) {
                try {
                    writeMap(path, mapModel.map);
                } catch (IOException except) {
                    ErrorShower.showErrorDialog(source, "I/O error writing to ``path``");
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
                FileChooser(JOptional.empty<JPath>()).call((path) {
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
                            ErrorShower.showErrorDialog(source,
                                "I/O error writing to ``file.get()``");
                            log.error("I/O error writing XML", except);
                        }
                    }
                }
            } else {
                FileChooser(JOptional.empty<JPath>(), fileChooser,
                    FileChooser.FileChooserOperation.save).call((path) {
                    try {
                        writeMap(path, mapModel.map);
                    } catch (IOException except) {
                        ErrorShower.showErrorDialog(source, "I/O error writing to ``path``");
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