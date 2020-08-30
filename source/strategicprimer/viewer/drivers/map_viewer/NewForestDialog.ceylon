import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.drivers.gui.common {
    SPDialog
}
import strategicprimer.drivers.common {
    NewFixtureSource,
    NewFixtureListener
}
import ceylon.collection {
    MutableList,
    ArrayList
}
import javax.swing {
    JTextField,
    JFormattedTextField,
    JLabel,
    JButton,
    JCheckBox
}
import java.text {
    NumberFormat
}
import lovelace.util.common {
    isNumeric,
    parseInt,
    silentListener
}
import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}
import java.awt {
    GridLayout,
    Container,
    Dimension
}
import lovelace.util.jvm {
    ListenedButton,
    platform,
    decimalize
}
import ceylon.decimal {
    parseDecimal,
    Decimal
}

"A dialog to let the user add a new forest to a tile."
shared class NewForestDialog(IDRegistrar idf) extends SPDialog(null, "Add a New Forest")
        satisfies NewFixtureSource {
    MutableList<NewFixtureListener> listeners = ArrayList<NewFixtureListener>();
    shared actual void addNewFixtureListener(NewFixtureListener listener) => listeners.add(listener);
    shared actual void removeNewFixtureListener(NewFixtureListener listener) => listeners.remove(listener);

    JTextField kindField = JTextField(10);
    JFormattedTextField idField = JFormattedTextField(NumberFormat.integerInstance);
    JCheckBox rowsField = JCheckBox();
    JFormattedTextField acresField = JFormattedTextField(NumberFormat.numberInstance);

    void okListener() {
        String kind = kindField.text.trimmed;
        String acresString = acresField.text.trimmed;
        if (kind.empty) {
            kindField.requestFocusInWindow();
        } else if (!acresString.empty, parseDecimal(acresString) is Null) {
            acresField.requestFocusInWindow();
        } else {
            String reqId = idField.text.trimmed;
            variable Integer idNum;
            if (isNumeric(reqId), exists temp = parseInt(reqId)) {
                idNum = temp;
                idf.register(idNum);
            } else {
                idNum = idf.createID();
            }
            Decimal acres = parseDecimal(acresString) else decimalize(-1);
            Forest forest = Forest(kind, rowsField.model.selected, idNum, acres);
            for (listener in listeners) {
                listener.addNewFixture(forest);
            }
            kindField.text = "";
            acresField.text = "-1";
            setVisible(false);
            dispose();
        }
    }

    setLayout(GridLayout(0, 2));

    void setupField(JTextField field) {
        field.setActionCommand("OK");
        field.addActionListener(silentListener(okListener));
        add(field);
    }

    for ([label, field] in [["Kind of Forest", kindField], ["Rows?", rowsField]]) {
        add(JLabel("<html><b>``label``</b></html>"));
        switch (field)
        case (is JTextField) { setupField(field); }
        else { add(field); }
    }
    add(JLabel("ID #: "));
    idField.columns = 10;
    setupField(idField);

    JButton okButton = ListenedButton("OK", okListener);
    add(okButton);

    void cancelListener() {
        kindField.text = "";
        acresField.text = "-1";
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
