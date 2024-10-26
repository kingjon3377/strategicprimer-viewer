package drivers.gui.common;

import java.awt.Window;
import java.awt.event.ActionListener;
import java.util.function.Supplier;

/**
 * TODO: explain this class
 *
 * @author Jonathan Lovelace
 */
public interface IMenuBroker extends ActionListener {
	/**
	 * Rgister a listener for a series of action commands.
	 */
	void register(ActionListener listener, String... actions);

	/**
	 * Register a listener for an action command that shows the given window.
	 */
	void registerWindowShower(Window window, String... actions);

	/**
	 * Register a listener for an action command that shows the given window.
	 */
	void registerWindowShower(Supplier<Window> window, String... actions);
}
