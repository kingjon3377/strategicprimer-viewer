import java.awt.event {
    ActionListener,
    ActionEvent
}

import javax.swing {
    JButton
}

"A button that takes its listeners as initializer parameters."
shared class ListenedButton("The text to put on the button" String text,
        Anything(ActionEvent)|ActionListener* listeners) extends JButton(text) {
    for (listener in listeners) {
        if (is ActionListener listener) {
            addActionListener(listener);
        } else {
            addActionListener(listener);
        }
    }
}
