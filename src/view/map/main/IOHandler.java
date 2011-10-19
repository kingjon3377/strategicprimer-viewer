package view.map.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.KeyStroke;
import javax.xml.stream.XMLStreamException;

import model.map.SPMap;
import model.viewer.MapModel;
import view.util.ErrorShower;
import view.util.MenuItemCreator;
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
	 * The helper to create menu items for us.
	 */
	private final MenuItemCreator creator = new MenuItemCreator();
	/**
	 * Handle menu selections.
	 * 
	 * @param event
	 *            the event to handle
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		if ("Load".equals(event.getActionCommand())) {
			if (chooser.showOpenDialog(menu) == JFileChooser.APPROVE_OPTION) {
				final String filename = chooser.getSelectedFile().getPath();
				// ESCA-JAVA0166:
				try {
					panel.setMainMap(readMap(filename));
				} catch (final Exception e) { // $codepro.audit.disable caughtExceptions
					handleError(e, filename);
				}
			}
		} else if ("Save As".equals(event.getActionCommand())) {
			saveMap(panel.getMainMap());
		} else if (LOAD_ALT_MAP_CMD.equals(event.getActionCommand())) {
			if (chooser.showOpenDialog(menu) == JFileChooser.APPROVE_OPTION) {
				final String filename = chooser.getSelectedFile().getPath();
				// ESCA-JAVA0166:
				try {
					panel.setSecondaryMap(readMap(filename));
				} catch (final Exception e) { // $codepro.audit.disable caughtExceptions
					handleError(e, filename);
				}
			}
		} else if (SAVE_ALT_MAP_CMD.equals(event.getActionCommand())) {
			saveMap(panel.getSecondaryMap());
		}
	}

	/**
	 * The panel that needs to be told about newly loaded maps and that holds
	 * maps to be saved.
	 */
	private final MapModel panel;

	/**
	 * Constructor.
	 * 
	 * @param map
	 *            the map model
	 * @param fchooser
	 *            the file chooser
	 */
	public IOHandler(final MapModel map, final JFileChooser fchooser) {
		panel = map;
		setUpMenu();
		chooser = fchooser;
	}
	/**
	 * A component to show the chooser under.
	 */
	private JMenu menu;
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
		ErrorShower.showErrorDialog(menu, msg);
	}

	/**
	 * Save a map.
	 * 
	 * @param map
	 *            the map to save.
	 */
	private void saveMap(final SPMap map) {
		if (chooser.showSaveDialog(menu) == JFileChooser.APPROVE_OPTION) {
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
	/**
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "IOHandler";
	}
	/**
	 * Set up the menu we'll be handling.
	 */
	private void setUpMenu() {
		menu = new JMenu("Map");
		menu.setMnemonic(KeyEvent.VK_M);
		menu.add(creator.createMenuItem("Load", KeyEvent.VK_L,
				KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK),
				"Load a main map from file", this));
		menu.add(creator.createMenuItem("Save", KeyEvent.VK_S,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK),
				"Save the main map to file", this));
		menu.addSeparator();
		menu.add(creator.createMenuItem(
				LOAD_ALT_MAP_CMD,
				KeyEvent.VK_D,
				KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK
						+ ActionEvent.ALT_MASK),
				"Load a secondary map from file", this));
		menu.add(creator.createMenuItem(SAVE_ALT_MAP_CMD,
				KeyEvent.VK_V, KeyStroke.getKeyStroke(KeyEvent.VK_S,
						ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK),
				"Save the secondary map to file", this));
		menu.addSeparator();
		menu.add(creator.createMenuItem("Switch maps", KeyEvent.VK_W,
				KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK),
				"Make the secondary map the main map and vice versa", this));
	}
	/**
	 * @return the menu we handle
	 */
	public JMenu getMenu() {
		return menu;
	}
}
