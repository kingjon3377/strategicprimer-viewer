package model.listeners;

/**
 * An interface for objects that can indicate a gained level.
 *
 * @author Jonathan Lovelace
 *
 */
public interface LevelGainSource {
	/**
	 * Add a listener.
	 *
	 * @param list the listener to add
	 */
	void addLevelGainListener(final LevelGainListener list);

	/**
	 * Remove a listener.
	 *
	 * @param list the listener to remove
	 */
	void removeLevelGainListener(final LevelGainListener list);
}
