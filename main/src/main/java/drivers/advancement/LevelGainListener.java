package drivers.advancement;

import java.util.EventListener;

/**
 * An interface for objects that want to be notified when a worker gains a
 * level in the currently selected skill.
 */
public interface LevelGainListener extends EventListener {
	/**
	 * Handle a gained level.
	 */
	void level(String workerName, String jobName, String skillName, int gains, int currentLevel);
}
