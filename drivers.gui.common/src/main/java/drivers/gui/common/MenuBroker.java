package drivers.gui.common;

import javax.swing.SwingUtilities;
import java.util.Map;
import java.util.HashMap;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Window;
import java.util.function.Supplier;

import lovelace.util.LovelaceLogger;

/**
 * A class to match menu item selections to the listeners to handle them. Note
 * that at most one listener will be notified of any given action-command;
 * subsequent registrations override previous ones.
 */
public class MenuBroker implements ActionListener {
    /**
     * The mapping from "actions" to listeners to handle them.
     */
    private final Map<String, ActionListener> mapping = new HashMap<>();

    /**
     * Rgister a listener for a series of action commands.
     */
    public void register(final ActionListener listener, final String... actions) {
        for (final String action : actions) {
            mapping.put(action.toLowerCase(), listener);
        }
    }

    /**
     * Register a listener for an action command that shows the given window.
     */
    public void registerWindowShower(final Window window, final String... actions) {
        register((event) -> window.setVisible(true), actions);
    }

    /**
     * Register a listener for an action command that shows the given window.
     */
    public void registerWindowShower(final Supplier<Window> window, final String... actions) {
        register((event) -> window.get().setVisible(true), actions);
    }

    /**
     * Handle an event by passing it to the listener that's registered to
     * handle its action command. If none is registered, log a warning.
     */
    @Override
    public void actionPerformed(final ActionEvent event) {
        final String action = event.getActionCommand();
        if (mapping.containsKey(action.toLowerCase())) {
            final ActionListener listener = mapping.get(action.toLowerCase());
            SwingUtilities.invokeLater(() -> listener.actionPerformed(event));
        } else {
            LovelaceLogger.warning("Unhandled action: %s", action);
        }
    }
}
