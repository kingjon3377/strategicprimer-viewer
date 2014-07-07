package model.misc;

import java.io.File;

import model.listeners.MapChangeSource;
import model.listeners.VersionChangeSource;
import model.map.MapDimensions;
import model.map.MapView;

/**
 * An interface for driver-model objects that hold a mutable map. Interfaces
 * deriving from this one will give the methods each driver needs.
 *
 * @author Jonathan Lovelace
 *
 */
public interface IDriverModel extends MapChangeSource, VersionChangeSource {
	/**
	 * @param newMap the new map
	 * @param origin the file from which it was loaded
	 */
	void setMap(MapView newMap, File origin);
	/**
	 *
	 * @return the map
	 */
	MapView getMap();

	/**
	 * @return the dimensions and version of the map
	 */
	MapDimensions getMapDimensions();
	/**
	 * @return the file from which the map was loaded
	 */
	File getMapFile();
}
