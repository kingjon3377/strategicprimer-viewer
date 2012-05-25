package view.map.details;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.dnd.DropTarget;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;

/**
 * A panel to encapsulate the ChitPanel and the label for showing a chit's
 * details.
 * 
 * @author Jonathan Lovelace
 */
public class ChitAndDetailPanel extends JPanel implements
		PropertyChangeListener {
	/**
	 * The detail label.
	 */
	private final ChitDescriptionLabel label = new ChitDescriptionLabel();
	/**
	 * The ChitPanel.
	 */
	private final ChitPanel panel = new ChitPanel(new ChitSelectionListener(
			label));
	/**
	 * The maximum width of either control.
	 */
	private static final int ITEM_MAX_WIDTH = 200;
	/**
	 * The minimum width of either control.
	 */
	private static final int ITEM_MIN_WIDTH = 120;
	/**
	 * The preferred width of either control.
	 */
	private static final int ITEM_WIDTH = 160;
	/**
	 * The property we listen for.
	 */
	private final String property;

	/**
	 * Constructor.
	 * 
	 * @param maxHeight
	 *            the maximum height of the panel we're part of
	 * @param minHeight
	 *            the minimum height of the panel we're part of
	 * @param height
	 *            the (preferred) height of the panel we're part of
	 * @param propertyName
	 *            the property we listen for
	 */
	public ChitAndDetailPanel(final int maxHeight, final int minHeight,
			final int height, final String propertyName) {
		super(new BorderLayout());
		final JPanel wrapperPanel = new JPanel();
		wrapperPanel
				.setLayout(new BoxLayout(wrapperPanel, BoxLayout.LINE_AXIS));
		final JScrollPane scroller = new JScrollPane(panel);
		scroller.setMaximumSize(new Dimension(ITEM_MAX_WIDTH, maxHeight));
		scroller.setMinimumSize(new Dimension(ITEM_MIN_WIDTH, minHeight));
		scroller.setPreferredSize(new Dimension(ITEM_WIDTH, height));
		scroller.setBorder(null);
		wrapperPanel.add(scroller);
		label.setMaximumSize(new Dimension(ITEM_MAX_WIDTH, maxHeight));
		label.setMinimumSize(new Dimension(ITEM_MIN_WIDTH, minHeight));
		label.setPreferredSize(new Dimension(ITEM_WIDTH, height));
		wrapperPanel.add(label);
		property = propertyName;
		final JLabel title = new JLabel(
				"tile".equals(property) ? "<html><center>Tile contents:</center></html>"
						: "<html><center>Tile contents on<br />secondary map:</center></html>",
				SwingConstants.CENTER);
		add(title, BorderLayout.NORTH);
		add(wrapperPanel, BorderLayout.CENTER);
		setDropTarget(new DropTarget(this, new ChitDropListener()));
	}

	/**
	 * The tile the chits are on.
	 */
	private Tile tile = new Tile(-1, -1, TileType.NotVisible, "");

	/**
	 * @param fix
	 *            a TileFixture to add to the underlying tile
	 */
	public void addFixture(final TileFixture fix) {
		if (!tile.getContents().contains(fix)) {
			tile.addFixture(fix);
			panel.add(fix);
			panel.validate();
		}
	}

	/**
	 * Update the chits for a new tile.
	 * 
	 * @param newTile
	 *            the new tile
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
	 * 
	 * @param evt
	 *            the event to handle.
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if (property.equals(evt.getPropertyName())) {
			updateChits((Tile) evt.getNewValue());
			repaint();
		}
	}
}
