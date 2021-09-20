import java.awt.event {
    ActionListener,
    ActionEvent
}

import javax.swing {
    JButton
}
import lovelace.util.common {
    silentListener
}

"A button that takes its listeners as initializer parameters."
shared class ListenedButton("The text to put on the button" String text,
        Anything(ActionEvent)|Anything()|ActionListener* listeners) extends JButton(text) {
    for (listener in listeners) {
        if (is ActionListener listener) {
            addActionListener(listener);
        } else if (is Anything() listener) {
            addActionListener(silentListener(listener));
        } else {
            addActionListener(listener);
        }
    }
}
