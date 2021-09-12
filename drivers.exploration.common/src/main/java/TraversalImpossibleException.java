/**
 * An exception thrown to signal traversal is impossible.
 *
 * FIXME: Ocean isn't impassable to everything, of course.
 */
public final class TraversalImpossibleException extends Exception {
	public TraversalImpossibleException() {
		super("Traversal is impossible.");
	}
}
