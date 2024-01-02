package drivers.common;

import org.jetbrains.annotations.Nullable;
import legacy.map.Player;

/**
 * An interface for things that want to be told when the current player changes.
 */
public interface PlayerChangeListener {
	/**
	 * Handle a change to which player is current.
	 */
	void playerChanged(@Nullable Player previousCurrent, Player newCurrent);
}
