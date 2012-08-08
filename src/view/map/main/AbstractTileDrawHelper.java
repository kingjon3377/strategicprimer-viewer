package view.map.main;

import java.awt.Color;
import java.awt.Graphics;

import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
import model.map.events.IEvent;
import model.map.fixtures.Fortress;
import model.map.fixtures.Unit;

/**
 * An abstract superclass containing helper methods for TileDrawHelpers.
 *
 * @author Jonathan Lovelace
 *
 */
public abstract class AbstractTileDrawHelper implements TileDrawHelper {

	/**
	 * The color of the icon used to show that a tile has an event or associated
	 * text.
	 */
	protected static final Color EVENT_COLOR = Color.pink;
	/**
	 * The color of the icon used to show that a tile has a forest.
	 */
	protected static final Color FOREST_COLOR = new Color(0, 117, 0);
	/**
	 * The color of the icon used to show that a tile has a mountain.
	 */
	protected static final Color MTN_COLOR = new Color(249, 137, 28);

	/**
	 * @param version the map version
	 * @param type a tile type
	 *
	 * @return the color associated with that tile-type.
	 */
	public static Color getTileColor(final int version, final TileType type) {
		return COLORS.get(version, type);
	}

	/**
	 * @param tile a tile
	 *
	 * @return whether the tile has any forts.
	 */
	protected static boolean hasAnyForts(final Tile tile) {
		for (final TileFixture fix : tile.getContents()) {
			if (fix instanceof Fortress) {
				return true; // NOPMD
			}
		}
		return false;
	}

	/**
	 * @param tile a tile
	 *
	 * @return whether the tile has any units.
	 */
	protected static boolean hasAnyUnits(final Tile tile) {
		for (final TileFixture fix : tile.getContents()) {
			if (fix instanceof Unit) {
				return true; // NOPMD
			}
		}
		return false;
	}

	/**
	 * @param tile a tile
	 *
	 * @return whether the tile has any events
	 */
	protected static boolean hasEvent(final Tile tile) {
		for (final TileFixture fix : tile.getContents()) {
			if (fix instanceof IEvent) {
				return true; // NOPMD
			}
		}
		return false; // NOPMD
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
	protected static final TileUIHelper COLORS = new TileUIHelper();
	/**
	 * The number of sides on the symbol for a miscellaneous event.
	 */
	protected static final int MISC_EVENT_SIDES = 3;

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
	public abstract void drawTile(final Graphics pen, final Tile tile,
			final int width, final int height);

	/**
	 * @return the UI helper.
	 */
	protected static TileUIHelper getHelper() {
		return COLORS;
	}
}
