package model.misc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import model.map.IMap;
import util.IteratorStack;
import util.IteratorWrapper;
import util.Pair;
import util.SetPairConverter;
/**
 * A superclass for implementations of interfaces inheriting from IMultiMapModel.
 * @author Jonathan Lovelace
 *
 */
//ESCA-JAVA0011:
public abstract class AbstractMultiMapModel extends AbstractDriverModel implements
		IMultiMapModel {
	/**
	 * The collection of subordinate maps.
	 */
	private final Map<IMap, String> subordinateMaps = new HashMap<IMap, String>();
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
		return new SetPairConverter<IMap, String>(subordinateMaps);
	}
	/**
	 * @return an iterator over both the main map and the subordinate maps
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Iterable<Pair<IMap, String>> getAllMaps() {
		return new IteratorWrapper<Pair<IMap, String>>(
				new IteratorStack<Pair<IMap, String>>(
						Collections.singletonList(Pair.of((IMap) getMap(),
								getMapFilename())), getSubordinateMaps()));
	}

}
