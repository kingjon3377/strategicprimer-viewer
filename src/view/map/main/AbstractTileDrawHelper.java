package view.map.main;

import java.awt.Color;
import java.awt.Graphics;

import model.viewer.Fortress;
import model.viewer.Tile;
import model.viewer.TileFixture;
import model.viewer.TileType;
import model.viewer.Unit;
import model.viewer.events.AbstractEvent;
/**
 * An abstract superclass containing helper methods for TileDrawHelpers.
 * @author Jonathan Lovelace
 *
 */
public abstract class AbstractTileDrawHelper implements TileDrawHelper {

	/**
	 * The color of the icon used to show that a tile has an event or associated text.
	 */
	protected static final Color EVENT_COLOR = Color.pink;
	/**
	 * Eight as a double. Used to make rivers take up 1/8 of the tile in their short dimension.
	 */
	protected static final double EIGHT = 8.0;
	/**
	 * 7/16: where the short side of a river starts, along the edge of the tile.
	 */
	protected static final double SEVEN_SIXTEENTHS = 7.0 / 16.0;

	/**
	 * @param type a tile type
	 * @return the color associated with that tile-type.
	 */
	public static Color getTileColor(final TileType type) {
		return COLORS.get(type);
	}

	/**
	 * @param tile a tile
	 * @return whether the tile has any forts.
	 */
	protected static boolean hasAnyForts(final Tile tile) {
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
	protected static boolean hasAnyUnits(final Tile tile) {
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
	protected static boolean hasEvent(final Tile tile) {
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
	protected static final Color FORT_COLOR = new Color(160, 82, 45);
	/**
	 * Purple, the color of a unit.
	 */
	protected static final Color UNIT_COLOR = new Color(148, 0, 211);
	/**
	 * Mapping from tile types to colors.
	 */
	protected static final TileColors COLORS = new TileColors();
	/**
	 * Two as a double.
	 */
	protected static final double TWO = 2.0;
	/**
	 * Four as a double.
	 */
	protected static final double FOUR = 4.0;
	/**
	 * Two-thirds as a double.
	 */
	protected static final double TWO_THIRDS = 2.0 / 3.0;
	/**
	 * Three-quarters as a double.
	 */
	protected static final double THREE_QUARTERS = 3.0 / 4.0;
	/**
	 * Three as a double.
	 */
	protected static final double THREE = 3.0;
	/**
	 * The number of sides on the symbol for a miscellaneous event.
	 */
	protected static final int MISC_EVENT_SIDES = 3;
	/**
	 * Draw a tile. At present, the graphics context needs to be translated so that its origin is the tile's upper-left-hand corner.
	 * @param pen the graphics context
	 * @param tile the tile to draw
	 * @param width the width of the drawing area
	 * @param height the height of the drawing area
	 */
	@Override
	public abstract void drawTile(final Graphics pen, final Tile tile, final int width, final int height);
}
