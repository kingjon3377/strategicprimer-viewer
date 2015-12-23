package view.map.main;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import model.map.MapDimensions;
import model.map.PointFactory;
import model.viewer.IViewerModel;
import util.NullCleaner;
import view.util.BoxPanel;
import view.util.ListenedButton;

/**
 * A dialog to let the user select a tile by coordinates.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class SelectTileDialog extends JDialog {
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
			                                            "This text should vanish from " +
					                                            "this label before it " +
					                                            "appears.");
	/**
	 * The map model to change the selection in.
	 */
	private final IViewerModel map;

	/**
	 * Constructor.
	 *
	 * @param parent the parent to attach this dialog to
	 * @param model  the map model to change the selection in
	 */
	public SelectTileDialog(final Frame parent, final IViewerModel model) {
		super(parent);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		final JLabel mainLabel = new JLabel("Coordinates of tile to select:");
		mainLabel.setAlignmentX(CENTER_ALIGNMENT);
		mainLabel.setAlignmentY(TOP_ALIGNMENT);

		final ActionListener okListener =
				evt -> handleOK(NullCleaner.assertNotNull(row.getText()),
						NullCleaner.assertNotNull(column.getText()));


		final BoxPanel contentPane = new BoxPanel(false);
		contentPane.add(mainLabel);
		final BoxPanel boxPanel = new BoxPanel(true);
		final JLabel rowLabel = new JLabel("Row: ");
		boxPanel.add(rowLabel);
		boxPanel.add(row);
		row.setActionCommand("OK");
		row.addActionListener(okListener);
		boxPanel.addGlue();
		boxPanel.add(new JLabel("Column: "));
		boxPanel.add(column);
		column.setActionCommand("OK");
		column.addActionListener(okListener);
		boxPanel.addGlue();
		contentPane.add(boxPanel);
		contentPane.add(errorLabel);
		errorLabel.setText("");
		errorLabel.setMinimumSize(new Dimension(200, 15));
		errorLabel.setAlignmentX(CENTER_ALIGNMENT);
		errorLabel.setAlignmentY(TOP_ALIGNMENT);
		final BoxPanel buttonPanel = new BoxPanel(true);
		buttonPanel.addGlue();
		buttonPanel.add(new ListenedButton("OK", okListener));
		buttonPanel.addGlue();
		buttonPanel.add(new ListenedButton("Cancel", evt -> {
			setVisible(false);
			row.setText("-1");
			column.setText("-1");
			dispose();
		}));
		buttonPanel.addGlue();
		contentPane.add(buttonPanel);
		setContentPane(contentPane);
		map = model;
		pack();
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
		Overflow
	}

	/**
	 * The parser to use for checking nnumbers.
	 */
	private static final NumberFormat NUM_PARSER = NullCleaner
			                                               .assertNotNull(NumberFormat
					                                                              .getIntegerInstance());

	/**
	 * @param text  a String to test, representing a number
	 * @param bound its maximum value
	 * @return a State representing any problems with it.
	 */
	private static State checkNumber(final String text, final int bound) {
		try {
			final int num = NUM_PARSER.parse(text).intValue();
			if (num < 0) {
				return State.Negative;
			} else if (num > bound) {
				return State.Overflow;
			} else {
				return State.Valid;
			}
		} catch (final ParseException e) {
			LOGGER.log(Level.FINE, "Non-numeric input", e);
			return State.Nonnumeric;
		}
	}

	/**
	 * Set text for an error.
	 *
	 * @param state the state to give an error message for
	 * @param bound the upper bound, for the overflow case.
	 * @return a suitable message for that error, suitable for following "row" or
	 * "column".
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
	 * Logger.
	 */
	private static final Logger LOGGER = NullCleaner.assertNotNull(Logger
			                                                               .getLogger(
					                                                               SelectTileDialog.class
							                                                               .getName()));

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
		if (colState != State.Valid) {
			errorLabel.setText(errorLabel.getText() + "Column "
					                   + getErrorMessage(colState, dim.cols));
			column.setText("-1");
			column.selectAll();
		}
		final State rowState = checkNumber(rowText, dim.rows - 1);
		if (rowState != State.Valid) {
			errorLabel.setText(errorLabel.getText() + "Row "
					                   + getErrorMessage(rowState, dim.rows));
			row.setText("-1");
			row.selectAll();
		}
		if ((rowState == State.Valid) && (colState == State.Valid)) {
			try {
				map.setSelection(
						PointFactory.point(NUM_PARSER.parse(rowText).intValue(),
								NUM_PARSER.parse(colText).intValue()));
			} catch (final ParseException e) {
				LOGGER.log(Level.SEVERE,
						"Parse failure after we checked input was numeric", e);
			}
			setVisible(false);
			dispose();
		} else {
			pack();
		}
	}
}
