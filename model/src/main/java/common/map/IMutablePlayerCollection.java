package common.map;

/**
 * An interface for player collections that can be modified.
 */
public interface IMutablePlayerCollection extends IPlayerCollection {
	/**
	 * Add a player to the collection. Precondition: Collection does not contain any other player with the same player
	 * ID.
	 */
	void add(Player player);

	/**
	 * Remove a player from the collection. Precondition: The collection contains an "equal" player.
	 */
	void remove(Player obj);

	/**
	 * Remove a player from the collection. Precondition: The collection contains a player with the specified ID.
	 */
	void remove(int obj);

	/**
	 * Set what player should be marked as current. Precondition: A player with the same ID exists in the collection.
	 * @param player A player with the same ID as the one to be marked as current.
	 */
	void setCurrentPlayer(Player player);

	/**
	 * Clone the collection.
	 */
	@Override
	IMutablePlayerCollection copy();
}
