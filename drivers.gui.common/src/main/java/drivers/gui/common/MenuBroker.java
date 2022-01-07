package drivers.gui.common;

import javax.swing.SwingUtilities;
import java.util.Map;
import java.util.HashMap;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Window;
import java.util.logging.Logger;
import java.util.function.Supplier;

/**
 * A class to match menu item selections to the listeners to handle them. Note
 * that at most one listener will be notified of any given action-command;
 * subsequent registrations override previous ones.
 */
public class MenuBroker implements ActionListener {
	private static final Logger LOGGER = Logger.getLogger(MenuBroker.class.getName());
	/**
	 * The mapping from "actions" to listeners to handle them.
	 */
	private final Map<String, ActionListener> mapping = new HashMap<>();

	/**
	 * Rgister a listener for a series of action commands.
	 */
	public void register(ActionListener listener, String... actions) {
		for (String action : actions) {
			mapping.put(action.toLowerCase(), listener);
		}
	}

	/**
	 * Register a listener for an action command that shows the given window.
	 */
	public void registerWindowShower(Window window, String... actions) {
		register((event) -> window.setVisible(true), actions);
	}

	/**
	 * Register a listener for an action command that shows the given window.
	 */
	public void registerWindowShower(Supplier<Window> window, String... actions) {
		register((event) -> window.get().setVisible(true), actions);
	}

	/**
	 * Handle an event by passing it to the listener that's registered to
	 * handle its action command. If none is registered, log a warning.
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		String action = event.getActionCommand();
		if (mapping.containsKey(action.toLowerCase())) {
			ActionListener listener = mapping.get(action.toLowerCase());
			SwingUtilities.invokeLater(() -> listener.actionPerformed(event));
		} else {
			LOGGER.warning("Unhandled action: " + action);
		}
	}
}
