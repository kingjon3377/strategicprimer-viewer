package model.map;
/**
 * An interface for things that are owned by a player.
 * @author Jonathan Lovelace
 */
public interface HasOwner {
	/**
	 * @return The player that owns whatever this is.
	 */
	Player getOwner();
}
