package model.exploration;

import model.viewer.Tile;

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
	 * @param val the value to return for all queries.
	 */
	public ConstantTable(final String val) {
		setValue(val);
	}
	/**
	 * @param val the value to return for subsequent queries.
	 */
	public final void setValue(final String val) {
		value = val;
	}
	/**
	 * @param tile ignored
	 * @returns our specified value.
	 */
	@Override
	public String generateEvent(Tile tile) {
		return value;
	}

}
