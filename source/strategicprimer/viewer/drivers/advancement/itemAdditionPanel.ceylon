import lovelace.util.common {
    todo,
    silentListener
}
import java.awt {
    Dimension
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
    ListenedButton,
    boxPanel,
    SimpleCardLayout
}

"A panel to be the GUI to add items to a list."
todo("Move to lovelace.util?",
     "At least make a FlipPanel (JPanel laid out by CardLayout with methods to
      flip forward and back) this can inherit from",
     "Or try to convert back to a class now we have SimpleCardLayout")
JPanel&AddRemoveSource itemAdditionPanel("What we're adding" String what) {
    MutableList<AddRemoveListener> listeners = ArrayList<AddRemoveListener>();
    JTextField field = JTextField(10);

    object retval extends JPanel() satisfies AddRemoveSource {
        shared actual void addAddRemoveListener(AddRemoveListener listener) =>
                listeners.add(listener);
        shared actual void removeAddRemoveListener(AddRemoveListener listener) =>
                listeners.remove(listener);
    }
    SimpleCardLayout layoutObj = SimpleCardLayout(retval);

    void setPanelSizes(JPanel panel) {
        panel.minimumSize = Dimension(60, 40);
        panel.preferredSize = Dimension(80, 50);
        panel.maximumSize = Dimension(90, 50);
    }
    setPanelSizes(retval);

    JPanel first = boxPanel(BoxAxis.lineAxis);
    first.add(ListenedButton("+", () {
        // I had wondered if Component.requestFocusInWindow() would make CardLayout flip
        // to the card containing the component, but it apparently doesn't work that way.
        layoutObj.goNext();
        field.requestFocusInWindow();
    }));
    setPanelSizes(first);
    retval.add(first);

    JPanel second = boxPanel(BoxAxis.pageAxis);
    second.add(field);

    void okListener() {
        String text = field.text;
        for (listener in listeners) {
            listener.add(what, text);
        }
        layoutObj.goFirst();
        field.text = "";
    }

    field.addActionListener(silentListener(okListener));
    field.setActionCommand("OK");

    JPanel okPanel = boxPanel(BoxAxis.lineAxis);
    JButton okButton = ListenedButton("OK", okListener);
    okPanel.add(okButton);

    JButton cancelButton = ListenedButton("Cancel", () {
        layoutObj.goFirst();
        field.text = "";
    });

    platform.makeButtonsSegmented(okButton, cancelButton);
    okPanel.add(cancelButton);

    second.add(okPanel);
    setPanelSizes(second);
    retval.add(second);

    return retval;
}
