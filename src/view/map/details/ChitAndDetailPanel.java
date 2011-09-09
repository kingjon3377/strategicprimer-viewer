package view.map.details;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import model.viewer.Tile;
import model.viewer.TileFixture;
import model.viewer.TileType;

/**
 * A panel to encapsulate the ChitPanel and the label for showing a chit's
 * details.
 * 
 * @author Jonathan Lovelace
 */
public class ChitAndDetailPanel extends JPanel implements PropertyChangeListener {
	/**
	 * The detail label.
	 */
	private final JLabel label = new JLabel();
	/**
	 * The ChitPanel.
	 */
	private final ChitPanel panel = new ChitPanel(new ChitSelectionListener(label));
	/**
	 * The maximum width of either control.
	 */
	private static final int ITEM_MAX_WIDTH = 200;
	/**
	 * The minimum width of either control.
	 */
	private static final int ITEM_MIN_WIDTH = 100;
	/**
	 * The preferred width of either control.
	 */
	private static final int ITEM_WIDTH = 150;
	/**
	 * Constructor.
	 * @param maxHeight the maximum height of the panel we're part of
	 * @param minHeight the minimum height of the panel we're part of
	 * @param height the (preferred) height of the panel we're part of
	 */
	public ChitAndDetailPanel(final int maxHeight, final int minHeight, final int height) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		final JScrollPane scroller = new JScrollPane(panel);
		scroller.setMaximumSize(new Dimension(ITEM_MAX_WIDTH, maxHeight));
		scroller.setMinimumSize(new Dimension(ITEM_MIN_WIDTH, minHeight));
		scroller.setPreferredSize(new Dimension(ITEM_WIDTH, height));
		scroller.setBorder(null);
		add(scroller);
		label.setMaximumSize(new Dimension(ITEM_MAX_WIDTH, maxHeight));
		label.setMinimumSize(new Dimension(ITEM_MIN_WIDTH, minHeight));
		label.setPreferredSize(new Dimension(ITEM_WIDTH, height));
		add(label);
	}
	/**
	 * The tile the chits are on.
	 */
	private Tile tile = new Tile(-1, -1, TileType.NotVisible);
	/**
	 * Update the chits for a new tile.
	 * @param newTile the new tile
	 */
	public void updateChits(final Tile newTile) {
		if (!newTile.equals(tile)) {
			tile = newTile;
		panel.clear();
		for (final TileFixture fix : tile.getContents()) {
			panel.add(fix);
		}
		}
	}
	/**
	 * Handle a property change.
	 * @param evt the event to handle.
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if ("tile".equals(evt.getPropertyName())) {
			updateChits((Tile) evt.getNewValue());
			repaint();
		}
	}
}
