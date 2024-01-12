package changesets;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.List;

/**
 * An exception for when a changeset cannot be applied because its preconditions are not met.
 */
public class PreconditionFailureException extends ChangesetFailureException {
	@Serial
	private static final long serialVersionUID = 1L;
	public PreconditionFailureException(final @NotNull String message, final @NotNull String... failedPreconditions) {
		super(message);
		this.failedPreconditions = List.of(failedPreconditions);
	}
	private final List<String> failedPreconditions;

	public List<String> getFailedPreconditions() {
		return failedPreconditions;
	}
}
