package drivers;

import drivers.common.DriverFailedException;
import drivers.common.ModelDriverFactory;
import drivers.common.ViewerDriverFactory;
import drivers.common.cli.ICLIHelper;

import java.util.Objects;
import java.util.ServiceLoader;
import javax.xml.stream.XMLStreamException;

import drivers.gui.common.about.AboutDialog;
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import drivers.gui.common.ISPWindow;
import drivers.gui.common.SPFileChooser;
import common.xmlio.Warning;
import common.xmlio.SPFormatException;
import legacy.map.ILegacyMap;
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
public final class IOHandler implements ActionListener {
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
	// TODO: Use the possibly-throwing interface from j.u.concurrent instead of Runnable?
	private void maybeSave(final String verb, final @Nullable Frame window, final Component source,
	                       final Runnable ifNotCanceled) {
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
			final int answer = JOptionPane.showConfirmDialog(window, prompt.formatted(verb),
					"Save Changes?", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			switch (answer) {
				case JOptionPane.CANCEL_OPTION -> {
					LovelaceLogger.trace("User selected 'Cancel'");
					return;
				}
				case JOptionPane.YES_OPTION -> {
					LovelaceLogger.trace("User selected 'Yes'; invoking 'Save' menu ...");
					actionPerformed(new ActionEvent(source, ActionEvent.ACTION_FIRST, "save"));
					LovelaceLogger.trace("Finished saving main map");
				}
				default -> LovelaceLogger.trace("User said not to save main map");
			}
		} else {
			LovelaceLogger.trace("main map was not modified");
		}

		if (md instanceof final MultiMapGUIDriver mmgd && mmgd.getModel().streamSubordinateMaps()
				.anyMatch(ILegacyMap::isModified)) {
			LovelaceLogger.trace("Subordinate map(s) modified.");
			final int answer = JOptionPane.showConfirmDialog(window,
					"Subordinate map(s) have unsaved changes. Save all before %s?".formatted(verb), "Save Changes?",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			switch (answer) {
				case JOptionPane.CANCEL_OPTION -> {
					LovelaceLogger.trace("User selected 'Cancel' rather than save-all.");
					return;
				}
				case JOptionPane.YES_OPTION -> {
					LovelaceLogger.trace("User selected 'Yes'; invoking 'Save All' menu ...");
					actionPerformed(new ActionEvent(source, ActionEvent.ACTION_FIRST,
							"save all"));
					LovelaceLogger.trace("Finished saving");
				}
				default -> LovelaceLogger.trace("User said not to save subordinate maps");
			}
		} else {
			LovelaceLogger.trace("No subordinate maps modified");
		}
		LovelaceLogger.trace("About to carry out action ...");
		ifNotCanceled.run();
	}

	private static void handleError(final Exception except, final String filename, final @Nullable Component source,
	                                final String errorTitle, final String verb) {
		final String message = switch (except) {
			case final XMLStreamException xse -> "Malformed XML in " + filename;
			case final FileNotFoundException fnfe -> "File %s not found".formatted(filename);
			case final NoSuchFileException nsfe -> "File %s not found".formatted(filename);
			case final IOException ioe -> "I/O error %s file %s".formatted(verb, filename);
			case final SPFormatException spfe -> "SP map format error in " + filename;
			default -> except.getMessage();
		};
		LovelaceLogger.error(except, message);
		JOptionPane.showMessageDialog(source, message, errorTitle, JOptionPane.ERROR_MESSAGE);
	}

	private static Consumer<Path> loadHandlerImpl(final Consumer<IMutableLegacyMap> handler,
	                                              final @Nullable Component source, final String errorTitle) {
		return path -> {
			try {
				handler.accept(MapIOHelper.readMap(path, Warning.getDefaultHandler()));
			} catch (final IOException | SPFormatException | XMLStreamException except) {
				handleError(except, path.toString(), source, errorTitle, "reading");
			}
		};
	}

	private void loadHandler(final @Nullable Component source, final String errorTitle) {
		switch (driver) {
			case final GUIDriver gd -> SPFileChooser.open((Path) null)
					.call(loadHandlerImpl(gd::open, source, errorTitle));
			case final UtilityGUI ug -> SPFileChooser.open((Path) null).call(ug::open);
			default -> LovelaceLogger.error("IOHandler asked to 'load' in app that doesn't support that");
		}
	}

	/**
	 * Start a new map-viewer window for a new map with the same dimensions
	 * as that represented by the given driver's model. Should be run on
	 * the EDT, i.e. using {@link SwingUtilities#invokeLater}.
	 */
	private void startNewViewerWindow(final ModelDriver driver) {
		final ViewerDriverFactory<?> vdf =
				StreamSupport.stream(ServiceLoader.load(ViewerDriverFactory.class).spliterator(), false)
						.findAny().orElse(null);
		if (Objects.isNull(vdf)) {
			// FIXME: Show error dialog
			LovelaceLogger.error("Map viewer was not included in this assembly, or service discovery failed");
		} else {
			try {
				createDriver(vdf, cli, driver.getOptions().copy(),
								vdf.createModel(new LegacyMap(driver.getModel().getMapDimensions(),
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
		final ViewerDriverFactory<?> vdf =
				StreamSupport.stream(ServiceLoader.load(ViewerDriverFactory.class).spliterator(), false)
						.findAny().orElse(null);
		if (Objects.isNull(vdf)) {
			// FIXME: Show error dialog
			LovelaceLogger.error("Map viewer was not included in this assembly, or service discovery failed");
		} else {
			try {
				createDriver(vdf, cli, options.copy(), model).startDriver();
			} catch (final DriverFailedException except) {
				// FIXME: show error dialog
				LovelaceLogger.error(except, "Driver failed");
			}
		}
	}

	private static <Model extends IDriverModel> ModelDriver createDriver(final ModelDriverFactory<Model> factory,
                                                                         final ICLIHelper cli, final SPOptions options,
                                                                         final IDriverModel model) {
		return factory.createDriver(cli, options, factory.createModel(model));
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
				.orElse(AboutDialog.APP_SUITE_TITLE);

		LovelaceLogger.trace("Menu item invoked: %s", event.getActionCommand());

		switch (event.getActionCommand().toLowerCase()) {
			case "load":
				// TODO: Open in another window if 'modified' instead of prompting before overwriting
				switch (driver) {
					case final ModelDriver modelDriver -> maybeSave("loading another map", parentWindow, source,
							() -> loadHandler(source, errorTitle));
					case final UtilityGUI utilityGUI -> loadHandler(source, errorTitle);
					default -> LovelaceLogger.error("IOHandler asked to 'load' in unsupported app");
				}
				break;

			case "save":
				if (driver instanceof final ModelDriver md) {
					final Path givenFile = md.getModel().getMap().getFilename();
					if (Objects.isNull(givenFile)) {
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
				switch (driver) {
					case final MultiMapGUIDriver mmgd -> {
						for (final ILegacyMap map : mmgd.getModel().getAllMaps()) {
							// FIXME: Doesn't MapIOHelper have a method for this?
							final Path file = map.getFilename();
							if (Objects.nonNull(file)) {
								try {
									MapIOHelper.writeMap(file, map);
									mmgd.getModel().clearModifiedFlag(map);
								} catch (final IOException | XMLStreamException except) {
									handleError(except, file.toString(), source,
											errorTitle, "writing to");
								}
							}
						}
					}
					case final ModelDriver modelDriver -> actionPerformed(new ActionEvent(event.getSource(),
							event.getID(), "save", event.getWhen(), event.getModifiers()));
					default -> LovelaceLogger.error("IOHandler asked to 'save all' in driver it can't do that for");
				}
				break;

			case "open in map viewer":
				if (driver instanceof final ModelDriver md) {
					final ViewerDriverFactory<?> vdf =
							StreamSupport.stream(ServiceLoader.load(ViewerDriverFactory.class).spliterator(), false)
									.findAny().orElse(null);
					if (Objects.isNull(vdf)) {
						JOptionPane.showMessageDialog(null,
								"Either the map viewer was not included in this package, or loading it failed.",
                                AboutDialog.APP_SUITE_TITLE, JOptionPane.ERROR_MESSAGE);
						LovelaceLogger.error(
								"Map viewer was not included in this assembly, or service discovery failed");
					} else {
						SwingUtilities.invokeLater(() -> {
							try {
								createDriver(vdf, cli, driver.getOptions().copy(), md.getModel())
										.startDriver();
							} catch (final DriverFailedException except) {
								LovelaceLogger.error(Objects.requireNonNullElse(except.getCause(), except),
										"Error thrown from viewer driver");
								JOptionPane.showMessageDialog(null,
										"Error starting map viewer:%n%s".formatted(except.getMessage()),
                                        AboutDialog.APP_SUITE_TITLE, JOptionPane.ERROR_MESSAGE);
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
					if (Objects.isNull(newModel)) {
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
				if (Objects.isNull(parentWindow)) {
					LovelaceLogger.error("IOHandler asked to close but couldn't get current window");
					LovelaceLogger.debug("Event details: %s", event);
					LovelaceLogger.debug("Source: %s", source);
					LovelaceLogger.trace(new Exception("Stack trace"), "Stack trace:");
				} else {
					switch (driver) {
						case final ModelDriver modelDriver -> {
							LovelaceLogger.trace("This operates on a model, maybe we should save ...");
							maybeSave("closing", parentWindow, source, parentWindow::dispose);
						}
						case final UtilityGUI utilityGUI -> {
							LovelaceLogger.trace("This is a utility GUI, so we just dispose the window.");
							parentWindow.dispose();
						}
						default -> LovelaceLogger.error("IOHandler asked to close in unsupported app");
					}
				}
				break;

			case "quit":
				switch (driver) {
					case final ModelDriver modelDriver ->
							maybeSave("quitting", parentWindow, source, SPMenu.getDefaultQuit());
					case final UtilityGUI utilityGUI -> SPMenu.getDefaultQuit().run();
					default -> LovelaceLogger.error("IOHandler asked to quit in unsupported app");
				}
				break;

			default:
				LovelaceLogger.info("Unhandled command %s in IOHandler",
						event.getActionCommand());
				break;
		}
	}
}
