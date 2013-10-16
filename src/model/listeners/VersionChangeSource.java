package model.listeners;

/**
 * An interface for things that will be able to tell when the map version
 * changes.
 *
 * @author Jonathan Lovelace
 */
public interface VersionChangeSource {
	/**
	 * @param list a listener to add
	 */
	void addVersionChangeListener(final VersionChangeListener list);

	/**
	 * @param list a listener to remove
	 */
	void removeVersionChangeListener(final VersionChangeListener list);
}
