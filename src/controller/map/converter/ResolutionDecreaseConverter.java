package controller.map.converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import model.map.IMap;
import model.map.MapView;
import model.map.Player;
import model.map.PointFactory;
import model.map.River;
import model.map.SPMap;
import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.RiverFixture;
import util.EnumCounter;

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
	public MapView convert(final IMap old) {
		checkRequirements(old);
		final int newRows = old.rows() / 2;
		final int newCols = old.cols() / 2;
		final SPMap newMap = new SPMap(2, newRows, newCols, old.getFile());
		for (final Player player : old.getPlayers()) {
			newMap.addPlayer(player);
		}
		final MapView retval = new MapView(newMap, newMap.getPlayers()
				.getCurrentPlayer().getPlayerId(), 0, old.getFile());
		for (int row = 0; row < newRows; row++) {
			for (int col = 0; col < newCols; col++) {
				retval.getMap().addTile(
						convertTile(old.getTile(PointFactory.point(row * 2,
								col * 2)), old.getTile(PointFactory.point(
								row * 2, col * 2 + 1)), old
								.getTile(PointFactory.point(row * 2 + 1,
										col * 2)), old.getTile(PointFactory
								.point(row * 2 + 1, col * 2 + 1))));
			}
		}
		return retval;
	}

	/**
	 * Check that the map has an even number of rows and columns.
	 * 
	 * @param map the map to check.
	 */
	private static void checkRequirements(final IMap map) {
		if (map.rows() % 2 != 0 || map.cols() % 2 != 0) {
			throw new IllegalArgumentException(
					"This converter can only work on maps with an even number of rows and columns.");
		}
	}

	/**
	 * @param upperLeft the upper-left tile of a group of four.
	 * @param upperRight the upper-right tile of a group of four
	 * @param lowerLeft the lower-left tile of a group of four
	 * @param lowerRight the lower-right tile of a group of four.
	 * @return a tile representing them on the lower-resolution map
	 */
	private static Tile convertTile(final Tile upperLeft,
			final Tile upperRight, final Tile lowerLeft, final Tile lowerRight) {
		final RiverFixture upperLeftRivers = getRivers(upperLeft);
		final RiverFixture upperRightRivers = getRivers(upperRight);
		final RiverFixture lowerLeftRivers = getRivers(lowerLeft);
		final RiverFixture lowerRightRivers = getRivers(lowerRight);
		final Tile retval = new Tile(upperLeft.getLocation().row() / 2,
				upperLeft.getLocation().col() / 2, consensus(
						upperLeft.getTerrain(), upperRight.getTerrain(),
						lowerLeft.getTerrain(), lowerRight.getTerrain()),
				upperLeft.getFile());
		addAllFixtures(upperLeft, retval);
		addAllFixtures(upperRight, retval);
		addAllFixtures(lowerLeft, retval);
		addAllFixtures(lowerRight, retval);
		final RiverFixture combined = new RiverFixture();
		removeRivers(upperLeftRivers, River.East, River.South);
		removeRivers(upperRightRivers, River.West, River.South);
		removeRivers(lowerLeftRivers, River.East, River.North);
		removeRivers(lowerRightRivers, River.West, River.North);
		combined.addRivers(upperLeftRivers);
		combined.addRivers(upperRightRivers);
		combined.addRivers(lowerLeftRivers);
		combined.addRivers(lowerRightRivers);
		retval.addFixture(combined);
		return retval;
	}

	/**
	 * Add all non-river fixtures from the source to the destination tile.
	 * 
	 * @param source a source tile
	 * @param dest a destination tile
	 */
	private static void addAllFixtures(final Tile source, final Tile dest) {
		for (final TileFixture fix : source.getContents()) {
			if (!(fix instanceof RiverFixture)) {
				dest.addFixture(fix);
			}
		}
	}

	/**
	 * @param tile a tile
	 * @return its RiverFixture, or an empty one if it doesn't have one
	 */
	private static RiverFixture getRivers(final Tile tile) {
		if (tile.hasRiver()) {
			return tile.getRivers(); // NOPMD
		} else {
			return new RiverFixture();
		}
	}

	/**
	 * @param fix a RiverFixture
	 * @param rivers a series of rivers to remove from it
	 */
	private static void removeRivers(final RiverFixture fix,
			final River... rivers) {
		for (final River river : rivers) {
			fix.removeRiver(river);
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
		final EnumCounter<TileType> counter = new EnumCounter<TileType>(
				TileType.class);
		counter.countMany(one, two, three, four);
		final Set<TileType> twos = EnumSet.noneOf(TileType.class);
		for (final TileType type : TileType.values()) {
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
			return twos.iterator().next(); // NOPMD
		} else {
			final List<TileType> list = Arrays.asList(one, two, three, four);
			Collections.shuffle(list);
			return list.get(0);
		}
	}

}
