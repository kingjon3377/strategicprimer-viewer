package drivers;

import drivers.common.DriverFailedException;
import drivers.common.ViewerDriverFactory;
import drivers.common.cli.ICLIHelper;
import java.util.ServiceLoader;
import lovelace.util.TriFunction;
import lovelace.util.MissingFileException;
import org.jetbrains.annotations.Nullable;

import drivers.gui.common.SPMenu;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.StreamSupport;
import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.util.logging.Logger;
import java.nio.file.Path;
import java.util.function.Consumer;
import impl.xmlio.MapIOHelper;
import drivers.map_viewer.ViewerModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import drivers.gui.common.ISPWindow;
import drivers.gui.common.SPFileChooser;
import common.xmlio.Warning;
import common.xmlio.SPFormatException;
import common.map.IMapNG;
import common.map.PlayerCollection;
import common.map.IMutableMapNG;
import common.map.SPMapNG;

import drivers.common.IDriverModel;
import drivers.common.ISPDriver;
import drivers.common.GUIDriver;
import drivers.common.ModelDriver;
import drivers.common.SPOptions;
import drivers.common.UtilityGUI;
import drivers.common.MultiMapGUIDriver;
import lovelace.util.MalformedXMLException;
import java.io.IOException;
import java.awt.Component;
import java.awt.Frame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lovelace.util.ShowErrorDialog;
import lovelace.util.ComponentParentStream;

/**
 * A handler for "open" and "save" menu items (and a few others)
 *
 * TODO: Further splitting up
 *
 * TODO: Fix circular dependency between this and viewerGUI
 */
public class IOHandler implements ActionListener {
	private static final Logger LOGGER = Logger.getLogger(IOHandler.class.getName());
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
	private void maybeSave(final String verb, @Nullable final Frame window, @Nullable final Component source,
	                       final Runnable ifNotCanceled) { // TODO: Use the possibly-throwing interface from j.u.concurrent?
		final ModelDriver md = (ModelDriver) driver;
		LOGGER.finer("Checking if we need to save ...");
		if (md.getModel().isMapModified()) {
			LOGGER.finer("main map was modified.");
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
				LOGGER.finer("User selected 'Cancel'");
				return;
			} else if (answer == JOptionPane.YES_OPTION) {
				LOGGER.finer("User selected 'Yes'; invoking 'Save' menu ...");
				actionPerformed(new ActionEvent(source, ActionEvent.ACTION_FIRST, "save"));
				LOGGER.finer("Finished saving main map");
			} else {
				LOGGER.finer("User said not to save main map");
			}
		} else {
			LOGGER.finer("main map was not modified");
		}

		if (md instanceof MultiMapGUIDriver &&
				    ((MultiMapGUIDriver) md).getModel().streamSubordinateMaps()
						    .anyMatch(IMapNG::isModified)) {
			LOGGER.finer("Subordinate map(s) modified.");
			final int answer = JOptionPane.showConfirmDialog(window,
				String.format("Subordinate map(s) have unsaved changes. Save all before %s?",
					verb), "Save Changes?", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);
			if (answer == JOptionPane.CANCEL_OPTION) {
				LOGGER.finer("User selected 'Cancel' rather than save-all.");
				return;
			} else if (answer == JOptionPane.YES_OPTION) {
				LOGGER.finer("User selected 'Yes'; invoking 'Save All' menu ...");
				actionPerformed(new ActionEvent(source, ActionEvent.ACTION_FIRST,
					"save all"));
				LOGGER.finer("Finished saving");
			} else {
				LOGGER.finer("User said not to save subordinate maps");
			}
		} else {
			LOGGER.finer("No subordinate maps modified");
		}
		LOGGER.finer("About to carry out action ...");
		ifNotCanceled.run();
	}

	private void handleError(final Exception except, final String filename, @Nullable final Component source,
	                         final String errorTitle, final String verb) {
		final String message;
		if (except instanceof MalformedXMLException) {
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
		LOGGER.log(Level.SEVERE, message, except);
		ShowErrorDialog.showErrorDialog(source, errorTitle, message);
	}

	private Consumer<Path> loadHandlerImpl(final Consumer<IMutableMapNG> handler, @Nullable final Component source,
	                                       final String errorTitle) {
		return path -> {
			try {
				handler.accept(MapIOHelper.readMap(path, Warning.getDefaultHandler()));
			} catch (final MissingFileException|IOException|SPFormatException|MalformedXMLException except) {
				handleError(except, path.toString(), source, errorTitle, "reading");
			}
		};
	}

	private void loadHandler(@Nullable final Component source, final String errorTitle) {
		if (driver instanceof GUIDriver) {
			SPFileChooser.open((Path) null)
				.call(loadHandlerImpl(((GUIDriver) driver)::open, source,
				errorTitle));
		} else if (driver instanceof UtilityGUI) {
			SPFileChooser.open((Path) null).call(((UtilityGUI) driver)::open);
		} else {
			LOGGER.severe("IOHandler asked to 'load' in app that doesn't support that");
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
			LOGGER.severe("Map viewer was not included in this assembly, or service discovery failed");
		} else {
			try {
				vdf.createDriver(cli, driver.getOptions().copy(),
								new ViewerModel(new SPMapNG(driver.getModel().getMapDimensions(),
										new PlayerCollection(),
										driver.getModel().getMap().getCurrentTurn())))
						.startDriver();
			} catch (final DriverFailedException except) {
				// FIXME: show error dialog
				LOGGER.log(Level.SEVERE, "Driver failed", except);
			}
		}
	}

	private void openSecondaryInViewer(final IDriverModel model, final SPOptions options) {
		final ViewerDriverFactory vdf =
				StreamSupport.stream(ServiceLoader.load(ViewerDriverFactory.class).spliterator(), false)
						.findAny().orElse(null);
		if (vdf == null) {
			// FIXME: Show error dialog
			LOGGER.severe("Map viewer was not included in this assembly, or service discovery failed");
		} else {
			try {
				vdf.createDriver(cli, options.copy(), new ViewerModel(model)).startDriver();
			} catch (final DriverFailedException except) {
				// FIXME: show error dialog
				LOGGER.log(Level.SEVERE, "Driver failed", except);
			}
		}
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		final Component source = Optional.ofNullable(event.getSource())
			.filter(Component.class::isInstance).map(Component.class::cast).orElse(null);
		final Frame parentWindow;
		if (source == null) {
			parentWindow = null;
		} else {
			parentWindow = StreamSupport.stream(new ComponentParentStream(source).spliterator(),
				false).filter(Frame.class::isInstance).map(Frame.class::cast)
				.findFirst().orElse(null);
		}
		final String errorTitle = Optional.ofNullable(parentWindow).filter(ISPWindow.class::isInstance)
			.map(ISPWindow.class::cast).map(ISPWindow::getWindowName)
			.orElse("Strategic Primer Assistive Programs");

		LOGGER.finer("Menu item invoked: " + event.getActionCommand());

		switch (event.getActionCommand().toLowerCase()) {
		case "load":
			// TODO: Open in another window if 'modified' instead of prompting before overwriting
			if (driver instanceof ModelDriver) {
				maybeSave("loading another map", parentWindow, source,
					() -> loadHandler(source, errorTitle));
			} else if (driver instanceof UtilityGUI) {
				loadHandler(source, errorTitle);
			} else {
				LOGGER.severe("IOHandler asked to 'load' in unsupported app");
			}
			break;

		case "save":
			if (driver instanceof ModelDriver) {
				final ModelDriver md = (ModelDriver) driver;
				final Path givenFile = md.getModel().getMap().getFilename();
				if (givenFile != null) {
					try {
						MapIOHelper.writeMap(givenFile, md.getModel().getMap());
						md.getModel().setMapModified(false);
					} catch (final IOException|MalformedXMLException except) {
						handleError(except, givenFile.toString(), source, errorTitle,
							"writing to");
					}
				} else {
					actionPerformed(new ActionEvent(event.getSource(), event.getID(),
						"save as", event.getWhen(), event.getModifiers()));
				}
			} else {
				LOGGER.severe("IOHandler asked to save in driver it can't do that for");
			}
			break;

		case "save as":
			if (driver instanceof ModelDriver) {
				final ModelDriver md = (ModelDriver) driver;
				SPFileChooser.save((Path) null).call(path -> {
					try {
							MapIOHelper.writeMap(path, md.getModel().getMap());
							md.getModel().setMapFilename(path);
							md.getModel().setMapModified(false);
						} catch (final IOException|MalformedXMLException except) {
							handleError(except, path.toString(), source,
								errorTitle, "writing to");
						}
					});
			} else {
				LOGGER.severe("IOHandler asked to save-as in driver it can't do that for");
			}
			break;

		case "new":
			if (driver instanceof ModelDriver) {
				SwingUtilities.invokeLater(() -> startNewViewerWindow((ModelDriver) driver));
			} else {
				LOGGER.severe("IOHandler asked to 'new' in driver it can't do that from");
			}
			break;

		case "load secondary":
			// TODO: Investigate how various apps handle transitioning between no secondaries
			// and one secondary map.
			if (driver instanceof MultiMapGUIDriver) {
				SPFileChooser.open((Path) null).call(loadHandlerImpl(
					((MultiMapGUIDriver) driver).getModel()::addSubordinateMap,
						source, errorTitle));
			} else {
				LOGGER.severe(
					"IOHandler asked to 'load secondary' in driver it can't do that for");
			}
			break;

		case "save all":
			if (driver instanceof MultiMapGUIDriver) {
				for (final IMapNG map : ((MultiMapGUIDriver) driver).getModel().getAllMaps()) {
					// FIXME: Doesn't MapIOHelper have a method for this?
					final Path file = map.getFilename();
					if (file != null) {
						try {
							MapIOHelper.writeMap(file, map);
							((MultiMapGUIDriver) driver).getModel()
								.clearModifiedFlag(map);
						} catch (final IOException|MalformedXMLException except) {
							handleError(except, file.toString(), source,
								errorTitle, "writing to");
						}
					}
				}
			} else if (driver instanceof ModelDriver) {
				actionPerformed(new ActionEvent(event.getSource(), event.getID(),
					"save", event.getWhen(), event.getModifiers()));
			} else {
				LOGGER.severe("IOHandler asked to 'save all' in driver it can't do that for");
			}
			break;

		case "open in map viewer":
			if (driver instanceof ModelDriver) {
				final ViewerDriverFactory vdf =
						StreamSupport.stream(ServiceLoader.load(ViewerDriverFactory.class).spliterator(), false)
								.findAny().orElse(null);
				if (vdf == null) {
					// FIXME: Show error dialog
					LOGGER.severe("Map viewer was not included in this assembly, or service discovery failed");
				} else {
					SwingUtilities.invokeLater(() -> {
						try {
							vdf.createDriver(cli,
											((ModelDriver) driver).getOptions().copy(),
											new ViewerModel(((ModelDriver) driver)
													.getModel()))
									.startDriver();
						} catch (final DriverFailedException except) {
							// FIXME: Log and show error dialog instead
							throw new RuntimeException(except);
						}
					});
				}
			} else {
				LOGGER.severe(
					"IOHandler asked to 'open in map viewer' in unsupported driver");
			}
			break;

		case "open secondary map in map viewer":
			if (driver instanceof MultiMapGUIDriver) {
				final IDriverModel newModel = ((MultiMapGUIDriver) driver).getModel()
					.fromSecondMap();
				if (newModel != null) {
					SwingUtilities.invokeLater(() -> openSecondaryInViewer(newModel,
						driver.getOptions()));
				} else {
					LOGGER.severe(
						"IOHandler asked to 'open secondary in map viewer'; none there");
				}
			} else {
				LOGGER.severe(
					"IOHandler asked to 'open secondary in viewer' in unsupported app");
			}
			break;

		case "close":
			if (parentWindow == null) {
				LOGGER.severe("IOHandler asked to close but couldn't get current window");
				LOGGER.fine("Event details: " + event);
				LOGGER.fine("Source: " + source);
				LOGGER.log(Level.FINER, "Stack trace:", new Exception("Stack trace"));
			} else {
				if (driver instanceof ModelDriver) {
					LOGGER.finer("This operates on a model, maybe we should save ...");
					maybeSave("closing", parentWindow, source, parentWindow::dispose);
				} else if (driver instanceof UtilityGUI) {
					LOGGER.finer("This is a utility GUI, so we just dispose the window.");
					parentWindow.dispose();
				} else {
					LOGGER.severe("IOHandler asked to close in unsupported app");
				}
			}
			break;

		case "quit":
			if (driver instanceof ModelDriver) {
				maybeSave("quitting", parentWindow, source, SPMenu.getDefaultQuit());
			} else if (driver instanceof UtilityGUI) {
				SPMenu.getDefaultQuit().run();
			} else {
				LOGGER.severe("IOHandler asked to quit in unsupported app");
			}
			break;

		default:
			LOGGER.info(String.format("Unhandled command %s in IOHandler",
				event.getActionCommand()));
			break;
		}
	}
}
