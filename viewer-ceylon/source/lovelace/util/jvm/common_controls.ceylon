import java.awt.event {
    ActionListener,ActionEvent,
    KeyEvent,
    InputEvent
}
import javax.swing {
    JButton,
    JOptionPane,
    GroupLayout,
    JComboBox
}
import java.awt {
    Component,
    Container
}
import lovelace.util.common {
    todo
}
"A factory method to construct a button and add listeners to it in one step."
shared JButton listenedButton("The text to put on the button" String text,
        Anything(ActionEvent)|ActionListener* listeners) {
    JButton retval = JButton(text);
    for (listener in listeners) {
        if (is ActionListener listener) {
            retval.addActionListener(listener);
        } else {
            retval.addActionListener(listener);
        }
    }
    return retval;
}
"Show an error dialog to the user."
shared void showErrorDialog(
        "The parent component for the dialog. [[JOptionPane]] doesn't seem to care if it
         is null."
        Component? parent,
        "What to title the dialog."
        String title,
        "The error message to show the user."
        String message) {
    JOptionPane.showMessageDialog(parent, message, title, JOptionPane.errorMessage);
}
"An extension to [[GroupLayout]] to provide additional methods to make initialization
 less verbose and more functional in style."
shared class FunctionalGroupLayout(Container host) extends GroupLayout(host) {
    "Add components and/or groups to a group."
    T initializeGroup<T>(T group, Component|Group* components) given T satisfies Group {
        for (component in components) {
            switch (component)
            case (is Component) { group.addComponent(component); }
            case (is Group) { group.addGroup(component); }
        }
        return group;
    }
    "Factory for a parallel group."
    todo("Rename to parallelGroupOf()")
    shared ParallelGroup createParallelGroupOf(Component|Group* components) =>
        initializeGroup(createParallelGroup(), *components);
    "Factory for a sequential group."
    todo("Rename to sequentialGroupOf()")
    shared SequentialGroup createSequentialGroupOf(Component|Group* components) =>
        initializeGroup(createSequentialGroup(), *components);
}
"An extension to [[JComboBox]] to improve it by making the Tab key do what one expects."
shared class ImprovedComboBox<T>() extends JComboBox<T>() {
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