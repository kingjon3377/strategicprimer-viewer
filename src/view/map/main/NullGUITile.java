package view.map.main;

import model.viewer.Tile;
import model.viewer.TileType;

/**
 * A GUITile for when the map shouldn't show a tile at a location.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class NullGUITile extends GUITile {
	/**
	 * Constructor.
	 * 
	 * @param row
	 *            the row number of the tile
	 * @param col
	 *            the column number of the tile
	 */
	public NullGUITile(final int row, final int col) {
		super(new Tile(row, col, TileType.NotVisible));
	}
}
