package view.worker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;

import org.eclipse.jdt.annotation.Nullable;

import com.bric.window.WindowMenu;

import controller.map.misc.IOHandler;
import model.misc.IDriverModel;
import view.util.MenuItemCreator;
import view.util.SPMenu;

/**
 * A set of menus for the worker GUI.
 *
 * @author Jonathan Lovelace
 *
 */
public class WorkerMenu extends SPMenu {
	/**
	 * Constructor.
	 *
	 * @param handler the I/O handler to handle I/O related items
	 * @param parent the window this is to be attached to, which should close on
	 *        "Close".
	 * @param pch a handler to listen to the 'change player' menu item.
	 * @param model the current driver model
	 */
	public WorkerMenu(final IOHandler handler, final JFrame parent,
			final PlayerChooserHandler pch, final IDriverModel model) {
		add(createFileMenu(handler, parent, model));
		addDisabled(createMapMenu(parent, model));
		add(createEditMenu(pch));
		add(new WindowMenu(parent));
	}
	/**
	 * Create the "edit"menu.
	 * @param pch the object to notify when the user selects a different player
	 * @return the "edit" menu
	 */
	private static JMenu createEditMenu(final PlayerChooserHandler pch) {
		final JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		editMenu.add(MenuItemCreator.createMenuItem(
				PlayerChooserHandler.MENU_ITEM, KeyEvent.VK_P,
				MenuItemCreator.createHotkey(KeyEvent.VK_P),
				"Look at a different player's units and workers", pch));
		editMenu.add(MenuItemCreator.createMenuItem("Reload tree",
				KeyEvent.VK_R, MenuItemCreator.createHotkey(KeyEvent.VK_R),
				"Refresh the view of the workers", new ActionListener() {

			@Override
			public void actionPerformed(@Nullable final ActionEvent e) {
				pch.reload();
			}
		}));
		return editMenu;
	}
}
