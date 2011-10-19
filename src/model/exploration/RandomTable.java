package model.exploration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.map.Tile;
import util.Pair;
import util.SingletonRandom;

/**
 * A table where the event is selected at random.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class RandomTable implements EncounterTable {
	/**
	 * @param tile
	 *            ignored
	 * 
	 * @return a random item from the table, or the last item in the table if
	 *         the normal procedure fails.
	 */
	@Override
	public String generateEvent(final Tile tile) {
		final int roll = SingletonRandom.RANDOM.nextInt(100);
		return getLowestMatch(roll);
	}

	/**
	 * @param value
	 *            a number to check the table against
	 * 
	 * @return the result of the check
	 */
	private String getLowestMatch(final int value) {
		for (final Pair<Integer, String> item : table) {
			if (value >= item.first()) {
				return item.second(); // NOPMD
			}
		}
		return table.get(table.size() - 1).second();
	}

	/**
	 * A list of items.
	 */
	private final List<Pair<Integer, String>> table;

	/**
	 * Constructor.
	 * 
	 * @param items
	 *            the items in the table.
	 */
	public RandomTable(final List<Pair<Integer, String>> items) {
		table = new ArrayList<Pair<Integer, String>>(items);
		Collections.sort(table, Collections.reverseOrder());
	}

	/**
	 * @return all events that this table can produce.
	 */
	@Override
	public Set<String> allEvents() {
		final Set<String> retval = new HashSet<String>();
		for (final Pair<Integer, String> pair : table) {
			retval.add(pair.second());
		}
		return retval;
	}

	/**
	 * 
	 * @return a String representation of the class
	 */
	@Override
	public String toString() {
		return "RandomTable";
	}
}
