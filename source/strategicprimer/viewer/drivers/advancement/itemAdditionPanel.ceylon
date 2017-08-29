import lovelace.util.common {
    todo
}
import java.awt {
    CardLayout,
    Dimension
}
import java.awt.event {
    ActionEvent
}
import javax.swing {
    JButton,
    JTextField,
    JPanel
}
import ceylon.collection {
    ArrayList,
    MutableList
}
import lovelace.util.jvm {
    platform,
    BoxAxis,
    listenedButton,
    boxPanel
}
"A panel to be the GUI to add items to a list."
todo("Move to lovelace.util?")
JPanel&AddRemoveSource itemAdditionPanel("What we're adding" String what) {
    CardLayout layoutObj = CardLayout();
    MutableList<AddRemoveListener> listeners = ArrayList<AddRemoveListener>();
    JTextField field = JTextField(10);
    object retval extends JPanel(layoutObj) satisfies AddRemoveSource {
        shared actual void addAddRemoveListener(AddRemoveListener listener) =>
                listeners.add(listener);
        shared actual void removeAddRemoveListener(AddRemoveListener listener) =>
                listeners.remove(listener);
    }
    void setPanelSizes(JPanel panel) {
        panel.minimumSize = Dimension(60, 40);
        panel.preferredSize = Dimension(80, 50);
        panel.maximumSize = Dimension(90, 50);
    }
    setPanelSizes(retval);
    JPanel first = boxPanel(BoxAxis.lineAxis);
    first.add(listenedButton("+"), (ActionEvent event) {
        layoutObj.next(retval);
        field.requestFocusInWindow();
    });
    setPanelSizes(first);
    retval.add(first);
    JPanel second = boxPanel(BoxAxis.pageAxis);
    second.add(field);
    void okListener(ActionEvent event) {
        String text = field.text;
        for (listener in listeners) {
            listener.add(what, text);
        }
        layoutObj.first(retval);
        field.text = "";
    }
    field.addActionListener(okListener);
    field.setActionCommand("OK");
    JPanel okPanel = boxPanel(BoxAxis.lineAxis);
    JButton okButton = listenedButton("OK", okListener);
    okPanel.add(okButton);
    JButton cancelButton = listenedButton("Cancel", (ActionEvent event) {
        layoutObj.first(retval);
        field.text = "";
    });
    platform.makeButtonsSegmented(okButton, cancelButton);
    okPanel.add(cancelButton);
    second.add(okPanel);
    setPanelSizes(second);
    retval.add(second);
    return retval;
}
