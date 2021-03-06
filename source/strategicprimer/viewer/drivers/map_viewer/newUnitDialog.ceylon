import ceylon.collection {
    ArrayList,
    MutableList
}

import java.awt {
    Dimension,
    GridLayout,
    Container
}
import java.text {
    NumberFormat
}

import javax.swing {
    JButton,
    JTextField,
    JFormattedTextField,
    JLabel
}

import lovelace.util.jvm {
    platform,
    ListenedButton
}

import strategicprimer.drivers.common {
    PlayerChangeListener
}
import strategicprimer.drivers.worker.common {
    NewUnitListener
}
import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.model.common.map {
    Player
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit,
    Unit
}
import strategicprimer.drivers.gui.common {
    SPDialog
}
import strategicprimer.viewer.drivers.worker_mgmt {
    NewUnitSource
}
import lovelace.util.common {
    isNumeric,
    parseInt,
    silentListener
}

"A dialog to let the user add a new unit."
// TODO: Use NewFixtureSource/NewFixtureListener for listeners that might want to handle other kinds of fixtures
shared class NewUnitDialog(variable Player player, IDRegistrar idf)
        extends SPDialog(null, "Add a New Unit")
        satisfies NewUnitSource&PlayerChangeListener {
    MutableList<NewUnitListener> listeners = ArrayList<NewUnitListener>();

    JTextField nameField = JTextField(10);
    JTextField kindField = JTextField(10);
    JFormattedTextField idField = JFormattedTextField(NumberFormat.integerInstance);

    shared actual void playerChanged(Player? old, Player newPlayer) =>
            player = newPlayer;
    shared actual void addNewUnitListener(NewUnitListener listener) =>
            listeners.add(listener);
    shared actual void removeNewUnitListener(NewUnitListener listener) =>
            listeners.remove(listener);

    void okListener() {
        String name = nameField.text.trimmed;
        String kind = kindField.text.trimmed;
        if (name.empty) {
            nameField.requestFocusInWindow();
        } else if (kind.empty) {
            kindField.requestFocusInWindow();
        } else {
            String reqId = idField.text.trimmed;
            variable Integer idNum;
            if (isNumeric(reqId), exists temp = parseInt(reqId)) {
                if (!idf.isIDUnused(temp)) {
                    // TODO: Show an error message
                    idField.text = "";
                    idField.requestFocusInWindow();
                    return;
                }
                idNum = temp;
                idf.register(idNum);
            } else {
                idNum = idf.createID();
            }
            IUnit unit = Unit(player, kind, name, idNum);
            for (listener in listeners) {
                listener.addNewUnit(unit);
            }
            kindField.text = "";
            nameField.text = "";
            idField.text = "";
            setVisible(false);
            dispose();
        }
    }

    setLayout(GridLayout(0, 2));
    add(JLabel("<html><b>Unit Name:&nbsp;</b></html>"));
    void setupField(JTextField field) {
        field.setActionCommand("OK");
        field.addActionListener(silentListener(okListener));
        add(field);
    }

    setupField(nameField);
    add(JLabel("<html><b>Kind of Unit:&nbsp;</b></html>"));
    setupField(kindField);
    add(JLabel("ID #: "));
    idField.columns = 10;
    setupField(idField);
    JButton okButton = ListenedButton("OK", okListener);
    add(okButton);

    void cancelListener() {
        nameField.text = "";
        kindField.text = "";
        idField.text = "";
        setVisible(false);
        dispose();
    }

    JButton cancelButton = ListenedButton("Cancel", cancelListener);
    platform.makeButtonsSegmented(okButton, cancelButton);
    add(cancelButton);
    setMinimumSize(Dimension(200, 100));
    (super of Container).preferredSize = Dimension(250, 120);
    (super of Container).maximumSize = Dimension(350, 130);
    pack();
}
