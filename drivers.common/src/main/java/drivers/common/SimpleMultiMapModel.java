package drivers.common;

import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import legacy.map.ILegacyMap;
import legacy.map.IMutableLegacyMap;

/**
 * A superclass for implementations of interfaces inheriting from {@link IMultiMapModel}.
 */
public class SimpleMultiMapModel extends SimpleDriverModel implements IMultiMapModel {
	/**
	 * The collection of subordinate maps.
	 */
	private final List<IMutableLegacyMap> subordinateMapsList = new ArrayList<>();

	/**
	 * Subordinate maps and the files from which they were loaded.
	 */
	@Override
	public final Iterable<ILegacyMap> getSubordinateMaps() {
		return Collections.unmodifiableList(subordinateMapsList);
	}

	/**
	 * Subordinate maps, as a stream.
	 */
	@Override
	public final Stream<ILegacyMap> streamSubordinateMaps() {
		return subordinateMapsList.stream().map(ILegacyMap.class::cast);
	}

	/**
	 * Subordinate maps and the files from which they were loaded, for use by subclasses only.
	 */
	@Override
	public final Iterable<IMutableLegacyMap> getRestrictedSubordinateMaps() {
		return Collections.unmodifiableList(subordinateMapsList);
	}

	public SimpleMultiMapModel(final IMutableLegacyMap map) {
		super(map);
	}

	/**
	 * TODO: This was a named constructor ({@code copyConstructor}) in
	 * Ceylon; should we make it private here and provide a static method
	 * instead?
	 */
	public SimpleMultiMapModel(final IDriverModel model) {
		super(model.getRestrictedMap());
		if (model instanceof final IMultiMapModel mmm) {
			mmm.getRestrictedSubordinateMaps().forEach(subordinateMapsList::add);
		}
	}

	@Override
	public final void addSubordinateMap(final IMutableLegacyMap map) {
		subordinateMapsList.add(map);
	}

	@Override
	public final @Nullable IDriverModel fromSecondMap() {
		if (subordinateMapsList.isEmpty()) {
			return null;
		} else {
			return new SimpleDriverModel(subordinateMapsList.getFirst());
		}
	}

	@Override
	public final int getCurrentTurn() {
		return streamAllMaps().mapToInt(ILegacyMap::getCurrentTurn).filter(i -> i >= 0)
				.findFirst().orElseGet(getMap()::getCurrentTurn);
	}

	@Override
	public final void setCurrentTurn(final int currentTurn) {
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			map.setCurrentTurn(currentTurn);
			map.setStatus(ILegacyMap.ModificationStatus.Modified);
		}
	}

	@Override
	public final void setMapStatus(final ILegacyMap map, final ILegacyMap.ModificationStatus flag) {
		for (final IMutableLegacyMap subMap : getRestrictedAllMaps()) {
			if (subMap == map) {
				subMap.setStatus(flag);
				return;
			}
		}
		for (final IMutableLegacyMap subMap : getRestrictedAllMaps()) {
			if (subMap.equals(map)) {
				subMap.setStatus(flag);
				return;
			}
		}
	}

	@Override
	public final void clearModifiedFlag(final ILegacyMap map) {
		setMapStatus(map, ILegacyMap.ModificationStatus.Unmodified);
	}
}
