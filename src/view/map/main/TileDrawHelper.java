package view.map.main;

import java.awt.Graphics;
import model.map.IMapNG;
import model.map.Point;
import view.util.Coordinate;

/**
 * A helper to do the actual drawing of a tile. Now an interface so we can easily compare
 * implementations.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface TileDrawHelper {

	/**
	 * Draw a tile. The graphics context needs to be translated so that its origin is the
	 * tile's upper-left-hand corner.
	 *
	 * @param pen      the graphics context
	 * @param map      the map to draw the tile from
	 * @param location the location to draw
	 * @param width    the width of the drawing area
	 * @param height   the height of the drawing area
	 */
	void drawTileTranslated(Graphics pen, IMapNG map, Point location,
	                        int width, int height);

	/**
	 * Draw a tile, at the given coordinates.
	 *
	 * @param pen         the graphics context
	 * @param map         the map to draw the tile from
	 * @param location    the location to draw
	 * @param coordinates the coordinates of the tile's upper-left corner
	 * @param dimensions  the width (X) and height (Y) of the tile
	 */
	void drawTile(Graphics pen, IMapNG map, Point location, Coordinate coordinates,
	              Coordinate dimensions);
}
