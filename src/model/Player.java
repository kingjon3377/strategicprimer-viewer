package model;

import java.io.Serializable;

/**
 * A representation of a player in the game.
 * 
 * @author jsl7
 * 
 */
public class Player implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3822757264856144605L;
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
	}

	/**
	 * @return the player's number
	 */
	public final int getId() {
		return playerID;
	}

	/**
	 * @return the player's code name
	 */
	public final String getName() {
		return playerName;
	}
}
