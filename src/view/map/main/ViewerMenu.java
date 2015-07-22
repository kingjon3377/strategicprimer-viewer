package view.map.main;

import javax.swing.JFrame;

import com.bric.window.WindowMenu;

import controller.map.misc.IOHandler;
import model.viewer.IViewerModel;
import view.util.SPMenu;
import view.worker.PlayerChooserHandler;

/**
 * A class encapsulating the menus.
 *
 * @author Jonathan Lovelace
 *
 */
public class ViewerMenu extends SPMenu {
	/**
	 * Constructor.
	 *
	 * @param handler the I/O handler to handle I/O related items
	 * @param parent the frame we'll be attached to
	 * @param model the map model
	 */
	public ViewerMenu(final IOHandler handler, final JFrame parent,
			final IViewerModel model) {
		add(createFileMenu(handler, parent, model));
		add(createMapMenu(parent, model));
		addDisabled(createEditMenu(new PlayerChooserHandler(parent, model)));
		add(new WindowMenu(parent));
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "SPMenu";
	}
}
