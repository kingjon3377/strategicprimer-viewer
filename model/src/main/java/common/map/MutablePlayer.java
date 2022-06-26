package common.map;

/**
 * A {@link Player} object that can be set as current or not.
 */
public interface MutablePlayer extends Player {
	/**
	 * Setter for whether this is the current player or not.
	 */
	void setCurrent(boolean current);
}
