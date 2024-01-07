package changesets;

import common.map.IMap;
import common.map.IMutableMap;

/**
 * An interface to represent a set of changes that can be made to a map (TODO:
 * or to what?).  It'll be used to represent the differences between an earlier
 * and a later map.
 *
 * TODO: Tests
 *
 * TODO: Think of how to implement this
 */
public interface Changeset {
	/**
     * The inverse of this set of operations.
     */
    Changeset invert();

    /**
     * Apply the changeset to a map, changing it in place.
     *
     * TODO: Should this possibly take different arguments?
     */
    void applyInPlace(IMutableMap map) throws ChangesetFailureException;

	/**
	 * Apply this changeset to a map, leaving it unmodified and returning a version that with the modification applied.
	 *
	 * TODO: Is this argument all we need?
	 *
	 * TODO: Consider making all IMutableMap methods return a modified IMap instead of actually mutating in place?
	 */
	IMap apply(IMap map) throws ChangesetFailureException;
}
