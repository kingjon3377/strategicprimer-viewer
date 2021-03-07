import java.awt.event {
    InputEvent,
    KeyEvent
}

import java.awt {
    Component
}

import javax.swing {
    JComboBox,
    ComboBoxModel
}

"An extension to [[JComboBox]] to improve it by making the Tab key do what one expects."
shared class ImprovedComboBox<Element> extends JComboBox<Element> {
    shared new () extends JComboBox<Element>() { }
    shared new withModel(ComboBoxModel<Element> boxModel)
        extends JComboBox<Element>(boxModel) { }
    editable = true;
    "Handle a key-press. If Tab is pressed when the pop-up list is visible, treat it like
     Enter."
    by("http://stackoverflow.com/a/24336768")
    shared actual void processKeyEvent(KeyEvent event) {
        if (event.id != KeyEvent.keyPressed || event.keyCode != KeyEvent.vkTab) {
            super.processKeyEvent(event);
            return;
        }
        if (popupVisible) {
            assert (is Component source = event.source);
            super.processKeyEvent(KeyEvent(source, event.id, event.when, 0,
                KeyEvent.vkEnter, KeyEvent.charUndefined));
        }
        if (event.modifiers == 0) {
            transferFocus();
        } else if (event.modifiers == InputEvent.shiftMask) {
            transferFocusBackward();
        }
    }
}
