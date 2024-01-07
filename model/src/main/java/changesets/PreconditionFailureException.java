package changesets;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An exception for when a changeset cannot be applied because its preconditions are not met.
 */
public class PreconditionFailureException extends ChangesetFailureException {
	public PreconditionFailureException(final @NotNull String message, final @NotNull String... failedPreconditions) {
		super(message);
		this.failedPreconditions = List.of(failedPreconditions);
	}
	private List<String> failedPreconditions;

	public List<String> getFailedPreconditions() {
		return failedPreconditions;
	}
}
