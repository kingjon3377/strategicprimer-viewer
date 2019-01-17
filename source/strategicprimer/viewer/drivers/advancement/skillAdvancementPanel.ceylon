import ceylon.collection {
    ArrayList,
    MutableList
}

import java.awt {
    Dimension
}
import java.awt.event {
    ActionEvent
}

import javax.swing {
    JButton,
    JTextField,
    JPanel,
    JLabel
}

import lovelace.util.jvm {
    showErrorDialog,
    platform,
    centeredHorizontalBox,
    BorderedPanel,
    FlowPanel
}

import lovelace.util.common {
    singletonRandom,
    as
}

import strategicprimer.model.common.map.fixtures.mobile.worker {
    ISkill
}
import strategicprimer.viewer.drivers.worker_mgmt {
    UnitMemberListener
}
import strategicprimer.model.common.map.fixtures {
    UnitMember
}
import strategicprimer.model.common.map.fixtures.mobile {
    IWorker
}

"A panel to let a user add hours of experience to a Skill."
final class SkillAdvancementPanel extends BorderedPanel
        satisfies SkillSelectionListener&LevelGainSource&UnitMemberListener {
    static JPanel secondPanelFactory(JButton* buttons) {
        platform.makeButtonsSegmented(*buttons);
        if (platform.systemIsMac) {
            return centeredHorizontalBox(*buttons);
        } else {
            return FlowPanel(*buttons);
        }
    }
    late JTextField hours;
    variable ISkill? skill = null;
    variable IWorker? worker = null;
    MutableList<LevelGainListener> listeners = ArrayList<LevelGainListener>();
    void okListener(ActionEvent event) {
        if (exists local = skill) {
            Integer level = local.level;
            if (is Integer number = Integer.parse(hours.text)) {
                // TODO: Make frequency of leveling checks (i.e. size of hour-chunks to
                // add at a time) configurable. This is correct (per documentation before
                // I added support for workers to the map format) for ordinary experience,
                // but workers learning or working under a more experienced mentor can get
                // multiple "hours" per hour, and they should only check for a level with
                // each *actual* hour.
                for (hour in 0:number) {
                    local.addHours(1, singletonRandom.nextInteger(100));
                }
            } else {
                showErrorDialog(hours, "Strategic Primer Worker Advancement",
                    "Hours to add must be a number");
                return;
            }
            Integer newLevel = local.level;
            if (newLevel != level) {
                for (listener in listeners) {
                    // TODO: Track Job containing the skill
                    // TODO: What if it's a proxy for all workers in a unit?
                    listener.level(worker?.name else "unknown", "unknown", local.name,
                        newLevel - level, newLevel);
                }
            }
        }
        // Clear if OK and no skill selected, on Cancel, and after successfully adding
        // skill
        hours.text = "";
    }
    void cancelListener(ActionEvent event) => hours.text = "";
    shared new delegate(JTextField hours, JButton okButton, JButton cancelButton)
        extends BorderedPanel(null, FlowPanel(JLabel("Add "), hours,
        JLabel(" hours to skill?")),
        secondPanelFactory(okButton, cancelButton)) {
        okButton.addActionListener(okListener);
        cancelButton.addActionListener(cancelListener);
        hours.setActionCommand("OK");
        hours.addActionListener(okListener);
        this.hours = hours;
        minimumSize = Dimension(200, 40);
        preferredSize = Dimension(220, 60);
        maximumSize = Dimension(240, 60);
    }
    shared new () extends delegate(JTextField(3), JButton("OK"), JButton("Cancel")) {}
    shared actual void selectSkill(ISkill? selectedSkill) {
        skill = selectedSkill;
        if (selectedSkill exists) {
            hours.requestFocusInWindow();
        }
    }
    shared actual void addLevelGainListener(LevelGainListener listener)
        => listeners.add(listener);
    shared actual void removeLevelGainListener(LevelGainListener listener)
        => listeners.remove(listener);
    shared actual void memberSelected(UnitMember? previousSelection,
            UnitMember? selected) => worker = as<IWorker>(selected);
}
