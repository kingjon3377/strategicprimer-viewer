package view.map.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

import model.map.MapView;
import model.viewer.MapModel;
import view.util.DriverQuit;
import view.util.MenuItemCreator;
import controller.map.drivers.ViewerStart;
import controller.map.misc.IOHandler;

/**
 * A class encapsulating the menus.
 *
 * @author Jonathan Lovelace
 *
 */
public class SPMenu extends JMenuBar {
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
		add(createFileMenu(handler, parent));
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
	}

	/**
	 * The helper to create menu items for us.
	 */
	private final transient MenuItemCreator creator = new MenuItemCreator();

	/**
	 * Create the map menu.
	 *
	 * @param handler the class to handle I/O related menu items
	 * @param parent the menu-bar's parent window---the window to close on "Close".
	 * @return the map menu.
	 */
	private JMenu createFileMenu(final IOHandler handler, final JFrame parent) {
		final JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(creator.createMenuItem("New", KeyEvent.VK_N,
				KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK),
				"Create a new map the same size as the current one",
				handler));
		fileMenu.add(creator.createMenuItem("Load", KeyEvent.VK_L,
				KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK),
				"Load a map from file", handler));
		fileMenu.add(creator.createMenuItem("Save As", KeyEvent.VK_S,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK),
				"Save the map to file", handler));
		fileMenu.addSeparator();
		fileMenu.add(creator.createMenuItem("Close", KeyEvent.VK_W,
				KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK),
				"Close this window", new ActionListener() {
					/**
					 * Close the window when pressed.
					 *
					 * @param evt the event to handle
					 */
					@Override
					public void actionPerformed(final ActionEvent evt) {
						if ("Close".equals(evt.getActionCommand())) {
							parent.setVisible(false);
							parent.dispose();
						}
					}
				}));
		fileMenu.add(creator.createMenuItem("Quit", KeyEvent.VK_Q,
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
		return fileMenu;
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "SPMenu";
	}
}
