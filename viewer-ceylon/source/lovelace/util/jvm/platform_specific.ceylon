import java.lang {
    JSystem=System
}
import java.awt.event {
    InputEvent
}
import javax.swing {
    JButton,
    JComponent
}
import ceylon.interop.java {
    javaString
}
"Set a String/String property pair in a way that won't blow up at runtime due to
 the difference between Ceylon and Java strings."
void setStringProperty(JComponent component, String key, String val) =>
        component.putClientProperty(javaString(key), javaString(val));
"An object encapsulating utility constants and functions that differ between Mac and
 non-Mac platforms."
shared object platform {
    "Whether this system is a Mac."
    shared Boolean systemIsMac;
    if (exists temp = JSystem.getProperty("os.name")) {
        systemIsMac = temp.lowercased.startsWith("mac os x");
    } else {
        systemIsMac = false;
    }
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
            variable {JButton*} temp = buttons.rest;
            while (exists button = temp.first) {
                setStringProperty(button, "JButton.buttonType", "segmented");
                if (!temp.rest.first exists) {
                    setStringProperty(button, "JButton.segmentPosition", "last");
                }
                temp = temp.rest;
            }
        }
    }
    "Whether the current platform's hotkey is pressed in the given event."
    shared Boolean hotKeyPressed(InputEvent event) {
        if (systemIsMac) {
            return event.metaDown;
        } else {
            return event.controlDown;
        }
    }
}