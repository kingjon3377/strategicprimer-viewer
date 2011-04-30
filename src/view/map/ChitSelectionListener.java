package view.map;

import java.awt.event.MouseEvent;

import javax.swing.JLabel;

/**
 * A class to handle selecting Chits. 
 * @author kingjon
 *
 */
public class ChitSelectionListener extends SelectionListener {
	/**
	 * The label we'll write the details to.
	 */
	private final JLabel detailLabel;
	/**
	 * Constructor.
	 * @param label the label we'll write the details of the selected item to.
	 */
	public ChitSelectionListener(final JLabel label) {
		super();
		if (label == null) {
			throw new IllegalArgumentException("Label was null");
		}
		detailLabel = label;
	}
	/**
	 * Handle mouse clicks.
	 * 
	 * @param event
	 *            the event to handle
	 */
	@Override
	public void mouseClicked(final MouseEvent event) {
		super.mouseClicked(event);
		if (selection instanceof Chit) {
			detailLabel.setText(((Chit) (selection)).describe());
		} else {
			detailLabel.setText("");
		}
	}

}
