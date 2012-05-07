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
import java.awt.geom.Rectangle2D;
import java.util.EnumMap;
import java.util.Map;

import model.map.River;
import model.map.Tile;
import model.map.TileType;

/**
 * A class to do the drawing of a tile, whether on a GUITile or on a
 * single-component map.
 * 
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
			event = new Polygon(new int[] { (int) (width * THREE_QUARTERS),
					(int) (width / TWO), width }, new int[] { 0,
					(int) (height / TWO), (int) (height / TWO) },
					MISC_EVENT_SIDES);
		}
	}

	/**
	 * Draw a tile. The graphics context needs to be translated so that its
	 * origin is the tile's upper-left-hand corner. Note that this makes few
	 * assumptions about the graphics context's initial state, but unlike Chits
	 * it makes no attempt to save and restore that state either.
	 * 
	 * @param pen
	 *            the graphics context
	 * @param version
	 *            the map version
	 * @param tile
	 *            the tile to draw
	 * @param width
	 *            the width of the drawing area
	 * @param height
	 *            the height of the drawing area
	 */
	@Override
	public void drawTile(final Graphics pen, final int version, final Tile tile, final int width,
			final int height) {
		checkCache(width, height);
		if (!(pen instanceof Graphics2D)) {
			throw new IllegalArgumentException(
					"CachingTileDrawHelper requires Graphics2D, not an old Graphics");
		}
		final Graphics2D pen2d = (Graphics2D) pen;
		pen2d.setColor(getTileColor(version, tile.getTerrain()));
		pen2d.fill(backgroundShape);
		pen2d.setColor(Color.BLACK);
		pen2d.draw(backgroundShape);
		if (!TileType.NotVisible.equals(tile.getTerrain())) {
			pen2d.setColor(Color.BLUE);
			if (tile.hasRiver()) {
				for (final River river : tile.getRivers()) {
					pen2d.fill(rivers.get(river));
				}
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
	 * 
	 * @param one
	 *            the first value
	 * @param two
	 *            the second value
	 * 
	 * @return whether the two are approximately equal
	 */
	private static boolean equalFloats(final double one, final double two) {
		return Math.abs(one - two) < APPROX_ZERO;
	}

	/**
	 * Draw a tile at the specified coordinates.
	 * 
	 * @param pen
	 *            the graphics context.
	 * @param version the map version
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
	// ESCA-JAVA0138:
	@Override
	public void drawTile(final Graphics pen, final int version, final Tile tile, final int xCoord,
			final int yCoord, final int width, final int height) {
		final Graphics context = pen.create(xCoord, yCoord, width, height);
		try {
			drawTile(context, version, tile, width, height);
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
