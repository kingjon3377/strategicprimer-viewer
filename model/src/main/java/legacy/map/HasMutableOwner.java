package legacy.map;

import common.map.Player;

/**
 * An interface for things that are owned by a player whose owner can change.
 */
public interface HasMutableOwner extends HasOwner {
	/**
	 * A setter for the owner of the whatever-this is.
	 */
	void setOwner(Player owner);
}
