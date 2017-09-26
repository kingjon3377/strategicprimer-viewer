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
"A superclass to perform setup common to dialogs."
shared class SPDialog(Frame? parentFrame, String title)
        extends JDialog(parentFrame, title) {
    defaultCloseOperation = WindowConstants.disposeOnClose;
    createHotKey(rootPane, "close", (ActionEvent event) => dispose(),
        JComponent.whenInFocusedWindow, KeyStroke.getKeyStroke(KeyEvent.vkW,
            platform.shortcutMask), KeyStroke.getKeyStroke(KeyEvent.vkEscape, 0));
}
