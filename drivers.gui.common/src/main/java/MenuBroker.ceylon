import javax.swing {
    SwingUtilities
}
import ceylon.collection {
    MutableMap,
    HashMap
}
import java.awt.event {
    ActionEvent,
    ActionListener
}
import java.awt {
    Window
}
import lovelace.util.common {
    defer
}

"A class to match menu item selections to the listeners to handle them. Note that at
 most one listener will be notified of any given action-command; subsequent registrations
 override previous ones."
shared class MenuBroker() satisfies ActionListener {
    """The mapping from "actions" to listeners to handle them."""
    MutableMap<String, Anything(ActionEvent)> mapping =
            HashMap<String, Anything(ActionEvent)>();

    "Rgister a listener for a series of action commands."
    shared void register(ActionListener|Anything(ActionEvent) listener, String* actions) {
        Anything(ActionEvent) actual;
        if (is ActionListener listener) {
            actual = listener.actionPerformed;
        } else {
            actual = listener;
        }
        for (action in actions) {
            mapping[action.lowercased] = actual;
        }
    }

    "Register a listener for an action command that shows the given window."
    shared void registerWindowShower(Window()|Window window, String* actions) {
        if (is Window window) {
            register((event) => window.setVisible(true), *actions);
        } else {
            register((event) => window().setVisible(true), *actions);
        }
    }

    "Handle an event by passing it to the listener that's registered to handle its action
     command. If none is registered, log a warning."
    shared actual void actionPerformed(ActionEvent event) {
        String action = event.actionCommand;
        if (exists listener = mapping[action.lowercased]) {
            SwingUtilities.invokeLater(defer(listener, [event]));
        } else {
            log.warn("Unhandled action: ``action``");
        }
    }
}
