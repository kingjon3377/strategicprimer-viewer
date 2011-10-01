package view.map.main;

import java.awt.Graphics2D;

import model.viewer.Tile;

/**
 * A helper to do the actual drawing of a tile. Now an interface so we can
 * easily compare implementations.
 * 
 * @author Jonathan Lovelace
 * 
 */
public interface TileDrawHelper {

	/**
	 * Draw a tile. The graphics context needs to be translated so that its origin is the tile's upper-left-hand corner.
	 * @param pen the graphics context
	 * @param tile the tile to draw
	 * @param width the width of the drawing area
	 * @param height the height of the drawing area
	 */
	void drawTile(final Graphics2D pen, final Tile tile,
			final int width, final int height);

}
