package model.misc;

import java.io.File;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import util.Pair;

/**
 * A driver-model for drivers that have a main map (like every driver) and any number of
 * "subordinate" maps.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface IMultiMapModel extends IDriverModel {
	/**
	 * Add a subordinate map.
	 *
	 * @param map  the map to remove
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
	 * @return an iterator over all the maps, including the main map and the subordinate
	 * maps
	 */
	Iterable<Pair<IMutableMapNG, File>> getAllMaps();
}
