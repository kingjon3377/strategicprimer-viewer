package legacy.map;

/**
 * An interface for player collections that can be modified.
 */
public interface IMutableLegacyPlayerCollection extends ILegacyPlayerCollection {
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
	 * Set the current player.
	 */
	void setCurrentPlayer(Player currentPlayer);
	/**
	 * Clone the collection.
	 */
	@Override
	IMutableLegacyPlayerCollection copy();
}
