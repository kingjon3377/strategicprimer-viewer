package view.map.main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.EnumMap;
import java.util.Map;

import model.viewer.River;
import model.viewer.Tile;
import model.viewer.TileType;

/**
 * A class to do the drawing of a tile, whether on a GUITile or on a single-component map.
 * @author Jonathan Lovelace
 *
 */
public class CachingTileDrawHelper extends AbstractTileDrawHelper {
	/**
	 * Constructor.
	 */
	public CachingTileDrawHelper() {
		super();
		checkCache(1, 1);
	}
	/**
	 * A cached copy of our background.
	 */
	private Rectangle backgroundShape = new Rectangle(0, 0, 1, 1);
	/**
	 * The shapes representing the rivers on the tile.
	 */
	private final Map<River, Shape> rivers = new EnumMap<River, Shape>(// NOPMD
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
	 * Check, and possibly regenerate, the cache.
	 * 
	 * @param width
	 *            the current width
	 * @param height
	 *            the current height
	 */
	private void checkCache(final int width, final int height) {
		if (!floatEquals(backgroundShape.getWidth(), width) || !floatEquals(backgroundShape.getHeight(), height)) {
			backgroundShape = new Rectangle(0, 0, width, height);
			rivers.clear();
			rivers.put(River.East, new Rectangle2D.Double(width / 2.0, height // NOPMD
					* SEVEN_SIXTEENTHS, width / 2.0, height / EIGHT));
			rivers.put(River.Lake, new Ellipse2D.Double(width / 4.0, // NOPMD
					height / 4.0, width / 2.0, height / 2.0));
			rivers.put(River.North, new Rectangle2D.Double(width
					* SEVEN_SIXTEENTHS, // NOPMD
					0, width / EIGHT, height / 2.0));
			rivers.put(River.South, new Rectangle2D.Double(width
					* SEVEN_SIXTEENTHS, // NOPMD
					height / 2.0, width / EIGHT, height / 2.0));
			rivers.put(River.West, new Rectangle2D.Double(0, height // NOPMD
					* SEVEN_SIXTEENTHS, width / 2.0, height / EIGHT));
			fort = new Rectangle2D.Double(width * 2.0 / 3.0 - 1.0,
					height * 2.0 / 3.0 - 1.0, width / 3.0, height / 3.0);
			unit = new Ellipse2D.Double(width / 4.0, height / 4.0, width / 4.0,
					height / 4.0);
			event = new Polygon(new int[] { (int) (width * 3.0 / 4.0),
					(int) (width / 2.0), width }, new int[] { 0,
					(int) (height / 2.0), (int) (height / 2.0) }, 3);
		}
	}
	/**
	 * Draw a tile. The graphics context needs to be translated so that its origin is the tile's upper-left-hand corner.
	 * @param pen the graphics context
	 * @param tile the tile to draw
	 * @param width the width of the drawing area
	 * @param height the height of the drawing area
	 */
	@Override
	public void drawTile(final Graphics2D pen, final Tile tile, final int width, final int height) {
		checkCache(width, height);
		pen.setColor(getTileColor(tile.getType()));
		pen.fill(backgroundShape);
		pen.setColor(Color.BLACK);
		pen.draw(backgroundShape);
		if (!TileType.NotVisible.equals(tile.getType())) {
			pen.setColor(Color.BLUE);
			for (final River river : tile.getRivers()) {
				pen.fill(rivers.get(river));
			}
			if (hasAnyForts(tile)) {
				pen.setColor(FORT_COLOR);
				pen.fill(fort);
			}
			if (hasAnyUnits(tile)) {
				pen.setColor(UNIT_COLOR);
				pen.fill(unit);
			}
			if (hasEvent(tile)) {
				pen.setColor(EVENT_COLOR);
				pen.fill(event);
			}
		}
	}
	/**
	 * Approximately zero. @see{floatEquals}.
	 */
	private static final double APPROX_ZERO = 0.000001;
	/**
	 * Compare two floating-point values.
	 * @param one the first value
	 * @param two the second value
	 * @return whether the two are approximately equal
	 */
	private static boolean floatEquals(final double one, final double two) {
		return Math.abs(one - two) < APPROX_ZERO;
	}
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
	public void drawTile(final Graphics2D pen, final Tile tile, final int xCoord,
			final int yCoord, final int width, final int height) {
		drawTile((Graphics2D) pen.create(xCoord, yCoord, width, height), tile, width, height);
	}
}
