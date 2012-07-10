package controller.map.misc;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box.Filler;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import model.map.MapView;
import model.viewer.MapModel;
import util.Warning;
import view.map.main.SelectTileDialog;
import view.util.DriverQuit;
import view.util.ErrorShower;
import view.util.MenuItemCreator;
import controller.map.SPFormatException;

/**
 * An ActionListener to dispatch file I/O.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class IOHandler implements ActionListener {
	/**
	 * A listener for the Quit menu item.
	 * @author Jonathan Lovelace
	 *
	 */
	static final class QuitListener implements ActionListener {
		/**
		 * Handle the menu "button" press.
		 * @param event the event to handle
		 */
		@Override
		public void actionPerformed(final ActionEvent event) {
			if ("Quit".equals(event.getActionCommand())) {
				DriverQuit.quit(0);
			}
		}
	}
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
	 * Handle the "load" menu item.
	 * @param source the source of the event
	 */
	private void handleLoadMenu(final Component source) {
		if (chooser.showOpenDialog(source) == JFileChooser.APPROVE_OPTION) {
			final String filename = chooser.getSelectedFile().getPath();
			// ESCA-JAVA0166:
			try {
				model.setMainMap(readMap(filename, Warning.INSTANCE));
			} catch (final Exception e) { // $codepro.audit.disable caughtExceptions
				handleError(e, filename, source);
			}
		}
	}
	/**
	 * Handle the "load secondary map" menu item.
	 * @param source the source of the event
	 */
	private void handleLoadAltMenu(final Component source) {
		if (chooser.showOpenDialog(source) == JFileChooser.APPROVE_OPTION) {
			final String filename = chooser.getSelectedFile().getPath();
			// ESCA-JAVA0166:
			try {
				model.setSecondaryMap(readMap(filename, Warning.INSTANCE));
			} catch (final Exception e) { // $codepro.audit.disable caughtExceptions
				handleError(e, filename, source);
			}
		}
	}
	/**
	 * Handle menu selections.
	 * 
	 * @param event
	 *            the event to handle
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		final Component source = event.getSource() instanceof Component ? (Component) event
				.getSource() : null; // NOPMD
		if ("Load".equals(event.getActionCommand())) {
			handleLoadMenu(source);
		} else if ("Save As".equals(event.getActionCommand())) {
			saveMap(model.getMainMap(), source);
		} else if (LOAD_ALT_MAP_CMD.equals(event.getActionCommand())) {
			handleLoadAltMenu(source);
		} else if (SAVE_ALT_MAP_CMD.equals(event.getActionCommand())) {
			saveMap(model.getSecondaryMap(), source);
		} else if ("Switch maps".equals(event.getActionCommand())) {
			model.swapMaps();
		}
	}

	/**
	 * The map model, which needs to be told about newly loaded maps and holds
	 * maps to be saved.
	 */
	private final MapModel model;

	/**
	 * Constructor.
	 * 
	 * @param map
	 *            the map model
	 * @param fchooser
	 *            the file chooser
	 */
	public IOHandler(final MapModel map, final JFileChooser fchooser) {
		model = map;
		chooser = fchooser;
	}

	/**
	 * Display an appropriate error message.
	 * 
	 * @param except
	 *            an Exception
	 * @param filename
	 *            the file we were trying to process
	 * @param source the component to use as the parent of the error dialog
	 */
	private static void handleError(final Exception except, final String filename, final Component source) {
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
	 * Save a map.
	 * 
	 * @param map
	 *            the map to save.
	 * @param source the source of the event
	 */
	private void saveMap(final IMap map, final Component source) {
		if (chooser.showSaveDialog(source) == JFileChooser.APPROVE_OPTION) {
			try {
				new MapReaderAdapter().write(chooser.getSelectedFile().getPath(), map);
			} catch (final IOException e) {
				ErrorShower.showErrorDialog(source, "I/O error writing to file " + chooser.getSelectedFile().getPath());
				LOGGER.log(Level.SEVERE, "I/O error writing XML", e);
			}
		}
	}

	/**
	 * @param filename
	 *            a file to load a map from
	 * @param warner the Warning instance to use for warnings.
	 * @return the map in that file
	 * @throws IOException
	 *             on other I/O error
	 * @throws XMLStreamException
	 *             if the XML isn't well-formed
	 * @throws SPFormatException
	 *             if the file contains invalid data
	 */
	private static MapView readMap(final String filename, final Warning warner) throws IOException,
			XMLStreamException, SPFormatException {
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

	/**
	 * Create a menu.
	 * @param frame the frame to which the menu will be attached
	 * @param map a reference to the map model  
	 * @return a menu whose I/O-related functions we handle
	 */
	public JMenuBar createMenu(final Frame frame, final MapModel map) {
		final JMenu mapMenu = new JMenu("Map");
		mapMenu.setMnemonic(KeyEvent.VK_M);
		mapMenu.add(creator.createMenuItem("Load", KeyEvent.VK_L,
				KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK),
				"Load a main map from file", this));
		mapMenu.add(creator.createMenuItem("Save As", KeyEvent.VK_S,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK),
				"Save the main map to file", this));
		mapMenu.addSeparator();
		mapMenu.add(creator.createMenuItem(
				LOAD_ALT_MAP_CMD,
				KeyEvent.VK_D,
				KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK
						+ ActionEvent.ALT_MASK),
				"Load a secondary map from file", this));
		mapMenu.add(creator.createMenuItem(
				SAVE_ALT_MAP_CMD,
				KeyEvent.VK_V,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK
						+ ActionEvent.ALT_MASK),
				"Save the secondary map to file", this));
		mapMenu.addSeparator();
		mapMenu.add(creator.createMenuItem("Switch maps", KeyEvent.VK_W,
				KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK),
				"Make the secondary map the main map and vice versa", this));
		final JMenuBar mbar = new JMenuBar();
		mbar.add(mapMenu);
		mbar.add(creator.createMenuItem("Go to tile", KeyEvent.VK_G,
				KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK),
				"Go to a tile by coordinates", new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				if ("Go to tile".equals(event.getActionCommand())) {
					new SelectTileDialog(frame, map).setVisible(true);
				}
			}
		}));
		mbar.add(new Filler(new Dimension(0, 0), new Dimension(0, 0),
				new Dimension(Integer.MAX_VALUE, 0)));
		mbar.add(creator.createMenuItem("Quit", KeyEvent.VK_Q,
				KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK),
				"Quit the viewer", new QuitListener()));
		return mbar;
	}
}
