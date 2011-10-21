package view.map.details;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.map.TileType;
import view.map.main.MapComponent;
import view.map.main.TileUIHelper;

/**
 * An element of the key.
 * 
 * @author Jonathan Lovelace
 */
final class KeyElement extends JPanel {
	/**
	 * UI helper for the terrain type descriptions and colors.
	 */
	private static final TileUIHelper TUI_HELPER = new TileUIHelper();
	/**
	 * Minimum buffer space between elements.
	 */
	private static final int HORIZ_BUFFER = 7;
	/**
	 * Minimum size of a colored area.
	 */
	private static final Dimension MIN_SIZE = new Dimension(4, 4);
	/**
	 * Maximum size of a colored area.
	 */
	private static final Dimension MAX_SIZE = new Dimension(
			MapComponent.getTileSize(), MapComponent.getTileSize());
	/**
	 * Preferred size of a colored area.
	 */
	private static final Dimension PREF_SIZE = new Dimension(8, 8);

	/**
	 * Constructor.
	 * 
	 * @param version the map version
	 * @param type
	 *            the type this is the key element for.
	 */
	KeyElement(final int version, final TileType type) {
		super();
		final TileUIHelper helper = TUI_HELPER;
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		add(Box.createHorizontalGlue());
		add(Box.createRigidArea(new Dimension(HORIZ_BUFFER, 0)));
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(Box.createRigidArea(new Dimension(0, 4)));
		final JComponent tile = new JComponent() {
			/**
			 * The color of this Component.
			 */
			private final Color color = helper.get(version, type);

			/**
			 * @param pen
			 *            the graphics context
			 */
			@Override
			public void paint(final Graphics pen) {
				final Graphics context = pen.create();
				context.setColor(color);
				context.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		tile.setMinimumSize(MIN_SIZE);
		tile.setPreferredSize(PREF_SIZE);
		tile.setMaximumSize(MAX_SIZE);
		panel.add(tile);
		panel.add(Box.createRigidArea(new Dimension(0, 4)));
		final JLabel label = new JLabel(TUI_HELPER.getDescription(type));
		panel.add(label);
		panel.add(Box.createRigidArea(new Dimension(0, 4)));
		add(panel);
		add(Box.createRigidArea(new Dimension(HORIZ_BUFFER, 0)));
		add(Box.createHorizontalGlue());
		setMinimumSize(new Dimension(Math.max(tile.getMinimumSize().width,
				label.getMinimumSize().width) + HORIZ_BUFFER * 2,
				tile.getMinimumSize().height + label.getMinimumSize().height
						+ 12));
	}
}
