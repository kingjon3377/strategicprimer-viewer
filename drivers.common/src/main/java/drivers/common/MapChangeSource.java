package drivers.common;

/**
 * An interface for things that can fire notifications of a new map being loaded.
 *
 * TODO: Split MapMetdataChangeSource out?
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

	/**
	 * Notify the given listener of changes to map metadata.
	 */
	void addMapMetadataListener(MapMetadataChangeListener listener);

	/**
	 * Stop notifying the given listener.
	 */
	void removeMapMetadataListener(MapMetadataChangeListener listener);
}
