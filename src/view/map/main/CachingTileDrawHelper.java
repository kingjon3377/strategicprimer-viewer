package view.map.main; // NOPMD

import static view.util.DrawingNumericConstants.EIGHT;
import static view.util.DrawingNumericConstants.FOUR;
import static view.util.DrawingNumericConstants.SEVEN_SIXTEENTHS;
import static view.util.DrawingNumericConstants.THREE;
import static view.util.DrawingNumericConstants.THREE_QUARTERS;
import static view.util.DrawingNumericConstants.TWO;
import static view.util.DrawingNumericConstants.TWO_THIRDS;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.EnumMap;
import java.util.Map;

import model.map.IMapNG;
import model.map.Point;
import model.map.River;
import model.map.TileType;
import view.util.Coordinate;

/**
 * A class to do the drawing of a tile, whether on a GUITile or on a
 * single-component map.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class CachingTileDrawHelper extends AbstractTileDrawHelper {
	/**
	 * Approximately zero. @see{floatEquals}.
	 */
	private static final double APPROX_ZERO = 0.000001;

	/**
	 * A cached copy of our background.
	 */
	private Rectangle backgroundShape = new Rectangle(0, 0, 1, 1);
	/**
	 * The shapes representing the rivers on the tile.
	 */
	private final Map<River, Shape> rivers = new EnumMap<>(// NOPMD
			River.class);
	/**
	 * Shape representing the fortress that might be on the tile.
	 */
	private Shape fort;
	/**
	 * Shape representing the unit that might be on the tile.
	 */
	private Shape unit;

	/**
	 * Shape representing an event, or relevant text, associated with the tile.
	 */
	private Shape event;

	/**
	 * Constructor.
	 */
	public CachingTileDrawHelper() {
		// Just to forestall the null-analysis checker's objection that they may
		// not have been initialized, we initialize the shapes to, essentially,
		// a dummy value.
		event = new Line2D.Double();
		fort = event;
		unit = event;
		checkCache(1, 1);
	}

	/**
	 * Check, and possibly regenerate, the cache.
	 *
	 * @param width the current width
	 * @param height the current height
	 */
	private void checkCache(final int width, final int height) {
		if (!equalFloats(backgroundShape.getWidth(), width)
				|| !equalFloats(backgroundShape.getHeight(), height)) {
			backgroundShape = new Rectangle(0, 0, width, height);
			rivers.clear();
			rivers.put(River.East, new Rectangle2D.Double(width / TWO, height
					* SEVEN_SIXTEENTHS, width / TWO, height / EIGHT));
			rivers.put(River.Lake, new Ellipse2D.Double(width / FOUR, height
					/ FOUR, width / TWO, height / TWO));
			rivers.put(River.North, new Rectangle2D.Double(width
					* SEVEN_SIXTEENTHS, 0, width / EIGHT, height / TWO));
			rivers.put(River.South, new Rectangle2D.Double(width
					* SEVEN_SIXTEENTHS, // NOPMD
					height / TWO, width / EIGHT, height / TWO));
			rivers.put(River.West, new Rectangle2D.Double(0, height
					* SEVEN_SIXTEENTHS, width / TWO, height / EIGHT));
			fort = new Rectangle2D.Double(width * TWO_THIRDS - 1.0, height
					* TWO_THIRDS - 1.0, width / THREE, height / THREE);
			unit = new Ellipse2D.Double(width / FOUR, height / FOUR, width
					/ FOUR, height / FOUR);
			event = new Polygon(new int[] {
					(int) Math.round(width * THREE_QUARTERS),
					(int) Math.round(width / TWO), width }, new int[] { 0,
					(int) Math.round(height / TWO),
					(int) Math.round(height / TWO) }, MISC_EVENT_SIDES);
		}
	}

	/**
	 * Draw a tile. The graphics context needs to be translated so that its
	 * origin is the tile's upper-left-hand corner. Note that this makes few
	 * assumptions about the graphics context's initial state, but unlike Chits
	 * it makes no attempt to save and restore that state either.
	 *
	 * @param pen the graphics context
	 * @param map the map to draw the tile from
	 * @param location the location to draw
	 * @param width the width of the drawing area
	 * @param height the height of the drawing area
	 */
	@Override
	public void drawTileTranslated(final Graphics pen, final IMapNG map,
			final Point location, final int width, final int height) {
		checkCache(width, height);
		if (!(pen instanceof Graphics2D)) {
			throw new IllegalArgumentException(
					"CachingTileDrawHelper requires Graphics2D");
		}
		final Graphics2D pen2d = (Graphics2D) pen;
		pen2d.setColor(getTileColor(1, map.getBaseTerrain(location)));
		pen2d.fill(backgroundShape);
		pen2d.setColor(Color.BLACK);
		pen2d.draw(backgroundShape);
		if (!TileType.NotVisible.equals(map.getBaseTerrain(location))) {
			pen2d.setColor(Color.BLUE);
			for (final River river : map.getRivers(location)) {
				pen2d.fill(rivers.get(river));
			}
			if (hasAnyForts(map, location)) {
				pen2d.setColor(FORT_COLOR);
				pen2d.fill(fort);
			}
			if (hasAnyUnits(map, location)) {
				pen2d.setColor(UNIT_COLOR);
				pen2d.fill(unit);
			}
			if (hasEvent(map, location)) {
				pen2d.setColor(EVENT_COLOR);
				pen2d.fill(event);
			}
		}
	}

	/**
	 * Compare two floating-point values.
	 *
	 * @param one the first value
	 * @param two the second value
	 *
	 * @return whether the two are approximately equal
	 */
	private static boolean equalFloats(final double one, final double two) {
		return Math.abs(one - two) < APPROX_ZERO;
	}

	/**
	 * Draw a tile at the specified coordinates.
	 *
	 * @param pen the graphics context.
	 * @param map the map to draw the tile from
	 * @param location the location to draw
	 * @param coordinates the coordinates of the tile's upper-left corner
	 * @param dimensions the width (X) and height (Y) of the tile
	 */
	@Override
	public void drawTile(final Graphics pen, final IMapNG map,
			final Point location, final Coordinate coordinates,
			final Coordinate dimensions) {
		final Graphics context = pen.create(coordinates.x, coordinates.y,
				dimensions.x, dimensions.y);
		if (context == null) {
			throw new IllegalStateException(
					"pen.create() created null Graphics");
		}
		try {
			drawTileTranslated(context, map, location, dimensions.x, dimensions.y);
		} finally {
			context.dispose();
		}
	}

	/**
	 *
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "CachingTileDrawHelper";
	}
}
