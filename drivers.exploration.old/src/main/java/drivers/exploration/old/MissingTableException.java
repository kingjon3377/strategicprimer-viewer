package drivers.exploration.old;

import java.io.Serial;

/**
 * An exception to throw when a table is missing.
 */
public final class MissingTableException extends Exception {
	@Serial
	private static final long serialVersionUID = 1L;
	/**
	 * The name of the missing table.
	 */
	private final String table;

	public MissingTableException(final String table) {
		super("Missing table" + table);
		this.table = table;
	}

	public String getTable() {
		return table;
	}
}
