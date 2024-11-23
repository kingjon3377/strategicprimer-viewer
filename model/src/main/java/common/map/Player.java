package common.map;

/**
 * An interface for the representation of a player in the game.
 */
public interface Player extends Comparable<Player> {
	/**
	 * The player's ID number.
	 */
	int playerId();

	/**
	 * The (code) name of the player
	 */
	String name();

	/**
	 * Whether this is the (or an) "independent" player---the "owner" of
	 * unowned fixtures.
	 */
	default boolean isIndependent() {
		return "independent".equalsIgnoreCase(name());
	}

	/**
	 * The filename of a flag for the player.
	 */
	String portrait();

	/**
	 * The country the player is associated with. The empty string if not provided. TODO: Should this be required to be
	 * unique in a map?
	 */
	String country();
}
