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

import model.map.ITile;
import model.map.PointFactory;
import model.map.River;
import model.map.TileType;
import view.util.Coordinate;

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
	 * @param pen the graphics context.
	 * @param tile the tile to draw
	 * @param position the coordinates of the tile's upper-left corner
	 * @param dims the width (X) and height (Y) of the tile
	 */
	// ESCA-JAVA0138:
	@Override
	public void drawTile(final Graphics pen, final ITile tile,
			final Coordinate position, final Coordinate dims) {
		final Graphics context = pen.create();
		try {
			context.setColor(getTileColor(1, tile.getTerrain()));
			context.fillRect(position.x, position.y, dims.x, dims.y);
			context.setColor(Color.black);
			context.drawRect(position.x, position.y, dims.x, dims.y);
			if (TileType.NotVisible.equals(tile.getTerrain())) {
				return;
			}
			context.setColor(Color.blue);
			if (tile.hasRiver()) {
				for (final River river : tile.getRivers()) {
					if (river != null) {
						drawRiver(context, river, position.x, position.y,
								dims.x, dims.y);
					}
				}
			}
			if (hasAnyForts(tile)) {
				context.setColor(FORT_COLOR);
				context.fillRect((int) Math.round(dims.x * TWO_THIRDS)
						- 1 + position.x,
						(int) Math.round(dims.y * TWO_THIRDS) - 1
								+ position.y,
						(int) Math.round(dims.x / THREE),
						(int) Math.round(dims.y / THREE));
			}
			if (hasAnyUnits(tile)) {
				context.setColor(UNIT_COLOR);
				context.fillOval(((int) Math.round(dims.x / FOUR))
						+ position.x, ((int) Math.round(dims.y / FOUR))
						+ position.y, (int) Math.round(dims.x / FOUR),
						(int) Math.round(dims.y / FOUR));
			} else if (hasEvent(tile)) {
				context.setColor(EVENT_COLOR);
				context.fillPolygon(
						new int[] {
								(int) Math.round(dims.x * THREE_QUARTERS)
										+ position.x,
								(int) Math.round(dims.x / TWO)
										+ position.x, dims.x + position.x },
						new int[] {
								position.y,
								(int) Math.round(dims.y / TWO)
										+ position.y,
								(int) Math.round(dims.y / TWO)
										+ position.y }, MISC_EVENT_SIDES);
			}
		} finally {
			context.dispose();
		}
	}

	/**
	 * Draw a tile. At present, the graphics context needs to be translated so
	 * that its origin is the tile's upper-left-hand corner.
	 *
	 * @param pen the graphics context
	 * @param tile the tile to draw
	 * @param width the width of the drawing area
	 * @param height the height of the drawing area
	 */
	@Override
	public void drawTileTranslated(final Graphics pen, final ITile tile, final int width,
			final int height) {
		drawTile(pen, tile, PointFactory.coordinate(0, 0),
				PointFactory.coordinate(width, height));
	}

	/**
	 * Draw a river.
	 *
	 * @param pen the graphics context---again, origin at tile's upper-left
	 *        corner
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
			pen.fillRect((int) Math.round(width / TWO) + xCoord,
					(int) Math.round(height * SEVEN_SIXTEENTHS) + yCoord,
					(int) Math.round(width / TWO),
					(int) Math.round(height / EIGHT));
			break;
		case Lake:
			pen.fillOval((int) Math.round(width / FOUR) + xCoord,
					(int) Math.round(height / FOUR) + yCoord,
					(int) Math.round(width / TWO),
					(int) Math.round(height / TWO));
			break;
		case North:
			pen.fillRect((int) Math.round(width * SEVEN_SIXTEENTHS) + xCoord,
					yCoord, (int) Math.round(width / EIGHT),
					(int) Math.round(height / TWO));
			break;
		case South:
			pen.fillRect((int) Math.round(width * SEVEN_SIXTEENTHS) + xCoord,
					(int) Math.round(height / TWO) + yCoord,
					(int) Math.round(width / EIGHT),
					(int) Math.round(height / TWO));
			break;
		case West:
			pen.fillRect(xCoord, (int) Math.round(height * SEVEN_SIXTEENTHS)
					+ yCoord, (int) Math.round(width / TWO),
					(int) Math.round(height / EIGHT));
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
