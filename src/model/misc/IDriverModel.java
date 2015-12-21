package model.misc;

import model.listeners.MapChangeSource;
import model.listeners.VersionChangeSource;
import model.map.IMutableMapNG;
import model.map.MapDimensions;

import java.io.File;

/**
 * An interface for driver-model objects that hold a mutable map. Interfaces deriving from
 * this one will give the methods each driver needs.
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
 *
 *         TODO: Maybe (to accomodate MapChecker et al) store format errors in the driver
 *         model?
 */
public interface IDriverModel extends MapChangeSource, VersionChangeSource {
	/**
	 * @param newMap the new map
	 * @param origin the file from which it was loaded
	 */
	void setMap(IMutableMapNG newMap, File origin);

	/**
	 * @return the map
	 */
	IMutableMapNG getMap();

	/**
	 * @return the dimensions and version of the map
	 */
	MapDimensions getMapDimensions();

	/**
	 * @return the file from which the map was loaded
	 */
	File getMapFile();
}
