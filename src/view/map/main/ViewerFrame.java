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
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import model.exploration.ExplorationRunner;
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
public final class ViewerFrame extends JFrame implements ActionListener {
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
	 * @param runner
	 *            an exploration runner
	 * @param map
	 *            The map model.
	 * @param mapMenu
	 *            the menu dealing with file I/O and map switching.
	 */
	public ViewerFrame(final ExplorationRunner runner, final MapModel map, final JMenu mapMenu) {
		super("Strategic Primer Map Viewer");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setIgnoreRepaint(false);
		chooser.setFileFilter(new MapFileFilter());
		mapPanel = new MapComponent(map);
		final DetailPanel details = new DetailPanel(runner, (MapComponent) mapPanel);
		add(details, BorderLayout.SOUTH);
		final JScrollPane scroller = new JScrollPane((JComponent) mapPanel);
		add(scroller, BorderLayout.CENTER);
		setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		setMaximumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		setMinimumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		createMenu(mapMenu);
		pack();
		repaint();
	}

	/**
	 * Set up the menu.
	 * @param mapMenu the map menu
	 */
	private void createMenu(final JMenu mapMenu) {
		final MenuItemCreator creator = new MenuItemCreator();
		final JMenuBar mbar = new JMenuBar();
		mbar.add(mapMenu);
		mbar.add(creator.createMenuItem("Restrict view",
				KeyEvent.VK_R,
				KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK),
				"Show only a subset of the map", this));
		mbar.add(new Filler(new Dimension(0, 0), new Dimension(0, 0),
				new Dimension(Integer.MAX_VALUE, 0)));
		mbar.add(creator.createMenuItem("Quit", KeyEvent.VK_Q,
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
	 * Handle button presses and the like.
	 * 
	 * @param event
	 *            the action we're handling
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		if ("Restrict view".equals(event.getActionCommand())) {
			new RestrictDialog(mapPanel).setVisible(true);
		} 
	}

}
