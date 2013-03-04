package model.exploration.old;

import java.util.HashSet;
import java.util.Set;

import model.map.Tile;

/**
 * An EncounterTable that always returns the same value.
 *
 * @author Jonathan Lovelace
 *
 */
public class ConstantTable implements EncounterTable {
	/**
	 * The value we'll always return.
	 */
	private String value;

	/**
	 * Constructor; defaults to the empty string.
	 */
	public ConstantTable() {
		this("");
	}

	/**
	 * Constructor.
	 *
	 * @param val the value to return for all queries.
	 */
	public ConstantTable(final String val) {
		value = val;
	}

	/**
	 * @param tile ignored
	 * @return our specified value.
	 */
	@Override
	public String generateEvent(final Tile tile) {
		return value;
	}

	/**
	 * @return all events that this table can produce.
	 */
	@Override
	public Set<String> allEvents() {
		final Set<String> retval = new HashSet<String>();
		retval.add(value);
		return retval;
	}

	/**
	 *
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ConstantTable";
	}
}
