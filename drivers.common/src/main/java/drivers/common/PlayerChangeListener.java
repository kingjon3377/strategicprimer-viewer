package drivers.common;

import legacy.map.Player;
import org.jspecify.annotations.Nullable;

/**
 * An interface for things that want to be told when the current player changes.
 */
public interface PlayerChangeListener {
	/**
	 * Handle a change to which player is current.
	 */
	void playerChanged(@Nullable Player previousCurrent, Player newCurrent);
}
