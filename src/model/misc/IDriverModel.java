package model.misc;

import java.nio.file.Path;
import java.util.Optional;
import model.listeners.MapChangeSource;
import model.listeners.VersionChangeSource;
import model.map.IMutableMapNG;
import model.map.MapDimensions;

/**
 * An interface for driver-model objects that hold a mutable map. Interfaces deriving from
 * this one will give the methods each driver needs.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface IDriverModel extends MapChangeSource, VersionChangeSource {
	/**
	 * Set the main map and its filename.
	 * @param newMap the new map
	 * @param origin the file from which it was loaded
	 */
	void setMap(IMutableMapNG newMap, Optional<Path> origin);

	/**
	 * Get the main map.
	 * @return the map
	 */
	IMutableMapNG getMap();

	/**
	 * Get the map's dimensions.
	 * @return the dimensions and version of the map
	 */
	MapDimensions getMapDimensions();

	/**
	 * Get the filename of the main map.
	 * @return the file from which the map was loaded
	 */
	Optional<Path> getMapFile();
}
