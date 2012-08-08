package model.exploration;

/**
 * An exception to throw when a table is missing.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class MissingTableException extends Exception {
	/**
	 * The missing table.
	 */
	private final String missingTable;

	/**
	 * Constructor.
	 * 
	 * @param table the missing table
	 */
	public MissingTableException(final String table) {
		super("Missing table " + table);
		missingTable = table;
	}

	/**
	 * @return the name of the missing table
	 */
	public String getTable() {
		return missingTable;
	}
}
