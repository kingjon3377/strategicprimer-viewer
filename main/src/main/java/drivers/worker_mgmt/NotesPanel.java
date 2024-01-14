package drivers.worker_mgmt;

import java.io.Serial;
import java.util.Objects;

import drivers.common.PlayerChangeListener;
import lovelace.util.BorderedPanel;
import lovelace.util.Platform;

import static lovelace.util.BoxPanel.centeredHorizontalBox;

import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JPanel;

import legacy.map.Player;
import legacy.map.HasNotes;
import legacy.map.fixtures.UnitMember;

import java.awt.Toolkit;

import org.jetbrains.annotations.Nullable;

public final class NotesPanel extends BorderedPanel
        implements UnitMemberListener, PlayerChangeListener {
    @Serial
    private static final long serialVersionUID = 1L;
    private final JTextArea notesArea = new JTextArea();
    private @Nullable HasNotes current = null;
    private Player player;
    private final JButton notesApplyButton = new JButton("Apply");
    private final JButton notesRevertButton = new JButton("Revert");

    public NotesPanel(final Player currentPlayer) {
        Platform.makeButtonsSegmented(notesApplyButton, notesRevertButton);
        final JPanel notesButtonPanel;
        if (Platform.SYSTEM_IS_MAC) {
            notesButtonPanel = centeredHorizontalBox(notesRevertButton, notesApplyButton);
        } else {
            notesButtonPanel = BorderedPanel.horizontalPanel(notesRevertButton, null,
                    notesApplyButton);
        }
        player = currentPlayer;
        notesApplyButton.addActionListener(ignored -> saveNotes());
        notesRevertButton.addActionListener(ignored -> revertNotes());
        setCenter(notesArea);
        setPageEnd(notesButtonPanel);
        notesArea.setText("");
        notesArea.setEnabled(false);
        notesApplyButton.setEnabled(false);
        notesRevertButton.setEnabled(false);
    }

    private void saveNotes() {
	    if (Objects.isNull(current)) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            current.setNote(player, notesArea.getText().strip());
        }
    }

    private void revertNotes() {
	    if (Objects.isNull(current)) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            notesArea.setText(current.getNote(player));
        }
    }

    @Override
    public void memberSelected(final @Nullable UnitMember previousSelection, final @Nullable UnitMember selected) {
        if (!Objects.equals(selected, current)) {
            if (selected instanceof final HasNotes hn) {
                current = hn;
                notesArea.setEnabled(true);
                revertNotes();
                notesApplyButton.setEnabled(true);
                notesRevertButton.setEnabled(true);
            } else {
                current = null;
                notesArea.setText("");
                notesArea.setEnabled(false);
                notesApplyButton.setEnabled(false);
                notesRevertButton.setEnabled(false);
            }
        }
    }

    @Override
    public void playerChanged(final @Nullable Player previousCurrent, final Player newCurrent) {
        if (!player.equals(newCurrent)) {
            player = newCurrent;
            notesArea.setText("");
            notesArea.setEnabled(false);
            notesApplyButton.setEnabled(false);
            notesRevertButton.setEnabled(false);
        }
    }
}
