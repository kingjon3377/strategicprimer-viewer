package drivers.worker_mgmt;

import java.util.Objects;
import drivers.common.PlayerChangeListener;
import lovelace.util.BorderedPanel;
import lovelace.util.Platform;
import static lovelace.util.BoxPanel.centeredHorizontalBox;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JPanel;
import common.map.Player;
import common.map.HasNotes;
import common.map.fixtures.UnitMember;

import java.awt.Toolkit;

import org.jetbrains.annotations.Nullable;

public final class NotesPanel extends BorderedPanel
		implements UnitMemberListener, PlayerChangeListener {
	private final JTextArea notesArea = new JTextArea();
	private @Nullable HasNotes current = null;
	private Player player;
	private final JButton notesApplyButton = new JButton("Apply");
	private final JButton notesRevertButton = new JButton("Revert");
	private final JPanel notesButtonPanel;

	public NotesPanel(Player currentPlayer) {
		Platform.makeButtonsSegmented(notesApplyButton, notesRevertButton);
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
		if (current != null) { // TODO: invert (here and in revertNotes())
			current.setNote(player, notesArea.getText().trim());
		} else {
			Toolkit.getDefaultToolkit().beep();
		}
	}

	private void revertNotes() {
		if (current != null) {
			notesArea.setText(current.getNote(player));
		} else {
			Toolkit.getDefaultToolkit().beep();
		}
	}

	@Override
	public void memberSelected(@Nullable UnitMember previousSelection, @Nullable UnitMember selected) {
		if (!Objects.equals(selected, current)) {
			if (selected instanceof HasNotes) {
				current = (HasNotes) selected;
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
	public void playerChanged(@Nullable Player previousCurrent, Player newCurrent) {
		if (!player.equals(newCurrent)) {
			player = newCurrent;
			notesArea.setText("");
			notesArea.setEnabled(false);
			notesApplyButton.setEnabled(false);
			notesRevertButton.setEnabled(false);
		}
	}
}
