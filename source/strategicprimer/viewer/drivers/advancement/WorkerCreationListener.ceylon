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
    JComponent
}

import lovelace.util.jvm {
    showErrorDialog,
    platform,
    listenedButton,
    BorderedPanel,
    singletonRandom
}

import strategicprimer.model.idreg {
    IDRegistrar
}
import strategicprimer.model.map.fixtures.mobile {
    IUnit,
    Worker,
    IWorker
}
import strategicprimer.model.map.fixtures.mobile.worker {
    WorkerStats,
    raceFactory
}
import strategicprimer.viewer.drivers.worker_mgmt {
    UnitSelectionListener
}
import strategicprimer.drivers.worker.common {
    IWorkerTreeModel
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
    shared actual void actionPerformed(ActionEvent event) {
        if (event.actionCommand.lowercased.startsWith("add worker")) {
            object frame extends JFrame("Create Worker") {
                defaultCloseOperation = WindowConstants.disposeOnClose;
                JTextField name = JTextField();
                JTextField race = JTextField(raceFactory.randomRace());
                JTextField hpBox = JTextField();
                JTextField maxHP = JTextField();
                JTextField strength = JTextField();
                JTextField dexterity = JTextField();
                JTextField constitution = JTextField();
                JTextField intelligence = JTextField();
                JTextField wisdom = JTextField();
                JTextField charisma = JTextField();
                JPanel textPanel = JPanel(GridLayout(0, 2));
                void addLabeledField(JPanel panel, String text, JComponent field) {
                    panel.add(JLabel(text));
                    panel.add(field);
                }
                addLabeledField(textPanel, "Worker Name:", name);
                addLabeledField(textPanel, "Worker Race", race);
                JPanel buttonPanel = JPanel(GridLayout(0, 2));
                JButton addButton = listenedButton("Add Worker", (ActionEvent event) {
                    String nameText = name.text.trimmed;
                    String raceText = race.text.trimmed;
                    value hpValue = Integer.parse(hpBox.text.trimmed);
                    value maxHPValue = Integer.parse(maxHP.text.trimmed);
                    value strValue = Integer.parse(strength.text.trimmed);
                    value dexValue = Integer.parse(dexterity.text.trimmed);
                    value conValue = Integer.parse(constitution.text.trimmed);
                    value intValue = Integer.parse(intelligence.text.trimmed);
                    value wisValue = Integer.parse(wisdom.text.trimmed);
                    value chaValue = Integer.parse(charisma.text.trimmed);
                    if (!nameText.empty, raceText.empty, is Integer hpValue,
                        is Integer maxHPValue, is Integer strValue,
                        is Integer dexValue, is Integer conValue,
                        is Integer intValue, is Integer wisValue,
                        is Integer chaValue) {
                        Worker retval = Worker(nameText, raceText, factory.createID());
                        retval.stats = WorkerStats(hpValue, maxHPValue, strValue,
                            dexValue, conValue, intValue, wisValue, chaValue);
                        addNewWorker(retval);
                        setVisible(false);
                        dispose();
                    } else {
                        StringBuilder builder = StringBuilder();
                        if (nameText.empty) {
                            builder.append("Worker needs a name.");
                            builder.appendNewline();
                        }
                        if (raceText.empty) {
                            builder.append("Worker needs a race.");
                            builder.appendNewline();
                        }
                        for (stat->val in ["HP"->hpValue,
	                            "Max HP"->maxHPValue, "Strength"->strValue,
	                            "Dexterity"->dexValue, "Constitution"->conValue,
	                            "Intelligence"->intValue, "Wisdom"->wisValue,
	                            "Charisma"->chaValue].narrow<String->ParseException>()) {
                            builder.append("``stat`` must be a number.");
                            builder.appendNewline();
                        }
                        showErrorDialog(null, "Strategic Primer Worker Advancement",
                            builder.string);
                    }
                });
                buttonPanel.add(addButton);
                JButton cancelButton = listenedButton("Cancel",
                            (ActionEvent event) => dispose());
                buttonPanel.add(cancelButton);
                platform.makeButtonsSegmented(addButton, cancelButton);
                JPanel statsPanel = JPanel(GridLayout(0, 4));
                hpBox.text = "8";
                addLabeledField(statsPanel, "HP:", hpBox);
                maxHP.text = "8";
                addLabeledField(statsPanel, "Max HP:", maxHP);
                for ([stat, box] in [["Strength:", strength],
	                    ["Intelligence:", intelligence], ["Dexterity:", dexterity],
	                    ["Wisdom:", wisdom], ["Constitution:", constitution],
	                    ["Charisma:", charisma]]) {
                    box.text = singletonRandom.elements(1..6).take(3)
                        .reduce(plus)?.string else "0";
                    addLabeledField(statsPanel, stat, box);
                }
                contentPane = BorderedPanel.verticalPanel(textPanel, statsPanel,
                    buttonPanel);
                setMinimumSize(Dimension(320, 240));
                pack();
            }
            frame.setVisible(true);
        }
    }
    "Update our currently-selected-unit reference."
    shared actual void selectUnit(IUnit? unit) {
        selectedUnit = unit;
    }
}
