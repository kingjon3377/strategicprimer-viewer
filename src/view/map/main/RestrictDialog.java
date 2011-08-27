package view.map.main;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A dialog to restrict the view to a subset of the map.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class RestrictDialog extends JDialog implements ActionListener {
	/**
	 * The map panel we'll update when the user orders a change.
	 */
	private final MapPanel mpanel;

	/**
	 * The minimum-row box.
	 */
	private final JTextField minRow = new JTextField(4);

	/**
	 * The maximum-row box.
	 */
	private final JTextField maxRow = new JTextField(4);

	/**
	 * The minimum-column box.
	 */
	private final JTextField minCol = new JTextField(4);

	/**
	 * The maximum-column box.
	 */
	private final JTextField maxCol = new JTextField(4);

	/**
	 * The "OK" button.
	 */
	private final JButton okButton = new JButton("OK");

	/**
	 * The "Cancel" button.
	 */
	private final JButton cancelButton = new JButton("Cancel");

	/**
	 * Constructor.
	 * 
	 * @param mapPanel
	 *            the map panel
	 */
	public RestrictDialog(final MapPanel mapPanel) {
		super(null, "Restrict view to map subset", Dialog.DEFAULT_MODALITY_TYPE);
		setMinimumSize(new Dimension(245, 100));
		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		mpanel = mapPanel;
		add(Box.createVerticalGlue());
		final JPanel rowPanel = new JPanel();
		rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.LINE_AXIS));
		rowPanel.add(Box.createHorizontalGlue());
		rowPanel.add(new JLabel("Display only rows "));
		rowPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		rowPanel.add(minRow);
		final VisibleDimensions visDim = mpanel.getVisibleDimensions();
		minRow.setText(Integer.toString(visDim.getMinimumRow()));
		rowPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		rowPanel.add(new JLabel(" to "));
		rowPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		rowPanel.add(maxRow);
		maxRow.setText(Integer.toString(visDim.getMaximumRow()));
		rowPanel.add(Box.createHorizontalGlue());
		add(rowPanel);
		add(Box.createRigidArea(new Dimension(0, 5)));
		final JPanel colPanel = new JPanel();
		colPanel.setLayout(new BoxLayout(colPanel, BoxLayout.LINE_AXIS));
		colPanel.add(Box.createHorizontalGlue());
		colPanel.add(new JLabel("Display only columns "));
		colPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		colPanel.add(minCol);
		minCol.setText(Integer.toString(visDim.getMinimumCol()));
		colPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		colPanel.add(new JLabel(" to "));
		colPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		colPanel.add(maxCol);
		maxCol.setText(Integer.toString(visDim.getMaximumCol()));
		colPanel.add(Box.createHorizontalGlue());
		add(colPanel);
		add(Box.createRigidArea(new Dimension(0, 5)));
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(okButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPanel.add(cancelButton);
		buttonPanel.add(Box.createHorizontalGlue());
		add(buttonPanel);
		add(Box.createVerticalGlue());
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
	}

	/**
	 * Handle button presses.
	 * 
	 * @param evt
	 *            the event to handle.
	 */
	@Override
	public void actionPerformed(final ActionEvent evt) {
		if ("Cancel".equals(evt.getActionCommand())) {
			setVisible(false);
			dispose();
		} else if ("OK".equals(evt.getActionCommand())) {
			if (verifyInput()) {
				setVisible(false);
				refreshMap();
				dispose();
			} else {
				JOptionPane.showMessageDialog(null, whyInvalidInput(),
						"Invalid restriction parameters",
						JOptionPane.WARNING_MESSAGE);
				correctInvalidInput(mpanel.getVisibleDimensions());
			}
		}
	}

	/**
	 * Verify whether our input is valid.
	 * 
	 * @return whether the input in the text-boxes makes sense.
	 */
	private boolean verifyInput() {
		return between(parse(minRow.getText()), 0, mpanel.getMap().rows() - 1)
				&& between(parse(maxRow.getText()), parse(minRow.getText()),
						mpanel.getMap().rows() - 1)
				&& between(parse(minCol.getText()), 0,
						mpanel.getMap().cols() - 1)
				&& between(parse(maxCol.getText()), parse(minCol.getText()),
						mpanel.getMap().cols() - 1);
	}

	/**
	 * A convenience method to "simplify" other methods.
	 * @param args a series of strings
	 * @return true if all of them are numeric, false otherwise.
	 */
	private static boolean areNumeric(final String... args) {
		for (String arg : args) {
			try {
				Integer.parseInt(arg);
			} catch (final NumberFormatException except) {
				return false; // NOPMD
			}
		}
		return true;
	}
	/**
	 * @return a description of why input is invalid
	 */
	private String whyInvalidInput() {
		if (areNumeric(minCol.getText(), maxCol.getText(), minRow.getText(),
				maxRow.getText())) {
			if (parse(minCol.getText()) < 0 || parse(minRow.getText()) < 0) {
				return "Minimum row and column must be greater than or equal to zero."; // NOPMD
			} else if (parse(maxCol.getText()) >= mpanel.getMap().cols()) {
				return "Maximum column must be less than " // NOPMD
						+ mpanel.getMap().cols();
			} else if (parse(maxRow.getText()) >= mpanel.getMap().rows()) {
				return "Maximum row must be less than " // NOPMD
						+ mpanel.getMap().rows();
			} else if (parse(minCol.getText()) > parse(maxCol.getText())) {
				return "Maximum column cannot be below minimum column."; // NOPMD
			} else if (parse(minRow.getText()) > parse(maxRow.getText())) {
				return "Maximum row cannot be below minimum row."; // NOPMD
			} else {
				return "Input is valid ... if you can see this message, please report this as a bug."; // NOPMD
			}
		} else {
			return "All bounds must be whole numbers."; // NOPMD
		}
	}

	/**
	 * Correct invalid input, so the user can try again.
	 * @param visDim the current visible dimensions of the map
	 */
	private void correctInvalidInput(final VisibleDimensions visDim) {
		if (parse(minCol.getText()) < 0) {
			minCol.setText(Integer.toString(visDim.getMinimumCol()));
		}
		if (parse(maxCol.getText()) >= mpanel.getMap().cols()
				|| parse(minCol.getText()) > parse(maxCol.getText())) {
			maxCol.setText(Integer.toString(visDim.getMaximumCol()));
		}
		if (parse(minRow.getText()) < 0) {
			minRow.setText(Integer.toString(visDim.getMinimumRow()));
		}
		if (parse(maxRow.getText()) >= mpanel.getMap().rows()
				|| parse(minRow.getText()) > parse(maxRow.getText())) {
			maxRow.setText(Integer.toString(visDim.getMaximumRow()));
		}
	}

	/**
	 * @param value
	 *            a value
	 * @param min
	 *            the bottom of a range
	 * @param max
	 *            the top of a range
	 * @return whether value is in the range
	 */
	private static boolean between(final int value, final int min, final int max) {
		return value >= min && value <= max;
	}

	/**
	 * Refresh the map. Its own method so we can run it in a separate thread.
	 */
	private void refreshMap() {
		final int minimumRow = betweenMin(0, parse(minRow.getText()), mpanel
				.getMap().rows() - 1);
		final int maximumRow = betweenMax(minimumRow, parse(maxRow.getText()),
				mpanel.getMap().rows() - 1);
		final int minimumCol = betweenMin(0, parse(minCol.getText()), mpanel
				.getMap().cols() - 1);
		final int maximumCol = betweenMax(minimumCol, parse(maxCol.getText()),
				mpanel.getMap().cols() - 1);
		mpanel.loadMap(mpanel.getMap(), minimumRow, maximumRow, minimumCol,
				maximumCol);
	}

	/**
	 * Parse a string into a number.
	 * 
	 * @param str
	 *            the string to parse
	 * @return the number, or -1 if it's non-numeric.
	 */
	private static int parse(final String str) {
		try {
			return Integer.parseInt(str); // NOPMD
		} catch (final NumberFormatException except) {
			return -1;
		}
	}

	/**
	 * @param min
	 *            the bottom of a range
	 * @param value
	 *            the value to test
	 * @param max
	 *            the top of the range
	 * @return value if it's in the range, or min otherwise.
	 */
	private static int betweenMin(final int min, final int value, final int max) {
		return (value < min || value > max) ? min : value;
	}

	/**
	 * @param min
	 *            the bottom of a range
	 * @param value
	 *            the value to test
	 * @param max
	 *            the top of the range
	 * @return value if it's in the range, or max otherwise.
	 */
	private static int betweenMax(final int min, final int value, final int max) {
		return (value < min || value > max) ? max : value;
	}
}
