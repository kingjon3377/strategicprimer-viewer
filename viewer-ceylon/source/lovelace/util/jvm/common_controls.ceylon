import java.awt.event {
    ActionListener,ActionEvent
}
import javax.swing {
    JButton,
    JOptionPane
}
import java.awt {
    Component
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