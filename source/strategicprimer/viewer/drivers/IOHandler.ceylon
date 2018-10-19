import strategicprimer.model.impl.xmlio {
    mapIOHelper
}
import strategicprimer.viewer.drivers.map_viewer {
    ViewerGUIFactory,
    ViewerModel,
    ViewerGUI
}
import java.awt.event {
    ActionEvent,
    ActionListener
}
import strategicprimer.drivers.gui.common {
    quitHandler,
    ISPWindow,
    SPFileChooser
}
import strategicprimer.model.common.xmlio {
    warningLevels,
    SPFormatException
}
import strategicprimer.model.common.map {
    PlayerCollection,
    IMutableMapNG,
    SPMapNG
}
import javax.xml.stream {
    XMLStreamException
}
import strategicprimer.drivers.common {
    ISPDriver,
    GUIDriver,
    ModelDriver,
    UtilityGUI,
    MultiMapGUIDriver
}
import lovelace.util.common {
    defer,
    PathWrapper,
    todo,
    as
}
import java.io {
    FileNotFoundException,
    IOException
}
import java.awt {
    Component,
    Frame
}
import java.nio.file {
    NoSuchFileException
}
import javax.swing {
    JOptionPane,
    SwingUtilities
}
import lovelace.util.jvm {
    showErrorDialog,
    FileChooser
}

"""A handler for "open" and "save" menu items (and a few others)"""
todo("Further splitting up", "Fix circular dependency between this and viewerGUI")
shared class IOHandler // TODO: reformat
        satisfies ActionListener {
    static ViewerGUIFactory vgf = ViewerGUIFactory();
    ISPDriver driver;
    shared new (ISPDriver driver) {
        this.driver = driver;
    }

    "If any files are marked as modified, ask the user whether to save them before
     closing/quitting."
    void maybeSave(String verb, Frame? window, Component? source,
            Anything() ifNotCanceled) {
        assert (is ModelDriver driver);
        if (driver.model.mapModified) {
            String prompt;
            if (is MultiMapGUIDriver driver) {
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

        if (is MultiMapGUIDriver driver, driver.model.subordinateMaps.map(Entry.item)
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

    void loadHandlerImpl(Anything(IMutableMapNG, PathWrapper) handler, Component? source,
            String errorTitle)(PathWrapper path) {
        try {
            handler(mapIOHelper.readMap(path, warningLevels.default), path);
        } catch (IOException|SPFormatException|XMLStreamException except) {
            handleError(except, path.string, source, errorTitle, "reading");
        }
    }

    void loadHandler(Component? source, String errorTitle) {
        if (is GUIDriver driver) {
            SPFileChooser.open(null).call(loadHandlerImpl(driver.open, source,
                errorTitle));
        } else if (is UtilityGUI driver) {
            SPFileChooser.open(null).call(driver.open);
        } else {
            log.error("IOHandler asked to 'load' in app that doesn't support that");
        }
    }

    shared actual void actionPerformed(ActionEvent event) {
        Component? source = as<Component>(event.source);
        variable String errorTitle = "Strategic Primer Assistive Programs";
        variable Component? iter = source;
        while (exists local = iter) { // TODO: Use ComponentParentStream to replace this loop with a one-liner and make 'iter' non-variable (rename it)
            if (is ISPWindow local) {
                errorTitle = local.windowName;
                break;
            } else {
                iter = local.parent;
            }
        }

        switch (event.actionCommand.lowercased)
        case ("load") { // TODO: Open in another window if modified flag set instead of prompting before overwriting
            if (is ModelDriver driver) {
                maybeSave("loading another map", as<Frame>(iter), source,
                    defer(loadHandler, [source, errorTitle]));
            } else if (is UtilityGUI driver) {
                loadHandler(source, errorTitle);
            } else {
                log.error("IOHandler asked to 'load' in unsupported app");
            }
        }

        case ("save") {
            if (is ModelDriver driver) {
                if (exists givenFile = driver.model.mapFile) {
                    try {
                        mapIOHelper.writeMap(givenFile, driver.model.map);
                        driver.model.mapModified = false;
                    } catch (IOException except) {
                        handleError(except, givenFile.string, source, errorTitle,
                            "writing to");
                    }
                } else {
                    actionPerformed(ActionEvent(event.source, event.id, "save as", event.when,
                        event.modifiers));
                }
            } else {
                log.error("IOHandler asked to save in driver it can't do that for");
            }
        }

        case ("save as") {
            if (is ModelDriver driver) {
                FileChooser.save(null, SPFileChooser.filteredFileChooser(false)).call((path) { // TODO: Why not use SPFileChooser.save()?
                    try {
                        mapIOHelper.writeMap(path, driver.model.map);
                        driver.model.mapFile = path;
                        driver.model.mapModified = false;
                    } catch (IOException except) {
                        handleError(except, path.string, source, errorTitle, "writing to");
                    }
                });
            } else {
                log.error("IOHandler asked to save-as in driver it can't do that for");
            }
        }

        case ("new") {
            if (is ModelDriver driver) {
                SwingUtilities.invokeLater(defer(compose(ViewerGUI.startDriver,
                    ViewerGUI), [vgf.createModel(SPMapNG(driver.model.mapDimensions,
                    PlayerCollection(), driver.model.map.currentTurn), null)]));
            } else {
                log.error("IOHandler asked to 'new' in driver it can't do that from");
            }
        }

        case ("load secondary") { // TODO: Investigate how various apps handle transitioning between no secondaries and one secondary map.
            if (is MultiMapGUIDriver driver) {
                SPFileChooser.open(null).call(loadHandlerImpl(
                    driver.model.addSubordinateMap, source, errorTitle));
            } else {
                log.error(
                    "IOHandler asked to 'load secondary' in driver it can't do that for");
            }
        }

        case ("save all") {
            if (is MultiMapGUIDriver driver) {
                for (map->[file, _] in driver.model.allMaps) {
                    if (exists file) {
                        try {
                            mapIOHelper.writeMap(file, map);
                            driver.model.setModifiedFlag(map, false);
                        } catch (IOException except) {
                            handleError(except, file.string, source, errorTitle,
                                "writing to");
                        }
                    }
                }
            } else if (is ModelDriver driver) {
                actionPerformed(ActionEvent(event.source, event.id, "save", event.when,
                    event.modifiers));
            } else {
                log.error("IOHandler asked to 'save all' in driver it can't do that for");
            }
        }

        case ("open in map viewer") {
            if (is ModelDriver driver) {
                SwingUtilities.invokeLater(defer(compose(ViewerGUI.startDriver,
                    ViewerGUI), [ViewerModel.copyConstructor(driver.model)]));
            } else {
                log.error(
                    "IOHandler asked to 'open in map viewer' in unsupported driver");
            }
        }

        case ("open secondary map in map viewer") {
            if (is MultiMapGUIDriver driver) {
                if (exists mapEntry = driver.model.subordinateMaps.first) {
                    SwingUtilities.invokeLater(defer(compose(ViewerGUI.startDriver,
                        ViewerGUI), [ViewerModel.fromEntry(mapEntry)]));
                } else {
                    log.error(
                        "IOHandler asked to 'open secondary in map viewer'; none there");
                }
            } else {
                log.error(
                    "IOHandler asked to 'open secondary in viewer' in unsupported app");
            }
        }

        case ("close") {
            if (is Frame local = iter) {
                if (is ModelDriver driver) {
                    maybeSave("closing", local, source, local.dispose);
                } else if (is UtilityGUI driver) {
                    local.dispose();
                } else {
                    log.error("IOHandler asked to close in unsupported app");
                }
            }
        }

        case ("quit") {
            if (is ModelDriver driver) {
                maybeSave("quitting", as<Frame>(iter), source, quitHandler.handler);
//              maybeSave("quitting", as<Frame>(iter), source, SPMenu.defaultQuit()); // TODO: switch to this once eclipse/ceylon#7396 fixed
            } else if (is UtilityGUI driver) {
                quitHandler.handler();
            } else {
                log.error("IOHandler asked to quit in unsupported app");
            }
        }

        else {
            log.info("Unhandled command ``event.actionCommand`` in IOHandler");
        }
    }
}
