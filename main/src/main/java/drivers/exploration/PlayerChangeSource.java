package drivers.exploration;

import drivers.common.PlayerChangeListener;

/**
 * An interface for things that can fire notifications of the current player changing.
 */
public interface PlayerChangeSource {
	/**
	 * Notify the given listener of future changes to which player is current.
	 */
	void addPlayerChangeListener(PlayerChangeListener listener);

	/**
	 * Stop notifying the given listener of changes to which player is current.
	 */
	void removePlayerChangeListener(PlayerChangeListener listener);
}
