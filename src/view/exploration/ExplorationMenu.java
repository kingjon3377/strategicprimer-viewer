package view.exploration;

import javax.swing.JFrame;
import javax.swing.JMenu;

import com.bric.window.WindowMenu;

import controller.map.misc.MultiIOHandler;
import model.exploration.IExplorationModel;
import view.util.SPMenu;

/**
 * Menus for the exploration GUI.
 *
 * @author Jonathan Lovelace
 *
 */
public class ExplorationMenu extends SPMenu {
	/**
	 * Constructor.
	 *
	 * @param ioh the I/O handler to handle I/O related items
	 * @param model the exploration model
	 * @param parent the window this is to be attached to, which should close on
	 *        "Close".
	 */
	public ExplorationMenu(final MultiIOHandler ioh,
			final IExplorationModel model, final JFrame parent) {
		add(createFileMenu(ioh, parent, model));
		JMenu mapMenu = createMapMenu(parent, model);
		add(mapMenu);
		mapMenu.setEnabled(false);
		add(new WindowMenu(parent));
	}
}
