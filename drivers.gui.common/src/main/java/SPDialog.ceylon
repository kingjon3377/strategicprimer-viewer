import java.awt {
    Frame
}
import java.awt.event {
    ActionEvent,
    KeyEvent
}

import javax.swing {
    JDialog,
    WindowConstants,
    JComponent,
    KeyStroke
}

import lovelace.util.jvm {
    createHotKey,
    platform
}
import lovelace.util.common {
    silentListener
}

"A superclass to perform setup common to dialogs: unlike app windows (which do
 nothing on close to let us ask the user to confirm closing a modified file)
 dialogs are set to dispose on close, and we set up hotkeys so both
 Command/Control+W and Escape hide and dispose the window. We also add a
 [[showWindow]] method so calling code doesn't have to create a lambda (or call
 [[lovelace.util.common::defer]]; I seem to recall that passing around Callable
 objects isn't free ...) to show a dialog."
shared class SPDialog(Frame? parentFrame, String title)
        extends JDialog(parentFrame, title) {
    defaultCloseOperation = WindowConstants.disposeOnClose;
    createHotKey(rootPane, "close", silentListener<ActionEvent>(dispose),
        JComponent.whenInFocusedWindow, KeyStroke.getKeyStroke(KeyEvent.vkW,
            platform.shortcutMask), KeyStroke.getKeyStroke(KeyEvent.vkEscape, 0));
    "Show the dialog."
    shared void showWindow() => setVisible(true);
}
