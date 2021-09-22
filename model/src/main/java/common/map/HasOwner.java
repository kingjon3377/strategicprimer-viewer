package common.map;

/**
 * An interface for things that are owned by a player.
 */
public interface HasOwner {
	/**
	 * The owner of whatever this is.
	 */
	Player getOwner();
}
