package drivers.common;

import legacy.map.ILegacyMap;
import legacy.map.IMutableLegacyMap;

import java.util.stream.Stream;

import lovelace.util.ConcatIterable;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;

/**
 * A driver-model for drivers that have a main map (like every driver) and any
 * number of "subordinate" maps.
 */
public interface IMultiMapModel extends IDriverModel {
	/**
	 * Add a subordinate map.
	 * @param map The map to add
	 */
	void addSubordinateMap(IMutableLegacyMap map);

	/**
	 * Subordinate maps
	 */
	Iterable<ILegacyMap> getSubordinateMaps();

	/**
	 * Subordinate maps, as a stream.
	 */
	Stream<ILegacyMap> streamSubordinateMaps();

	/**
	 * Subordinate maps. For use by subclasses only.
	 */
	Iterable<IMutableLegacyMap> getRestrictedSubordinateMaps();

	/**
	 * All maps.
	 */
	default Iterable<ILegacyMap> getAllMaps() {
		return new ConcatIterable<>(Collections.singleton(getMap()), getSubordinateMaps());
	}

	default Stream<ILegacyMap> streamAllMaps() {
		return Stream.concat(Stream.of(getMap()), streamSubordinateMaps());
	}

	/**
	 * All maps. For use by subclasses only.
	 */
	default Iterable<IMutableLegacyMap> getRestrictedAllMaps() {
		return new ConcatIterable<>(Collections.singleton(getRestrictedMap()),
			getRestrictedSubordinateMaps());
	}

	/**
	 * A driver model with the second map as its first or sole map.
	 */
	@Nullable
	IDriverModel fromSecondMap();

	/**
	 * Set the modified flag on the given map.
	 *
	 * @deprecated Modification to the map should ideally only come through model methods
	 */
	@Deprecated
	void setMapModified(ILegacyMap map, boolean flag);

	/**
	 * Clear the modified flag on the given map. (For the code that saves the map to file.)
	 */
	void clearModifiedFlag(ILegacyMap map);
}
