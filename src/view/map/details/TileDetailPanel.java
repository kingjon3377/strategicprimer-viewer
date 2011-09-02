package view.map.details;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import model.viewer.Tile;

/**
 * A panel to show the coordinates and tile type of a tile.
 * 
 * @author Jonathan Lovelace
 */
public class TileDetailPanel extends JPanel {
	/**
	 * The label for showing the coordinates.
	 */
	private final JLabel coordLabel = new JLabel("<html>Coordinates: (-1, -1)</html>");
	/**
	 * The label for showing the tile type.
	 */
	private final JLabel typeLabel = new JLabel("<html>Tile type: NotVisible</html>");
	/**
	 * Constructor.
	 */
	public TileDetailPanel() {
		super(new BorderLayout());
		add(coordLabel, BorderLayout.NORTH);
		add(typeLabel, BorderLayout.SOUTH);
	}
	/**
	 * Set the labels appropriately based on a new tile.
	 * @param tile the tile we now represent
	 */
	public void updateText(final Tile tile) {
		coordLabel.setText("<html>Coordinates: (" + tile.getRow() + ", " + tile.getCol() + ")</html>");
		typeLabel.setText("<html>Tile type: " + DetailPanel.terrainText(tile.getType()) + "</html>");
	}
}
