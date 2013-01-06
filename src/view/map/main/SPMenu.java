package view.map.main;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Box.Filler;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

import model.viewer.MapModel;
import view.util.DriverQuit;
import view.util.MenuItemCreator;
import controller.map.misc.IOHandler;

/**
 * A class encapsulating the menus.
 *
 * @author Jonathan Lovelace
 *
 */
public class SPMenu extends JMenuBar {
	/**
	 * Command to load the secondary map.
	 */
	private static final String LOAD_ALT_MAP_CMD = "<html><p>Load secondary map</p></html>";
	/**
	 * Command to save the secondary map.
	 */
	private static final String SAVE_ALT_MAP_CMD = "<html><p>Save secondary map</p></html>";

	/**
	 * Constructor.
	 *
	 * @param handler the I/O handler to handle I/O related items
	 * @param parent the frame we'll be attached to
	 * @param model the map model
	 */
	public SPMenu(final IOHandler handler, final JFrame parent,
			final MapModel model) {
		super();
		add(createMapMenu(handler));
		add(creator.createMenuItem("Go to tile", KeyEvent.VK_G,
				KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK),
				"Go to a tile by coordinates", new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent event) {
						if ("Go to tile".equals(event.getActionCommand())) {
							new SelectTileDialog(parent, model)
									.setVisible(true);
						}
					}
				}));
		final FindDialog finder = new FindDialog(parent, model);
		add(creator.createMenuItem("Find a fixture", KeyEvent.VK_SLASH,
				KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0),
				"Find a fixture by name, kind, or ID#", new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent event) {
						if ("Find a fixture".equals(event.getActionCommand())) {
							finder.setVisible(true);
						}
					}
				}));
		add(creator.createMenuItem("Find next", KeyEvent.VK_N,
				KeyStroke.getKeyStroke(KeyEvent.VK_N, 0),
				"Find the next fixture matching the pattern",
				new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent event) {
						if ("Find next".equals(event.getActionCommand())) {
							finder.search();
						}
					}
				}));
		add(new Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(
				Integer.MAX_VALUE, 0)));
		add(creator.createMenuItem("Quit", KeyEvent.VK_Q,
				KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK),
				"Quit the viewer", new ActionListener() {
					/**
					 * Handle the menu "button" press.
					 *
					 * @param event the event to handle
					 */
					@Override
					public void actionPerformed(final ActionEvent event) {
						if ("Quit".equals(event.getActionCommand())) {
							DriverQuit.quit(0);
						}
					}
		}));
	}

	/**
	 * The helper to create menu items for us.
	 */
	private final MenuItemCreator creator = new MenuItemCreator();

	/**
	 * Create the map menu.
	 *
	 * @param handler the class to handle I/O related menu items
	 * @return the map menu.
	 */
	private JMenu createMapMenu(final IOHandler handler) {
		final JMenu mapMenu = new JMenu("Map");
		mapMenu.setMnemonic(KeyEvent.VK_M);
		mapMenu.add(creator.createMenuItem("Load", KeyEvent.VK_L,
				KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK),
				"Load a main map from file", handler));
		mapMenu.add(creator.createMenuItem("Save As", KeyEvent.VK_S,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK),
				"Save the main map to file", handler));
		mapMenu.addSeparator();
		mapMenu.add(creator.createMenuItem(
				LOAD_ALT_MAP_CMD,
				KeyEvent.VK_D,
				KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK
						+ ActionEvent.ALT_MASK),
				"Load a secondary map from file", handler));
		mapMenu.add(creator.createMenuItem(
				SAVE_ALT_MAP_CMD,
				KeyEvent.VK_V,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK
						+ ActionEvent.ALT_MASK),
				"Save the secondary map to file", handler));
		mapMenu.addSeparator();
		mapMenu.add(creator.createMenuItem("Switch maps", KeyEvent.VK_W,
				KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK),
				"Make the secondary map the main map and vice versa", handler));
		return mapMenu;
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "SPMenu";
	}
}
