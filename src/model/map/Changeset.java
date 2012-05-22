package model.map;

/**
 * An interface to represent a set of changes that can be made to a map (TODO:
 * or to what?) It'll be used to represent the differences between an earlier
 * and a later map.
 * 
 * TODO: Tests.
 * 
 * TODO: Think of how to implement this.
 * 
 * @author Jonathan Lovelace
 * 
 */
public interface Changeset {
	/**
	 * @return The number of the "before" turn.
	 */
	int from();
	/**
	 * @return The number of the "after" turn.
	 */
	int to(); // NOPMD
	/**
	 * @return the inverse of this set of operations
	 */
	Changeset invert();
	/**
	 * Apply the changeset to a map. TODO: Should this possibly take different arguments?
	 * @param map the map to apply it to.
	 */
	void apply(IMap map);
}
