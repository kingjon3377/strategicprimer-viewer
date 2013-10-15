package model.listeners;

/**
 * An interface for objects that want to be notified when the currently selected
 * skill gains a level.
 *
 * @author Jonathan Lovelace
 *
 */
public interface LevelGainListener {
	/**
	 * Indicate a level gain.
	 */
	void level();
}
