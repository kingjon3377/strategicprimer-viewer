package drivers.map_viewer;

import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;

import java.awt.event.ActionEvent;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;

import lovelace.util.Platform;
import static lovelace.util.BoxPanel.BoxAxis;
import lovelace.util.ListenedButton;
import lovelace.util.BoxPanel;
import lovelace.util.AlignedLabel;

import drivers.gui.common.SPDialog;
import common.map.MapDimensions;
import common.map.Point;

/**
 * A dialog to let the user select a tile by coordinates.
 */
/* package */ class SelectTileDialog extends SPDialog {

	private static final Logger LOGGER = Logger.getLogger(SelectTileDialog.class.getName());

	private enum NumberState { Valid, NonNumeric, Negative, Overflow}

	private final IViewerModel model;

	public SelectTileDialog(@Nullable final Frame parentFrame, final IViewerModel model) {
		super(parentFrame, "Go To ...");
		this.model = model;

		final JPanel contentPanel = new BoxPanel(BoxAxis.PageAxis);
		JLabel mainLabel = new AlignedLabel("Coordinates of tile to select:",
				Component.CENTER_ALIGNMENT, Component.TOP_ALIGNMENT);
		contentPanel.add(mainLabel);
		final BoxPanel boxPanelObj = new BoxPanel(BoxAxis.LineAxis);
		boxPanelObj.add(new JLabel("Row: "));
		boxPanelObj.add(rowField);
		rowField.setActionCommand("OK");
		rowField.addActionListener(ignored -> handleOK());

		boxPanelObj.addGlue();
		boxPanelObj.add(new JLabel("Column:"));
		boxPanelObj.add(columnField);
		columnField.setActionCommand("OK");
		columnField.addActionListener(ignored -> handleOK());

		boxPanelObj.addGlue();
		contentPanel.add(boxPanelObj);
		contentPanel.add(errorLabel);

		errorLabel.setText("");
		errorLabel.setMinimumSize(new Dimension(200, 15));
		errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		errorLabel.setAlignmentY(Component.TOP_ALIGNMENT);

		final BoxPanel buttonPanel = new BoxPanel(BoxAxis.LineAxis);
		buttonPanel.addGlue();
		final JButton okButton = new ListenedButton("OK", this::handleOK);

		final JButton cancelButton = new ListenedButton("Cancel", this::cancelHandler);

		Platform.makeButtonsSegmented(okButton, cancelButton);
		buttonPanel.add(okButton);

		if (!Platform.SYSTEM_IS_MAC) {
			buttonPanel.addGlue();
		}
		buttonPanel.add(cancelButton);
		buttonPanel.addGlue();
		contentPanel.add(buttonPanel);
		setContentPane(contentPanel);
		pack();
	}

	private static final NumberFormat NUM_PARSER = NumberFormat.getIntegerInstance();

	private final JTextField rowField = new JTextField("-1", 4);
	private final JTextField columnField = new JTextField("-1", 4);

	private final JLabel errorLabel = new JLabel(
		"This text should vanish from this label before it appears.");

	private static NumberState checkNumber(final String text, final int bound) {
		try {
			final int number = NUM_PARSER.parse(text).intValue();
			if (number < 0) {
				return NumberState.Negative;
			} else if (number > bound) {
				return NumberState.Overflow;
			} else {
				return NumberState.Valid;
			}
		} catch (final ParseException except) {
			LOGGER.log(Level.FINE, "Non-numeric input", except);
			return NumberState.NonNumeric;
		}
	}

	// TODO: put into NumberState, as a method taking int (for the overflow case)?
	private static String getErrorMessage(final NumberState state, final int bound) {
		switch (state) {
		case Valid:
			return "";
		case NonNumeric:
			return "must be a whole number. ";
		case Negative:
			return "must be positive. ";
		case Overflow:
			return String.format("must be less than %d.", bound);
		default:
			throw new IllegalStateException("Exhaustive switch wasn't");
		}
	}

	private void handleOK() {
		final String rowText = rowField.getText();
		final String columnText = columnField.getText();
		errorLabel.setText("");
		final MapDimensions dimensions = model.getMapDimensions();
		final NumberState columnState = checkNumber(columnText, dimensions.getColumns() - 1);
		if (NumberState.Valid != columnState) {
			errorLabel.setText(errorLabel.getText() + "Column" +
				getErrorMessage(columnState, dimensions.getColumns()));
			columnField.setText("-1");
			columnField.selectAll();
		}

		final NumberState rowState = checkNumber(rowText, dimensions.getRows() - 1);
		if (NumberState.Valid != rowState) {
			errorLabel.setText(errorLabel.getText() + "Row " +
				getErrorMessage(rowState, dimensions.getRows()));
			rowField.setText("-1");
			rowField.selectAll();
		}

		if (NumberState.Valid == rowState && NumberState.Valid == columnState) {
			try {
				model.setSelection(new Point(NUM_PARSER.parse(rowText).intValue(),
					NUM_PARSER.parse(columnText).intValue()));
			} catch (final ParseException except) {
				LOGGER.log(Level.SEVERE,
					"Parse failure after we checked input was numeric", except);
				// TODO: return here, surely?
			}
			setVisible(false);
			dispose();
		} else {
			pack();
		}
	}

	private void cancelHandler() {
		setVisible(false);
		rowField.setText("-1");
		columnField.setText("-1");
		dispose();
	}
}
