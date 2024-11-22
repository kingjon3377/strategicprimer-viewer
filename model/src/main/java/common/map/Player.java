package common.map;

/**
 * An interface for the representation of a player in the game.
 *
 * TODO: Maybe drop the Has* interfaces?
 */
public interface Player extends Comparable<Player>, HasName, HasPortrait {
	/**
	 * The player's ID number.
	 */
	int playerId();

	/**
	 * The (code) name of the player
	 */
	String name();

	@Override
	default String getName() {
		return name();
	}

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

	@Override
	default String getPortrait() {
		return portrait();
	}

	/**
	 * The country the player is associated with. The empty string if not provided. TODO: Should this be required to be
	 * unique in a map?
	 */
	String country();
}
