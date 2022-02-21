package common.map;

import java.util.Objects;
import org.jetbrains.annotations.Nullable;

/**
 * A representation of a player in the game.
 */
public final class PlayerImpl implements MutablePlayer {

	public PlayerImpl(final int playerId, final String name, final String country) {
		this.playerId = playerId;
		this.name = name;
		this.country = country;
	}

	public PlayerImpl(final int playerId, final String name) {
		this.playerId = playerId;
		this.name = name;
		country = null;
	}

	/**
	 * The player's number.
	 */
	private final int playerId;

	/**
	 * The player's number.
	 */
	@Override
	public int getPlayerId() {
		return playerId;
	}

	/**
	 * The player's code name.
	 */
	private final String name;

	/**
	 * The player's code name.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Whether this is the current player or not.
	 *
	 * TODO: Should this really be encapsulated in Player, not PlayerCollection?"
	 */
	private boolean current = false;

	/**
	 * Whether this is the current player or not.
	 */
	@Override
	public boolean isCurrent() {
		return current;
	}

	@Override
	public void setCurrent(final boolean current) {
		this.current = current;
	}

	/**
	 * The country the player is associated with.
	 */
	private final @Nullable String country;

	/**
	 * The country the player is associated with.
	 */
	@Override
	public @Nullable String getCountry() {
		return country;
	}

	/**
	 * An object is equal iff it is a Player with the same number, name, and country.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Player) {
			return playerId == ((Player) obj).getPlayerId() &&
				name.equals(((Player) obj).getName()) &&
				Objects.equals(country, ((Player) obj).getCountry());
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
		return Integer.compare(playerId, player.getPlayerId());
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

	private String portrait = "";

	@Override
	public String getPortrait() {
		return portrait;
	}

	@Override
	public void setPortrait(final String portrait) {
		this.portrait = portrait;
	}
}
