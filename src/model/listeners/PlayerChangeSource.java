package model.listeners;

/**
 * An interface for things that can fire notifications of the current player changing.
 * @author Jonathan Lovelace
 *
 */
public interface PlayerChangeSource {
	/**
	 * Add a PlayerChangeListener.
	 *
	 * @param list the listener to add
	 */
	void addPlayerChangeListener(final PlayerChangeListener list);

	/**
	 * Remove a PlayerChangeListener.
	 *
	 * @param list the listener to remove
	 */
	void removePlayerChangeListener(final PlayerChangeListener list);
}
