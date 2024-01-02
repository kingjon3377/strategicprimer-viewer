package common.map;

import org.jetbrains.annotations.Nullable;

/**
 * An interface for the representation of a player in the game.
 *
 * TODO: Split mutability into separate interface (or just move down to impl?)
 */
public interface Player extends Comparable<Player>, HasName, HasMutablePortrait {
    /**
     * The player's ID number.
     */
    int getPlayerId();

    /**
     * Whether this is the current player.
     */
    boolean isCurrent();

    /**
     * Whether this is the (or an) "independent" player---the "owner" of
     * unowned fixtures.
     */
    default boolean isIndependent() {
        return "independent".equalsIgnoreCase(getName());
    }

    /**
     * The filename of a flag for the player.
     */
    @Override
    String getPortrait();

    /**
     * @param portrait The filename of a flag for the player.
     */
    @Override
    void setPortrait(String portrait);

    /**
     * The country the player is associated with. TODO: Should this be required to be unique in a map?
     */
    @Nullable
    String getCountry();
}
