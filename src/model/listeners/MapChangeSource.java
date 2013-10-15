package model.listeners;
/**
 * An interface for things that can fire notifications of a new map being loaded.
 * @author Jonathan Lovelace
 *
 */
public interface MapChangeSource {
	/**
	 * Add a MapChangeListener.
	 *
	 * @param list the listener to add
	 */
	void addMapChangeListener(final MapChangeListener list);

	/**
	 * Remove a MapChangeListener.
	 *
	 * @param list the listener to remove
	 */
	void removeMapChangeListener(final MapChangeListener list);
}
