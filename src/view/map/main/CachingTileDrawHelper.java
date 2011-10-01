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

import model.viewer.Fortress;
import model.viewer.River;
import model.viewer.Tile;
import model.viewer.TileFixture;
import model.viewer.TileType;
import model.viewer.Unit;
import model.viewer.events.AbstractEvent;

/**
 * A class to do the drawing of a tile, whether on a GUITile or on a single-component map.
 * @author Jonathan Lovelace
 *
 */
public class CachingTileDrawHelper implements TileDrawHelper {
	/**
	 * Constructor.
	 */
	public CachingTileDrawHelper() {
		checkCache(1, 1);
	}
	/**
	 * The color of the icon used to show that a tile has an event or associated text.
	 */
	private static final Color EVENT_COLOR = Color.pink;

	/**
	 * Eight as a double. Used to make rivers take up 1/8 of the tile in their short dimension.
	 */
	private static final double EIGHT = 8.0;
	/**
	 * 7/16: where the short side of a river starts, along the edge of the tile.
	 */
	private static final double SEVEN_SIXTEENTHS = 7.0 / 16.0;
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
	 * @param type a tile type
	 * @return the color associated with that tile-type.
	 */
	private static Color getTileColor(final TileType type) {
		return COLORS.get(type);
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
	 * @param tile a tile
	 * @return whether the tile has any forts.
	 */
	private static boolean hasAnyForts(final Tile tile) {
		for (TileFixture fix : tile.getContents()) {
			if (fix instanceof Fortress) {
				return true; // NOPMD
			}
		}
		return false;
	}
	/**
	 * @param tile a tile
	 * @return whether the tile has any units.
	 */
	private static boolean hasAnyUnits(final Tile tile) {
		for (TileFixture fix : tile.getContents()) {
			if (fix instanceof Unit) {
				return true; // NOPMD
			}
		}
		return false;
	}
	/**
	 * @param tile a tile
	 * @return whether the tile has any events
	 */
	private static boolean hasEvent(final Tile tile) {
		if ("".equals(tile.getTileText())) {
			for (TileFixture fix : tile.getContents()) {
				if (fix instanceof AbstractEvent) {
					return true; // NOPMD
				}
			}
			return false; // NOPMD
		} else {
			return true;
		}
	}
	/**
	 * Brown, the color of a fortress.
	 */
	private static final Color FORT_COLOR = new Color(160, 82, 45);
	/**
	 * Purple, the color of a unit.
	 */
	private static final Color UNIT_COLOR = new Color(148, 0, 211);
	/**
	 * Mapping from tile types to colors.
	 */
	private static final Map<TileType, Color> COLORS = new EnumMap<TileType, Color>(
			TileType.class);
	// ESCA-JAVA0076:
	static {
		COLORS.put(TileType.BorealForest, new Color(72, 218, 164));
		COLORS.put(TileType.Desert, new Color(249, 233, 28));
		COLORS.put(TileType.Jungle, new Color(229, 46, 46));
		COLORS.put(TileType.Mountain, new Color(249, 137, 28));
		COLORS.put(TileType.NotVisible, new Color(255, 255, 255));
		COLORS.put(TileType.Ocean, new Color(0, 0, 255));
		COLORS.put(TileType.Plains, new Color(0, 117, 0));
		COLORS.put(TileType.TemperateForest, new Color(72, 250, 72));
		COLORS.put(TileType.Tundra, new Color(153, 153, 153));
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
}
