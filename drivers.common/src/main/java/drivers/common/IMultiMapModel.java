package drivers.common;

import common.map.IMapNG;
import common.map.IMutableMapNG;

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
	void addSubordinateMap(IMutableMapNG map);

	/**
	 * Subordinate maps
	 */
	Iterable<IMapNG> getSubordinateMaps();

	/**
	 * Subordinate maps, as a stream.
	 */
	Stream<IMapNG> streamSubordinateMaps();

	/**
	 * Subordinate maps. For use by subclasses only.
	 */
	Iterable<IMutableMapNG> getRestrictedSubordinateMaps();

	/**
	 * All maps.
	 */
	default Iterable<IMapNG> getAllMaps() {
		return new ConcatIterable<>(Collections.singleton(getMap()), getSubordinateMaps());
	}

	default Stream<IMapNG> streamAllMaps() {
		return Stream.concat(Stream.of(getMap()), streamSubordinateMaps());
	}

	/**
	 * All maps. For use by subclasses only.
	 */
	default Iterable<IMutableMapNG> getRestrictedAllMaps() {
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
	void setMapModified(IMapNG map, boolean flag);

	/**
	 * Clear the modified flag on the given map. (For the code that saves the map to file.)
	 */
	void clearModifiedFlag(IMapNG map);
}
