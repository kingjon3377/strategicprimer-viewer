package view.map.main;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.xml.stream.XMLStreamException;

import model.viewer.SPMap;
import controller.map.XMLWriter;
import controller.map.simplexml.SPFormatException;
import controller.map.simplexml.SimpleXMLReader;

/**
 * An ActionListener to dispatch file I/O.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class IOHandler implements ActionListener {
	/**
	 * Error message fragment when file not found.
	 */
	private static final String NOT_FOUND_ERROR = " not found";
	/**
	 * Error message when the map contains invalid data.
	 */
	private static final String INV_DATA_ERROR = "Map contained invalid data";
	/**
	 * Command to load the secondary map.
	 */
	private static final String LOAD_ALT_MAP_CMD = "<html><p>Load secondary map</p></html>";
	/**
	 * Command to save the secondary map.
	 */
	private static final String SAVE_ALT_MAP_CMD = "<html><p>Save secondary map</p></html>";
	/**
	 * An error message refactored from at least four uses.
	 */
	private static final String XML_ERROR_STRING = "Error reading XML file";
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(IOHandler.class
			.getName());
	/**
	 * File chooser.
	 */
	private final JFileChooser chooser;

	/**
	 * Handle menu selections.
	 * 
	 * @param event
	 *            the event to handle
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		if ("Load".equals(event.getActionCommand())) {
			if (chooser.showOpenDialog((Component) panel) == JFileChooser.APPROVE_OPTION) {
				final String filename = chooser.getSelectedFile().getPath();
				// ESCA-JAVA0166:
				try {
					panel.loadMap(readMap(filename));
				} catch (final Exception e) { // $codepro.audit.disable caughtExceptions
					handleError(e, filename);
				}
			}
		} else if ("Save As".equals(event.getActionCommand())) {
			saveMap(panel.getModel().getMainMap());
		} else if (LOAD_ALT_MAP_CMD.equals(event.getActionCommand())) {
			if (chooser.showOpenDialog((Component) panel) == JFileChooser.APPROVE_OPTION) {
				final String filename = chooser.getSelectedFile().getPath();
				// ESCA-JAVA0166:
				try {
					panel.getModel().setSecondaryMap(readMap(filename));
				} catch (final Exception e) { // $codepro.audit.disable caughtExceptions
					handleError(e, filename);
				}
			}
		} else if (SAVE_ALT_MAP_CMD.equals(event.getActionCommand())) {
			saveMap(panel.getModel().getSecondaryMap());
		}
	}

	/**
	 * The panel that needs to be told about newly loaded maps and that holds
	 * maps to be saved.
	 */
	private final MapGUI panel;

	/**
	 * Constructor.
	 * 
	 * @param map
	 *            the map panel
	 * @param fchooser
	 *            the file chooser
	 */
	public IOHandler(final MapGUI map, final JFileChooser fchooser) {
		panel = map;
		chooser = fchooser;
	}

	/**
	 * Display an appropriate error message.
	 * 
	 * @param except
	 *            an Exception
	 * @param filename
	 *            the file we were trying to process
	 */
	private void handleError(final Exception except, final String filename) {
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
		ViewerFrame.showErrorDialog((Component) panel, msg);
	}

	/**
	 * Save a map.
	 * 
	 * @param map
	 *            the map to save.
	 */
	private void saveMap(final SPMap map) {
		if (chooser.showSaveDialog((Component) panel) == JFileChooser.APPROVE_OPTION) {
			try {
				new XMLWriter(chooser.getSelectedFile().getPath()).write(map);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "I/O error writing XML", e);
			}
		}
	}

	/**
	 * @param filename
	 *            a file to load a map from
	 * @return the map in that file
	 * @throws SPFormatException
	 *             if the file contains invalid data
	 * @throws XMLStreamException
	 *             if the XML isn't well-formed
	 * @throws IOException
	 *             on other I/O error
	 */
	private static SPMap readMap(final String filename) throws IOException,
			XMLStreamException, SPFormatException {
		return new SimpleXMLReader().readMap(filename);
	}
}
