// $codepro.audit.disable com.instantiations.assist.eclipse.analysis.avoidInnerClasses
package view.map.details;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import model.exploration.ExplorationRunner;
import model.map.Tile;

/**
 * A panel for displaying and editing tile text i.e. results.
 * 
 * @author Jonathan Lovelace
 */
public class ResultsPanel extends JPanel implements PropertyChangeListener {
	/**
	 * Exploration runner to produce exploration results.
	 */
	private final ExplorationRunner runner;
	/**
	 * Minimum width of this panel.
	 */
	private static final int MINIMUM_WIDTH = 200;
	/**
	 * Preferred width of this panel.
	 */
	private static final int PREF_WIDTH = 250;
	/**
	 * Maximum width of this panel.
	 */
	private static final int MAX_WIDTH = 300;
	/**
	 * Minimum height of the label.
	 */
	private static final int LABEL_MIN_HT = 10;
	/**
	 * Preferred height of the label.
	 */
	private static final int LABEL_HEIGHT = 15;
	/**
	 * Maximum height of the label.
	 */
	private static final int LABEL_MAX_HT = 20;
	/**
	 * Field to show and edit exploration results.
	 */
	private final JTextArea field = new JTextArea();

	/**
	 * Constructor.
	 * 
	 * @param minHeight
	 *            the minimum height of this panel
	 * @param height
	 *            the (preferred) height of this panel
	 * @param maxHeight
	 *            the maximum height of this panel
	 * @param xrunner
	 *            an exploration runner.
	 */
	public ResultsPanel(final int minHeight, final int height,
			final int maxHeight, final ExplorationRunner xrunner) {
		super(new BorderLayout());
		runner = xrunner;
		final JLabel label = new JLabel("Exploration results:");
		label.setAlignmentY(SwingConstants.CENTER);
		label.setMinimumSize(new Dimension(MINIMUM_WIDTH, LABEL_MIN_HT));
		label.setPreferredSize(new Dimension(PREF_WIDTH, LABEL_HEIGHT));
		label.setMaximumSize(new Dimension(MAX_WIDTH, LABEL_MAX_HT));
		add(label, BorderLayout.NORTH);

		final ResultsTextSaveButton button = new ResultsTextSaveButton(
				MINIMUM_WIDTH, PREF_WIDTH, MAX_WIDTH);
		button.addPropertyChangeListener(this);
		add(button, BorderLayout.SOUTH);
	}

	/**
	 * @param newTile
	 *            the new tile
	 */
	public void setTile(final Tile newTile) {
		if (!newTile.equals(tile)) {
			tile = newTile;
		}
	}

	/**
	 * The tile we get and save results from and to.
	 */
	private Tile tile;

	/**
	 * Handle a property change.
	 * 
	 * @param evt
	 *            the event to handle.
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if ("tile".equals(evt.getPropertyName())) {
			setTile((Tile) evt.getNewValue());
			repaint();
		} 
	}
}
