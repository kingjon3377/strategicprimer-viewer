package model.map.fixtures.mobile;

import model.map.ITile;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.Ground;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.towns.Fortress;

import org.eclipse.jdt.annotation.Nullable;

import util.EqualsAny;

/**
 * A class encapsulating knowledge about movement costs associated with various
 * tile types. FIXME: This ought to be per-unit-type, rather than one
 * centralized set of figures.
 *
 * TODO: tests
 *
 * @author Jonathan Lovelace
 *
 */
public final class SimpleMovement {
	/**
	 * Do not instantiate.
	 */
	private SimpleMovement() {
		// Do not instantiate.
	}

	/**
	 * An exception thrown to signal traversal is impossible.
	 *
	 * FIXME: Ocean isn't impassable to everything, of course.
	 * @author Jonathan Lovelace
	 */
	public static final class TraversalImpossibleException extends Exception {
		/**
		 * Constructor.
		 */
		public TraversalImpossibleException() {
			super("Traversal is impossible.");
		}
	}

	/**
	 * @param tile a tile
	 * @return whether it's passable by land movement.
	 */
	public static boolean isLandMovementPossible(final ITile tile) {
		return !TileType.Ocean.equals(tile.getTerrain());
	}

	/**
	 * @param tile a tile
	 * @return the movement cost to traverse it.
	 */
	public static int getMovementCost(final ITile tile) {
		if (TileType.Ocean.equals(tile.getTerrain())) {
			return Integer.MAX_VALUE; // NOPMD
		} else if (isForest(tile) || isHill(tile)
				|| TileType.Desert.equals(tile.getTerrain())) {
			return 3; // NOPMD
		} else if (TileType.Jungle.equals(tile.getTerrain())) {
			return 6; // NOPMD
		} else if (EqualsAny.equalsAny(tile.getTerrain(), TileType.Steppe,
				TileType.Plains, TileType.Tundra)) {
			return 2;
		} else {
			throw new IllegalArgumentException("Unknown tile type");
		}
	}

	/**
	 * @param tile a tile
	 * @return whether it is or contains a forest
	 */
	@SuppressWarnings("deprecation")
	private static boolean isForest(final ITile tile) {
		if (EqualsAny.equalsAny(tile.getTerrain(), TileType.BorealForest,
				TileType.TemperateForest)) {
			return true; // NOPMD
		} else {
			for (final TileFixture fix : tile) {
				if (fix instanceof Forest) {
					return true; // NOPMD
				}
			}
			return false;
		}
	}

	/**
	 * @param tile a tile
	 * @return whether it is mountainous or hilly
	 */
	@SuppressWarnings("deprecation")
	private static boolean isHill(final ITile tile) {
		if (TileType.Mountain.equals(tile.getTerrain())) {
			return true; // NOPMD
		} else {
			for (final TileFixture fix : tile) {
				if (fix instanceof Mountain || fix instanceof Hill) {
					return true; // NOPMD
				}
			}
			return false;
		}
	}

	/**
	 * FIXME: *Some* explorers *would* notice even unexposed ground.
	 *
	 * @param unit a unit
	 * @param fix a fixture
	 * @return whether the unit might notice it. Units do not notice themselves,
	 *         and do not notice unexposed ground, and do not notice null fixtures.
	 */
	public static boolean mightNotice(final IUnit unit,
			@Nullable final TileFixture fix) {
		return fix instanceof Ground && ((Ground) fix).isExposed()
				|| !(fix instanceof Ground || unit.equals(fix));
	}

	/**
	 * @param unit
	 *            a unit
	 * @param fix
	 *            a fixture
	 * @return whether the unit should always notice it. A null fixture is never
	 *         noticed
	 */
	public static boolean shouldAlwaysNotice(final IUnit unit,
			@Nullable final TileFixture fix) {
		return fix instanceof Mountain || fix instanceof RiverFixture
				|| fix instanceof Hill || fix instanceof Forest
				|| fix instanceof Fortress
				&& ((Fortress) fix).getOwner().equals(unit.getOwner());
	}
}
