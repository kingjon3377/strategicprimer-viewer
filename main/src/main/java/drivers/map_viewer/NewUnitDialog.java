package drivers.map_viewer;

import java.util.OptionalInt;
import java.util.ArrayList;
import java.util.List;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

import lovelace.util.Platform;
import lovelace.util.ListenedButton;

import org.jetbrains.annotations.Nullable;

import drivers.common.PlayerChangeListener;
import worker.common.NewUnitListener;
import common.idreg.IDRegistrar;
import common.map.Player;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Unit;
import drivers.gui.common.SPDialog;
import drivers.worker_mgmt.NewUnitSource;
import static lovelace.util.NumParsingHelper.isNumeric;
import static lovelace.util.NumParsingHelper.parseInt;

/**
 * A dialog to let the user add a new unit.
 *
 * TODO: Use NewFixtureSource/NewFixtureListener for listeners that might want
 * to handle other kinds of fixtures
 */
public final class NewUnitDialog extends SPDialog implements NewUnitSource, PlayerChangeListener {
	private static final long serialVersionUID = 1L;
	public NewUnitDialog(final Player player, final IDRegistrar idf) {
		super(null, "Add a New Unit");
		this.player = player;
		this.idf = idf;

		setLayout(new GridLayout(0, 2));
		add(new JLabel("<html><b>Unit Name:&nbsp;</b></html>"));
		setupField(nameField);
		add(new JLabel("<html><b>Kind of Unit:&nbsp;</b></html>"));
		setupField(kindField);
		add(new JLabel("ID #: "));
		idField.setColumns(10);
		setupField(idField);
		final JButton okButton = new ListenedButton("OK", this::okListener);
		add(okButton);

		final JButton cancelButton = new ListenedButton("Cancel", this::cancelListener);
		Platform.makeButtonsSegmented(okButton, cancelButton);
		add(cancelButton);
		setMinimumSize(new Dimension(200, 100));
		setPreferredSize(new Dimension(250, 120));
		setMaximumSize(new Dimension(350, 130));
		pack();
	}

	private Player player;
	private final IDRegistrar idf;

	private final List<NewUnitListener> listeners = new ArrayList<>();

	private final JTextField nameField = new JTextField(10);
	private final JTextField kindField = new JTextField(10);
	private final JFormattedTextField idField =
		new JFormattedTextField(NumberFormat.getIntegerInstance());

	@Override
	public void playerChanged(final @Nullable Player old, final Player newPlayer) {
		player = newPlayer;
	}

	@Override
	public void addNewUnitListener(final NewUnitListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeNewUnitListener(final NewUnitListener listener) {
		listeners.remove(listener);
	}

	private void okListener() {
		final String name = nameField.getText().strip();
		final String kind = kindField.getText().strip();
		if (name.isEmpty()) {
			nameField.requestFocusInWindow();
		} else if (kind.isEmpty()) {
			kindField.requestFocusInWindow();
		} else {
			final String reqId = idField.getText().strip();
			final int idNum;
			if (isNumeric(reqId)) {
				final OptionalInt temp = parseInt(reqId);
				if (temp.isPresent()) {
					if (idf.isIDUnused(temp.getAsInt())) {
						idNum = temp.getAsInt();
						idf.register(idNum);
					} else {
						// TODO: Show an error message
						idField.setText("");
						idField.requestFocusInWindow();
						return;
					}
				} else {
					// TODO: log and/or show error message
					idNum = idf.createID();
				}
			} else {
				idNum = idf.createID();
			}
			final IUnit unit = new Unit(player, kind, name, idNum);
			for (final NewUnitListener listener : listeners) {
				listener.addNewUnit(unit);
			}
			// TODO: delegate to cancelListener()
			kindField.setText("");
			nameField.setText("");
			idField.setText("");
			setVisible(false);
			dispose();
		}
	}

	private void setupField(final JTextField field) {
		field.setActionCommand("OK");
		field.addActionListener(ignored -> okListener());
		add(field);
	}

	private void cancelListener() {
		nameField.setText("");
		kindField.setText("");
		idField.setText("");
		setVisible(false);
		dispose();
	}
}
