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

    "Get the window containing the given component. This goes beyond
     [[ComponentParentStream|lovelace.util.jvm::ComponentParentStream]] in that
     we get the invoker of any [[pop-up menu|JPopupMenu]], and we also throw
     away the results of the intermediate steps."
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

    "Handle the event caused by the player choosing the menu item: show a
     dialog asking the user to choose the new current player. Once the user
     has done so, notify all listeners of the change."
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
            getContainingFrame(as<Component>(event.source)), // TODO: fix indentation here
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
