package common.idreg;

/**
 * An exception to warn about duplicate IDs.
 */
public class DuplicateIDException extends Exception {
	public DuplicateIDException(final int id) {
		super("Duplicate ID #" + id);
	}

	// TODO: Should probably take filename as well
	public DuplicateIDException(final int id, final int line, final int column) {
		super(String.format("Duplicate ID #%d at line %d, column %d", id, line, column));
	}
}
