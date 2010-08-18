package view.map;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A panel to let the user restrict the portion of the map that's drawn.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class ViewRestrictorPanel extends JPanel implements ActionListener {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = -3225777528295810270L;
	/**
	 * The map panel we'll update when the user orders a change.
	 */
	private final MapPanel mpanel;
	/**
	 * The text box for the minimum row.
	 */
	private final JTextField minRowBox = new JTextField(2);
	/**
	 * The text box for the maximum row.
	 */
	private final JTextField maxRowBox = new JTextField(2);
	/**
	 * The text box for the minimum column.
	 */
	private final JTextField minColBox = new JTextField(2);
	/**
	 * The text box for the maxiumum column.
	 */
	private final JTextField maxColBox = new JTextField(2);
	/**
	 * The button to execute the new restricted view.
	 */
	private final JButton button = new JButton("Refresh");

	/**
	 * Constructor.
	 * 
	 * @param mapPanel
	 *            the map panel.
	 */
	public ViewRestrictorPanel(final MapPanel mapPanel) {
		super();
		mpanel = mapPanel;
		add(new JLabel("Display only rows "));
		minRowBox.setText("0");
		add(minRowBox);
		add(new JLabel(" to "));
		maxRowBox.setText(Integer.toString(mpanel.getMap().rows() - 1));
		add(maxRowBox);
		add(new JLabel(" and columns "));
		minColBox.setText("0");
		add(minColBox);
		add(new JLabel(" to "));
		maxColBox.setText(Integer.toString(mpanel.getMap().cols() - 1));
		add(maxColBox);
		button.addActionListener(this);
		add(button);
	}

	/**
	 * Handle button presses.
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		if ("Refresh".equals(e.getActionCommand())) {
			int minRow = parse(minRowBox.getText());
			if (minRow < 0 || minRow > mpanel.getMap().rows() - 1) {
				minRowBox.setText("0");
				minRow = 0;
			}
			int maxRow = parse(maxRowBox.getText());
			if (maxRow < minRow || maxRow > mpanel.getMap().rows() - 1) {
				maxRow = mpanel.getMap().rows() - 1;
				maxRowBox.setText(Integer.toString(maxRow));
			}
			int minCol = parse(minColBox.getText());
			if (minCol < 0 || minCol > mpanel.getMap().cols() - 1) {
				minColBox.setText("0");
				minCol = 0;
			}
			int maxCol = parse(maxColBox.getText());
			if (maxCol < minCol || maxCol > mpanel.getMap().cols() - 1) {
				maxCol = mpanel.getMap().cols() - 1;
				maxColBox.setText(Integer.toString(maxCol));
			}
			mpanel.loadMap(mpanel.getMap(), minRow, maxRow, minCol, maxCol);
		}
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
}
