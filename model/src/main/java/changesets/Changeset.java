package changesets;

import common.map.IMap;
import common.map.IMutableMap;
import org.jetbrains.annotations.NotNull;

/**
 * An interface to represent a set of changes that can be made to a map.  It'll be used to represent the differences
 * between an earlier and a later map.
 *
 * TODO: Tests
 */
public interface Changeset {
	/**
	 * The inverse of this set of operations.
	 */
	@NotNull
	Changeset invert();

	/**
	 * Apply the changeset to a map, changing it in place.
	 *
	 * TODO: Should this possibly take different arguments?
	 */
	void applyInPlace(@NotNull IMutableMap map) throws ChangesetFailureException;

	/**
	 * Apply this changeset to a map, leaving it unmodified and returning a version that with the modification applied.
	 *
	 * TODO: Is this argument all we need?
	 *
	 * TODO: Consider making all IMutableMap methods return a modified IMap instead of actually mutating in place?
	 */
	@NotNull
	IMap apply(@NotNull IMap map) throws ChangesetFailureException;
}
