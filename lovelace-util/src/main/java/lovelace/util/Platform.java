package lovelace.util;

import java.awt.event.InputEvent;

import javax.swing.JButton;
import javax.swing.JComponent;

/**
 * An object encapsulating utility constants and functions that differ between
 * Mac and non-Mac platforms.
 *
 * TODO: Use Apache Commons SystemUtils?
 */
public final class Platform {
    /**
     * Don't instantiate.
     */
    private Platform() {
        // Don't instantiate.
    }

    /**
     * @deprecated Just use {@link JComponent#putClientProperty}.
     */
    @Deprecated
    public static void setStringProperty(final JComponent component, final String key, final String val) {
        component.putClientProperty(key, val);
    }

    /**
     * Whether this system is a Mac.
     */
    public static final boolean SYSTEM_IS_MAC =
            System.getProperty("os.name").toLowerCase().startsWith("mac");

    private static int initShortcutMask() {
        if (SYSTEM_IS_MAC) {
            return InputEvent.META_DOWN_MASK;
        } else {
            return InputEvent.CTRL_DOWN_MASK;
        }
    }

    private static String initShortcutDesc() {
        if (SYSTEM_IS_MAC) {
            return "\u2318";
        } else {
            return "Ctrl+";
        }
    }

    /**
     * The usual shortcut-key modifier on this system.
     */
    public static final int SHORTCUT_MASK = initShortcutMask();

    /**
     * A String describing the usual shortcut-key modifier on this system.
     */
    public static final String SHORTCUT_DESCRIPTION = initShortcutDesc();

    /**
     * Make buttons segmented on Mac. Does nothing if zero or one buttons, or if not on Mac.
     */
    public static void makeButtonsSegmented(final JButton... buttons) {
        if (SYSTEM_IS_MAC && buttons.length > 1) {
            final JButton first = buttons[0];
            first.putClientProperty("JButton.buttonType", "segmented");
            first.putClientProperty("JButton.segmentPosition", "first");
            JButton last = first;
            boolean initial = true;
            for (final JButton button : buttons) {
                if (initial) {
                    initial = false;
                    continue;
                }
                button.putClientProperty("JButton.buttonType", "segmented");
                button.putClientProperty("JButton.segmentPosition", "middle");
                last = button;
            }
            last.putClientProperty("JButton.segmentPosition", "last");
        }
    }

    /**
     * Whether the current platform's hotkey is pressed in the given event.
     */
    public static boolean isHotKeyPressed(final InputEvent event) {
        if (SYSTEM_IS_MAC) {
            return event.isMetaDown();
        } else {
            return event.isControlDown();
        }
    }
}
