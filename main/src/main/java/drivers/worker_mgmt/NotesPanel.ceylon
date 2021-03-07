import strategicprimer.drivers.common {
    PlayerChangeListener
}
import lovelace.util.jvm {
    BorderedPanel,
    platform,
    centeredHorizontalBox
}
import javax.swing {
    JTextArea,
    JButton,
    JPanel
}
import strategicprimer.model.common.map {
    Player,
    HasNotes
}
import strategicprimer.model.common.map.fixtures {
    UnitMember
}
import lovelace.util.common {
    anythingEqual,
    as,
    silentListener
}
import java.awt {
    Toolkit
}

final class NotesPanel extends BorderedPanel satisfies UnitMemberListener&PlayerChangeListener {
    JTextArea notesArea = JTextArea();
    variable HasNotes? current = null;
    variable Player player;
    JButton notesApplyButton = JButton("Apply");
    JButton notesRevertButton = JButton("Revert");
    platform.makeButtonsSegmented(notesApplyButton, notesRevertButton);
    JPanel notesButtonPanel = (platform.systemIsMac) then
        centeredHorizontalBox(notesRevertButton, notesApplyButton)
        else BorderedPanel.horizontalPanel(notesRevertButton, null, notesApplyButton);
    shared new(Player currentPlayer) extends BorderedPanel() {
        player = currentPlayer;
    }
    void saveNotes() {
        if (exists local = current) {
            local.notes[player] = notesArea.text.trimmed;
        } else {
            Toolkit.defaultToolkit.beep();
        }
    }
    notesApplyButton.addActionListener(silentListener(saveNotes));
    void revertNotes() {
        if (exists local = current) {
            notesArea.text = local.notes.get(player) else "";
        } else {
            Toolkit.defaultToolkit.beep();
        }
    }
    notesRevertButton.addActionListener(silentListener(revertNotes));
    shared actual void memberSelected(UnitMember? previousSelection, UnitMember? selected) {
        if (!anythingEqual(selected, current)) {
            current = as<HasNotes>(selected);
            if (current exists) {
                notesArea.enabled = true;
                revertNotes();
                notesApplyButton.enabled = true;
                notesRevertButton.enabled = true;
            } else {
                notesArea.text = "";
                notesArea.enabled = false;
                notesApplyButton.enabled = false;
                notesRevertButton.enabled = false;
            }
        }
    }

    "Set up the panel in ways Ceylon won't let us do from the constructor"
    shared void initialize() {
        center = notesArea;
        pageEnd = notesButtonPanel;
        notesArea.text = "";
        notesArea.enabled = false;
        notesApplyButton.enabled = false;
        notesRevertButton.enabled = false;
    }
    shared actual void playerChanged(Player? previousCurrent, Player newCurrent) {
        if (player != newCurrent) {
            player = newCurrent;
            notesArea.text = "";
            notesArea.enabled = false;
            notesApplyButton.enabled = false;
            notesRevertButton.enabled = false;
        }
    }
}

shared JPanel&UnitMemberListener&PlayerChangeListener notesPanel(Player player) {
    value retval = NotesPanel(player);
    retval.initialize();
    return retval;
}
