package exploration.common;

import java.io.Serial;

/**
 * An exception thrown to signal traversal is impossible.
 *
 * FIXME: Ocean isn't impassable to everything, of course.
 */
public final class TraversalImpossibleException extends Exception {
	@Serial
	private static final long serialVersionUID = 1L;

	public TraversalImpossibleException() {
		super("Traversal is impossible.");
	}
}
