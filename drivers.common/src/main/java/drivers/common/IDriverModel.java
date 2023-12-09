package drivers.common;

import legacy.map.ILegacyMap;
import legacy.map.IMutableLegacyMap;
import legacy.map.MapDimensions;

import java.nio.file.Path;

/**
 * An interface for driver-model objects that hold a mutable map. Interfaces
 * deriving from this one will give the methods each driver needs.
 */
public interface IDriverModel extends MapChangeSource, VersionChangeSource {
	/**
	 * Set the (main) map
	 * @param newMap The new map
	 */
	void setMap(IMutableLegacyMap newMap);

	/**
	 * The (main) map.
	 */
	ILegacyMap getMap();

	/**
	 * Set the map filename.
	 */
	void setMapFilename(Path filename);

	/**
	 * The (main) map, for use by subclasses only.
	 */
	IMutableLegacyMap getRestrictedMap();

	/**
	 * The map's dimensions.
	 */
	default MapDimensions getMapDimensions() {
		return getMap().getDimensions();
	}

	/**
	 * Whether the map has been changed since it was loaded or last saved.
	 */
	default boolean isMapModified() {
		return getMap().isModified();
	}

	default void setMapModified(final boolean mapModified) {
		getRestrictedMap().setModified(mapModified);
	}

	/**
	 * The current turn for the map.
	 */
	int getCurrentTurn();

	/**
	 * The current turn for the map.
	 */
	void setCurrentTurn(int currentTurn);
}
