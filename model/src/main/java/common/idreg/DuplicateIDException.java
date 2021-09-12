package common.idreg;

/**
 * An exception to warn about duplicate IDs.
 */
public class DuplicateIDException extends Exception {
	public DuplicateIDException(int id) {
		super("Duplicate ID #" + id);
	}

	// TODO: Should probably take filename as well
	public DuplicateIDException(int id, int line, int column) {
		super(String.format("Duplicate ID #%d at line %d, column %d", id, line, column));
	}

	// TODO: Deprecate this for removal once fully ported to Java?
	public static DuplicateIDException atLocation(int id, int line, int column) {
		return new DuplicateIDException(id, line, column);
	}
}
