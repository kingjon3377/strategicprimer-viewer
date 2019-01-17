import java.awt.event {
    ActionEvent
}

import javax.swing {
    KeyStroke,
    InputMap,
    JComponent,
    JMenuItem
}

import lovelace.util.common {
    silentListener
}

"Create a menu item."
shared JMenuItem createMenuItem(
        "The text of the item"
        String item,
        "The mnemonic key"
        Integer mnemonic,
        "The description to show to accessibility software."
        String description,
        "The listener to handle when the item is selected."
        Anything(ActionEvent)|Anything() listener,
        "The keyboard accelerators (hot-keys). The first one is shown in the menu, but all
         are listened for."
        KeyStroke* accelerators) {
    JMenuItem menuItem = JMenuItem(item, mnemonic);
    if (exists accelerator = accelerators.first) {
        menuItem.accelerator = accelerator;
    }
    menuItem.accessibleContext.accessibleDescription = description;
    if (is Anything(ActionEvent) listener) {
        menuItem.addActionListener(listener);
    } else {
        menuItem.addActionListener(silentListener(listener));
    }
    InputMap inputMap = menuItem.getInputMap(JComponent.whenInFocusedWindow);
    for (accelerator in accelerators) {
        inputMap.put(accelerator, menuItem.action);
    }
    return menuItem;
}
