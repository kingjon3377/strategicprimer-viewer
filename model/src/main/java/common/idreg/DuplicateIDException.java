package common.idreg;

/**
 * An exception to warn about duplicate IDs.
 */
public class DuplicateIDException extends Exception {
    private static final long serialVersionUID = 1L;

    public DuplicateIDException(final int id) {
        super("Duplicate ID #" + id);
    }

    // TODO: Should probably take filename as well. Note getting this from call-sites requires heavy refactoring.
    public DuplicateIDException(final int id, final int line, final int column) {
        super(String.format("Duplicate ID #%d at line %d, column %d", id, line, column));
    }
}
