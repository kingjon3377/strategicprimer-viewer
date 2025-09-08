package changesets;

import java.io.Serial;
import java.util.List;

/**
 * An exception for when a changeset cannot be applied because its preconditions are not met.
 */
public class PreconditionFailureException extends ChangesetFailureException {
	@Serial
	private static final long serialVersionUID = 1L;

	public PreconditionFailureException(final String message, final String... failedPreconditions) {
		super(message);
		this.failedPreconditions = List.of(failedPreconditions);
	}

	private final List<String> failedPreconditions;

	public final List<String> getFailedPreconditions() {
		return failedPreconditions;
	}
}
