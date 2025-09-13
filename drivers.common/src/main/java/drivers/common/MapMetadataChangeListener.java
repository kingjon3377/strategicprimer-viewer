package drivers.common;

/**
 * An interface for listeners that want to be notified when a map's metadata change.
 *
 * @author Jonathan Lovelace
 */
@FunctionalInterface
public interface MapMetadataChangeListener {
	/**
	 * React to a change in the map's filename (without a change in the map
	 * itself) or the 'modified' flag.
	 */
	void mapMetadataChanged();
}
