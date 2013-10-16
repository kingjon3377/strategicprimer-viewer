package model.listeners;

import java.util.EventListener;

/**
 * An interface for objects that want to be notified when the currently selected
 * skill gains a level.
 *
 * @author Jonathan Lovelace
 *
 */
public interface LevelGainListener extends EventListener {
	/**
	 * Indicate a level gain.
	 */
	void level();
}
