import strategicprimer.drivers.gui.common {
    SPDialog
}
import strategicprimer.drivers.common {
    NewFixtureSource,
    NewFixtureListener
}
import ceylon.collection {
    ArrayList,
    MutableList
}
import javax.swing {
    JTextArea,
    JButton
}
import strategicprimer.model.common.map.fixtures {
    TextFixture
}
import lovelace.util.jvm {
    ListenedButton,
    BorderedPanel,
    platform
}
import java.awt {
    Container,
    Dimension
}

"A dialog to let the user add a text note to a tile."
// TODO: Add the ability to edit an existing note.
shared class TextNoteDialog(Integer() currentTurn) extends SPDialog(null, "Add Text Note") satisfies NewFixtureSource {
    MutableList<NewFixtureListener> listeners = ArrayList<NewFixtureListener>();
    shared actual void addNewFixtureListener(NewFixtureListener listener) => listeners.add(listener);
    shared actual void removeNewFixtureListener(NewFixtureListener listener) => listeners.remove(listener);

    JTextArea noteField = JTextArea(15, 3);
    noteField.lineWrap = true;

    void okListener() {
        String text = noteField.text.trimmed;
        if (text.empty) {
            noteField.requestFocusInWindow();
        } else {
            TextFixture fixture = TextFixture(text, currentTurn());
            for (listener in listeners) {
                listener.addNewFixture(fixture);
            }
            noteField.text = "";
            setVisible(false);
            dispose();
        }
    }

    void cancelListener() {
        noteField.text = "";
        setVisible(false);
        dispose();
    }

    JButton okButton = ListenedButton("OK", okListener);
    JButton cancelButton = ListenedButton("Cancel", cancelListener);

    // FIXME: Here and elsewhere, I think the order of "OK" and "Cancel" needs to be platform-dependent. We probably need a helper for this in [[platform]].
    contentPane = BorderedPanel.verticalPanel(null, noteField,
        BorderedPanel.horizontalPanel(okButton, null, cancelButton));
    platform.makeButtonsSegmented(okButton, cancelButton);

    setMinimumSize(Dimension(200, 100));
    (super of Container).preferredSize = Dimension(250, 120);
    (super of Container).maximumSize = Dimension(350, 130);
    pack();
}
