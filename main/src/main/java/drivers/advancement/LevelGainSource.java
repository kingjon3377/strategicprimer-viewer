package drivers.advancement;

import drivers.common.LevelGainListener;

/**
 * An interface for objects that can indicate a gained level.
 */
public interface LevelGainSource {
	/**
	 * Notify the given listener of future gained levels.
	 */
	void addLevelGainListener(LevelGainListener listener);

	/**
	 * Stop notifying the given listener of gained levels.
	 */
	void removeLevelGainListener(LevelGainListener listener);
}
