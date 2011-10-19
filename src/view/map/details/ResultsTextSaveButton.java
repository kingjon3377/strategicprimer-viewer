package view.map.details;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

/**
 * The button to save results. Pulled out of ResultsPanel because it had too
 * many fields, and this also removes an anonymous inner class.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class ResultsTextSaveButton extends JButton implements ActionListener {
	/**
	 * Command for saving changed results to the map.
	 */
	private static final String SAVE_COMMAND = "<html><p>Save changed results</p></html>";
	/**
	 * Minimum height of the button.
	 */
	private static final int BUTTON_MIN_HT = 15;
	/**
	 * Preferred height of the button.
	 */
	private static final int BUTTON_HEIGHT = 20;
	/**
	 * Maximum height of the button.
	 */
	private static final int BUTTON_MAX_HT = 25;
	/**
	 * Constructor.
	 * @param minWidth the minimum width of the button.
	 * @param width the preferred width of the button.
	 * @param maxWidth the maximum width of the button.
	 */
	public ResultsTextSaveButton(final int minWidth, final int width,
			final int maxWidth) {
		super(SAVE_COMMAND);
		setMinimumSize(new Dimension(minWidth, BUTTON_MIN_HT));
		setPreferredSize(new Dimension(width, BUTTON_HEIGHT));
		setMaximumSize(new Dimension(maxWidth, BUTTON_MAX_HT));
	}
	/**
	 * Handle button press.
	 * @param event the event to handle.
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		if (SAVE_COMMAND.equals(event.getActionCommand())) {
			firePropertyChange("save-text", false, true);
		}
	}
}
