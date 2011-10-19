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

import model.map.River;
import model.map.Tile;
import model.map.TileType;

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
	 * 
	 * @param pen
	 *            the graphics context.
	 * @param tile
	 *            the tile to draw
	 * @param xCoord
	 *            the tile's left boundary
	 * @param yCoord
	 *            the tile's right boundary
	 * @param width
	 *            the tile's width
	 * @param height
	 *            the tile's height
	 */
	@Override
	public void drawTile(final Graphics pen, final Tile tile, final int xCoord,
			final int yCoord, final int width, final int height) {
		final Color save = pen.getColor();
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
				pen.fillRect((int) (width * TWO_THIRDS) - 1 + xCoord,
						(int) (height * TWO_THIRDS) - 1 + yCoord,
						(int) (width / THREE), (int) (height / THREE));
			}
			if (hasAnyUnits(tile)) {
				pen.setColor(UNIT_COLOR);
				pen.fillOval(((int) (width / FOUR)) + xCoord,
						((int) (height / FOUR)) + yCoord,
						((int) (width / FOUR)), ((int) (height / FOUR)));
			} else if (hasEvent(tile)) {
				pen.setColor(EVENT_COLOR);
				pen.fillPolygon(new int[] {
						(int) (width * THREE_QUARTERS) + xCoord,
						(int) (width / TWO) + xCoord, width + xCoord },
						new int[] { yCoord, (int) (height / TWO) + yCoord,
								(int) (height / TWO) + yCoord },
						MISC_EVENT_SIDES);
			}
		}
		pen.setColor(save);
	}

	/**
	 * Draw a tile. At present, the graphics context needs to be translated so
	 * that its origin is the tile's upper-left-hand corner.
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
	@Override
	public void drawTile(final Graphics pen, final Tile tile, final int width,
			final int height) {
		drawTile(pen, tile, 0, 0, width, height);
	}

	/**
	 * Draw a river.
	 * 
	 * @param pen
	 *            the graphics context---again, origin at tile's upper-left
	 *            corner
	 * @param river
	 *            the river to draw
	 * @param xCoord
	 *            the left boundary of the tile
	 * @param yCoord
	 *            the upper boundary of the tile
	 * @param width
	 *            the width of the tile's drawing-space
	 * @param height
	 *            the height of the tile's drawing-space
	 */
	private static void drawRiver(final Graphics pen, final River river,
			final int xCoord, final int yCoord, final int width,
			final int height) {
		switch (river) {
		case East:
			pen.fillRect((int) (width / TWO) + xCoord,
					(int) (height * SEVEN_SIXTEENTHS) + yCoord,
					(int) (width / TWO), (int) (height / EIGHT));
			break;
		case Lake:
			pen.fillOval((int) (width / FOUR) + xCoord, (int) (height / FOUR)
					+ yCoord, (int) (width / TWO), (int) (height / TWO));
			break;
		case North:
			pen.fillRect((int) (width * SEVEN_SIXTEENTHS) + xCoord, yCoord,
					(int) (width / EIGHT), (int) (height / TWO));
			break;
		case South:
			pen.fillRect((int) (width * SEVEN_SIXTEENTHS) + xCoord,
					(int) (height / TWO) + yCoord, (int) (width / EIGHT),
					(int) (height / TWO));
			break;
		case West:
			pen.fillRect(xCoord, (int) (height * SEVEN_SIXTEENTHS) + yCoord,
					(int) (width / TWO), (int) (height / EIGHT));
			break;
		default:
			// Shouldn't get here, but let's ignore it anyway
			break;
		}
	}

	/**
	 * 
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "DirectTileDrawHelper";
	}
}
