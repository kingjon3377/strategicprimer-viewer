package view.exploration;

import javax.swing.JFrame;

import com.bric.window.WindowMenu;

import controller.map.misc.IOHandler;
import model.exploration.IExplorationModel;
import view.util.SPMenu;
import view.worker.PlayerChooserHandler;

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
	public ExplorationMenu(final IOHandler ioh,
			final IExplorationModel model, final JFrame parent) {
		add(createFileMenu(ioh, parent, model));
		addDisabled(createMapMenu(parent, model));
		addDisabled(createEditMenu(new PlayerChooserHandler(parent, model)));
		add(new WindowMenu(parent));
	}
}
