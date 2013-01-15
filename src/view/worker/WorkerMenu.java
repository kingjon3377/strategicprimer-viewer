package view.worker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

import view.util.DriverQuit;
import view.util.MenuItemCreator;
import controller.map.misc.IOHandler;
/**
 * A set of menus for the worker GUI.
 * @author Jonathan Lovelace
 *
 */
public class WorkerMenu extends JMenuBar {
	/**
	 * The helper to create menu items for us.
	 */
	private final MenuItemCreator creator = new MenuItemCreator();
	/**
	 * Constructor.
	 * @param handler the I/O handler to handle I/O related items
	 */
	public WorkerMenu(final IOHandler handler) {
		final JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(creator.createMenuItem("Load", KeyEvent.VK_L,
				KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK),
				"Load a map from file", handler));
		fileMenu.add(creator.createMenuItem("Save As", KeyEvent.VK_S,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK),
				"Save the map to file", handler));
		fileMenu.addSeparator();
		fileMenu.add(creator.createMenuItem("Quit", KeyEvent.VK_Q,
				KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK),
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
