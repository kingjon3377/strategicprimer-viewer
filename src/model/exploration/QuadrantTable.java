package model.exploration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.viewer.Tile;
import util.Pair;

/**
 * A class for things where results are by quadrant rather than randomly.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class QuadrantTable implements EncounterTable {
	/**
	 * The size of the map in rows. TODO: this should be dynamic.
	 */
	private static final int MAP_SIZE_ROWS = 69;
	/**
	 * The size of the map in columns. TODO: this should be dynamic.
	 */
	private static final int MAP_SIZE_COLS = 88;
	/**
	 * The collection of results
	 */
	private final Map<Pair<Integer, Integer>, String> quadrants;

	/**
	 * Constructor
	 * 
	 * @param rows
	 *            the number of rows of quadrants
	 * @param items
	 *            the items to allocate by quadrant
	 */
	public QuadrantTable(final int rows, final List<String> items) {
		if (items.size() % rows != 0) {
			throw new IllegalArgumentException(Integer.toString(items.size())
					+ " items won't divide evenly into "
					+ Integer.toString(rows));
		}
		final int cols = items.size() / rows;
		final int rowstep = MAP_SIZE_ROWS / rows;
		final int colstep = MAP_SIZE_COLS / cols;
		final int rowRemain = MAP_SIZE_ROWS % rows;
		final int colRemain = MAP_SIZE_COLS % cols;
		quadrants = new HashMap<Pair<Integer, Integer>, String>();
		for (int row = 0; row < MAP_SIZE_ROWS - rowRemain; row += rowstep) {
			for (int col = 0; col < MAP_SIZE_COLS - colRemain; col += colstep) {
				// System.out.println("Adding " + items.get(0) + " at (" + row +
				// ", " + col +").");
				quadrants.put(Pair.of(row, col), items.remove(0));
			}
		}
		Pair.of(rows, cols);
	}

	/**
	 * @param row
	 *            the row of a tile
	 * @param col
	 *            the column of a tile
	 * @return the result from the quadrant containing that tile.
	 */
	public String getQuadrantValue(final int row, final int col) {
		Pair<Integer, Integer> bestKey = Pair.of(-1, -1);
		for (Pair<Integer, Integer> iter : quadrants.keySet()) {
			if (iter.first <= row && iter.first > bestKey.first
					&& iter.second <= col && iter.second > bestKey.second) {
				bestKey = iter;
			}
		}
		return quadrants.get(bestKey);
	}
	/**
	 * @param tile a tile
	 * @return what the table has for that tile
	 */
	@Override
	public String generateEvent(final Tile tile) {
		return getQuadrantValue(tile.getRow(),tile.getCol());
	}
	/**
	 * @return all events that this table can produce. 
	 */
	@Override
	public Set<String> allEvents() {
		return new HashSet<String>(quadrants.values());
	}
}
