package view.exploration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import view.util.DriverQuit;
import view.util.MenuItemCreator;
import controller.map.misc.MultiIOHandler;
/**
 * Menus for the exploration GUI.
 * @author Jonathan Lovelace
 *
 */
public class ExplorationMenu extends JMenuBar {
	/**
	 * The helper to create menu items for us.
	 */
	private final transient MenuItemCreator creator = new MenuItemCreator();
	/**
	 * Constructor.
	 * @param handler the I/O handler to handle I/O related items
	 * @param parent the window this is to be attached to, which should close on "Close".
	 */
	public ExplorationMenu(final MultiIOHandler handler, final JFrame parent) {
		final JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(creator.createMenuItem("Load", KeyEvent.VK_L,
				creator.createHotkey(KeyEvent.VK_O),
				"Load the main map from file", handler));
		fileMenu.add(creator.createMenuItem("Load secondary", KeyEvent.VK_E,
				creator.createShiftHotkey(KeyEvent.VK_O),
				"Load an additional secondary map from file", handler));
		fileMenu.add(creator.createMenuItem("Save", KeyEvent.VK_S,
				creator.createHotkey(KeyEvent.VK_S),
				"Save the main map to the file it was loaded from", handler));
		fileMenu.add(creator.createMenuItem("Save As", KeyEvent.VK_A,
				creator.createShiftHotkey(KeyEvent.VK_S),
				"Save the main map to file", handler));
		fileMenu.add(creator.createMenuItem("Save All", KeyEvent.VK_V,
				creator.createHotkey(KeyEvent.VK_L),
				"Save all maps to their files", handler));
		fileMenu.addSeparator();
		fileMenu.add(creator.createMenuItem("Close", KeyEvent.VK_W,
				creator.createHotkey(KeyEvent.VK_W),
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
		fileMenu.addSeparator();
		fileMenu.add(creator.createMenuItem("Quit", KeyEvent.VK_Q,
				creator.createHotkey(KeyEvent.VK_Q),
				"Quit the application", new ActionListener() {
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
		add(fileMenu);
	}
}
