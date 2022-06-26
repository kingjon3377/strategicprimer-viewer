package drivers.common;

/**
 * An interface for things that want to be called when a new map is loaded.
 *
 * TODO: Split so this can become a SAM interface?
 */
public interface MapChangeListener {
	/**
	 * React to the loading of a new map.
	 *
	 * Since all implementations of this interface went to their reference
	 * to a map model rather than taking the map from an event even when
	 * one was provided, this method specifies no parameters.
	 */
	void mapChanged();

	/**
	 * React to a change in the map's filename (without a change in the map
	 * itself) or the 'modified' flag.
	 */
	void mapMetadataChanged();
}
