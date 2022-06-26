package drivers.common;

/**
 * An interface for things that will be able to tell when the map version changes.
 */
public interface VersionChangeSource {
	/**
	 * Add a listener.
	 */
	void addVersionChangeListener(VersionChangeListener listener);

	/**
	 * Remove a listener.
	 */
	void removeVersionChangeListener(VersionChangeListener listener);
}
