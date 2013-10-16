package model.map;

/**
 * An interface for things that are owned by a player.
 *
 * @author Jonathan Lovelace
 */
public interface HasOwner {
	/**
	 * @return The player that owns whatever this is.
	 */
	Player getOwner();

	/**
	 * @param player the player that should now own it.
	 */
	void setOwner(final Player player);
}
