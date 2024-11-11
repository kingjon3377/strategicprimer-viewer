package common.idreg;

import common.entity.EntityIdentifier;

import java.io.Serial;
import java.nio.file.Path;

/**
 * An exception to warn about duplicate IDs.
 */
public class DuplicateIDException extends Exception {
	@Serial
	private static final long serialVersionUID = 1L;

	public DuplicateIDException(final EntityIdentifier id) {
		super("Duplicate ID #" + id.getIdentifierString());
	}

	public DuplicateIDException(final EntityIdentifier id, final int line, final int column) {
		super("Duplicate ID #%s at line %d, column %d".formatted(id.getIdentifierString(), line, column));
	}

	public DuplicateIDException(final EntityIdentifier id, final Path filename, final int line, final int column) {
		super("Duplicate ID #%s in %s at line %d, column %d".formatted(id.getIdentifierString(),
				filename, line, column));
	}
}
