package model.misc;

import java.io.File;

import model.map.IMapNG;
import model.map.IMutableMapNG;
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
	 * @param file the file it was loaded from
	 */
	void addSubordinateMap(IMutableMapNG map, File file);
	/**
	 * Remove a subordinate map.
	 *
	 * @param map the map to remove
	 */
	void removeSubordinateMap(IMapNG map);

	/**
	 * @return an iterator over the subordinate maps
	 */
	Iterable<Pair<IMutableMapNG, File>> getSubordinateMaps();
	/**
	 * @return an iterator over all the maps, including the main map and the
	 *         subordinate maps
	 */
	Iterable<Pair<IMutableMapNG, File>> getAllMaps();
}
