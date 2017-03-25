import java.awt.event {
    ActionListener,
    ActionEvent
}
import model.listeners {
    PlayerChangeSource,
    PlayerChangeListener
}
import ceylon.collection {
    MutableList,
    ArrayList,
    MutableMap,
    HashMap
}
import model.map {
    Player
}
import javax.swing {
    JOptionPane,
    JPopupMenu,
    SwingUtilities
}
import ceylon.interop.java {
    javaObjectArray
}
import java.awt {
    Component,
    Frame
}
import strategicprimer.viewer.model {
    IDriverModel
}
"""A class to respond to "change current player" menu items."""
shared class PlayerChangeMenuListener(IDriverModel model)
        satisfies ActionListener&PlayerChangeSource {
    MutableList<PlayerChangeListener> listeners = ArrayList<PlayerChangeListener>();
    shared actual void addPlayerChangeListener(PlayerChangeListener listener) =>
            listeners.add(listener);
    shared actual void removePlayerChangeListener(PlayerChangeListener listener) =>
            listeners.remove(listener);
    Component? eventSource(Anything obj) {
        if (is Component obj) {
            return obj;
        } else {
            return null;
        }
    }
    Frame? getContainingFrame(Component? component) {
        variable Component? temp = component;
        while (exists local = temp) {
            if (is Frame local) {
                return local;
            } else if (is JPopupMenu local) {
                temp = local.invoker;
            }  else {
                temp = local.parent;
            }
        }
        return null;
    }
    shared actual void actionPerformed(ActionEvent event) {
        Player currentPlayer = model.map.currentPlayer;
        {Player*} players = model.map.players;
        if (is Player retval = JOptionPane.showInputDialog(
                getContainingFrame(eventSource(event.source)),
                "Player to view:", "Choose New Player:",
                JOptionPane.plainMessage, null, javaObjectArray(Array<Player?>(players)),
                currentPlayer)) {
            for (listener in listeners) {
                listener.playerChanged(currentPlayer, retval);
            }
        }
    }
}
"A class to match menu item selections to the listeners to handle them. Note that at
 most one listener will be notified of any given action-command; subsequent registrations
 override previous ones."
shared class MenuBroker() satisfies ActionListener {
    """The mapping from "actions" to listeners to handle them."""
    MutableMap<String, Anything(ActionEvent)> mapping = HashMap<String, Anything(ActionEvent)>();
    "Rgister a listener for a series of action commands."
    shared void register(ActionListener|Anything(ActionEvent) listener, String* actions) {
        Anything(ActionEvent) actual;
        if (is ActionListener listener) {
            actual = listener.actionPerformed;
        } else {
            actual = listener;
        }
        for (action in actions) {
            mapping.put(action.lowercased, actual);
        }
    }
    "Handle an event by passing it to the listener that's registered to handle its action
     command. If none is registered, log a warning."
    shared actual void actionPerformed(ActionEvent event) {
        String action = event.actionCommand;
        if (exists listener = mapping.get(action.lowercased)) {
            SwingUtilities.invokeLater(() => listener(event));
        } else {
            log.warn("Unhandled action: ``action``");
        }
    }
}