package common.idreg;

import common.entity.EntityIdentifier;

import java.io.Serial;

/**
 * An exception to warn about duplicate IDs.
 */
public class DuplicateIDException extends Exception {
	@Serial
	private static final long serialVersionUID = 1L;

	public DuplicateIDException(final EntityIdentifier id) {
		super("Duplicate ID #" + id.getIdentifierString());
	}

	// TODO: Should probably take filename as well. Note getting this from call-sites requires heavy refactoring.
	public DuplicateIDException(final EntityIdentifier id, final int line, final int column) {
		super("Duplicate ID #%s at line %d, column %d".formatted(id.getIdentifierString(), line, column));
	}
}
