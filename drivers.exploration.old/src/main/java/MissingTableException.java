/**
 * An exception to throw when a table is missing.
 */
public class MissingTableException extends Exception {
	/**
	 * The name of the missing table.
	 */
	private final String table;

	public MissingTableException(String table) {
		super("Missing table" + table);
		this.table = table;
	}

	public String getTable() {
		return table;
	}
}
