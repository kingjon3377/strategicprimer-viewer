package view.map.main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box.Filler;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.xml.stream.XMLStreamException;

import view.map.details.DetailPanel;
import view.util.DriverQuit;
import controller.map.simplexml.SPFormatException;
import controller.map.simplexml.SimpleXMLReader;

/**
 * The main driver class for the viewer app.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class ViewerFrame extends JFrame implements ActionListener {
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
	private static final Logger LOGGER = Logger.getLogger(ViewerFrame.class
			.getName());
	/**
	 * Default width of the Frame.
	 */
	private static final int DEFAULT_WIDTH = 800;
	/**
	 * Default height of the Frame.
	 */
	private static final int DEFAULT_HEIGHT = 600;
	/**
	 * The quasi-Singleton.
	 */
	private static ViewerFrame frame;
	/**
	 * The map (view) itself.
	 */
	private final MapGUI mapPanel;
	/**
	 * File-choosing dialog. Used often, but immutable, so we don't want to have
	 * to construct it every time.
	 */
	private final JFileChooser chooser = new JFileChooser(".");
	/**
	 * A thread to switch the maps.
	 * @author Jonathan Lovelace
	 */
	private static class MapSwitcher extends Thread {
		/**
		 * The panel to switch.
		 */
		private final MapGUI panel;
		// ESCA-JAVA0128:
		/**
		 * Constructor; otherwise it's "emulated by a synthetic ... method."
		 * @param mpanel the panel whose maps we'll be swapping.
		 */
		public MapSwitcher(final MapGUI mpanel) { // $codepro.audit.disable publicConstructorInNonPublicType
			super();
			panel = mpanel;
		}
		/**
		 * Switch the maps.
		 */
		@Override
		public void run() {
			panel.swapMaps();
		}
	}
	/**
	 * @return the quasi-Singleton objects
	 */
	public static ViewerFrame getFrame() {
		return frame;
	}

	/**
	 * Run the app.
	 * 
	 * @param args
	 *            Command-line arguments: args[0] is the map filename, others
	 *            are ignored. TODO: Add option handling.
	 * 
	 */
	public static void main(final String[] args) {
		// ESCA-JAVA0177:
		final String filename; // NOPMD // $codepro.audit.disable localDeclaration
		if (args.length == 0) {
			final JFileChooser chooser = new JFileChooser(".");
			chooser.setFileFilter(new MapFileFilter());
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				filename = chooser.getSelectedFile().getPath();
			} else {
				return;
			}
		} else {
			filename = args[0];
		}
		try {
			frame = new ViewerFrame(filename);
			new Thread() {
				@Override
				@SuppressWarnings("synthetic-access")
				public void run() {
					frame.setVisible(true);
				}
			} .start();
			if (args.length > 1) {
				frame.mapPanel.setSecondaryMap(new SimpleXMLReader().readMap(args[1]));
			}
		} catch (final XMLStreamException e) {
			LOGGER.log(Level.SEVERE, XML_ERROR_STRING, e);
			showErrorDialog(null, XML_ERROR_STRING + ' ' + filename);
		} catch (final FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, filename + NOT_FOUND_ERROR, e);
			showErrorDialog(null, "File " + filename + NOT_FOUND_ERROR);
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, XML_ERROR_STRING, e);
			showErrorDialog(null, "I/O error reading " + filename);
		} catch (SPFormatException e) {
			LOGGER.log(Level.SEVERE, INV_DATA_ERROR, e);
			showErrorDialog(null, INV_DATA_ERROR);
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param filename
	 *            The filename of an XML file describing the map
	 * @throws IOException
	 *             on I/O error
	 * @throws XMLStreamException
	 *             on XML reading error
	 * @throws SPFormatException
	 *             if the map contained invalid data
	 */
	private ViewerFrame(final String filename) throws XMLStreamException,
			IOException, SPFormatException {
		super("Strategic Primer Map Viewer");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setIgnoreRepaint(false);
		chooser.setFileFilter(new MapFileFilter());
		mapPanel = new MapPanel(new SimpleXMLReader().readMap(filename));
		final TileSelectionListener tsl = new TileSelectionListener((MapPanel) mapPanel);
		((MapPanel) mapPanel).setUpListeners(tsl);
		final DetailPanel details = new DetailPanel(tsl);
		createMenu();
		add(details, BorderLayout.SOUTH);
		final JScrollPane scroller = new JScrollPane((JComponent) mapPanel);
		add(scroller, BorderLayout.CENTER);
		setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		setMaximumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		setMinimumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		pack();
		repaint();
	}

	/**
	 * Set up the menu.
	 */
	private void createMenu() {
		final JMenuBar mbar = new JMenuBar();
		final JMenu mapMenu = new JMenu("Map");
		final ActionListener ioListener = new IOHandler(mapPanel, chooser);
		mapMenu.setMnemonic(KeyEvent.VK_M);
		mapMenu.add(createMenuItem("Load", KeyEvent.VK_L,
				KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK),
				"Load a main map from file", ioListener));
		mapMenu.add(createMenuItem("Save", KeyEvent.VK_S,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK),
				"Save the main map to file", ioListener));
		mapMenu.addSeparator();
		mapMenu.add(createMenuItem(
				LOAD_ALT_MAP_CMD,
				KeyEvent.VK_D,
				KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK
						+ ActionEvent.ALT_MASK),
				"Load a secondary map from file", ioListener));
		mapMenu.add(createMenuItem(SAVE_ALT_MAP_CMD,
				KeyEvent.VK_V, KeyStroke.getKeyStroke(KeyEvent.VK_S,
						ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK),
				"Save the secondary map to file", ioListener));
		mapMenu.addSeparator();
		mapMenu.add(createMenuItem("Switch maps", KeyEvent.VK_W,
				KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK),
				"Make the secondary map the main map and vice versa", this));
		mbar.add(mapMenu);
		mbar.add(createMenuItem("Restrict view",
				KeyEvent.VK_R,
				KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK),
				"Show only a subset of the map", this));
		mbar.add(new Filler(new Dimension(0, 0), new Dimension(0, 0),
				new Dimension(Integer.MAX_VALUE, 0)));
		mbar.add(createMenuItem("Quit", KeyEvent.VK_Q,
				KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK),
				"Quit the viewer", new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				if ("Quit".equals(event.getActionCommand())) {
					DriverQuit.quit(0);
				}
			}
		}));
		setJMenuBar(mbar);
	}
	/**
	 * Create a menu item.
	 * @param item the text of the item
	 * @param mnemonic the mnemonic key
	 * @param accel the keyboard accelerator
	 * @param desc the accessibile description.
	 * @param list the listener to hande when the item is selected.
	 * @return the configured menu item.
	 */
	private static JMenuItem createMenuItem(final String item,
			final int mnemonic, final KeyStroke accel, final String desc,
			final ActionListener list) {
		final JMenuItem mitem = new JMenuItem(item, mnemonic);
		mitem.setAccelerator(accel);
		mitem.getAccessibleContext().setAccessibleDescription(desc);
		mitem.addActionListener(list);
		return mitem;
	}
	/**
	 * Handle button presses and the like.
	 * 
	 * @param event
	 *            the action we're handling
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		if ("Switch maps".equals(event.getActionCommand())) {
			new MapSwitcher(mapPanel).start();
		} else if ("Restrict view".equals(event.getActionCommand())) {
			new RestrictDialog(mapPanel).setVisible(true);
		} 
	}

	/**
	 * Show an error dialog.
	 * 
	 * @param parent
	 *            the parent component for the dialog
	 * @param message
	 *            the error message.
	 */
	public static void showErrorDialog(final Component parent, final String message) {
		JOptionPane.showMessageDialog(parent,
				message, "Strategic Primer Map Viewer error",
				JOptionPane.ERROR_MESSAGE);
	}

}
