package model.misc;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import model.listeners.MapChangeListener;
import model.listeners.VersionChangeListener;
import model.map.IMutableMapNG;
import model.map.MapDimensions;
import model.map.PlayerCollection;
import model.map.SPMapNG;

/**
 * A superclass for driver-models, to handle the common details.
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
public class SimpleDriverModel implements IDriverModel {
	/**
	 * The list of map-change listeners.
	 */
	private final Collection<MapChangeListener> mcListeners = new ArrayList<>();
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
	private Optional<Path> file = Optional.empty();

	/**
	 * @param newMap the new map
	 * @param origin the file from which the map was loaded
	 */
	@Override
	public void setMap(final IMutableMapNG newMap, final Optional<Path> origin) {
		for (final VersionChangeListener list : vcListeners) {
			list.changeVersion(mapDim.version, newMap.dimensions().version);
		}
		map = newMap;
		mapDim = newMap.dimensions();
		file = origin;
		mcListeners.forEach(MapChangeListener::mapChanged);
	}

	/**
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
	public final Optional<Path> getMapFile() {
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
	private final Collection<VersionChangeListener> vcListeners = new ArrayList<>();

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
	 * Remove a version-change listener.
	 *
	 * @param list the listener to remove
	 */
	@Override
	public final void removeVersionChangeListener(final VersionChangeListener list) {
		vcListeners.remove(list);
	}
	/**
	 * @return a diagnostic String
	 */
	@Override
	public String toString() {
		return "SimpleDriverModel representing map in " + file;
	}
}
