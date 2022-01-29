package drivers.map_viewer;

import java.util.OptionalInt;
import org.javatuples.Pair;
import java.util.Arrays;
import javax.swing.JComponent;
import common.idreg.IDRegistrar;
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

import common.map.fixtures.terrain.Forest;
import java.awt.GridLayout;
import java.awt.Container;
import java.awt.Dimension;
import lovelace.util.ListenedButton;
import lovelace.util.Platform;
import java.math.BigDecimal;

/**
 * A dialog to let the user add a new forest to a tile.
 */
public class NewForestDialog extends SPDialog implements NewFixtureSource {
	public NewForestDialog(IDRegistrar idf) {
		super(null, "Add a New Forest");
		this.idf = idf;
		setLayout(new GridLayout(0, 2));
		for (Pair<String, JComponent> pair : Arrays.asList(
				Pair.<String, JComponent>with("Kind of Forest", kindField),
				Pair.<String, JComponent>with("Rows?", rowsField))) {
			JComponent field = pair.getValue1();
			add(new JLabel(String.format("<html><b>%s</b></html>", pair.getValue0())));
			if (field instanceof JTextField) {
				setupField((JTextField) field);
			} else {
				add(field);
			}
		}
		add(new JLabel("ID #: "));
		idField.setColumns(10);
		setupField(idField);

		JButton okButton = new ListenedButton("OK", ignored -> okListener());
		add(okButton);

		JButton cancelButton = new ListenedButton("Cancel", ignored -> cancelListener());
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
	public void addNewFixtureListener(NewFixtureListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeNewFixtureListener(NewFixtureListener listener) {
		listeners.remove(listener);
	}

	private final JTextField kindField = new JTextField(10);
	private final JFormattedTextField idField =
		new JFormattedTextField(NumberFormat.getIntegerInstance());
	private final JCheckBox rowsField = new JCheckBox();
	private final JFormattedTextField acresField =
		new JFormattedTextField(NumberFormat.getNumberInstance());

	private void okListener() {
		String kind = kindField.getText().trim();
		String acresString = acresField.getText().trim();
		if (kind.isEmpty()) {
			kindField.requestFocusInWindow();
			return;
		} else if (!acresString.isEmpty()) {
			try {
				new BigDecimal(acresString);
			} catch (NumberFormatException except) {
				acresField.requestFocusInWindow();
				return;
			}
		} else {
			String reqId = idField.getText().trim();
			int idNum;
			if (isNumeric(reqId)) {
				OptionalInt temp = parseInt(reqId);
				if (temp.isPresent() && temp.getAsInt() >= 0) {
					idNum = temp.getAsInt();
					idf.register(idNum);
				} else { // TODO: log inconsistency
					idNum = idf.createID();
				}
			} else {
				idNum = idf.createID();
			}
			BigDecimal acres;
			try {
				acres = new BigDecimal(acresString);
			} catch (NumberFormatException except) {
				acres = new BigDecimal(-1);
			}
			Forest forest = new Forest(kind, rowsField.getModel().isSelected(), idNum, acres);
			for (NewFixtureListener listener : listeners) {
				listener.addNewFixture(forest);
			}
			kindField.setText("");
			acresField.setText("-1");
			setVisible(false);
			dispose();
		}
	}

	private void setupField(JTextField field) {
		field.setActionCommand("OK");
		field.addActionListener(ignored -> okListener());
		add(field);
	}

	private void cancelListener() {
		kindField.setText("");
		acresField.setText("-1");
		setVisible(false);
		dispose();
	}
}