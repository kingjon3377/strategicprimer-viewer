package changesets;

import common.map.IMutableMapNG;

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
	 * The number of the turn before the changeset is applied.
	 */
	int getFrom();

	/**
	 * The number of the turn after the changeset is applied.
	 */
	int getTo();

	/**
	 * The inverse of this set of operations.
	 */
	Changeset invert();

	/**
	 * Apply the changeset to a map.
	 *
	 * TODO: Should this possibly take different arguments?
	 *
	 * TODO: Should this possibly take {@link common.map.IMapNG} and return
	 * the modified map, instead of modifying an {@link IMutableMapNG} in
	 * place?
	 */
	void apply(IMutableMapNG map);
}
