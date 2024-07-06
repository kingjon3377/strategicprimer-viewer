package drivers.map_viewer;

import drivers.gui.common.SPDialog;

import drivers.common.NewFixtureSource;
import drivers.common.NewFixtureListener;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.JButton;

import legacy.map.TileFixture;
import legacy.map.fixtures.TextFixture;

import lovelace.util.ListenedButton;
import lovelace.util.BorderedPanel;
import lovelace.util.Platform;

import java.awt.Dimension;

import java.util.function.IntSupplier;

/**
 * A dialog to let the user add a text note to a tile.
 *
 * TODO: Add the ability to edit an existing note.
 */
public final class TextNoteDialog extends SPDialog implements NewFixtureSource {
	@Serial
	private static final long serialVersionUID = 1L;
	private final IntSupplier currentTurn;

	public TextNoteDialog(final IntSupplier currentTurn) {
		super(null, "Add Text Note");
		this.currentTurn = currentTurn;
		noteField.setLineWrap(true);

		final JButton okButton = new ListenedButton("OK", this::okListener);
		final JButton cancelButton = new ListenedButton("Cancel", this::cancelListener);

		// FIXME: Here and elsewhere, I think the order of "OK" and
		// "Cancel" needs to be platform-dependent. We probably need a
		// helper for this in {@link Platform}.
		setContentPane(BorderedPanel.verticalPanel(null, noteField,
				BorderedPanel.horizontalPanel(okButton, null, cancelButton)));
		Platform.makeButtonsSegmented(okButton, cancelButton);

		setMinimumSize(new Dimension(200, 100));
		setPreferredSize(new Dimension(250, 120));
		setMaximumSize(new Dimension(350, 130));
		pack();
	}

	private final Collection<NewFixtureListener> listeners = new ArrayList<>();

	@Override
	public void addNewFixtureListener(final NewFixtureListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeNewFixtureListener(final NewFixtureListener listener) {
		listeners.remove(listener);
	}

	private final JTextArea noteField = new JTextArea(15, 3);

	private void okListener() {
		final String text = noteField.getText().strip();
		if (text.isEmpty()) {
			noteField.requestFocusInWindow();
		} else {
			final TileFixture fixture = new TextFixture(text, currentTurn.getAsInt());
			for (final NewFixtureListener listener : listeners) {
				listener.addNewFixture(fixture);
			}
			noteField.setText(""); // TODO: delegate to cancelListener()?
			setVisible(false);
			dispose();
		}
	}

	private void cancelListener() {
		noteField.setText("");
		setVisible(false);
		dispose();
	}
}
