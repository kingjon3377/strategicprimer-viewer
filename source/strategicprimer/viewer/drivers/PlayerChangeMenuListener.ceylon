import lovelace.util.common {
    as
}
import java.lang {
    ObjectArray
}
import javax.swing {
    JOptionPane,
    JPopupMenu
}
import strategicprimer.drivers.common {
    IDriverModel,
    PlayerChangeListener,
    IWorkerModel
}
import java.awt.event {
    ActionListener,
    ActionEvent
}
import strategicprimer.viewer.drivers.exploration {
    PlayerChangeSource
}
import ceylon.collection {
    MutableList,
    ArrayList
}
import java.awt {
    Frame,
    Component
}
import strategicprimer.model.common.map {
    Player
}
"""A class to respond to "change current player" menu items."""
shared class PlayerChangeMenuListener(IDriverModel model)
        satisfies ActionListener&PlayerChangeSource {
    MutableList<PlayerChangeListener> listeners = ArrayList<PlayerChangeListener>();
    shared actual void addPlayerChangeListener(PlayerChangeListener listener) =>
            listeners.add(listener);
    shared actual void removePlayerChangeListener(PlayerChangeListener listener) =>
            listeners.remove(listener);
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
        Player currentPlayer;
        {Player*} players;
        if (is IWorkerModel model) {
            currentPlayer = model.currentPlayer;
            players = model.players;
        } else {
            currentPlayer = model.map.currentPlayer;
            players = model.map.players;
        }
        if (is Player retval = JOptionPane.showInputDialog(
            getContainingFrame(as<Component>(event.source)),
            "Player to view:", "Choose New Player:",
            JOptionPane.plainMessage, null, ObjectArray.with(players),
            currentPlayer)) {
            if (is IWorkerModel model) {
                model.currentPlayer = retval;
            }
            for (listener in listeners) {
                listener.playerChanged(currentPlayer, retval);
            }
        }
    }
}
