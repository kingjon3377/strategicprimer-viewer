package changesets;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;

/**
 * An exception class to represent failures to apply a changeset.
 */
public abstract class ChangesetFailureException extends Exception {
	@Serial
	private static final long serialVersionUID = 1L;

	protected ChangesetFailureException(final @NotNull String message, final @NotNull Throwable cause) {
		super(message, cause);
	}

	protected ChangesetFailureException(final @NotNull String message) {
		super(message);
	}
}
