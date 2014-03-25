package view.map.main;

import java.awt.Color;
import java.awt.Graphics;

import model.map.IEvent;
import model.map.ITile;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.towns.Fortress;

import org.eclipse.jdt.annotation.Nullable;

/**
 * An abstract superclass containing helper methods for TileDrawHelpers.
 *
 * @author Jonathan Lovelace
 *
 */
public abstract class AbstractTileDrawHelper implements TileDrawHelper {
	/**
	 * Because the JDK is not annotated with Eclipse's nullness annotations,
	 * Eclipse thinks that static constants in the JDK might be null, leading to
	 * several warnings in this class.
	 * @param color a color
	 * @return it without Eclipse having the idea anymore that it might be null
	 */
	protected static Color assertNonNull(@Nullable final Color color) {
		assert color != null;
		return color;
	}
	/**
	 * The color of the icon used to show that a tile has an event or associated
	 * text.
	 */
	protected static final Color EVENT_COLOR = assertNonNull(Color.pink);
	/**
	 * The color of the icon used to show that a tile has a forest.
	 */
	protected static final Color FOREST_COLOR = new Color(0, 117, 0);
	/**
	 * The color of the icon used to show that a tile has a mountain.
	 */
	protected static final Color MTN_COLOR = new Color(249, 137, 28);

	/**
	 * @param ver the map version
	 * @param type a tile type
	 *
	 * @return the color associated with that tile-type.
	 */
	protected static Color getTileColor(final int ver, final TileType type) {
		return COLORS.get(ver, type);
	}

	/**
	 * @param tile a tile
	 *
	 * @return whether the tile has any forts.
	 */
	protected static boolean hasAnyForts(final ITile tile) {
		for (final TileFixture fix : tile) {
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
	protected static boolean hasAnyUnits(final ITile tile) {
		for (final TileFixture fix : tile) {
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
	protected static boolean hasEvent(final ITile tile) {
		for (final TileFixture fix : tile) {
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
	public abstract void drawTileTranslated(final Graphics pen,
			final ITile tile, final int width, final int height);

	/**
	 * @return the UI helper.
	 */
	protected static TileUIHelper getHelper() {
		return COLORS;
	}
}
