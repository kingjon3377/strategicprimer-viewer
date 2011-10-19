package view.map.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Box.Filler;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import model.viewer.MapModel;
import view.map.details.DetailPanel;
import view.util.DriverQuit;

/**
 * The main driver class for the viewer app.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class ViewerFrame extends JFrame implements ActionListener {
	/**
	 * Command to load the secondary map.
	 */
	private static final String LOAD_ALT_MAP_CMD = "<html><p>Load secondary map</p></html>";
	/**
	 * Command to save the secondary map.
	 */
	private static final String SAVE_ALT_MAP_CMD = "<html><p>Save secondary map</p></html>";
	/**
	 * Default width of the Frame.
	 */
	private static final int DEFAULT_WIDTH = 800;
	/**
	 * Default height of the Frame.
	 */
	private static final int DEFAULT_HEIGHT = 600;
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
	 * Constructor.
	 * 
	 * @param map
	 *            The map model.
	 */
	public ViewerFrame(final MapModel map) {
		super("Strategic Primer Map Viewer");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setIgnoreRepaint(false);
		chooser.setFileFilter(new MapFileFilter());
		mapPanel = new MapComponent(map);
		final DetailPanel details = new DetailPanel((MapComponent) mapPanel);
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
			mapPanel.getModel().swapMaps();
		} else if ("Restrict view".equals(event.getActionCommand())) {
			new RestrictDialog(mapPanel).setVisible(true);
		} 
	}

}
