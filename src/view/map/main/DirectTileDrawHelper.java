package view.map.main;

import java.awt.Color;
import java.awt.Graphics;

import model.viewer.River;
import model.viewer.Tile;
import model.viewer.TileType;

/**
 * A TileDrawHelper that doesn't create Shapes, but draws directly. If this is
 * faster, we'll be able to drop the requirement that the graphics context's
 * origin be translated.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class DirectTileDrawHelper extends AbstractTileDrawHelper {
	/**
	 * Draw a tile at the specified coordinates.
	 * @param pen the graphics context.
	 * @param tile the tile to draw
	 * @param xCoord the tile's left boundary
	 * @param yCoord the tile's right boundary
	 * @param width the tile's width
	 * @param height the tile's height
	 */
	@Override
	public void drawTile(final Graphics pen, final Tile tile, final int xCoord,
			final int yCoord, final int width, final int height) {
		pen.setColor(getTileColor(tile.getType()));
		pen.fillRect(xCoord, yCoord, width, height);
		pen.setColor(Color.black);
		pen.drawRect(xCoord, yCoord, width, height);
		if (!TileType.NotVisible.equals(tile.getType())) {
			pen.setColor(Color.blue);
			for (final River river : tile.getRivers()) {
				drawRiver(pen, river, xCoord, yCoord, width, height);
			}
			if (hasAnyForts(tile)) {
				pen.setColor(FORT_COLOR);
				pen.fillRect((int) (width * 2.0 / 3.0) - 1 + xCoord,
						(int) (height * 2.0 / 3.0) - 1 + yCoord, (int) (width / 3.0),
						(int) (height / 3.0));
			}
			if (hasAnyUnits(tile)) {
				pen.setColor(UNIT_COLOR);
				pen.fillOval(((int) (width / 4.0)) + xCoord, ((int) (height / 4.0)) + yCoord,
						((int) (width / 4.0)), ((int) (height / 4.0)));
			} else if (hasEvent(tile)) {
				pen.setColor(EVENT_COLOR);
				pen.fillPolygon(new int[] { (int) (width * 3.0 / 4.0) + xCoord,
					(int) (width / 2.0) + xCoord, width + xCoord}, new int[] { yCoord,
					(int) (height / 2.0) + yCoord, (int) (height / 2.0) + yCoord }, 3);
			}
		}
	}
	/**
	 * Draw a tile. At present, the graphics context needs to be translated so that its origin is the tile's upper-left-hand corner.
	 * @param pen the graphics context
	 * @param tile the tile to draw
	 * @param width the width of the drawing area
	 * @param height the height of the drawing area
	 */
	@Override
	public void drawTile(final Graphics pen, final Tile tile, final int width, final int height) {
		drawTile(pen, tile, 0, 0, width, height);
	}
	/**
	 * Draw a river.
	 * @param pen the graphics context---again, origin at tile's upper-left corner 
	 * @param river the river to draw
	 * @param xCoord the left boundary of the tile
	 * @param yCoord the upper boundary of the tile
	 * @param width the width of the tile's drawing-space
	 * @param height the height of the tile's drawing-space
	 */
	private static void drawRiver(final Graphics pen, final River river,
			final int xCoord, final int yCoord, final int width,
			final int height) {
		switch (river) {
		case East:
			pen.fillRect((int) (width / 2.0) + xCoord,
					(int) (height * SEVEN_SIXTEENTHS) + yCoord, (int) (width / 2.0),
					(int) (height / EIGHT));
			break;
		case Lake:
			pen.fillOval((int) (width / 4.0) + xCoord, (int) (height / 4.0) + yCoord,
					(int) (width / 2.0), (int) (height / 2.0)); 
			break;
		case North:
			pen.fillRect((int) (width * SEVEN_SIXTEENTHS) + xCoord, yCoord,
					(int) (width / EIGHT), (int) (height / 2.0));
			break;
		case South:
			pen.fillRect((int) (width * SEVEN_SIXTEENTHS) + xCoord,
					(int) (height / 2.0) + yCoord, (int) (width / EIGHT),
					(int) (height / 2.0));
			break;
		case West:
			pen.fillRect(xCoord, (int) (height * SEVEN_SIXTEENTHS) + yCoord,
					(int) (width / 2.0), (int) (height / EIGHT));
			break;
		default:
			// Shouldn't get here, but let's ignore it anyway
			break;
		}
	}
}
