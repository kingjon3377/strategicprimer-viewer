package view.map.main;

import java.awt.Graphics;

import model.map.Tile;

/**
 * A helper to do the actual drawing of a tile. Now an interface so we can
 * easily compare implementations.
 * 
 * @author Jonathan Lovelace
 * 
 */
public interface TileDrawHelper {

	/**
	 * Draw a tile. The graphics context needs to be translated so that its
	 * origin is the tile's upper-left-hand corner.
	 * 
	 * @param pen
	 *            the graphics context
	 * @param tile
	 *            the tile to draw
	 * @param width
	 *            the width of the drawing area
	 * @param height
	 *            the height of the drawing area
	 */
	void drawTile(final Graphics pen, final Tile tile,
			final int width, final int height);
	/**
	 * Draw a tile, at the given coordinates.
	 * @param pen the graphics context
	 * @param tile the tile to draw
	 * @param xCoord the tile's left-hand edge
	 * @param yCoord the tile's upper edge
	 * @param width the tile's width
	 * @param height the tile's height
	 */
	void drawTile(final Graphics pen, final Tile tile, final int xCoord,
			final int yCoord, final int width, final int height);
}
