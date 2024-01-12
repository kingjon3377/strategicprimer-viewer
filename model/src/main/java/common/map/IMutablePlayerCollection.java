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
     * Clone the collection.
     */
    @Override
    IMutablePlayerCollection copy();
}
