package common.map;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

/**
 * A representation of a player in the game.
 */
public final class PlayerImpl implements Player {

    public PlayerImpl(final int playerId, final String name, final String country, final boolean current, final String portrait) {
        this.playerId = playerId;
        this.name = name;
        this.country = country;
		this.current = current;
		this.portrait = portrait;
    }

    /**
     * The player's number.
     */
    private final int playerId;

    /**
     * The player's number.
     */
    @Override
    public final int getPlayerId() {
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
    private final boolean current;

    /**
     * Whether this is the current player or not.
     */
    @Override
    public boolean isCurrent() {
        return current;
    }

    /**
     * The country the player is associated with.
     */
    private final String country;

    /**
     * The country the player is associated with.
     */
    @Override
    public String getCountry() {
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
            return playerId == p.getPlayerId() &&
                    name.equals(p.getName()) &&
                    country.equals(p.getCountry());
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

    private final String portrait;

    @Override
    public String getPortrait() {
        return portrait;
    }
}
