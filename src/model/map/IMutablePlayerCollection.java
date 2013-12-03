package model.map;
/**
 * An interface for player collections that can be modified.
 * @author Jonathan Lovelace
 */
public interface IMutablePlayerCollection extends IPlayerCollection {

	/**
	 * Add a player to the collection.
	 *
	 * @param player the player to add
	 * @return whether the collection was changed by the operation.
	 */
	boolean add(Player player);

	/**
	 * Remove an object from the collection.
	 *
	 * @param obj an object
	 * @return true if it was removed as a result of this call
	 */
	boolean remove(Object obj);

}
