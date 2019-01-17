import ceylon.collection {
    ArrayList,
    MutableList
}

import java.awt {
    FlowLayout,
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
    ListenedButton,
    BorderedPanel
}

import lovelace.util.common {
    singletonRandom
}

import strategicprimer.model.common.map.fixtures.mobile.worker {
    ISkill
}

"A panel to let a user add hours of experience to a Skill."
// TODO: Try to convert back to class
JPanel&SkillSelectionListener&LevelGainSource skillAdvancementPanel() {
    JTextField hours = JTextField(3);
    JPanel firstPanel = JPanel();
    // TODO: Add FlowPanel to lovelace.util.jvm to condense this
    firstPanel.add(JLabel("Add "));
    firstPanel.add(hours);
    firstPanel.add(JLabel(" hours to skill?"));
    variable ISkill? skill = null;
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
                    // FIXME: Track worker and Job names
                    listener.level("unknown", "unknown", local.name, newLevel - level,
                        newLevel);
                }
            }
        }
        // Clear if OK and no skill selected, on Cancel, and after successfully adding
        // skill
        hours.text = "";
    }
    JButton okButton = ListenedButton("OK", okListener);
    hours.setActionCommand("OK");
    hours.addActionListener(okListener);
    JButton cancelButton = ListenedButton("Cancel",
        // TODO: Figure out a way to defer() an assignment
                (ActionEvent event) => hours.text = "");
    platform.makeButtonsSegmented(okButton, cancelButton);
    JPanel secondPanel;
    if (platform.systemIsMac) {
        secondPanel = centeredHorizontalBox(okButton, cancelButton);
    } else {
        secondPanel = JPanel(FlowLayout());
        secondPanel.add(okButton);
        secondPanel.add(cancelButton);
    }
    object retval extends BorderedPanel()
            satisfies SkillSelectionListener&LevelGainSource {
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
    }
    retval.pageStart = firstPanel;
    retval.pageEnd = secondPanel;
    retval.minimumSize = Dimension(200, 40);
    retval.preferredSize = Dimension(220, 60);
    retval.maximumSize = Dimension(240, 60);
    return retval;
}
