package drivers.common;

/**
 * An interface for things that can fire notifications of a new map being loaded.
 */
public interface MapChangeSource {
	/**
	 * Notify the given listener of any newly loaded maps.
	 */
	void addMapChangeListener(MapChangeListener listener);

	/**
	 * Stop notifying the given listener.
	 */
	void removeMapChangeListener(MapChangeListener listener);
}
