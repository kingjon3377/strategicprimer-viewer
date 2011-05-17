package view.map.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

/**
 * A panel to let the user restrict the portion of the map that's drawn.
 * 
 * Note that the SpringLayout stuff was all tool-generated, though I extracted
 * the constants to clear up the static analysis warnings about "magic numbers."
 * 
 * @author Jonathan Lovelace
 * 
 */
public class ViewRestrictorPanel extends JPanel implements ActionListener {
	/**
	 * Spring amount for the west constraint on the third label.
	 */
	private static final int LABEL3_WEST = 155;
	/**
	 * Spring amount for the north constraint on the third label.
	 */
	private static final int LABEL3_NORTH = 21;
	/**
	 * Spring amount for the north constraint on the second label.
	 */
	private static final int LABEL2_NORTH = 21;
	/**
	 * Spring amount for the west constraint on the first label.
	 */
	private static final int LABEL1_WEST = 155;
	/**
	 * Spring amount for the west constraint on minRowBox.
	 */
	private static final int MINROWBOX_WEST = 129;
	/**
	 * Spring amount for the west constraint on minColBox.
	 */
	private static final int MINCOLBOX_WEST = 129;
	/**
	 * Spring amount for the north constraint on minColBox.
	 */
	private static final int MINCOLBOX_NORTH = 19;
	/**
	 * Spring amount for the west constraint on maxRowBox.
	 */
	private static final int MAXROWBOX_WEST = 178;
	/**
	 * Spring amount for the west constraint on maxColBox.
	 */
	private static final int MAXCOLBOX_WEST = 178;
	/**
	 * Spring amount for the north constraint on maxColBox.
	 */
	private static final int MAXCOLBOX_NORTH = 19;
	/**
	 * Spring amount for the west constraint on the button.
	 */
	private static final int BUTTON_WEST = 204;
	/**
	 * Spring amount for the north constraint on the button.
	 */
	private static final int BUTTON_NORTH = 6;
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
		final SpringLayout springLayout = new SpringLayout();
		springLayout.putConstraint(SpringLayout.NORTH, button, BUTTON_NORTH,
				SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, button, BUTTON_WEST,
				SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, maxColBox,
				MAXCOLBOX_NORTH, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, maxColBox,
				MAXCOLBOX_WEST, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, maxRowBox, 0,
				SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, maxRowBox,
				MAXROWBOX_WEST, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, minColBox,
				MINCOLBOX_NORTH, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, minColBox,
				MINCOLBOX_WEST, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, minRowBox, 0,
				SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, minRowBox,
				MINROWBOX_WEST, SpringLayout.WEST, this);
		setLayout(springLayout);
		final JLabel label = new JLabel("Display only rows ");
		springLayout.putConstraint(SpringLayout.NORTH, label, 2,
				SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, label, 0,
				SpringLayout.WEST, this);
		add(label);
		minRowBox.setText("0");
		add(minRowBox);
		final JLabel labelOne = new JLabel(" to ");
		springLayout.putConstraint(SpringLayout.NORTH, labelOne, 2,
				SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelOne, LABEL1_WEST,
				SpringLayout.WEST, this);
		add(labelOne);
		maxRowBox.setText(Integer.toString(mpanel.getMap().rows() - 1));
		add(maxRowBox);
		final JLabel labelTwo = new JLabel(" and columns ");
		springLayout.putConstraint(SpringLayout.NORTH, labelTwo, LABEL2_NORTH,
				SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelTwo, 16,
				SpringLayout.WEST, this);
		add(labelTwo);
		minColBox.setText("0");
		add(minColBox);
		final JLabel labelThree = new JLabel(" to ");
		springLayout.putConstraint(SpringLayout.NORTH, labelThree,
				LABEL3_NORTH, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelThree, LABEL3_WEST,
				SpringLayout.WEST, this);
		add(labelThree);
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
