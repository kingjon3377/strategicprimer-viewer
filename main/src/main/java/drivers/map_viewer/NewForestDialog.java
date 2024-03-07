package drivers.map_viewer;

import java.io.Serial;
import java.util.OptionalInt;

import lovelace.util.LovelaceLogger;
import org.javatuples.Pair;

import java.util.Arrays;
import javax.swing.JComponent;

import legacy.idreg.IDRegistrar;
import drivers.gui.common.SPDialog;
import drivers.common.NewFixtureSource;
import drivers.common.NewFixtureListener;

import java.util.List;
import java.util.ArrayList;
import javax.swing.JTextField;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import java.text.NumberFormat;

import static lovelace.util.NumParsingHelper.isNumeric;
import static lovelace.util.NumParsingHelper.parseInt;

import legacy.map.fixtures.terrain.Forest;

import java.awt.GridLayout;
import java.awt.Dimension;

import lovelace.util.ListenedButton;
import lovelace.util.Platform;

import java.math.BigDecimal;

/**
 * A dialog to let the user add a new forest to a tile.
 */
public final class NewForestDialog extends SPDialog implements NewFixtureSource {
	@Serial
	private static final long serialVersionUID = 1L;

	public NewForestDialog(final IDRegistrar idf) {
		super(null, "Add a New Forest");
		this.idf = idf;
		setLayout(new GridLayout(0, 2));
		for (final Pair<String, JComponent> pair : Arrays.asList(
				Pair.<String, JComponent>with("Kind of Forest", kindField),
				Pair.<String, JComponent>with("Rows?", rowsField))) {
			final JComponent field = pair.getValue1();
			add(new JLabel(String.format("<html><b>%s</b></html>", pair.getValue0())));
			if (field instanceof final JTextField tf) {
				setupField(tf);
			} else {
				add(field);
			}
		}
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

	private final IDRegistrar idf;

	private final List<NewFixtureListener> listeners = new ArrayList<>();

	@Override
	public void addNewFixtureListener(final NewFixtureListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeNewFixtureListener(final NewFixtureListener listener) {
		listeners.remove(listener);
	}

	private final JTextField kindField = new JTextField(10);
	private final JFormattedTextField idField =
			new JFormattedTextField(NumberFormat.getIntegerInstance());
	private final JCheckBox rowsField = new JCheckBox();
	private final JFormattedTextField acresField =
			new JFormattedTextField(NumberFormat.getNumberInstance());

	private void okListener() {
		final String kind = kindField.getText().strip();
		final String acresString = acresField.getText().strip();
		if (kind.isEmpty()) {
			kindField.requestFocusInWindow();
			return;
		} else if (!acresString.isEmpty()) {
			try {
				new BigDecimal(acresString);
			} catch (final NumberFormatException except) {
				acresField.requestFocusInWindow();
				return;
			}
		} else {
			final String reqId = idField.getText().strip();
			final int idNum;
			if (isNumeric(reqId)) {
				final OptionalInt temp = parseInt(reqId);
				if (temp.isPresent() && temp.getAsInt() >= 0) {
					idNum = temp.getAsInt();
					idf.register(idNum);
				} else {
					LovelaceLogger.warning("Failed to parse input detected as numeric");
					idNum = idf.createID();
				}
			} else {
				idNum = idf.createID();
			}
			BigDecimal acres;
			try {
				acres = new BigDecimal(acresString);
			} catch (final NumberFormatException except) {
				acres = new BigDecimal(-1);
			}
			final Forest forest = new Forest(kind, rowsField.getModel().isSelected(), idNum, acres);
			for (final NewFixtureListener listener : listeners) {
				listener.addNewFixture(forest);
			}
//			kindField.setText("");
			acresField.setText("-1");
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
//		kindField.setText("");
		acresField.setText("-1");
		setVisible(false);
		dispose();
	}
}
