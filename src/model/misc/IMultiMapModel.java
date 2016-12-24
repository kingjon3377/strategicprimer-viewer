package model.misc;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
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
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
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
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void addSubordinateMap(IMutableMapNG map, Optional<Path> file);

	/**
	 * Remove a subordinate map.
	 *
	 * @param map the map to remove
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void removeSubordinateMap(IMapNG map);

	/**
	 * Subordinate maps with their filenames.
	 * @return an iterator over the subordinate maps
	 */
	Iterable<Pair<IMutableMapNG, Optional<Path>>> getSubordinateMaps();

	/**
	 * All maps with their filenames.
	 * @return an iterator over all the maps, including the main map and the subordinate
	 * maps
	 */
	Iterable<Pair<IMutableMapNG, Optional<Path>>> getAllMaps();
	/**
	 * A stream of subordinate maps with their filenames.
	 * @return a stream over the subordinate maps and their files
	 */
	default Stream<Pair<IMutableMapNG, Optional<Path>>> streamSubordinateMaps() {
		return StreamSupport.stream(getSubordinateMaps().spliterator(), false);
	}
	/**
	 * A stream of all maps and their filenames.
	 * @return a stream over all the maps and their filenames
	 */
	default Stream<Pair<IMutableMapNG, Optional<Path>>> streamAllMaps() {
		return StreamSupport.stream(getAllMaps().spliterator(), false);
	}
}
