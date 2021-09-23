package common.map;

/**
 * An interface for player collections that can be modified.
 */
public interface IMutablePlayerCollection extends IPlayerCollection {
	/**
	 * Add a player to the collection.
	 */
	void add(Player player);

	/**
	 * Remove a player from the collection.
	 */
	void remove(Player obj);

	/**
	 * Remove a player from the collection.
	 */
	void remove(int obj);

	/**
	 * Clone the collection.
	 */
	@Override
	IMutablePlayerCollection copy();

	/**
	 * Set the current player.
	 */
	void setCurrentPlayer(Player currentPlayer);
}
