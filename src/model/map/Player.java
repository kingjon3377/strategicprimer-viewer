package model.map;

/**
 * A representation of a player in the game.
 *
 * @author jsl7
 *
 */
public class Player implements Comparable<Player>, XMLWritable,
		DeepCloneable<Player> {
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
	 * @param idNum the player's number
	 * @param name the player's code name
	 * @param fileName the file this was loaded from
	 */
	public Player(final int idNum, final String name, final String fileName) {
		playerID = idNum;
		playerName = name;
		setCurrent(false);
		file = fileName;
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
	 * Write the player to XML.
	 *
	 * @deprecated Replaced by SPIntermediateRepresentation-based output
	 * @return an XML representation of the player.
	 */
	@Override
	@Deprecated
	public String toXML() {
		return new StringBuilder("<player number=\"").append(getPlayerId())
				.append("\" code_name=\"").append(getName()).append("\" />")
				.toString();
	}

	/**
	 * @return The name of the file this is to be written to.
	 */
	@Override
	public String getFile() {
		return file;
	}

	/**
	 * @param fileName the name of the file this should be written to.
	 */
	@Override
	public void setFile(final String fileName) {
		file = fileName;
	}

	/**
	 * The name of the file this is to be written to.
	 */
	private String file;

	/**
	 * @return a clone of the player
	 */
	@Override
	public Player deepCopy() {
		return new Player(getPlayerId(), getName(), getFile());
	}
}
