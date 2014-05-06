package view.worker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.eclipse.jdt.annotation.Nullable;

import view.util.DriverQuit;
import view.util.MenuItemCreator;
import view.window.WindowMenu;
import controller.map.misc.IOHandler;

/**
 * A set of menus for the worker GUI.
 *
 * @author Jonathan Lovelace
 *
 */
public class WorkerMenu extends JMenuBar {
	/**
	 * Constructor.
	 *
	 * @param handler the I/O handler to handle I/O related items
	 * @param parent the window this is to be attached to, which should close on
	 *        "Close".
	 * @param pch a handler to listen to the 'change player' menu item.
	 */
	public WorkerMenu(final IOHandler handler, final JFrame parent,
			final PlayerChooserHandler pch) {
		final JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(MenuItemCreator.createMenuItem("Load", KeyEvent.VK_L,
				MenuItemCreator.createHotkey(KeyEvent.VK_O),
				"Load a map from file", handler));
		fileMenu.add(MenuItemCreator.createMenuItem("Save", KeyEvent.VK_S,
				MenuItemCreator.createHotkey(KeyEvent.VK_S),
				"Save the map to the file it was loaded from", handler));
		fileMenu.add(MenuItemCreator.createMenuItem("Save As", KeyEvent.VK_A,
				MenuItemCreator.createShiftHotkey(KeyEvent.VK_S),
				"Save the map to file", handler));
		fileMenu.addSeparator();
		fileMenu.add(MenuItemCreator.createMenuItem("Close", KeyEvent.VK_W,
				MenuItemCreator.createHotkey(KeyEvent.VK_W),
				"Close this window", new ActionListener() {
					/**
					 * Close the window when pressed.
					 *
					 * @param evt the event to handle
					 */
					@Override
					public void actionPerformed(@Nullable final ActionEvent evt) {
						if (evt != null
								&& "Close".equals(evt.getActionCommand())) {
							parent.setVisible(false);
							parent.dispose();
						}
					}
				}));
		fileMenu.addSeparator();
		fileMenu.add(MenuItemCreator.createMenuItem("Quit", KeyEvent.VK_Q,
				MenuItemCreator.createHotkey(KeyEvent.VK_Q),
				"Quit the application", new ActionListener() {
					/**
					 * Handle the menu "button" press.
					 *
					 * @param event the event to handle
					 */
					@Override
					public void actionPerformed(
							@Nullable final ActionEvent event) {
						if (event != null
								&& "Quit".equals(event.getActionCommand())) {
							DriverQuit.quit(0);
						}
					}
				}));
		add(fileMenu);
		final JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		editMenu.add(MenuItemCreator.createMenuItem(
				PlayerChooserHandler.MENU_ITEM, KeyEvent.VK_P,
				MenuItemCreator.createHotkey(KeyEvent.VK_P),
				"Look at a different player's units and workers", pch));
		add(editMenu);
		add(new WindowMenu());
	}
}
