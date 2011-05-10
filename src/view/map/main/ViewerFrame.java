package view.map.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.xml.stream.XMLStreamException;

import model.viewer.SPMap;
import view.util.SizeLimiter;
import controller.map.MapReader;
import controller.map.XMLWriter;

/**
 * The main driver class for the viewer app.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class ViewerFrame extends JFrame implements ActionListener {
	private static final String LOAD_2D_MAP_CMD = "<html><p>Load secondary map</p></html>";
	private static final String SAVE_2D_MAP_CMD = "<html><p>Save secondary map</p></html>";
	private static final double BUTTON_PANEL_HEIGHT = 0.1;
	/**
	 * The width of the details panel, as a percentage of the window's width.
	 */
	private static final double DETAIL_PANEL_WIDTH = 0.25; // NOPMD
	/**
	 * An error message refactored from at least four uses.
	 */
	private static final String XML_ERROR_STRING = "Error reading XML file:";
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(ViewerFrame.class
			.getName());
	/**
	 * Default width of the Frame.
	 */
	private static final int DEFAULT_WIDTH = 640;
	/**
	 * Default height of the Frame.
	 */
	private static final int DEFAULT_HEIGHT = 480;
	/**
	 * 
	 */
	private static final long serialVersionUID = -5978274834544191806L;
	/**
	 * The quasi-Singleton.
	 */
	private static ViewerFrame frame;
	/**
	 * The map (view) itself.
	 */
	private final MapPanel mapPanel;

	private final JFileChooser chooser = new JFileChooser();

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
		final String filename; // NOPMD
		if (args.length > 0) {
			filename = args[0];
		} else {
			final JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new MapFileFilter());
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				filename = chooser.getSelectedFile().getPath();
			} else {
				return;
			}
		}
		try {
			frame = new ViewerFrame(filename);
			frame.setVisible(true);
		} catch (XMLStreamException e) {
			LOGGER.log(Level.SEVERE, XML_ERROR_STRING, e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, XML_ERROR_STRING, e);
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
	 */
	private ViewerFrame(final String filename) throws XMLStreamException,
			IOException {
		super();
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setIgnoreRepaint(false);
		chooser.setFileFilter(new MapFileFilter());
		addWindowListener(new WindowListener() {
			/**
			 * Do nothing special on window activation.
			 * 
			 * @param event
			 *            ignored
			 */
			@Override
			public void windowActivated(final WindowEvent event) {
				repaint();
			}

			/**
			 * Quit on window close.
			 * 
			 * @param event
			 *            ignored
			 */
			@Override
			public void windowClosed(final WindowEvent event) {
				System.exit(0);
			}

			/**
			 * Do nothing special on window-closing.
			 * 
			 * @param event
			 *            ignored
			 */
			@Override
			public void windowClosing(final WindowEvent event) {
				// Do nothing
			}

			/**
			 * Do nothing special on window-deactivation.
			 * 
			 * @param event
			 *            ignored
			 */
			@Override
			public void windowDeactivated(final WindowEvent event) {
				// Do nothing
			}

			/**
			 * Do nothing special when deiconified.
			 * 
			 * @param event
			 *            ignored
			 */
			@Override
			public void windowDeiconified(final WindowEvent event) {
				repaint();
			}

			/**
			 * do nothing special when iconified.
			 * 
			 * @param event
			 *            ignored
			 */
			@Override
			public void windowIconified(final WindowEvent event) {
				// Do nothing
			}

			/**
			 * Do nothing special when opened.
			 * 
			 * @param event
			 *            ignored
			 */
			@Override
			public void windowOpened(final WindowEvent event) {
				repaint();
			}
		});
		final JPanel buttonPanel = new JPanel(new BorderLayout());
		final JPanel innerButtonPanel = new JPanel(new BorderLayout());
		final JPanel firstButtonPanel = new JPanel(new BorderLayout());
		final JButton loadButton = new JButton("Load");
		loadButton.addActionListener(this);
		firstButtonPanel.add(loadButton, BorderLayout.NORTH);
		final JButton saveButton = new JButton("Save As");
		saveButton.addActionListener(this);
		firstButtonPanel.add(saveButton, BorderLayout.SOUTH);
		innerButtonPanel.addComponentListener(new SizeLimiter(firstButtonPanel,
				0.35, 1.0));
		innerButtonPanel.add(firstButtonPanel, BorderLayout.WEST);
		final JPanel secondButtonPanel = new JPanel(new BorderLayout());
		final JButton loadSecondaryMap = new JButton(LOAD_2D_MAP_CMD);
		loadSecondaryMap.addActionListener(this);
		secondButtonPanel.addComponentListener(new SizeLimiter(
				loadSecondaryMap, 1.0, 0.5));
		secondButtonPanel.add(loadSecondaryMap, BorderLayout.NORTH);
		final JButton saveSecondaryMap = new JButton(SAVE_2D_MAP_CMD);
		saveSecondaryMap.addActionListener(this);
		secondButtonPanel.addComponentListener(new SizeLimiter(
				saveSecondaryMap, 1.0, 0.5));
		secondButtonPanel.add(saveSecondaryMap, BorderLayout.SOUTH);
		innerButtonPanel.addComponentListener(new SizeLimiter(
				secondButtonPanel, 0.65, 1.0));
		innerButtonPanel.add(secondButtonPanel, BorderLayout.EAST);
		buttonPanel.addComponentListener(new SizeLimiter(innerButtonPanel, 0.4,
				1.0));
		buttonPanel.add(innerButtonPanel, BorderLayout.WEST);
		final DetailPanel details = new DetailPanel();
		mapPanel = new MapPanel(new MapReader().readMap(filename), details);
		addComponentListener(new SizeLimiter(details, DETAIL_PANEL_WIDTH, 1.0));
		final ViewRestrictorPanel vrpanel = new ViewRestrictorPanel(mapPanel);
		buttonPanel.addComponentListener(new SizeLimiter(vrpanel, 0.55, 1.0));
		buttonPanel.add(vrpanel, BorderLayout.CENTER);
		final JPanel thirdButtonPanel = new JPanel(new BorderLayout());
		final JButton swapButton = new JButton("Switch maps");
		swapButton.addActionListener(this);
		thirdButtonPanel.addComponentListener(new SizeLimiter(swapButton, 1.0,
				0.5));
		thirdButtonPanel.add(swapButton, BorderLayout.NORTH);
		final JButton quitButton = new JButton("Quit");
		quitButton.addActionListener(this);
		thirdButtonPanel.addComponentListener(new SizeLimiter(quitButton, 1.0,
				0.5));
		thirdButtonPanel.add(quitButton, BorderLayout.SOUTH);
		buttonPanel.addComponentListener(new SizeLimiter(thirdButtonPanel,
				0.15, 0.4));
		buttonPanel.add(thirdButtonPanel, BorderLayout.EAST);
		add(buttonPanel, BorderLayout.SOUTH);
		addComponentListener(new SizeLimiter(buttonPanel,
				1.0 - DETAIL_PANEL_WIDTH, BUTTON_PANEL_HEIGHT));
		add(details, BorderLayout.EAST);
		final JScrollPane scroller = new JScrollPane(mapPanel);
		add(scroller, BorderLayout.CENTER);
		addComponentListener(new SizeLimiter(scroller,
				1.0 - DETAIL_PANEL_WIDTH, 1.0 - BUTTON_PANEL_HEIGHT));
		setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		setMaximumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		setMinimumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		pack();
		repaint();
	}

	/**
	 * Handle button presses and the like.
	 * 
	 * @param event
	 *            the action we're handling
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		if ("Load".equals(event.getActionCommand())) {
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				mapPanel.loadMap(readMap(chooser.getSelectedFile().getPath()));
			}
		} else if ("Save As".equals(event.getActionCommand())) {
			if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				try {
					new XMLWriter(chooser.getSelectedFile().getPath())
							.write(mapPanel.getMap());
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "I/O error writing XML", e);
				}
			}
		} else if (LOAD_2D_MAP_CMD.equals(event.getActionCommand())) {
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				mapPanel.setSecondaryMap(readMap(chooser.getSelectedFile()
						.getPath()));
			}
		} else if (SAVE_2D_MAP_CMD.equals(event.getActionCommand())) {
			if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				try {
					new XMLWriter(chooser.getSelectedFile().getPath())
							.write(mapPanel.getSecondaryMap());
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "I/O error writing XML", e);
				}
			}
		} else if ("Switch maps".equals(event.getActionCommand())) {
			new Thread() {
				@Override
				public void run() {
					mapPanel.swapMaps();
				}
			}.start();
		} else if ("Quit".equals(event.getActionCommand())) {
			quit(0);
		}
	}

	/**
	 * Note that runtime loading of a new map should always ignore calls with a
	 * null map.
	 * 
	 * @param filename
	 *            a file to load a map from
	 * @return the map in that file, or null if errors happen.
	 */
	private static SPMap readMap(final String filename) {
		try {
			return new MapReader().readMap(filename); // NOPMD
		} catch (XMLStreamException e) {
			LOGGER.log(Level.SEVERE, XML_ERROR_STRING, e);
			return null; // NOPMD
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, XML_ERROR_STRING, e);
			return null;
		}
	}

	/**
	 * Quit. (Don't halt the VM from a non-static context.)
	 * 
	 * @param code
	 *            The exit code.
	 */
	private static void quit(final int code) {
		System.exit(code);
	}
}
