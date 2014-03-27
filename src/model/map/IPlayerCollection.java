package model.map;
/**
 * An interface for collections of players.
 * @author Jonathan Lovelace
 */
public interface IPlayerCollection extends Iterable<Player>,
		Subsettable<IPlayerCollection> {

	/**
	 * @param player a player-id
	 *
	 * @return the player with that ID, or a new Player with that number if we
	 *         don't have it.
	 */
	Player getPlayer(int player);

	/**
	 * Note that this method currently iterates through all the players to find
	 * the one marked current.
	 *
	 *
	 * @return the current player, or a new player with a negative number and
	 *         the empty string for a name.
	 */
	Player getCurrentPlayer();

	/**
	 * @param obj an object
	 * @return whether we contain it
	 */
	boolean contains(Player obj);

	/**
	 * @return a player for "independent" fixtures.
	 */
	Player getIndependent();

}
