package controller.map.misc;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import model.map.MapView;
import model.map.SPMap;
import model.misc.IDriverModel;
import model.viewer.ViewerModel;
import util.Warning;
import view.map.main.ViewerFrame;
import view.util.ErrorShower;
import controller.map.formatexceptions.SPFormatException;

/**
 * An ActionListener to dispatch file I/O.
 *
 * @author Jonathan Lovelace
 *
 */
public class IOHandler implements ActionListener {
	/**
	 * Error message fragment when file not found.
	 */
	protected static final String NOT_FOUND_ERROR = " not found";
	/**
	 * Error message when the map contains invalid data.
	 */
	protected static final String INV_DATA_ERROR = "Map contained invalid data";
	/**
	 * An error message refactored from at least four uses.
	 */
	protected static final String XML_ERROR_STRING = "Error reading XML file";
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(IOHandler.class
			.getName());
	/**
	 * File chooser.
	 */
	protected final JFileChooser chooser;

	/**
	 * Handle the "load" menu item.
	 *
	 * @param source the source of the event
	 */
	private void handleLoadMenu(final Component source) {
		if (chooser.showOpenDialog(source) == JFileChooser.APPROVE_OPTION) {
			final String filename = chooser.getSelectedFile().getPath();
			// ESCA-JAVA0166:
			try {
				model.setMap(readMap(filename, Warning.INSTANCE), filename);
			} catch (final IOException e) {
				handleError(e, filename, source);
			} catch (final SPFormatException e) {
				handleError(e, filename, source);
			} catch (final XMLStreamException e) {
				handleError(e, filename, source);
			}
		}
	}

	/**
	 * Handle menu selections.
	 *
	 * @param event the event to handle
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		final Component source = event.getSource() instanceof Component ? (Component) event
				.getSource() : null; // NOPMD
		if ("Load".equals(event.getActionCommand())) {
			handleLoadMenu(source);
		} else if ("Save".equals(event.getActionCommand())) {
			saveMap(source);
		} else if ("Save As".equals(event.getActionCommand())) {
			saveMapAs(model.getMap(), source);
		} else if ("New".equals(event.getActionCommand())) {
			startNewViewerWindow();
		}
	}
	/**
	 * Start a new viewer window with a blank map of the same size as the model's current map.
	 */
	private void startNewViewerWindow() {
		SwingUtilities.invokeLater(new WindowThread(new ViewerFrame(
				new ViewerModel(new MapView(
						new SPMap(model.getMapDimensions()), 0, model.getMap()
								.getCurrentTurn()), ""), this)));
	}
	/**
	 * The map model, which needs to be told about newly loaded maps and holds
	 * maps to be saved.
	 */
	private final IDriverModel model;

	/**
	 * Constructor.
	 *
	 * @param map the map model
	 * @param fchooser the file chooser
	 */
	public IOHandler(final IDriverModel map, final JFileChooser fchooser) {
		model = map;
		chooser = fchooser;
	}

	/**
	 * Display an appropriate error message.
	 *
	 * @param except an Exception
	 * @param filename the file we were trying to process
	 * @param source the component to use as the parent of the error dialog
	 */
	protected static void handleError(final Exception except,
			final String filename, final Component source) {
		// ESCA-JAVA0177:
		String msg;
		if (except instanceof XMLStreamException) {
			msg = XML_ERROR_STRING + ' ' + filename;
		} else if (except instanceof FileNotFoundException) {
			msg = "File " + filename + NOT_FOUND_ERROR;
		} else if (except instanceof IOException) {
			msg = "I/O error reading file " + filename;
		} else if (except instanceof SPFormatException) {
			msg = INV_DATA_ERROR + " in file " + filename;
		} else {
			throw new IllegalStateException("Unknown exception type", except);
		}
		LOGGER.log(Level.SEVERE, msg, except);
		ErrorShower.showErrorDialog(source, msg);
	}
	/**
	 * Save a map to the filename it was loaded from.
	 * @param source the source of the event that triggered this
	 */
	private void saveMap(final Component source) {
		try {
			new MapReaderAdapter().write(model.getMapFilename(), model.getMap());
		} catch (final IOException e) {
			ErrorShower.showErrorDialog(source,
					"I/O error writing to file "
							+ model.getMapFilename());
			LOGGER.log(Level.SEVERE, "I/O error writing XML", e);
		}
	}
	/**
	 * Save a map.
	 *
	 * @param map the map to save.
	 * @param source the source of the event
	 */
	private void saveMapAs(final IMap map, final Component source) {
		if (chooser.showSaveDialog(source) == JFileChooser.APPROVE_OPTION) {
			try {
				new MapReaderAdapter().write(chooser.getSelectedFile()
						.getPath(), map);
			} catch (final IOException e) {
				ErrorShower.showErrorDialog(source,
						"I/O error writing to file "
								+ chooser.getSelectedFile().getPath());
				LOGGER.log(Level.SEVERE, "I/O error writing XML", e);
			}
		}
	}

	/**
	 * @param filename a file to load a map from
	 * @param warner the Warning instance to use for warnings.
	 * @return the map in that file
	 * @throws IOException on other I/O error
	 * @throws XMLStreamException if the XML isn't well-formed
	 * @throws SPFormatException if the file contains invalid data
	 */
	protected static MapView readMap(final String filename, final Warning warner)
			throws IOException, XMLStreamException, SPFormatException {
		return new MapReaderAdapter().readMap(filename, warner);
	}

	/**
	 *
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "IOHandler";
	}
}
