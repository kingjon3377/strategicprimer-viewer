package controller.map.converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import model.map.IMap;
import model.map.IMutableTile;
import model.map.ITile;
import model.map.MapDimensions;
import model.map.MapView;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import model.map.River;
import model.map.SPMap;
import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.RiverFixture;
import util.EnumCounter;
import util.NullCleaner;

/**
 * A class to convert a map to an equivalent half-resolution one.
 *
 * @author Jonathan Lovelace
 *
 */
public class ResolutionDecreaseConverter {
	/**
	 * Convert a map. It needs to have an even number of rows and columns.
	 *
	 * @param old the map to convert.
	 * @return an equivalent MapView.
	 */
	public static MapView convert(final IMap old) {
		checkRequirements(old);
		final int newRows = old.getDimensions().rows / 2;
		final int newCols = old.getDimensions().cols / 2;
		final SPMap newMap = new SPMap(new MapDimensions(newRows, newCols, 2));
		for (final Player player : old.getPlayers()) {
			if (player != null) {
				newMap.addPlayer(player);
			}
		}
		for (int row = 0; row < newRows; row++) {
			for (int col = 0; col < newCols; col++) {
				final Point point = PointFactory.point(row, col);
				newMap.addTile(
						point,
						convertTile(old.getTile(PointFactory.point(row * 2,
								col * 2)), old.getTile(PointFactory.point(
								row * 2, col * 2 + 1)), old
								.getTile(PointFactory.point(row * 2 + 1,
										col * 2)), old.getTile(PointFactory
								.point(row * 2 + 1, col * 2 + 1))));
			}
		}
		final MapView retval = new MapView(newMap, newMap.getPlayers()
				.getCurrentPlayer().getPlayerId(), 0);
		return retval;
	}

	/**
	 * Check that the map has an even number of rows and columns.
	 *
	 * @param map the map to check.
	 */
	private static void checkRequirements(final IMap map) {
		if (map.getDimensions().rows % 2 != 0
				|| map.getDimensions().cols % 2 != 0) {
			throw new IllegalArgumentException(
					"Can only convert maps with even numbers of rows and columns.");
		}
	}

	/**
	 * @param upperLeft the upper-left tile of a group of four.
	 * @param upperRight the upper-right tile of a group of four
	 * @param lowerLeft the lower-left tile of a group of four
	 * @param lowerRight the lower-right tile of a group of four.
	 * @return a tile representing them on the lower-resolution map
	 */
	private static IMutableTile convertTile(final ITile upperLeft,
			final ITile upperRight, final ITile lowerLeft, final ITile lowerRight) {
		final Set<River> upperLeftRivers = getRivers(upperLeft);
		final Set<River> upperRightRivers = getRivers(upperRight);
		final Set<River> lowerLeftRivers = getRivers(lowerLeft);
		final Set<River> lowerRightRivers = getRivers(lowerRight);
		final IMutableTile retval = new Tile(consensus(upperLeft.getTerrain(),
				upperRight.getTerrain(), lowerLeft.getTerrain(),
				lowerRight.getTerrain()));
		addAllFixtures(upperLeft, retval);
		addAllFixtures(upperRight, retval);
		addAllFixtures(lowerLeft, retval);
		addAllFixtures(lowerRight, retval);
		final RiverFixture combined = new RiverFixture();
		removeRivers(upperLeftRivers, River.East, River.South);
		removeRivers(upperRightRivers, River.West, River.South);
		removeRivers(lowerLeftRivers, River.East, River.North);
		removeRivers(lowerRightRivers, River.West, River.North);
		addRivers(combined, upperLeftRivers, upperRightRivers, lowerLeftRivers,
				lowerRightRivers);
		retval.addFixture(combined);
		return retval;
	}

	/**
	 * Add all non-river fixtures from the source to the destination tile.
	 *
	 * @param source a source tile
	 * @param dest a destination tile
	 */
	private static void addAllFixtures(final ITile source, final IMutableTile dest) {
		for (final TileFixture fix : source) {
			if (fix != null && !(fix instanceof RiverFixture)) {
				dest.addFixture(fix);
			}
		}
	}

	/**
	 * @param tile a tile
	 * @return its RiverFixture, or an empty one if it doesn't have one
	 */
	private static Set<River> getRivers(final ITile tile) {
		final Set<River> retval =
				NullCleaner.assertNotNull(EnumSet.noneOf(River.class));
		if (tile.hasRiver()) {
			for (final River river : tile.getRivers()) {
				retval.add(river);
			}
		}
		return retval;
	}

	/**
	 * @param fix a RiverFixture
	 * @param rivers a series of rivers to add to it
	 */
	@SafeVarargs
	private static void addRivers(final RiverFixture fix,
			final Iterable<River>... rivers) {
		for (final Iterable<River> riverFix : rivers) {
			for (final River river : riverFix) {
				if (river != null) {
					fix.addRiver(river);
				}
			}
		}
	}

	/**
	 * @param set a set of rivers
	 * @param rivers a series of rivers to remove from it
	 */
	private static void removeRivers(final Set<River> set,
			final River... rivers) {
		for (final River river : rivers) {
			if (river != null) {
				set.remove(river);
			}
		}
	}

	/**
	 * @param one one tile-type
	 * @param two a second tile-type
	 * @param three a third tile-type
	 * @param four a fourth tile-type
	 * @return the most common tile of them, or if there are two or four with
	 *         equal representation one selected from among them at random.
	 */
	private static TileType consensus(final TileType one, final TileType two,
			final TileType three, final TileType four) {
		final EnumCounter<TileType> counter = new EnumCounter<>(TileType.class);
		counter.countMany(one, two, three, four);
		final Set<TileType> twos = EnumSet.noneOf(TileType.class);
		for (final TileType type : TileType.values()) {
			assert type != null;
			switch (counter.getCount(type)) {
			case 0:
				// skip
				break;
			case 1:
				// skip
				break;
			case 2:
				twos.add(type);
				break;
			default:
				return type; // NOPMD
			}
		}
		if (twos.size() == 1) {
			return NullCleaner.assertNotNull(twos.iterator().next()); // NOPMD
		} else {
			final List<TileType> list = Arrays.asList(one, two, three, four);
			Collections.shuffle(list);
			return NullCleaner.assertNotNull(list.get(0));
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ResolutionDecreaseConverter";
	}
}
