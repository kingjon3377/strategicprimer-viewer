package model.misc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import model.listeners.MapChangeListener;
import model.listeners.VersionChangeListener;
import model.map.IMutableMapNG;
import model.map.MapDimensions;
import model.map.PlayerCollection;
import model.map.SPMapNG;

/**
 * A superclass for driver-models, to handle the common details.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
// ESCA-JAVA0011:
public abstract class AbstractDriverModel implements IDriverModel {
	/**
	 * The list of map-change listeners.
	 */
	private final List<MapChangeListener> mcListeners = new ArrayList<>();
	/**
	 * The dimensions of the map.
	 */
	private MapDimensions mapDim = new MapDimensions(-1, -1, -1);
	/**
	 * The main map.
	 */
	private IMutableMapNG map = new SPMapNG(mapDim, new PlayerCollection(), -1);
	/**
	 * The name from which the map was loaded.
	 */
	private File file = new File("");
	/**
	 * @param newMap the new map
	 * @param origin the file from which the map was loaded
	 */
	@Override
	public void setMap(final IMutableMapNG newMap, final File origin) {
		for (final VersionChangeListener list : vcListeners) {
			list.changeVersion(mapDim.version, newMap.dimensions().version);
		}
		map = newMap;
		mapDim = newMap.dimensions();
		file = origin;
		for (final MapChangeListener list : mcListeners) {
			list.mapChanged();
		}
	}

	/**
	 *
	 * @return the main map
	 */
	@Override
	public final IMutableMapNG getMap() {
		return map;
	}

	/**
	 * @return the dimensions and version of the map
	 */
	@Override
	public final MapDimensions getMapDimensions() {
		return mapDim;
	}
	/**
	 * @return the file from which the map was loaded
	 */
	@Override
	public final File getMapFile() {
		return file;
	}
	/**
	 * Add a MapChangeListener.
	 *
	 * @param list the listener to add
	 */
	@Override
	public final void addMapChangeListener(final MapChangeListener list) {
		mcListeners.add(list);
	}

	/**
	 * Remove a MapChangeListener.
	 *
	 * @param list the listener to remove
	 */
	@Override
	public final void removeMapChangeListener(final MapChangeListener list) {
		mcListeners.remove(list);
	}

	/**
	 * The list of version change listeners.
	 */
	private final List<VersionChangeListener> vcListeners = new ArrayList<>();

	/**
	 * Add a version change listener.
	 *
	 * @param list the listener to add
	 */
	@Override
	public final void addVersionChangeListener(final VersionChangeListener list) {
		vcListeners.add(list);
	}

	/**
	 * Removve a version-change listener.
	 *
	 * @param list the listener to remove
	 */
	@Override
	public final void removeVersionChangeListener(
			final VersionChangeListener list) {
		vcListeners.remove(list);
	}
}
