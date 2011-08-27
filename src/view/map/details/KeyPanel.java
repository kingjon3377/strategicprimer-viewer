package view.map.details;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.viewer.Tile;
import model.viewer.TileType;
import view.map.main.GUITile;
/**
 * Provides a visual "key" to the various terrain types.
 * @author Jonathan Lovelace
 *
 */
public class KeyPanel extends JPanel {
	/**
	 * Constructor.
	 */
	public KeyPanel() {
		super(new GridLayout(0, 3));
		for (TileType type : TileType.values()) {
			add(new KeyElement(type)); // NOPMD
		}
		setMinimumSize(new Dimension(getMinimumSize().width,
				new KeyElement(TileType.NotVisible).getMinimumSize()
						.height * 3));
	}
	/**
	 * An element of the key.
	 * @author Jonathan Lovelace
	 */
	private static final class KeyElement extends JPanel {
		/**
		 * Minimum buffer space between elements.
		 */
		private static final int HORIZ_BUFFER = 7;
		/**
		 * Constructor.
		 * @param type the type this is the key element for.
		 */
		KeyElement(final TileType type) {
			super();
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			add(Box.createHorizontalGlue());
			add(Box.createRigidArea(new Dimension(HORIZ_BUFFER, 0)));
			final JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			panel.add(Box.createRigidArea(new Dimension(0, 4)));
			final GUITile tile = new GUITile(new Tile(-1, -1, type)); 
			panel.add(tile);
			panel.add(Box.createRigidArea(new Dimension(0, 4)));
			final JLabel label = new JLabel(DetailPanel.terrainText(type));
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
}
