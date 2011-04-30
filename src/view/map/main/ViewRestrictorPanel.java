package view.map.main;

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
	 * 
	 * @param evt
	 *            the event to handle.
	 */
	@Override
	public void actionPerformed(final ActionEvent evt) {
		if ("Refresh".equals(evt.getActionCommand())) {
			new Thread() {
				@Override
				public void run() {
					refreshMap();
				}
			}.start();
		}
	}

	/**
	 * Refresh the map. Its own method so we can run it in a separate thread.
	 */
	public void refreshMap() {
		final int minRow = betweenMin(0, parse(minRowBox.getText()), mpanel
				.getMap().rows() - 1);
		minRowBox.setText(Integer.toString(minRow));
		final int maxRow = betweenMax(minRow, parse(maxRowBox.getText()),
				mpanel.getMap().rows() - 1);
		maxRowBox.setText(Integer.toString(maxRow));
		final int minCol = betweenMin(0, parse(minColBox.getText()), mpanel
				.getMap().cols() - 1);
		minColBox.setText(Integer.toString(minCol));
		final int maxCol = betweenMax(minCol, parse(maxColBox.getText()),
				mpanel.getMap().cols() - 1);
				mpanel.loadMap(mpanel.getMap(), minRow, maxRow, minCol, maxCol);
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
		return value < min || value > max ? min : value;
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
		return value < min || value > max ? max : value;
	}
}
