import ceylon.collection {
    ArrayList,
    MutableList
}

import strategicprimer.model.common.map {
    Point
}
import strategicprimer.drivers.common {
    SelectionChangeListener,
    SelectionChangeSource
}

"A helper class to proxy selection-changing calls."
shared class SelectionChangeSupport() satisfies SelectionChangeSource {
    "The list of listeners to notify."
    MutableList<SelectionChangeListener> listeners = ArrayList<SelectionChangeListener>();

    "Notify the given listener of future selection changes."
    shared actual void addSelectionChangeListener(SelectionChangeListener listener) =>
            listeners.add(listener);

    "Stop notifying the given listener of selection changes."
    shared actual void removeSelectionChangeListener(SelectionChangeListener listener) =>
            listeners.remove(listener);

    "Tell all listeners about a change.

     We don't satisfy SelectionChangeListener ourselves to avoid accidental infinite
     recursion."
    shared void fireChanges(Point? oldSelection, Point newSelection) {
        for (listener in listeners) {
            listener.selectedPointChanged(oldSelection, newSelection);
        }
    }

    "Tell all listeners about a change to the cursor location."
    shared void fireCursorChanges(Point? oldCursor, Point newCursor) {
        for (listener in listeners) {
            listener.cursorPointChanged(oldCursor, newCursor);
        }
    }

    "Tell all listeners about a change to the interaction point."
    shared void fireInteraction() {
        for (listener in listeners) {
            listener.interactionPointChanged();
        }
    }
}
