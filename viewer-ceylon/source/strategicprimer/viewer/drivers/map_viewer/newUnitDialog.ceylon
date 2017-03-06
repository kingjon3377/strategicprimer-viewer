import java.text {
    NumberFormat
}
import model.map {
    Player
}
import util {
    IsNumeric
}
import strategicprimer.viewer.drivers {
    SPDialog
}
import java.awt.event {
    ActionEvent
}
import controller.map.misc {
    IDRegistrar
}
import javax.swing {
    JButton,
    JTextField,
    JFormattedTextField,
    JLabel
}
import ceylon.collection {
    ArrayList,
    MutableList
}
import java.awt {
    Component,
    Dimension
}
import model.listeners {
    PlayerChangeListener,
    NewUnitSource,
    NewUnitListener
}
import model.map.fixtures.mobile {
    IUnit,
    Unit
}
import lovelace.util.jvm {
    platform,
    listenedButton
}
"A dialog to let the user add a new unit."
shared SPDialog&NewUnitSource&PlayerChangeListener newUnitDialog(variable Player player,
        IDRegistrar idf) {
    MutableList<NewUnitListener> listeners = ArrayList<NewUnitListener>();
    JTextField nameField = JTextField(10);
    JTextField kindField = JTextField(10);
    JFormattedTextField idField = JFormattedTextField(NumberFormat.integerInstance);
    object retval extends SPDialog(null, "Add a New Unit")
            satisfies NewUnitSource&PlayerChangeListener {
        shared actual void playerChanged(Player? old, Player newPlayer) =>
                player = newPlayer;
        shared actual void addNewUnitListener(NewUnitListener listener) =>
                listeners.add(listener);
        shared actual void removeNewUnitListener(NewUnitListener listener) =>
                listeners.remove(listener);
    }
    void okListener(ActionEvent event) {
        String name = nameField.text.trimmed;
        String kind = kindField.text.trimmed;
        if (name.empty) {
            nameField.requestFocusInWindow();
        } else if (kind.empty) {
            kindField.requestFocusInWindow();
        } else {
            String reqId = idField.text.trimmed;
            variable Integer idNum;
            if (IsNumeric.isNumeric(reqId)) {
                try {
                    idNum = NumberFormat.integerInstance.parse(reqId).intValue();
                    idf.register(idNum);
                } catch (ParseException except) {
                    log.info("Parse error parsing user-specified ID", except);
                    idNum = idf.createID();
                }
            } else {
                idNum = idf.createID();
            }
            IUnit unit = Unit(player, kind, name, idNum);
            for (listener in listeners) {
                listener.addNewUnit(unit);
            }
            kindField.text = "";
            nameField.text = "";
            retval.setVisible(false);
            retval.dispose();
        }
    }
    retval.add(JLabel("<html><b>Unit Name:&nbsp;</b></html>"));
    void setupField(JTextField field) {
        field.setActionCommand("OK");
        field.addActionListener(okListener);
        retval.add(field);
    }
    setupField(nameField);
    retval.add(JLabel("<html><b>Kind of Unit:&nbsp;</b></html>"));
    setupField(kindField);
    retval.add(JLabel("ID #: "));
    idField.columns = 10;
    setupField(idField);
    JButton okButton = listenedButton("OK", okListener);
    retval.add(okButton);
    JButton cancelButton = listenedButton("Cancel", (ActionEvent event) {
        nameField.text = "";
        kindField.text = "";
        retval.setVisible(false);
        retval.dispose();
    });
    platform.makeButtonsSegmented(okButton, cancelButton);
    retval.add(cancelButton);
    retval.setMinimumSize(Dimension(150, 80));
    (retval of Component).preferredSize = Dimension(200, 90);
    (retval of Component).maximumSize = Dimension(300, 90);
    retval.pack();
    return retval;
}
