import java.awt.event {
    WindowAdapter,
    ActionEvent,
    WindowEvent
}

"""A simple listener to send a "Close" menu event when the user clicks the
   "Close" window-decoration button, since we now set windows to "do nothing on
   close"."""
shared class WindowCloseListener(Anything(ActionEvent) menuListener)
        extends WindowAdapter() {
    shared actual void windowClosing(WindowEvent evt) =>
            menuListener(ActionEvent(evt.source, ActionEvent.actionFirst, "Close"));
}
