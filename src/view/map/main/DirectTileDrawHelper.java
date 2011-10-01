package view.map.main;

import java.awt.Color;
import java.awt.Graphics2D;

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
	 * Draw a tile. At present, the graphics context needs to be translated so that its origin is the tile's upper-left-hand corner.
	 * @param pen the graphics context
	 * @param tile the tile to draw
	 * @param width the width of the drawing area
	 * @param height the height of the drawing area
	 */
	@Override
	public void drawTile(final Graphics2D pen, final Tile tile, final int width, final int height) {
		pen.setColor(getTileColor(tile.getType()));
		pen.fillRect(0, 0, width, height);
		pen.setColor(Color.black);
		pen.drawRect(0, 0, width, height);
		if (!TileType.NotVisible.equals(tile.getType())) {
			pen.setColor(Color.blue);
			for (final River river : tile.getRivers()) {
				drawRiver(pen, river, width, height);
			}
			if (hasAnyForts(tile)) {
				pen.setColor(FORT_COLOR);
				pen.fillRect((int) (width * 2.0 / 3.0) - 1,
						(int) (height * 2.0 / 3.0) - 1, (int) (width / 3.0),
						(int) (height / 3.0));
			}
			if (hasAnyUnits(tile)) {
				pen.setColor(UNIT_COLOR);
				pen.fillOval(((int) (width / 4.0)), ((int) (height / 4.0)),
						((int) (width / 4.0)), ((int) (height / 4.0)));
			} else if (hasEvent(tile)) {
				pen.setColor(EVENT_COLOR);
				pen.fillPolygon(new int[] { (int) (width * 3.0 / 4.0),
					(int) (width / 2.0), width }, new int[] { 0,
					(int) (height / 2.0), (int) (height / 2.0) }, 3);
			}
		}
	}
	/**
	 * Draw a river.
	 * @param pen the graphics context---again, origin at tile's upper-left corner 
	 * @param river the river to draw
	 * @param width the width of the tile's drawing-space
	 * @param height the height of the tile's drawing-space
	 */
	private static void drawRiver(final Graphics2D pen, final River river, final int width, final int height) {
		switch (river) {
		case East:
			pen.fillRect((int) (width / 2.0),
					(int) (height * SEVEN_SIXTEENTHS), (int) (width / 2.0),
					(int) (height / EIGHT));
			break;
		case Lake:
			pen.fillOval((int) (width / 4.0), (int) (height / 4.0),
					(int) (width / 2.0), (int) (height / 2.0)); 
			break;
		case North:
			pen.fillRect((int) (width * SEVEN_SIXTEENTHS), 0,
					(int) (width / EIGHT), (int) (height / 2.0));
			break;
		case South:
			pen.fillRect((int) (width * SEVEN_SIXTEENTHS),
					(int) (height / 2.0), (int) (width / EIGHT),
					(int) (height / 2.0));
			break;
		case West:
			pen.fillRect(0, (int) (height * SEVEN_SIXTEENTHS),
					(int) (width / 2.0), (int) (height / EIGHT));
			break;
		default:
			// Shouldn't get here, but let's ignore it anyway
			break;
		}
	}
}
