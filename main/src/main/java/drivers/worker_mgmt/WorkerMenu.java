package drivers.worker_mgmt;

import javax.swing.JMenuBar;
import java.awt.event.ActionListener;
import drivers.common.ISPDriver;
import drivers.gui.common.SPMenu;
import java.awt.Component;

/**
 * A set of menus for the worker GUI (and other related apps).
 */
public class WorkerMenu {
	private WorkerMenu() {}
	/**
	 * @param handler The broker that handles menu items, or arranges for them to be handled
	 * @param component Any component in the window this is to be attached
	 * to, which should close on "Close"
	 * @param driver The current driver
	 */
	public static JMenuBar workerMenu(final ActionListener handler, final Component component,
	                                  final ISPDriver driver) {
		return SPMenu.forWindowContaining(component, SPMenu.createFileMenu(handler, driver),
			SPMenu.disabledMenu(SPMenu.createMapMenu(handler, driver)),
			SPMenu.createViewMenu(handler, driver));
	}
}
