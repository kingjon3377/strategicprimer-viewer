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
    ArrayList
}
import model.misc {
    IDriverModel
}
import model.map {
    Player
}
import javax.swing {
    JOptionPane,
    JPopupMenu
}
import ceylon.interop.java {
    CeylonIterable,
    javaObjectArray
}
import java.awt {
    Component,
    Frame
}
"""A class to respond to "change current player" menu items."""
class PlayerChangeMenuListener(IDriverModel model)
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
        Iterable<Player> players = CeylonIterable(model.map.players());
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