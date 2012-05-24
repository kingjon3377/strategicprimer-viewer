package view.map.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Box.Filler;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

import model.viewer.MapModel;
import view.map.details.DetailPanel;
import view.util.DriverQuit;
import view.util.MenuItemCreator;

/**
 * The main driver class for the viewer app.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class ViewerFrame extends JFrame {
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
	 * Default width of the Frame.
	 */
	private static final int DEFAULT_WIDTH = 800;
	/**
	 * Default height of the Frame.
	 */
	private static final int DEFAULT_HEIGHT = 600;
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
	 * @param mapMenu
	 *            the menu dealing with file I/O and map switching.
	 */
	public ViewerFrame(final MapModel map, final JMenu mapMenu) {
		super("Strategic Primer Map Viewer");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setIgnoreRepaint(false);
		chooser.setFileFilter(new MapFileFilter());
		final MapGUI mapPanel = new MapComponent(map);
		add(new DetailPanel(map.getMainMap().getVersion(), map, mapPanel), BorderLayout.SOUTH);
//		final JScrollPane scroller = new JScrollPane((JComponent) mapPanel);
//		add(scroller, BorderLayout.CENTER);
		add((JComponent) mapPanel, BorderLayout.CENTER);
		setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		setMaximumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		setMinimumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		createMenu(mapMenu, map);
		pack();
		repaint();
	}

	/**
	 * Set up the menu.
	 * 
	 * @param mapMenu
	 *            the map menu
	 * @param model the map model
	 */
	private void createMenu(final JMenu mapMenu, final MapModel model) {
		final MenuItemCreator creator = new MenuItemCreator();
		final JMenuBar mbar = new JMenuBar();
		mbar.add(mapMenu);
		mbar.add(creator.createMenuItem("Go to tile", KeyEvent.VK_G,
				KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK),
				"Go to a tile by coordinates", new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				if ("Go to tile".equals(event.getActionCommand())) {
					new SelectTileDialog(getFrame(), model).setVisible(true);
				}
			}
		}));
		mbar.add(new Filler(new Dimension(0, 0), new Dimension(0, 0),
				new Dimension(Integer.MAX_VALUE, 0)));
		mbar.add(creator.createMenuItem("Quit", KeyEvent.VK_Q,
				KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK),
				"Quit the viewer", new QuitListener()));
		setJMenuBar(mbar);
	}
	/**
	 * @return this frame
	 */
	public Frame getFrame() {
		return this;
	}
}
