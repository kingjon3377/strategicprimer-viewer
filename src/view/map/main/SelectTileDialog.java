package view.map.main;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.map.MapDimensions;
import model.map.PointFactory;
import model.viewer.MapModel;
import util.IsNumeric;

/**
 * A dialog to let the user select a tile by coordinates.
 *
 * @author Jonathan Lovelace
 *
 */
public class SelectTileDialog extends JDialog implements ActionListener {
	/**
	 * The first text field.
	 */
	private final JTextField row = new JTextField("-1", 4);
	/**
	 * The second text field.
	 */
	private final JTextField column = new JTextField("-1", 4);
	/**
	 * A label to display error messages.
	 */
	private final JLabel errorLabel = new JLabel(
			"This text should vanish from the error-message label before the constructor ends.");
	/**
	 * The map model to change the selection in.
	 */
	private final MapModel map;

	/**
	 * Constructor.
	 *
	 * @param parent the parent to attach this dialog to
	 * @param model the map model to change the selection in
	 */
	public SelectTileDialog(final Frame parent, final MapModel model) {
		super(parent);
		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		final JLabel mainLabel = new JLabel("Coordinates of tile to select:");
		mainLabel.setAlignmentX(CENTER_ALIGNMENT);
		mainLabel.setAlignmentY(LEFT_ALIGNMENT);
		add(mainLabel);
		final JPanel boxPanel = new JPanel();
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.LINE_AXIS));
		final JLabel rowLabel = new JLabel("Row: ");
		boxPanel.add(rowLabel);
		boxPanel.add(row);
		boxPanel.add(Box.createHorizontalGlue());
		boxPanel.add(new JLabel("Column: "));
		boxPanel.add(column);
		boxPanel.add(Box.createHorizontalGlue());
		add(boxPanel);
		add(errorLabel);
		errorLabel.setText("");
		errorLabel.setMinimumSize(new Dimension(200, 15));
		errorLabel.setAlignmentX(CENTER_ALIGNMENT);
		errorLabel.setAlignmentY(LEFT_ALIGNMENT);
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		final JButton okButton = new JButton("OK");
		okButton.addActionListener(this);
		buttonPanel.add(okButton);
		buttonPanel.add(Box.createHorizontalGlue());
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		buttonPanel.add(Box.createHorizontalGlue());
		add(buttonPanel);
		map = model;
		pack();
	}

	/**
	 * Handle button presses.
	 *
	 * @param event the event to handle
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		if ("OK".equals(event.getActionCommand())) {
			handleOK(row.getText(), column.getText());
		} else if ("Cancel".equals(event.getActionCommand())) {
			setVisible(false);
			row.setText("-1");
			column.setText("-1");
		}
	}

	/**
	 * Possible states.
	 */
	private enum State {
		/**
		 * Valid.
		 */
		Valid,
		/**
		 * Nonnumeric.
		 */
		Nonnumeric,
		/**
		 * Negative.
		 */
		Negative,
		/**
		 * Too large.
		 */
		Overflow;
	}

	/**
	 * @param text a String to test, representing a number
	 * @param bound its maximum value
	 * @return a State representing any problems with it.
	 */
	private static State checkNumber(final String text, final int bound) {
		if (IsNumeric.isNumeric(text)) {
			if (Integer.parseInt(text) < 0) {
				return State.Negative; // NOPMD
			} else if (Integer.parseInt(text) > bound) {
				return State.Overflow; // NOPMD
			} else {
				return State.Valid; // NOPMD
			}
		} else {
			return State.Nonnumeric; // NOPMD
		}
	}

	/**
	 * Set text for an error.
	 *
	 * @param state the state to give an error message for
	 * @param bound the upper bound, for the overflow case.
	 * @return a suitable message for that error, suitable for following "row"
	 *         or "column".
	 */
	private static String getErrorMessage(final State state, final int bound) {
		switch (state) {
		case Negative:
			return " must be positive. "; // NOPMD
		case Nonnumeric:
			return " must be a whole number. "; // NOPMD
		case Overflow:
			return " must be less than " + Integer.toString(bound); // NOPMD
		case Valid:
			return "";
		default:
			throw new IllegalStateException("Default case of enum switch");
		}
	}

	/**
	 * Handle the OK button.
	 *
	 * @param rowText the text in the row box
	 * @param colText the text in the column box
	 */
	private void handleOK(final String rowText, final String colText) {
		errorLabel.setText("");
		final MapDimensions dim = map.getMapDimensions();
		final State colState = checkNumber(colText, dim.cols - 1);
		if (!(colState == State.Valid)) {
			errorLabel.setText(errorLabel.getText() + "Column "
					+ getErrorMessage(colState, dim.cols));
			column.setText("-1");
			column.selectAll();
		}
		final State rowState = checkNumber(rowText, dim.rows - 1);
		if (!(rowState == State.Valid)) {
			errorLabel.setText(errorLabel.getText() + "Row "
					+ getErrorMessage(rowState, dim.rows));
			row.setText("-1");
			row.selectAll();
		}
		if (rowState == State.Valid && colState == State.Valid) {
			map.setSelection(PointFactory.point(Integer.parseInt(rowText),
					Integer.parseInt(colText)));
			setVisible(false);
		} else {
			pack();
		}
	}
}
