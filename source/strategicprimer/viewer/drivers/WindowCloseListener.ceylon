import java.awt.event {
    WindowAdapter,
    ActionEvent,
    WindowEvent
}
shared class WindowCloseListener(Anything(ActionEvent) menuListener)
		extends WindowAdapter() {
	shared actual void windowClosing(WindowEvent evt) =>
			menuListener(ActionEvent(evt.source, ActionEvent.actionFirst, "Close"));
}