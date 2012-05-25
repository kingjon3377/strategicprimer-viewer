package controller.map.converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import model.map.IMap;
import model.map.MapView;
import model.map.Player;
import model.map.PointFactory;
import model.map.River;
import model.map.SPMap;
import model.map.TerrainFixture;
import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.RiverFixture;
import util.EnumCounter;
import util.IteratorStack;
import util.IteratorWrapper;

/**
 * A class to convert a map to an equivalent half-resolution one, also creating
 * hundred-tile submaps for each. Submaps will be stored in separate files and
 * included by reference, but be warned that this will quite probably break if
 * any tiles in the original map are so referenced.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class ResolutionDecreaseConverter {
	/**
	 * The size of a submap.
	 */
	private static final int SUBMAP_SIZE = 10;
	/**
	 * Convert a map. It needs to have an even number of rows and columns.
	 * @param old the map to convert.
	 * @return an equivalent MapView.
	 */
	public MapView convert(final IMap old) {
		checkRequirements(old);
		final int newRows = old.rows() / 2;
		final int newCols = old.cols() / 2;
		final SPMap newMap = new SPMap(2, newRows, newCols, old.getFile());
		for (Player player : old.getPlayers()) {
			newMap.addPlayer(player);
		}
		final MapView retval = new MapView(newMap, newMap.getPlayers()
				.getCurrentPlayer().getPlayerId(), 0, old.getFile());
		for (int row = 0; row < newRows; row++) {
			for (int col = 0; col < newCols; col++) {
				retval.getMap().addTile(
						convertTile(old.getTile(row * 2, col * 2),
								old.getTile(row * 2, col * 2 + 1),
								old.getTile(row * 2 + 1, col * 2),
								old.getTile(row * 2 + 1, col * 2 + 1)));
				retval.addSubmap(
						PointFactory.point(row, col),
						createSubmap(
								old.getTile(row * 2, col * 2),
								old.getTile(row * 2, col * 2 + 1),
								old.getTile(row * 2 + 1, col * 2),
								old.getTile(row * 2 + 1, col * 2 + 1),
								Objects.toString(old.getFile(), ".xml")
										.replaceAll(
												".xml$",
												String.format("_%d_%d.xml",
														Integer.valueOf(row),
														Integer.valueOf(col)))));
			}
		}
		return retval;
	}
	/**
	 * Check that the map has an even number of rows and columns.
	 * @param map the map to check.
	 */
	private static void checkRequirements(final IMap map) {
		if (map.rows() % 2 != 0 || map.cols() % 2 != 0) {
			throw new IllegalArgumentException("This converter can only work on maps with an even number of rows and columns.");
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
		final RiverFixture empty = new RiverFixture();
		final RiverFixture upperLeftRivers = (upperLeft.hasRiver() ? upperLeft.getRivers() : empty);
		final RiverFixture upperRightRivers = (upperRight.hasRiver() ? upperRight.getRivers() : empty);
		final RiverFixture lowerLeftRivers = (lowerLeft.hasRiver() ? lowerLeft.getRivers() : empty);
		final RiverFixture lowerRightRivers = (lowerRight.hasRiver() ? lowerRight.getRivers() : empty);
		final Tile retval = new Tile(upperLeft.getLocation().row() / 2,
				upperLeft.getLocation().col() / 2, consensus(
						upperLeft.getTerrain(), upperRight.getTerrain(),
						lowerLeft.getTerrain(), lowerRight.getTerrain()),
				upperLeft.getFile());
		@SuppressWarnings("unchecked")
		final Iterable<TileFixture> iter = new IteratorWrapper<TileFixture>(
				new IteratorStack<TileFixture>(upperLeft.getContents(),
						upperRight.getContents(), lowerLeft.getContents(),
						lowerRight.getContents()));
		for (TileFixture fix : iter) {
			if (!(fix instanceof RiverFixture)) {
				retval.addFixture(fix);
			}
		}
		final RiverFixture combined = new RiverFixture();
		upperLeftRivers.removeRiver(River.East);
		upperLeftRivers.removeRiver(River.South);
		upperRightRivers.removeRiver(River.West);
		upperRightRivers.removeRiver(River.South);
		lowerLeftRivers.removeRiver(River.East);
		lowerLeftRivers.removeRiver(River.North);
		lowerRightRivers.removeRiver(River.West);
		lowerRightRivers.removeRiver(River.North);
		combined.addRivers(upperLeftRivers);
		combined.addRivers(upperRightRivers);
		combined.addRivers(lowerLeftRivers);
		combined.addRivers(lowerRightRivers);
		retval.addFixture(combined);
		return retval;
	}
	/**
	 * @param one
	 *            one tile-type
	 * @param two
	 *            a second tile-type
	 * @param three
	 *            a third tile-type
	 * @param four
	 *            a fourth tile-type
	 * @return the most common tile of them, or if there are two or four with
	 *         equal representation one selected from among them at random.
	 */
	private static TileType consensus(final TileType one, final TileType two,
			final TileType three, final TileType four) {
		final EnumCounter<TileType> counter = new EnumCounter<TileType>(TileType.class);
		counter.countMany(one, two, three, four);
		final Set<TileType> twos = EnumSet.noneOf(TileType.class);
		for (TileType type : TileType.values()) {
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
	/**
	 * @param upperLeft the upper-left tile of a group of four.
	 * @param upperRight the upper-right tile of a group of four
	 * @param lowerLeft the lower-left tile of a group of four
	 * @param lowerRight the lower-right tile of a group of four.
	 * @param filename the file to store the submap in
	 * @return a submap representing them on the lower-resolution map
	 */
	private static SPMap createSubmap(final Tile upperLeft,
			final Tile upperRight, final Tile lowerLeft, final Tile lowerRight, final String filename) {
		final SPMap retval = new SPMap(2, SUBMAP_SIZE, SUBMAP_SIZE, filename);
		paintAndFillTiles(upperLeft, retval, 0, SUBMAP_SIZE / 2, 0, SUBMAP_SIZE / 2);
		// ESCA-JAVA0076:
		paintAndFillTiles(upperRight, retval, 0, SUBMAP_SIZE / 2, SUBMAP_SIZE / 2, SUBMAP_SIZE);
		paintAndFillTiles(lowerLeft, retval, SUBMAP_SIZE / 2, SUBMAP_SIZE, 0, SUBMAP_SIZE / 2);
		paintAndFillTiles(lowerRight, retval, SUBMAP_SIZE / 2, SUBMAP_SIZE, SUBMAP_SIZE / 2, SUBMAP_SIZE);
		retval.setFileOnChildren(filename);
		return retval;
	}
	
	/**
	 * Fill a range of tiles in on the map, creating them with the terrain type
	 * of the specified tile, and then distribute fixtures among those subtiles.
	 * 
	 * @param tile
	 *            the source tile
	 * @param map
	 *            the (sub)map we're working with
	 * @param minRow
	 *            the minimum row in the submap (inclusive)
	 * @param maxRow
	 *            the maximum row in the submap (exclusive)
	 * @param minCol
	 *            the minimum column in the submap (inclusive)
	 * @param maxCol
	 *            the maximum column in the submap (exclusive)
	 */
	private static void paintAndFillTiles(final Tile tile, final SPMap map,
			final int minRow, final int maxRow, final int minCol,
			final int maxCol) {
		for (int i = minRow; i < maxRow; i++) {
			for (int j = minCol; j < maxCol; j++) {
				map.addTile(new Tile(i, j, tile.getTerrain(), map.getFile())); // NOPMD
			}
		}
		final Random random = new Random();
		for (TileFixture fix : tile.getContents()) {
			random.setSeed(fix.getID());
			if (fix instanceof TerrainFixture) {
				addToAll(fix, map, minRow, maxRow, minCol, maxCol);
			} else if (fix instanceof RiverFixture) {
				for (final River river : (RiverFixture) fix) {
					extendRiver(river, map, minRow, maxRow, minCol, maxCol);
				}
			} else {
				map.getTile(random.nextInt(maxRow - minRow) + minRow,
						random.nextInt(maxCol - minCol) + minCol).addFixture(
						setFile(map.getFile(), fix.deepCopy()));
			}
		}
	}
	
	/**
	 * Add a fixture to every tile in a specified range.
	 * @param fix the fixture to add.
	 * @param map the (sub)map we're working with
	 * @param minRow the minimum row in the submap (inclusive)
	 * @param maxRow the maximum row in the submap (exclusive)
	 * @param minCol the minimum column in the submap (inclusive)
	 * @param maxCol the maximum column in the submap (exclusive)
	 */
	private static void addToAll(final TileFixture fix, final SPMap map,
			final int minRow, final int maxRow, final int minCol,
			final int maxCol) {
		for (int i = minRow; i < maxRow; i++) {
			for (int j = minCol; j < maxCol; j++) {
				map.getTile(i, j).addFixture(setFile(map.getFile(), fix.deepCopy()));
			}
		}
	}
	/**
	 * Set the filename on a TileFixture.
	 * @param filename the filename to set
	 * @param fix the fixture to operate on
	 * @return the fixture
	 */
	private static TileFixture setFile(final String filename, final TileFixture fix) {
		fix.setFile(filename);
		return fix;
	}
	/**
	 * Paint a river over a region of a map. Most "lakes" should actually be
	 * changed to ocean tiles, but we need to do that by hand so fixtures don't
	 * get put there by accident.
	 * 
	 * @param river
	 *            the river to paint
	 * @param map
	 *            the (sub)map we're working with
	 * @param minRow
	 *            the minimum row in the submap (inclusive)
	 * @param maxRow
	 *            the maximum row in the submap (exclusive)
	 * @param minCol
	 *            the minimum column in the submap (inclusive)
	 * @param maxCol
	 *            the maximum column in the submap (exclusive)
	 */
	private static void extendRiver(final River river, final SPMap map,
			final int minRow, final int maxRow, final int minCol,
			final int maxCol) {
		switch (river) {
		case East:
			paintRiver(true, map, middle(minRow, maxRow), middle(minCol, maxCol), maxCol);
			break;
		case Lake:
			map.getTile(middle(minRow, maxRow), middle(minCol, maxCol)).addRiver(river);
			break;
		case North:
			paintRiver(false, map, middle(minCol, maxCol), minRow, middle(minRow, maxRow));
			break;
		case South:
			paintRiver(false, map, middle(minCol, maxCol), middle(minRow, maxRow), maxRow);
			break;
		case West:
			paintRiver(true, map, middle(minRow, maxRow), minCol, middle(minCol, maxCol));
			break;
		default:
			throw new IllegalStateException("Default case of an enum switch");
		}
	}
	/**
	 * Paint a river along a line in a map.
	 * @param horizontal whether it's a horizontal river we're painting
	 * @param map the map to get tiles to paint on
	 * @param constant the row (if horizontal) or column (if vertical) to paint along
	 * @param min the column or row we start at (inclusive)
	 * @param max the column or row we finish before (i.e. exclusive)
	 */
	private static void paintRiver(final boolean horizontal, final SPMap map,
			final int constant, final int min, final int max) {
		if (horizontal) {
			for (int col = min; col < max; col++) {
				final Tile tile = map.getTile(constant, col);
				tile.addRiver(River.East);
				tile.addRiver(River.West);
			}
		} else {
			for (int row = min; row < max; row++) {
				final Tile tile = map.getTile(row, constant);
				tile.addRiver(River.North);
				tile.addRiver(River.South);
			}
		}
	}
	/**
	 * Return the middle number of a range given its endpoints.
	 * @param min the minimum (inclusive)
	 * @param max the maximum (exclusive)
	 * @return the middle number---the average plus one.
	 */
	private static int middle(final int min, final int max) {
		return (min + max) / 2 + 1;
	}
	
}
