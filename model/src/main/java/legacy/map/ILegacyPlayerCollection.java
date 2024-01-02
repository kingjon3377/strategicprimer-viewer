package legacy.map;

/**
 * An interface for collections of players.
 */
public interface ILegacyPlayerCollection extends Iterable<Player>, Subsettable<Iterable<Player>> {
	/**
	 * Get the player with the given player-ID, or a new player with that
	 * number if we didn't have one. In the latter case, if this is
	 * mutable, add it to the collection.
	 */
	Player getPlayer(int player);

	/**
	 * The current player, or if no player was marked current a new player
	 * with a negative number and empty name.
	 */
	Player getCurrentPlayer();

	/**
	 * The player that should own "independent" fixtures.
	 */
	Player getIndependent();

	/**
	 * Clone the collection.
	 */
	ILegacyPlayerCollection copy();

	/**
	 * Whether we contain all players in the given collection.
	 */
	boolean containsAll(ILegacyPlayerCollection other);
}
