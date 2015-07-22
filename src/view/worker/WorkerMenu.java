package view.worker;

import javax.swing.JFrame;

import com.bric.window.WindowMenu;

import controller.map.misc.IOHandler;
import model.misc.IDriverModel;
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
}
