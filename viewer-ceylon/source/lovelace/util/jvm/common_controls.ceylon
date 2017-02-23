import java.awt.event {
    ActionListener,ActionEvent
}
import javax.swing {
    JButton
}
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