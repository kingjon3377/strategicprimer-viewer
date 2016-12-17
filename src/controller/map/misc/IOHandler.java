package controller.map.misc;

import controller.map.drivers.SPOptionsImpl;
import controller.map.drivers.ViewerStart;
import controller.map.formatexceptions.SPFormatException;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.xml.stream.XMLStreamException;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.PlayerCollection;
import model.map.SPMapNG;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import model.viewer.IViewerModel;
import model.viewer.ViewerModel;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;
import util.Pair;
import util.TypesafeLogger;
import util.Warning;
import view.util.ErrorShower;
import view.util.FilteredFileChooser;

/**
 * An ActionListener to dispatch file I/O.
 *
 * TODO: Split this up, with a central class having just a Map from action commands to
 * handlers, then one class handling just file I/O, one handling just zooming, one
 * handling just tree expanding and collapsing, etc, and each menu constructor passing in
 * whatever it needs.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class IOHandler implements ActionListener {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(IOHandler.class);
	/**
	 * File chooser.
	 */
	private final JFileChooser chooser;

	/**
	 * The map model, which needs to be told about newly loaded maps and holds maps to be
	 * saved.
	 */
	private final IDriverModel model;

	/**
	 * Constructor.
	 *
	 * @param map         the map model
	 * @param fileChooser the file chooser
	 */
	public IOHandler(final IDriverModel map, final JFileChooser fileChooser) {
		model = NullCleaner.assertNotNull(map);
		chooser = fileChooser;
	}

	/**
	 * Constructor. File-chooser defaults to the current directory filtered to include
	 * only maps.
	 *
	 * @param map the map model
	 */
	public IOHandler(final IDriverModel map) {
		this(map, new FilteredFileChooser());
	}

	/**
	 * @param obj an object
	 * @return it if it's a component, or null
	 */
	@SuppressWarnings("ReturnOfNull")
	@Nullable
	private static Component eventSource(@Nullable final Object obj) {
		if (obj instanceof Component) {
			return (Component) obj;
		} else {
			return null;
		}
	}

	/**
	 * Display an appropriate error message.
	 *
	 * @param except   an Exception
	 * @param filename the file we were trying to process
	 * @param source   the component to use as the parent of the error dialog. May be
	 *                 null.
	 */
	private static void handleError(final Exception except, final String filename,
									@Nullable final Component source) {
		final String msg;
		if (except instanceof XMLStreamException) {
			msg = "Error reading XML file %s";
		} else if (except instanceof FileNotFoundException ||
						   except instanceof NoSuchFileException) {
			//noinspection StringConcatenationMissingWhitespace
			msg = "File %s not found";
		} else if (except instanceof IOException) {
			//noinspection HardcodedFileSeparator
			msg = "I/O error reading file %s";
		} else if (except instanceof SPFormatException) {
			msg = "Map contained invalid data in file %s";
		} else {
			throw new IllegalStateException("Unknown exception type", except);
		}
		final String formatted = String.format(msg, filename);
		LOGGER.log(Level.SEVERE, formatted, except);
		ErrorShower.showErrorDialog(source, formatted);
	}

	/**
	 * @param file a file to load a map from
	 * @return the map in that file
	 * @throws IOException        on other I/O error
	 * @throws XMLStreamException if the XML isn't well-formed
	 * @throws SPFormatException  if the file contains invalid data
	 */
	private static IMutableMapNG readMap(final Path file)
			throws IOException, XMLStreamException, SPFormatException {
		return new MapReaderAdapter().readMap(file, Warning.DEFAULT);
	}

	/**
	 * Handle the "load" menu item.
	 *
	 * @param source the source of the event. May be null, since JFileChooser doesn't
	 *                  seem
	 *               to care
	 */
	private void handleLoadMenu(@Nullable final Component source) {
		new FileChooser(Optional.empty()).call(path -> {
			try {
				model.setMap(readMap(path), Optional.of(path));
			} catch (final IOException | SPFormatException | XMLStreamException e) {
				handleError(e, path.toString(), source);
			}
		});
	}

	/**
	 * Handle menu selections.
	 *
	 * @param event the event to handle
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public void actionPerformed(@Nullable final ActionEvent event) {
		if (event != null) { // it wouldn't be @Nullable except that the JDK
			// isn't annotated
			final Component source = eventSource(event.getSource());
			switch (event.getActionCommand().toLowerCase()) {
			case "load":
				handleLoadMenu(source);
				break;
			case "save":
				saveMap(source);
				break;
			case "save as":
				saveMapAs(model.getMap(), source);
				break;
			case "new":
				startNewViewerWindow();
				break;
			case "load secondary":
				handleSecondaryLoadMenu(source);
				break;
			case "save all":
				saveAll(source);
				break;
			case "open in map viewer":
				// FIXME: Somehow get the CLI interface and options from previous ...
				new ViewerStart().startDriver(new CLIHelper(), new SPOptionsImpl(),
						new ViewerModel(model));
				break;
			case "open secondary map in map viewer":
				if (model instanceof IMultiMapModel) {
					final Optional<Pair<IMutableMapNG, Optional<Path>>> mapPair =
							((IMultiMapModel) model).streamSubordinateMaps().findFirst();
					if (mapPair.isPresent()) {
						final IViewerModel newModel =
								new ViewerModel(mapPair.get().first(),
													   mapPair.get().second());
						// FIXME: Somehow get the CLI interface and options from previous ...
						new ViewerStart()
								.startDriver(new CLIHelper(), new SPOptionsImpl(),
										newModel);
					}
				}
				break;
			default:
				LOGGER.log(Level.INFO, "Unhandled command in IOHandler");
				break;
			}
		}
	}

	/**
	 * Start a new viewer window with a blank map of the same size as the model's current
	 * map.
	 */
	private void startNewViewerWindow() {
		// FIXME: Somehow get the CLI interface and options from previous ...
		new ViewerStart().startDriver(new CLIHelper(), new SPOptionsImpl(),
				new ViewerModel(new SPMapNG(model.getMapDimensions(),
												   new PlayerCollection(),
												   model.getMap().getCurrentTurn()),
									   Optional.empty()));
	}

	/**
	 * Save a map to the filename it was loaded from.
	 *
	 * @param source the source of the event that triggered this. May be null if it
	 *                  wasn't
	 *               a Component.
	 */
	private void saveMap(@Nullable final Component source) {
		final Optional<Path> givenFile = model.getMapFile();
		if (givenFile.isPresent()) {
			try {
				new MapReaderAdapter().write(givenFile.get(), model.getMap());
			} catch (final IOException e) {
				//noinspection HardcodedFileSeparator
				ErrorShower.showErrorDialog(source, "I/O error writing to file "
															+ givenFile.get());
				//noinspection HardcodedFileSeparator
				LOGGER.log(Level.SEVERE, "I/O error writing XML", e);
			}
		} else {
			saveMapAs(model.getMap(), source);
		}
	}

	/**
	 * Save a map.
	 *
	 * @param map    the map to save.
	 * @param source the source of the event. May be null if the source wasn't a
	 *               component.
	 */
	private void saveMapAs(final IMapNG map, @Nullable final Component source) {
		new FileChooser(Optional.empty(), chooser, FileChooser.FileChooserOperation.Save)
				.call(path -> {
					try {
						new MapReaderAdapter().write(path, map);
					} catch (final IOException e) {
						//noinspection HardcodedFileSeparator
						ErrorShower.showErrorDialog(source,
								"I/O error writing to file "
										+ path);
						//noinspection HardcodedFileSeparator
						LOGGER.log(Level.SEVERE, "I/O error writing XML", e);
					}
				});
	}

	/**
	 * Save all maps to the filenames they were loaded from.
	 *
	 * @param source the source of the event that triggered this. May be null if it was
	 *               not a component.
	 */
	private void saveAll(@Nullable final Component source) {
		if (model instanceof IMultiMapModel) {
			final MapReaderAdapter adapter = new MapReaderAdapter();
			for (final Pair<IMutableMapNG, Optional<Path>> pair : ((IMultiMapModel)
																		   model)
																		  .getAllMaps
																				   ()) {
				final Optional<Path> file = pair.second();
				if (file.isPresent()) {
					try {
						adapter.write(file.get(), pair.first());
					} catch (final IOException e) {
						//noinspection HardcodedFileSeparator
						ErrorShower.showErrorDialog(source,
								"I/O error writing to file " + file.get());
						//noinspection HardcodedFileSeparator
						LOGGER.log(Level.SEVERE, "I/O error writing XML", e);
					}
				} else {
					saveMapAs(pair.first(), source);
				}
			}
		}
	}

	/**
	 * Handle the 'load secondary map' menu item.
	 *
	 * @param source the component to attach the dialog box to. May be null.
	 */
	private void handleSecondaryLoadMenu(@Nullable final Component source) {
		if (model instanceof IMultiMapModel) {
			new FileChooser(Optional.empty()).call(path -> {
				try {
					((IMultiMapModel) model).addSubordinateMap(
							readMap(path), Optional.of(path));
				} catch (final IOException | SPFormatException | XMLStreamException e) {
					handleError(e, path.toString(), source);
				}
			});
		}
	}

	/**
	 * @return a String representation of the object.
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "IOHandler";
	}
}
