package model.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.map.IMap;
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
	private final Map<IMap, String> subordinateMaps = new HashMap<>();

	/**
	 * @param map the subordinate map to add
	 * @param filename the name of the file it was loaded from
	 */
	@Override
	public void addSubordinateMap(final IMap map, final String filename) {
		subordinateMaps.put(map, filename);
	}

	/**
	 * @param map the subordinate map to remove
	 */
	@Override
	public void removeSubordinateMap(final IMap map) {
		subordinateMaps.remove(map);
	}

	/**
	 * @return an iterator over the subordinate maps
	 */
	@Override
	public Iterable<Pair<IMap, String>> getSubordinateMaps() {
		return new SetPairConverter<>(subordinateMaps);
	}

	/**
	 * @return an iterator over both the main map and the subordinate maps
	 */
	// @SuppressWarnings("unchecked")
	@Override
	public Iterable<Pair<IMap, String>> getAllMaps() {
		final List<Pair<IMap, String>> retval = new ArrayList<>();
		retval.add(Pair.of((IMap) getMap(), getMapFilename()));
		for (Pair<IMap, String> pair : getSubordinateMaps()) {
			retval.add(pair);
		}
		return retval;
		// return new IteratorWrapper<>(
		// new IteratorStack<>(
		// Collections.singletonList(Pair.of((IMap) getMap(),
		// getMapFilename())), getSubordinateMaps()));
	}

}
