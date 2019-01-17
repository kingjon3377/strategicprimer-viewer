import javax.swing {
    JOptionPane
}

import java.awt {
    Component
}

"Show an error dialog to the user."
shared void showErrorDialog(
        "The parent component for the dialog. [[JOptionPane]] doesn't seem to care if it
         is null."
        Component? parent,
        "What to title the dialog."
        String title,
        "The error message to show the user."
        String message) => JOptionPane.showMessageDialog(parent, message, title,
            JOptionPane.errorMessage);
