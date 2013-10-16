package model.misc;

import model.map.IMap;
import util.Pair;

/**
 * A driver-model for drivers that have a main map (like every driver) and any
 * number of "subordinate" maps.
 *
 * @author Jonathan Lovelace
 *
 */
public interface IMultiMapModel extends IDriverModel {
	/**
	 * Add a subordinate map.
	 *
	 * @param map the map to remove
	 * @param filename the file it was loaded from
	 */
	void addSubordinateMap(IMap map, String filename);

	/**
	 * Remove a subordinate map.
	 *
	 * @param map the map to remove
	 */
	void removeSubordinateMap(IMap map);

	/**
	 * @return an iterator over the subordinate maps
	 */
	Iterable<Pair<IMap, String>> getSubordinateMaps();

	/**
	 * @return an iterator over all the maps, including the main map and the
	 *         subordinate maps
	 */
	Iterable<Pair<IMap, String>> getAllMaps();
}
