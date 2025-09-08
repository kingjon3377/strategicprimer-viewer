package changesets;

import java.io.Serial;

/**
 * An exception class to represent failures to apply a changeset.
 */
public abstract class ChangesetFailureException extends Exception {
	@Serial
	private static final long serialVersionUID = 1L;

	protected ChangesetFailureException(final String message, final Throwable cause) {
		super(message, cause);
	}

	protected ChangesetFailureException(final String message) {
		super(message);
	}
}
