package model.map;

/**
 * A representation of a player in the game.
 * 
 * @author jsl7
 * 
 */
public class Player implements Comparable<Player> {
	/**
	 * The player's number.
	 */
	private final int playerID;
	/**
	 * The player's code name.
	 */
	private final String playerName;

	/**
	 * Constructor.
	 * 
	 * @param idNum
	 *            the player's number
	 * @param name
	 *            the player's code name
	 */
	public Player(final int idNum, final String name) {
		playerID = idNum;
		playerName = name;
		setCurrent(false);
	}

	/**
	 * 
	 * @return the player's number
	 */
	public final int getId() {
		return playerID;
	}

	/**
	 * 
	 * @return the player's code name
	 */
	public final String getName() {
		return playerName;
	}

	/**
	 * @param obj
	 *            an object
	 * 
	 * @return whether it's an identical Player
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof Player && playerID == ((Player) obj).getId() && playerName
						.equals(((Player) obj).getName()));
	}

	/**
	 * 
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return playerID | playerName.hashCode();
	}

	/**
	 * Compare to another Player.
	 * 
	 * @param player
	 *            the Player to compare to
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final Player player) {
		return Integer.valueOf(hashCode()).compareTo(player.hashCode());
	}

	/**
	 * 
	 * @return a String representation of the Player
	 */
	@Override
	public String toString() {
		return "".equals(playerName) ? "player #" + playerID : playerName;
	}

	/**
	 * Whether this is the current player or not.
	 */
	private boolean current;

	/**
	 * @param curr
	 *            whether this is the current player or not
	 */
	public final void setCurrent(final boolean curr) {
		current = curr;
	}

	/**
	 * 
	 * @return true iff this is the current player
	 */
	public final boolean isCurrent() {
		return current;
	}
}
