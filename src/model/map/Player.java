package model.map;

import java.io.Serializable;

/**
 * A representation of a player in the game.
 *
 * @author jsl7
 *
 */
public class Player implements Comparable<Player>, HasName, Serializable {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The player's number.
	 */
	private final int playerID;
	/**
	 * The player's code name.
	 */
	private String playerName;

	/**
	 * Constructor.
	 *
	 * @param idNum the player's number
	 * @param name the player's code name
	 */
	public Player(final int idNum, final String name) {
		super();
		playerID = idNum;
		playerName = name;
		setCurrent(false);
	}

	/**
	 *
	 * @return the player's number
	 */
	public final int getPlayerId() {
		return playerID;
	}

	/**
	 *
	 * @return the player's code name
	 */
	@Override
	public final String getName() {
		return playerName;
	}

	/**
	 * @param obj an object
	 *
	 * @return whether it's an identical Player
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof Player
						&& playerID == ((Player) obj).getPlayerId() && playerName
							.equals(((Player) obj).getName()));
	}

	/**
	 *
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return playerID;
	}

	/**
	 * Compare to another Player.
	 *
	 * @param player the Player to compare to
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final Player player) {
		return player.hashCode() - hashCode();
	}

	/**
	 *
	 * @return a String representation of the Player
	 */
	@Override
	public String toString() {
		return playerName.isEmpty() ? "player #" + playerID : playerName;
	}

	/**
	 * Whether this is the current player or not.
	 */
	private boolean current;

	/**
	 * @param curr whether this is the current player or not
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
	/**
	 * @return whether this is the (or an) "independent" player---the "owner" of unowned fixtures.
	 */
	public final boolean isIndependent() {
		return "independent".equalsIgnoreCase(getName());
	}
	/**
	 * @param name the player's new name
	 */
	@Override
	public final void setName(final String name) {
		playerName = name;
	}
}
