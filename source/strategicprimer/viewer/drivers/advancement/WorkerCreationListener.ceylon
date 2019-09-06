import java.awt {
    GridLayout,
    Dimension
}
import java.awt.event {
    ActionEvent,
    ActionListener
}

import javax.swing {
    JButton,
    JTextField,
    JPanel,
    JLabel,
    WindowConstants,
    JFrame,
    SpinnerNumberModel,
    JSpinner,
    JComponent
}

import lovelace.util.jvm {
    showErrorDialog,
    platform,
    ListenedButton,
    BorderedPanel
}

import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit,
    Worker,
    IWorker
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    WorkerStats,
    raceFactory
}
import strategicprimer.viewer.drivers.worker_mgmt {
    UnitSelectionListener
}
import strategicprimer.drivers.worker.common {
    IWorkerTreeModel
}
import lovelace.util.common {
    silentListener,
    singletonRandom
}

import java.lang {
    JInteger=Integer
}

"A listener to keep track of the currently selected unit and listen for new-worker
 notifications, then pass this information on to the tree model."
class WorkerCreationListener(IWorkerTreeModel model, IDRegistrar factory)
        satisfies ActionListener&UnitSelectionListener {
    "The currently selected unit"
    variable IUnit? selectedUnit = null;

    shared void addNewWorker(IWorker worker) {
        if (exists local = selectedUnit) {
            model.addUnitMember(local, worker);
        } else {
            log.warn("New worker created when no unit selected");
            showErrorDialog(null, "Strategic Primer Worker Advancement",
                "As no unit was selected, the new worker wasn't added to a unit.");
        }
    }

    object workerCreationFrame extends JFrame("Create Worker") {
        defaultCloseOperation = WindowConstants.disposeOnClose;
        JTextField name = JTextField();
        JTextField race = JTextField(raceFactory.randomRace());
	SpinnerNumberModel hpModel = SpinnerNumberModel(0, -1, 100, 1);
	SpinnerNumberModel maxHP = SpinnerNumberModel(0, -1, 100, 1);
	SpinnerNumberModel strength = SpinnerNumberModel(0, -1, 32, 1);
        SpinnerNumberModel dexterity = SpinnerNumberModel(0, -1, 32, 1);
        SpinnerNumberModel constitution = SpinnerNumberModel(0, -1, 32, 1);
        SpinnerNumberModel intelligence = SpinnerNumberModel(0, -1, 32, 1);
        SpinnerNumberModel wisdom = SpinnerNumberModel(0, -1, 32, 1);
        SpinnerNumberModel charisma = SpinnerNumberModel(0, -1, 32, 1);
        JPanel textPanel = JPanel(GridLayout(0, 2));

        void accept() {
            String nameText = name.text.trimmed;
            String raceText = race.text.trimmed;
            value hpValue = hpModel.number.intValue();
            value maxHPValue = maxHP.number.intValue();
            value strValue = strength.number.intValue();
            value dexValue = dexterity.number.intValue();
            value conValue = constitution.number.intValue();
            value intValue = intelligence.number.intValue();
            value wisValue = wisdom.number.intValue();
            value chaValue = charisma.number.intValue();
            if (!nameText.empty, !raceText.empty, !hpValue.negative,
                    !maxHPValue.negative, !strValue.negative, !dexValue.negative,
                    !conValue.negative, !intValue.negative, !wisValue.negative,
                    !chaValue.negative) {
                log.debug("All worker-creation-dialog fields are acceptable");
                Worker retval = Worker(nameText, raceText, factory.createID());
                retval.stats = WorkerStats(hpValue, maxHPValue, strValue,
                    dexValue, conValue, intValue, wisValue, chaValue);
                addNewWorker(retval);
                log.debug("Created and added the worker; about to hide the window");
                setVisible(false);
                dispose();
            } else {
                StringBuilder builder = StringBuilder();
                if (nameText.empty) {
                    log.debug("Worker not created because name field was empty.");
                    builder.append("Worker needs a name.");
                    builder.appendNewline();
                }
                if (raceText.empty) {
                    log.debug("Worker not created because race field was empty.");
                    builder.append("Worker needs a race.");
                    builder.appendNewline();
                }
                for (stat->val in ["HP"->hpValue, "Max HP"->maxHPValue, "Strength"->strValue,
                            "Dexterity"->dexValue, "Constitution"->conValue,
                            "Intelligence"->intValue, "Wisdom"->wisValue,
                            "Charisma"->chaValue]) {
                    log.debug("Worker not created because non-positive ``stat`` provided");
                    builder.append("``stat`` must be a non-negative number.");
                    builder.appendNewline();
                }
                showErrorDialog(parent, "Strategic Primer Worker Advancement",
                    builder.string);
            }
        }

        void addLabeledField(JPanel panel, String text, JComponent field) {
            panel.add(JLabel(text));
            panel.add(field);
            if (is JTextField field) {
                field.addActionListener(silentListener(accept));
                field.setActionCommand("Add Worker");
            }
        }

        addLabeledField(textPanel, "Worker Name:", name);
        addLabeledField(textPanel, "Worker Race", race);

        JPanel buttonPanel = JPanel(GridLayout(0, 2));

        JButton addButton = ListenedButton("Add Worker", accept);
        buttonPanel.add(addButton);

        shared void revert() {
            name.text = "";
            for (field in [hpModel, maxHP, strength, dexterity, constitution,
                    intelligence, wisdom, charisma]) {
                field.\ivalue = JInteger.valueOf(-1);
            }
            race.text = raceFactory.randomRace();
            dispose();
        }

        JButton cancelButton = ListenedButton("Cancel", revert);
        buttonPanel.add(cancelButton);

        platform.makeButtonsSegmented(addButton, cancelButton);

        JPanel statsPanel = JPanel(GridLayout(0, 4));
        hpModel.\ivalue = JInteger(8);
        addLabeledField(statsPanel, "HP:", JSpinner(hpModel));

        maxHP.\ivalue = JInteger(8);
        addLabeledField(statsPanel, "Max HP:", JSpinner(maxHP));

        for ([stat, model] in [["Strength:", strength],
                ["Intelligence:", intelligence], ["Dexterity:", dexterity],
                ["Wisdom:", wisdom], ["Constitution:", constitution],
                ["Charisma:", charisma]]) {
            model.\ivalue = JInteger(singletonRandom.elements(1..6).take(3)
                .reduce(plus) else 0);
            addLabeledField(statsPanel, stat, JSpinner(model));
        }

        contentPane = BorderedPanel.verticalPanel(textPanel, statsPanel,
            buttonPanel);

        setMinimumSize(Dimension(320, 240));

        pack();
    }

    shared actual void actionPerformed(ActionEvent event) {
        if (event.actionCommand.lowercased.startsWith("add worker")) {
            workerCreationFrame.revert();
            workerCreationFrame.setVisible(true);
        }
    }

    "Update our currently-selected-unit reference."
    shared actual void selectUnit(IUnit? unit) {
        selectedUnit = unit;
    }
}
