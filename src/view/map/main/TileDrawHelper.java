package view.map.main;

import java.awt.Graphics;

import model.map.IMapNG;
import model.map.Point;
import view.util.Coordinate;

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
	 * @param pen the graphics context
	 * @param map the map to draw the tile from
	 * @param location the location to draw
	 * @param width the width of the drawing area
	 * @param height the height of the drawing area
	 */
	void drawTileTranslated(Graphics pen, IMapNG map, Point location,
			int width, int height);

	/**
	 * Draw a tile, at the given coordinates.
	 *
	 * @param pen the graphics context
	 * @param map the map to draw the tile from
	 * @param location the location to draw
	 * @param coordinates the coordinates of the tile's upper-left corner
	 * @param dimensions the width (X) and height (Y) of the tile
	 */
	void drawTile(Graphics pen, IMapNG map, Point location, Coordinate coordinates,
			Coordinate dimensions);
}
