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

import model.viewer.MapModel;
import util.IsNumeric;
/**
 * A dialog to let the user select a tile by coordinates.
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
			"This is a label for error messages. This text should be removed before the constructor finishes.");
	/**
	 * The map model to change the selection in.
	 */
	private final MapModel map;
	/**
	 * Constructor.
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
	 * Handle the OK button.
	 * @param rowText the text in the row box
	 * @param colText the text in the column box 
	 */
	private void handleOK(final String rowText, final String colText) {
		boolean valid = true;
		errorLabel.setText("");
		if (!IsNumeric.isNumeric(colText)) { // NOPMD
			errorLabel.setText(errorLabel.getText()
					+ "Column must be a whole number. ");
			column.setText("-1");
			column.selectAll();
			valid = false;
		} else if (Integer.parseInt(colText) < 0) {
			errorLabel.setText(errorLabel.getText()
					+ "Column must be positive. ");
			column.setText("-1");
			column.selectAll();
			valid = false;
		} else if (Integer.parseInt(colText) >= map.getSizeCols()) {
			errorLabel.setText(errorLabel.getText()
					+ "Column must be less than "
					+ Integer.toString(map.getSizeCols()));
			column.setText("-1");
			column.selectAll();
			valid = false;
		}
		if (!IsNumeric.isNumeric(rowText)) { // NOPMD
			errorLabel.setText(errorLabel.getText()
					+ "Row must be a whole number. ");
			row.setText("-1");
			row.selectAll();
			valid = false;
		} else if (Integer.parseInt(rowText) < 0) {
			errorLabel.setText(errorLabel.getText() + "Row must be positive. ");
			row.setText("-1");
			row.selectAll();
			valid = false;
		} else if (Integer.parseInt(rowText) >= map.getSizeRows()) {
			errorLabel.setText(errorLabel.getText() + "Row must be less than "
					+ Integer.toString(map.getSizeRows()));
			row.setText("-1");
			row.selectAll();
			valid = false;
		}
		if (valid) {
			map.setSelection(Integer.parseInt(rowText),
					Integer.parseInt(colText));
			setVisible(false);
		} else {
			pack();
		}
	}
}
