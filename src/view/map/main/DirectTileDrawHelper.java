package view.map.main; // NOPMD

import java.awt.Color;
import java.awt.Graphics;
import model.map.IMapNG;
import model.map.Point;
import model.map.PointFactory;
import model.map.River;
import model.map.TileType;
import view.util.Coordinate;

import static view.util.DrawingNumericConstants.EventStart;
import static view.util.DrawingNumericConstants.FortSize;
import static view.util.DrawingNumericConstants.FortStart;
import static view.util.DrawingNumericConstants.LakeStart;
import static view.util.DrawingNumericConstants.RiverLongDimension;
import static view.util.DrawingNumericConstants.RiverShortDimension;
import static view.util.DrawingNumericConstants.RiverShortStart;

/**
 * A TileDrawHelper that doesn't create Shapes, but draws directly. If this is faster,
 * we'll be able to drop the requirement that the graphics context's origin be
 * translated.
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
public final class DirectTileDrawHelper extends AbstractTileDrawHelper {
	/**
	 * Draw a tile at the specified coordinates.
	 *
	 * @param pen      the graphics context.
	 * @param map      the map to draw the tile from
	 * @param location the location to draw
	 * @param position the coordinates of the tile's upper-left corner
	 * @param dims     the width (X) and height (Y) of the tile
	 */
	@Override
	public void drawTile(final Graphics pen, final IMapNG map,
	                     final Point location, final Coordinate position,
	                     final Coordinate dims) {
		final Graphics context = pen.create();
		try {
			context.setColor(getTileColor(1, map.getBaseTerrain(location)));
			context.fillRect(position.x, position.y, dims.x, dims.y);
			context.setColor(Color.black);
			context.drawRect(position.x, position.y, dims.x, dims.y);
			if (TileType.NotVisible == map.getBaseTerrain(location)) {
				return;
			}
			context.setColor(Color.blue);
			for (final River river : map.getRivers(location)) {
				drawRiver(context, river, position.x, position.y, dims.x,
						dims.y);
			}
			if (hasAnyForts(map, location)) {
				context.setColor(FORT_COLOR);
				context.fillRect(((int) Math.round(dims.x * FortStart.constant)
						                  - 1) + position.x,
						((int) Math.round(dims.y * FortStart.constant) - 1)
								+ position.y,
						(int) Math.round(dims.x * FortSize.constant),
						(int) Math.round(dims.y * FortSize.constant));
			}
			if (hasAnyUnits(map, location)) {
				context.setColor(UNIT_COLOR);
				context.fillOval((int) Math.round(dims.x * LakeStart.constant)
						                 + position.x, (int) Math.round(dims.y * LakeStart.constant)
								                               + position.y,
						(int) Math.round(dims.x * LakeStart.constant),
						(int) Math.round(dims.y * LakeStart.constant));
			} else if (hasEvent(map, location)) {
				context.setColor(EVENT_COLOR);
				context.fillPolygon(
						new int[]{
								(int) Math.round(dims.x * EventStart.constant)
										+ position.x,
								(int) Math.round(dims.x * RiverLongDimension.constant)
										+ position.x, dims.x + position.x},
						new int[]{
								position.y,
								(int) Math.round(dims.y * RiverLongDimension.constant)
										+ position.y,
								(int) Math.round(dims.y * RiverLongDimension.constant)
										+ position.y}, MISC_EVENT_SIDES);
			}
		} finally {
			context.dispose();
		}
	}

	/**
	 * Draw a tile. At present, the graphics context needs to be translated so that its
	 * origin is the tile's upper-left-hand corner.
	 *
	 * @param pen      the graphics context
	 * @param map      the map to draw the tile from
	 * @param location the location to draw
	 * @param width    the width of the drawing area
	 * @param height   the height of the drawing area
	 */
	@Override
	public void drawTileTranslated(final Graphics pen, final IMapNG map,
	                               final Point location, final int width,
	                               final int height) {
		drawTile(pen, map, location, PointFactory.coordinate(0, 0),
				PointFactory.coordinate(width, height));
	}

	/**
	 * Draw a river.
	 *
	 * @param pen    the graphics context---again, origin at tile's upper-left corner
	 * @param river  the river to draw
	 * @param xCoord the left boundary of the tile
	 * @param yCoord the upper boundary of the tile
	 * @param width  the width of the tile's drawing-space
	 * @param height the height of the tile's drawing-space
	 */
	private static void drawRiver(final Graphics pen, final River river,
	                              final int xCoord, final int yCoord, final int width,
	                              final int height) {
		switch (river) {
		case East:
			pen.fillRect((int) Math.round(width * RiverLongDimension.constant) + xCoord,
					(int) Math.round(height * RiverShortStart.constant) + yCoord,
					(int) Math.round(width * RiverLongDimension.constant),
					(int) Math.round(height * RiverShortDimension.constant));
			break;
		case Lake:
			pen.fillOval((int) Math.round(width * LakeStart.constant) + xCoord,
					(int) Math.round(height * LakeStart.constant) + yCoord,
					(int) Math.round(width * RiverLongDimension.constant),
					(int) Math.round(height * RiverLongDimension.constant));
			break;
		case North:
			pen.fillRect((int) Math.round(width * RiverShortStart.constant) + xCoord,
					yCoord, (int) Math.round(width * RiverShortDimension.constant),
					(int) Math.round(height * RiverLongDimension.constant));
			break;
		case South:
			pen.fillRect((int) Math.round(width * RiverShortStart.constant) + xCoord,
					(int) Math.round(height * RiverLongDimension.constant) + yCoord,
					(int) Math.round(width * RiverShortDimension.constant),
					(int) Math.round(height * RiverLongDimension.constant));
			break;
		case West:
			pen.fillRect(xCoord, (int) Math.round(height * RiverShortStart.constant)
					                     + yCoord, (int) Math.round(width * RiverLongDimension.constant),
					(int) Math.round(height * RiverShortDimension.constant));
			break;
		default:
			// Shouldn't get here, but let's ignore it anyway
			break;
		}
	}

	/**
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "DirectTileDrawHelper";
	}
}
