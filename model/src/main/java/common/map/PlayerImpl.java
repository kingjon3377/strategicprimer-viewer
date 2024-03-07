package common.map;

/**
 * A representation of a player in the game.
 *
 * @param playerId The player's number.
 * @param name     The player's code name.
 * @param current  Whether this is the current player or not.
 *                                 TODO: Should this really be encapsulated in Player, not PlayerCollection?
 * @param country  The country the player is associated with.
 */
public record PlayerImpl(int playerId, String name, String country, boolean current,
                         String portrait) implements Player {

	/**
	 * The player's number.
	 */
	@Override
	public final int playerId() {
		return playerId;
	}

	/**
	 * The player's code name.
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * Whether this is the current player or not.
	 */
	@Override
	public boolean current() {
		return current;
	}

	/**
	 * The country the player is associated with.
	 */
	@Override
	public String country() {
		return country;
	}

	/**
	 * An object is equal iff it is a Player with the same number, name, and country. TODO: Match "current"?
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof final Player p) {
			return playerId == p.playerId() &&
					name.equals(p.getName()) &&
					country.equals(p.country());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return playerId;
	}

	@Override
	public int compareTo(final Player player) {
		return Integer.compare(playerId, player.playerId());
	}

	/**
	 * If the player name is non-empty, use it; otherwise, use "player #NN".
	 */
	@Override
	public String toString() {
		if (name.isEmpty()) {
			return "player #" + playerId;
		} else {
			return name;
		}
	}
}
