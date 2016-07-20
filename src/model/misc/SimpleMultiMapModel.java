package model.misc;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import util.Pair;

/**
 * A superclass for implementations of interfaces inheriting from IMultiMapModel.
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
public class SimpleMultiMapModel extends SimpleDriverModel
		implements IMultiMapModel {
	/**
	 * @param map  the map we're wrapping
	 * @param file the file the map was loaded from or should be saved to
	 */
	@SuppressWarnings("UnnecessarySuperQualifier")
	public SimpleMultiMapModel(final IMutableMapNG map, final Path file) {
		super.setMap(map, file);
	}

	/**
	 * Copy constructor.]
	 *
	 * @param model a driver model
	 */
	@SuppressWarnings("UnnecessarySuperQualifier")
	public SimpleMultiMapModel(final IDriverModel model) {
		super.setMap(model.getMap(), model.getMapFile());
		if (model instanceof IMultiMapModel) {
			for (final Pair<IMutableMapNG, Path> pair : ((IMultiMapModel) model)
																.getSubordinateMaps()) {
				addSubordinateMap(pair.first(), pair.second());
			}
		}
	}

	/**
	 * The collection of subordinate maps.
	 */
	private final List<Pair<IMutableMapNG, Path>> subordinateMaps = new ArrayList<>();

	/**
	 * @param map  the subordinate map to add
	 * @param file the name of the file it was loaded from
	 */
	@Override
	public final void addSubordinateMap(final IMutableMapNG map, final Path file) {
		subordinateMaps.add(Pair.of(map, file));
	}

	/**
	 * FIXME: Test this; I fixed the clearly-wrong implementation, but this
	 * might cause ConcurrentModificationException.
	 *
	 * @param map
	 *            the subordinate map to remove
	 */
	@Override
	public final void removeSubordinateMap(final IMapNG map) {
		for (final Pair<IMutableMapNG, Path> pair : subordinateMaps) {
			if (map.equals(pair.first())) {
				subordinateMaps.remove(pair);
				return;
			}
		}
	}

	/**
	 * @return an iterator over the subordinate maps
	 */
	@Override
	public final Iterable<Pair<IMutableMapNG, Path>> getSubordinateMaps() {
		return new ArrayList<>(subordinateMaps);
	}

	/**
	 * @return an iterator over both the main map and the subordinate maps
	 */
	@Override
	public final Iterable<Pair<IMutableMapNG, Path>> getAllMaps() {
		final Collection<Pair<IMutableMapNG, Path>> retval = new ArrayList<>();
		retval.add(Pair.of(getMap(), getMapFile()));
		getSubordinateMaps().forEach(retval::add);
		return retval;
	}

	/**
	 * @return a string representation of this class
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "SimpleMultiMapModel";
	}
}
