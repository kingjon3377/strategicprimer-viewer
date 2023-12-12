package drivers;

import drivers.common.DriverFailedException;
import drivers.common.ViewerDriverFactory;
import drivers.common.cli.ICLIHelper;

import java.util.Objects;
import java.util.ServiceLoader;
import javax.xml.stream.XMLStreamException;

import legacy.map.LegacyPlayerCollection;
import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import drivers.gui.common.SPMenu;

import java.util.Optional;
import java.util.stream.StreamSupport;
import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.function.Consumer;

import legacy.xmlio.MapIOHelper;
import drivers.map_viewer.ViewerModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import drivers.gui.common.ISPWindow;
import drivers.gui.common.SPFileChooser;
import common.xmlio.Warning;
import common.xmlio.SPFormatException;
import legacy.map.ILegacyMap;
import common.map.PlayerCollection;
import legacy.map.IMutableLegacyMap;
import legacy.map.LegacyMap;

import drivers.common.IDriverModel;
import drivers.common.ISPDriver;
import drivers.common.GUIDriver;
import drivers.common.ModelDriver;
import drivers.common.SPOptions;
import drivers.common.UtilityGUI;
import drivers.common.MultiMapGUIDriver;

import java.io.IOException;
import java.awt.Component;
import java.awt.Frame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import lovelace.util.ComponentParentStream;

/**
 * A handler for "open" and "save" menu items (and a few others)
 *
 * TODO: Further splitting up
 */
public class IOHandler implements ActionListener {
    private final ISPDriver driver;
    private final ICLIHelper cli;

    public IOHandler(final ISPDriver driver, final ICLIHelper cli) {
        this.driver = driver;
        this.cli = cli;
    }

    /**
     * If any files are marked as modified, ask the user whether to save
     * them before closing/quitting.
     */
    private void maybeSave(final String verb, final @Nullable Frame window, final Component source,
                           final Runnable ifNotCanceled) { // TODO: Use the possibly-throwing interface from j.u.concurrent?
        final ModelDriver md = (ModelDriver) driver;
        LovelaceLogger.trace("Checking if we need to save ...");
        if (md.getModel().isMapModified()) {
            LovelaceLogger.trace("main map was modified.");
            final String prompt;
            if (md instanceof MultiMapGUIDriver) {
                prompt = "Save changes to main map before %s?";
            } else {
                prompt = "Save changes to map before %s?";
            }
            final int answer = JOptionPane.showConfirmDialog(window, String.format(prompt, verb),
                    "Save Changes?", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (answer == JOptionPane.CANCEL_OPTION) {
                LovelaceLogger.trace("User selected 'Cancel'");
                return;
            } else if (answer == JOptionPane.YES_OPTION) {
                LovelaceLogger.trace("User selected 'Yes'; invoking 'Save' menu ...");
                actionPerformed(new ActionEvent(source, ActionEvent.ACTION_FIRST, "save"));
                LovelaceLogger.trace("Finished saving main map");
            } else {
                LovelaceLogger.trace("User said not to save main map");
            }
        } else {
            LovelaceLogger.trace("main map was not modified");
        }

        if (md instanceof final MultiMapGUIDriver mmgd && mmgd.getModel().streamSubordinateMaps()
                .anyMatch(ILegacyMap::isModified)) {
            LovelaceLogger.trace("Subordinate map(s) modified.");
            final int answer = JOptionPane.showConfirmDialog(window,
                    String.format("Subordinate map(s) have unsaved changes. Save all before %s?",
                            verb), "Save Changes?", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (answer == JOptionPane.CANCEL_OPTION) {
                LovelaceLogger.trace("User selected 'Cancel' rather than save-all.");
                return;
            } else if (answer == JOptionPane.YES_OPTION) {
                LovelaceLogger.trace("User selected 'Yes'; invoking 'Save All' menu ...");
                actionPerformed(new ActionEvent(source, ActionEvent.ACTION_FIRST,
                        "save all"));
                LovelaceLogger.trace("Finished saving");
            } else {
                LovelaceLogger.trace("User said not to save subordinate maps");
            }
        } else {
            LovelaceLogger.trace("No subordinate maps modified");
        }
        LovelaceLogger.trace("About to carry out action ...");
        ifNotCanceled.run();
    }

    private static void handleError(final Exception except, final String filename, final @Nullable Component source,
                                    final String errorTitle, final String verb) {
        final String message;
        if (except instanceof XMLStreamException) {
            message = "Malformed XML in " + filename;
        } else if (except instanceof FileNotFoundException || except instanceof NoSuchFileException) {
            message = String.format("File %s not found", filename);
        } else if (except instanceof IOException) {
            message = String.format("I/O error %s file %s", verb, filename);
        } else if (except instanceof SPFormatException) {
            message = "SP map format error in " + filename;
        } else {
            message = except.getMessage();
        }
        LovelaceLogger.error(except, message);
        JOptionPane.showMessageDialog(source, message, errorTitle, JOptionPane.ERROR_MESSAGE);
    }

    private static Consumer<Path> loadHandlerImpl(final Consumer<IMutableLegacyMap> handler, final @Nullable Component source,
												  final String errorTitle) {
        return path -> {
            try {
                handler.accept(MapIOHelper.readMap(path, Warning.getDefaultHandler()));
            } catch (final IOException | SPFormatException | XMLStreamException except) {
                handleError(except, path.toString(), source, errorTitle, "reading");
            }
        };
    }

    private void loadHandler(final @Nullable Component source, final String errorTitle) {
        if (driver instanceof final GUIDriver gd) {
            SPFileChooser.open((Path) null)
                    .call(loadHandlerImpl(gd::open, source, errorTitle));
        } else if (driver instanceof final UtilityGUI ug) {
            SPFileChooser.open((Path) null).call(ug::open);
        } else {
            LovelaceLogger.error("IOHandler asked to 'load' in app that doesn't support that");
        }
    }

    /**
     * Start a new map-viewer window for a new map with the same dimensions
     * as that represented by the given driver's model. Should be run on
     * the EDT, i.e. using {@link SwingUtilities#invokeLater}.
     */
    private void startNewViewerWindow(final ModelDriver driver) {
        final ViewerDriverFactory vdf =
                StreamSupport.stream(ServiceLoader.load(ViewerDriverFactory.class).spliterator(), false)
                        .findAny().orElse(null);
        if (vdf == null) {
            // FIXME: Show error dialog
            LovelaceLogger.error("Map viewer was not included in this assembly, or service discovery failed");
        } else {
            try {
                vdf.createDriver(cli, driver.getOptions().copy(),
                                new ViewerModel(new LegacyMap(driver.getModel().getMapDimensions(),
                                        new LegacyPlayerCollection(),
                                        driver.getModel().getMap().getCurrentTurn())))
                        .startDriver();
            } catch (final DriverFailedException except) {
                // FIXME: show error dialog
                LovelaceLogger.error(except, "Driver failed");
            }
        }
    }

    private void openSecondaryInViewer(final IDriverModel model, final SPOptions options) {
        final ViewerDriverFactory vdf =
                StreamSupport.stream(ServiceLoader.load(ViewerDriverFactory.class).spliterator(), false)
                        .findAny().orElse(null);
        if (vdf == null) {
            // FIXME: Show error dialog
            LovelaceLogger.error("Map viewer was not included in this assembly, or service discovery failed");
        } else {
            try {
                vdf.createDriver(cli, options.copy(), new ViewerModel(model)).startDriver();
            } catch (final DriverFailedException except) {
                // FIXME: show error dialog
                LovelaceLogger.error(except, "Driver failed");
            }
        }
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        final Component source = Optional.ofNullable(event.getSource())
                .filter(Component.class::isInstance).map(Component.class::cast)
			.orElseThrow(() -> new IllegalStateException("Can't get source of event"));
        final Frame parentWindow = new ComponentParentStream(source).stream()
                    .filter(Frame.class::isInstance).map(Frame.class::cast)
                    .findFirst().orElse(null);
        final String errorTitle = Optional.ofNullable(parentWindow).filter(ISPWindow.class::isInstance)
                .map(ISPWindow.class::cast).map(ISPWindow::getWindowName)
                .orElse("Strategic Primer Assistive Programs");

        LovelaceLogger.trace("Menu item invoked: %s", event.getActionCommand());

        switch (event.getActionCommand().toLowerCase()) {
            case "load":
                // TODO: Open in another window if 'modified' instead of prompting before overwriting
                if (driver instanceof ModelDriver) {
                    maybeSave("loading another map", parentWindow, source,
                            () -> loadHandler(source, errorTitle));
                } else if (driver instanceof UtilityGUI) {
                    loadHandler(source, errorTitle);
                } else {
                    LovelaceLogger.error("IOHandler asked to 'load' in unsupported app");
                }
                break;

            case "save":
                if (driver instanceof final ModelDriver md) {
                    final Path givenFile = md.getModel().getMap().getFilename();
                    if (givenFile == null) {
                        actionPerformed(new ActionEvent(event.getSource(), event.getID(),
                                "save as", event.getWhen(), event.getModifiers()));
                    } else {
                        try {
                            MapIOHelper.writeMap(givenFile, md.getModel().getMap());
                            md.getModel().setMapModified(false);
                        } catch (final IOException | XMLStreamException except) {
                            handleError(except, givenFile.toString(), source, errorTitle,
                                    "writing to");
                        }
                    }
                } else {
                    LovelaceLogger.error("IOHandler asked to save in driver it can't do that for");
                }
                break;

            case "save as":
                if (driver instanceof final ModelDriver md) {
                    SPFileChooser.save((Path) null).call(path -> {
                        try {
                            MapIOHelper.writeMap(path, md.getModel().getMap());
                            md.getModel().setMapFilename(path);
                            md.getModel().setMapModified(false);
                        } catch (final IOException | XMLStreamException except) {
                            handleError(except, path.toString(), source,
                                    errorTitle, "writing to");
                        }
                    });
                } else {
                    LovelaceLogger.error("IOHandler asked to save-as in driver it can't do that for");
                }
                break;

            case "new":
                if (driver instanceof final ModelDriver md) {
                    SwingUtilities.invokeLater(() -> startNewViewerWindow(md));
                } else {
                    LovelaceLogger.error("IOHandler asked to 'new' in driver it can't do that from");
                }
                break;

            case "load secondary":
                // TODO: Investigate how various apps handle transitioning between no secondaries
                // and one secondary map.
                if (driver instanceof final MultiMapGUIDriver mmgd) {
                    SPFileChooser.open((Path) null).call(loadHandlerImpl(
                            mmgd.getModel()::addSubordinateMap,
                            source, errorTitle));
                } else {
                    LovelaceLogger.error(
                            "IOHandler asked to 'load secondary' in driver it can't do that for");
                }
                break;

            case "save all":
                if (driver instanceof final MultiMapGUIDriver mmgd) {
                    for (final ILegacyMap map : mmgd.getModel().getAllMaps()) {
                        // FIXME: Doesn't MapIOHelper have a method for this?
                        final Path file = map.getFilename();
                        if (file != null) {
                            try {
                                MapIOHelper.writeMap(file, map);
                                mmgd.getModel().clearModifiedFlag(map);
                            } catch (final IOException | XMLStreamException except) {
                                handleError(except, file.toString(), source,
                                        errorTitle, "writing to");
                            }
                        }
                    }
                } else if (driver instanceof ModelDriver) {
                    actionPerformed(new ActionEvent(event.getSource(), event.getID(),
                            "save", event.getWhen(), event.getModifiers()));
                } else {
                    LovelaceLogger.error("IOHandler asked to 'save all' in driver it can't do that for");
                }
                break;

            case "open in map viewer":
                if (driver instanceof final ModelDriver md) {
                    final ViewerDriverFactory vdf =
                            StreamSupport.stream(ServiceLoader.load(ViewerDriverFactory.class).spliterator(), false)
                                    .findAny().orElse(null);
                    if (vdf == null) {
                        JOptionPane.showMessageDialog(null,
							"Either the map viewer was not included in this edition of the assistive programs, or the logic to load it failed.",
							"Strategic Primer Assistive Programs", JOptionPane.ERROR_MESSAGE);
                        LovelaceLogger.error("Map viewer was not included in this assembly, or service discovery failed");
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            try {
                                vdf.createDriver(cli, driver.getOptions().copy(), new ViewerModel(md.getModel()))
                                        .startDriver();
                            } catch (final DriverFailedException except) {
                                LovelaceLogger.error(Objects.requireNonNullElse(except.getCause(), except),
                                        "Error thrown from viewer driver");
								JOptionPane.showMessageDialog(null,
									String.format("Error starting map viewer:%n%s", except.getMessage()),
									"Strategic Primer Assistive Programs", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }
                } else {
                    LovelaceLogger.error(
                            "IOHandler asked to 'open in map viewer' in unsupported driver");
                }
                break;

            case "open secondary map in map viewer":
                if (driver instanceof final MultiMapGUIDriver mmgd) {
                    final IDriverModel newModel = mmgd.getModel().fromSecondMap();
                    if (newModel == null) {
                        LovelaceLogger.error(
                                "IOHandler asked to 'open secondary in map viewer'; none there");
                    } else {
                        SwingUtilities.invokeLater(() -> openSecondaryInViewer(newModel,
                                driver.getOptions()));
                    }
                } else {
                    LovelaceLogger.error(
                            "IOHandler asked to 'open secondary in viewer' in unsupported app");
                }
                break;

            case "close":
                if (parentWindow == null) {
                    LovelaceLogger.error("IOHandler asked to close but couldn't get current window");
                    LovelaceLogger.debug("Event details: %s", event);
                    LovelaceLogger.debug("Source: %s", source);
                    LovelaceLogger.trace(new Exception("Stack trace"), "Stack trace:");
                } else {
                    if (driver instanceof ModelDriver) {
                        LovelaceLogger.trace("This operates on a model, maybe we should save ...");
                        maybeSave("closing", parentWindow, source, parentWindow::dispose);
                    } else if (driver instanceof UtilityGUI) {
                        LovelaceLogger.trace("This is a utility GUI, so we just dispose the window.");
                        parentWindow.dispose();
                    } else {
                        LovelaceLogger.error("IOHandler asked to close in unsupported app");
                    }
                }
                break;

            case "quit":
                if (driver instanceof ModelDriver) {
                    maybeSave("quitting", parentWindow, source, SPMenu.getDefaultQuit());
                } else if (driver instanceof UtilityGUI) {
                    SPMenu.getDefaultQuit().run();
                } else {
                    LovelaceLogger.error("IOHandler asked to quit in unsupported app");
                }
                break;

            default:
                LovelaceLogger.info("Unhandled command %s in IOHandler",
                        event.getActionCommand());
                break;
        }
    }
}
