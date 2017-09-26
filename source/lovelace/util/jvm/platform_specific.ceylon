import java.awt.event {
    InputEvent
}

import javax.swing {
    JButton,
    JComponent
}
import java.lang {
    Types
}

"Set a String/String property pair in a way that won't blow up at runtime due to
 the difference between Ceylon and Java strings."
void setStringProperty(JComponent component, String key, String val) =>
        component.putClientProperty(Types.nativeString(key), Types.nativeString(val));
"An object encapsulating utility constants and functions that differ between Mac and
 non-Mac platforms."
shared object platform {
    "Whether this system is a Mac."
    shared Boolean systemIsMac = operatingSystem.name == "mac";
    "The usual shortcut-key modifier on this system."
    shared Integer shortcutMask;
    "A String describing that modifier."
    shared String shortcutDescription;
    if (systemIsMac) {
        shortcutMask = InputEvent.metaDownMask;
        shortcutDescription = "\{#2318}";
    } else {
        shortcutMask = InputEvent.ctrlDownMask;
        shortcutDescription = "Ctrl+";
    }
    "Make buttons segmented on Mac. Does nothing if zero or one buttons, or if not on
     Mac."
    shared void makeButtonsSegmented(JButton* buttons) {
        if (systemIsMac, exists first = buttons.first, buttons.rest.first exists) {
            setStringProperty(first, "JButton.buttonType", "segmented");
            setStringProperty(first, "JButton.segmentPosition", "first");
            variable JButton last = first;
            for (button in buttons.rest) {
                setStringProperty(button, "JButton.buttonType", "segmented");
                setStringProperty(button, "JButton.segmentPosition", "last");
                last = button;
            }
            setStringProperty(last, "JButton.segmentPosition", "last");
        }
    }
    "Whether the current platform's hotkey is pressed in the given event."
    shared Boolean hotKeyPressed(InputEvent event) =>
            (systemIsMac) then event.metaDown else event.controlDown;
}
