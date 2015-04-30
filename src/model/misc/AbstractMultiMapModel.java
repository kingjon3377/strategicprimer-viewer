package model.misc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.map.IMapNG;
import model.map.IMutableMapNG;
import util.Pair;
import util.SetPairConverter;

/**
 * A superclass for implementations of interfaces inheriting from
 * IMultiMapModel.
 *
 * @author Jonathan Lovelace
 *
 */
// ESCA-JAVA0011:
public abstract class AbstractMultiMapModel extends AbstractDriverModel
		implements IMultiMapModel {
	/**
	 * The collection of subordinate maps.
	 */
	private final Map<IMutableMapNG, File> subordinateMaps = new HashMap<>();
	/**
	 * @param map the subordinate map to add
	 * @param file the name of the file it was loaded from
	 */
	@Override
	public final void addSubordinateMap(final IMutableMapNG map, final File file) {
		subordinateMaps.put(map, file);
	}

	/**
	 * @param map the subordinate map to remove
	 */
	@Override
	public final void removeSubordinateMap(final IMapNG map) {
		subordinateMaps.remove(map);
	}

	/**
	 * @return an iterator over the subordinate maps
	 */
	@Override
	public final Iterable<Pair<IMutableMapNG, File>> getSubordinateMaps() {
		return new SetPairConverter<>(subordinateMaps);
	}

	/**
	 * @return an iterator over both the main map and the subordinate maps
	 */
	@Override
	public final Iterable<Pair<IMutableMapNG, File>> getAllMaps() {
		final List<Pair<IMutableMapNG, File>> retval = new ArrayList<>();
		retval.add(Pair.of(getMap(), getMapFile()));
		for (final Pair<IMutableMapNG, File> pair : getSubordinateMaps()) {
			retval.add(pair);
		}
		return retval;
	}

}
