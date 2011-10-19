package view.map.main;

import java.awt.Color;
import java.awt.Graphics;
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
	 * Two as a double.
	 */
	private static final double TWO = 2.0;
	/**
	 * Four as a double.
	 */
	private static final double FOUR = 4.0;
	/**
	 * Two-thirds as a double.
	 */
	private static final double TWO_THIRDS = 2.0 / 3.0;
	/**
	 * Three-quarters as a double.
	 */
	private static final double THREE_QUARTERS = 3.0 / 4.0;
	/**
	 * Three as a double.
	 */
	private static final double THREE = 3.0;
	/**
	 * The number of sides on the symbol for a miscellaneous event.
	 */
	private static final int MISC_EVENT_SIDES = 3;
	/**
	 * Check, and possibly regenerate, the cache.
	 * 
	 * @param width
	 *            the current width
	 * @param height
	 *            the current height
	 */
	private void checkCache(final int width, final int height) {
		if (!equalFloats(backgroundShape.getWidth(), width) || !equalFloats(backgroundShape.getHeight(), height)) {
			backgroundShape = new Rectangle(0, 0, width, height);
			rivers.clear();
			rivers.put(River.East, new Rectangle2D.Double(width / TWO, height
					* SEVEN_SIXTEENTHS, width / TWO, height / EIGHT));
			rivers.put(River.Lake, new Ellipse2D.Double(width / FOUR,
					height / FOUR, width / TWO, height / TWO));
			rivers.put(River.North, new Rectangle2D.Double(width
					* SEVEN_SIXTEENTHS, 
					0, width / EIGHT, height / TWO));
			rivers.put(River.South, new Rectangle2D.Double(width
					* SEVEN_SIXTEENTHS, // NOPMD
					height / TWO, width / EIGHT, height / TWO));
			rivers.put(River.West, new Rectangle2D.Double(0, height 
					* SEVEN_SIXTEENTHS, width / TWO, height / EIGHT));
			fort = new Rectangle2D.Double(width * TWO_THIRDS - 1.0,
					height * TWO_THIRDS - 1.0, width / THREE, height / THREE);
			unit = new Ellipse2D.Double(width / FOUR, height / FOUR, width
					/ FOUR, height / FOUR);
			event = new Polygon(new int[] { (int) (width * THREE_QUARTERS),
					(int) (width / TWO), width }, new int[] { 0,
					(int) (height / TWO), (int) (height / TWO) },
					MISC_EVENT_SIDES);
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
	public void drawTile(final Graphics pen, final Tile tile, final int width, final int height) {
		checkCache(width, height);
		if (!(pen instanceof Graphics2D)) {
			throw new IllegalArgumentException("CachingTileDrawHelper requires Graphics2D, not an old Graphics");
		}
		final Graphics2D pen2d = (Graphics2D) pen;
		pen2d.setColor(getTileColor(tile.getType()));
		pen2d.fill(backgroundShape);
		pen2d.setColor(Color.BLACK);
		pen2d.draw(backgroundShape);
		if (!TileType.NotVisible.equals(tile.getType())) {
			pen2d.setColor(Color.BLUE);
			for (final River river : tile.getRivers()) {
				pen2d.fill(rivers.get(river));
			}
			if (hasAnyForts(tile)) {
				pen2d.setColor(FORT_COLOR);
				pen2d.fill(fort);
			}
			if (hasAnyUnits(tile)) {
				pen2d.setColor(UNIT_COLOR);
				pen2d.fill(unit);
			}
			if (hasEvent(tile)) {
				pen2d.setColor(EVENT_COLOR);
				pen2d.fill(event);
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
	private static boolean equalFloats(final double one, final double two) {
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
	public void drawTile(final Graphics pen, final Tile tile, final int xCoord,
			final int yCoord, final int width, final int height) {
		drawTile(pen.create(xCoord, yCoord, width, height), tile, width, height);
	}
}
