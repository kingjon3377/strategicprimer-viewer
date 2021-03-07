import java.awt.event {
    InputEvent
}

import javax.swing {
    KeyStroke
}

import java.awt {
    Toolkit
}

"An enumeration of possible modifiers to hot-keys."
shared class HotKeyModifier {
    "The mask to OR with the default mask when creating the hot-key."
    shared Integer mask;
    shared new shift { mask = InputEvent.shiftDownMask; }
    shared new ctrl { mask = InputEvent.ctrlDownMask; }
    shared new meta { mask = InputEvent.metaDownMask; }
}

"Create a key-stroke representing a hot-key accelerator."
shared KeyStroke createAccelerator(Integer key, HotKeyModifier* modifiers) =>
        KeyStroke.getKeyStroke(key, modifiers.map(HotKeyModifier.mask)
            .fold(Toolkit.defaultToolkit.menuShortcutKeyMask)(uncurry(Integer.or)));
