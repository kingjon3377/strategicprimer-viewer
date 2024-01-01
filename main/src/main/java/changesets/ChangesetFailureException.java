package changesets;

import org.jetbrains.annotations.NotNull;

/**
 * An exception class to represent failures to apply a changeset.
 */
public abstract class ChangesetFailureException extends Exception {
	protected ChangesetFailureException(final @NotNull String message, final @NotNull Throwable cause) {
		super(message, cause);
	}

	protected ChangesetFailureException(final @NotNull String message) {
		super(message);
	}
}
