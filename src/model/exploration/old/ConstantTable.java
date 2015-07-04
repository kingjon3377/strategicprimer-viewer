package model.exploration.old;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import model.map.Point;
import model.map.TileFixture;
import model.map.TileType;

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
	 * @param terrain ignored
	 * @param point ignored
	 * @param fixtures ignored
	 * @return our specified value.
	 */
	@Override
	public String generateEvent(final Point point, final TileType terrain,
			@Nullable final Iterable<TileFixture> fixtures) {
		return value;
	}

	/**
	 * @return all events that this table can produce.
	 */
	@Override
	public Set<String> allEvents() {
		final Set<String> retval = new HashSet<>();
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
