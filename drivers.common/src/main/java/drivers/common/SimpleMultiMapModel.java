package drivers.common;

import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import common.map.IMapNG;
import common.map.IMutableMapNG;

/**
 * A superclass for implementations of interfaces inheriting from {@link IMultiMapModel}.
 */
public class SimpleMultiMapModel extends SimpleDriverModel implements IMultiMapModel {
	/**
	 * The collection of subordinate maps.
	 */
	private final List<IMutableMapNG> subordinateMapsList = new ArrayList<>();

	/**
	 * Subordinate maps and the files from which they were loaded.
	 */
	@Override
	public final Iterable<IMapNG> getSubordinateMaps() {
		return Collections.unmodifiableList(subordinateMapsList);
	}

	/**
	 * Subordinate maps, as a stream.
	 */
	@Override
	public Stream<IMapNG> streamSubordinateMaps() {
		return subordinateMapsList.stream().map(IMapNG.class::cast);
	}

	/**
	 * Subordinate maps and the files from which they were loaded, for use by subclasses only.
	 */
	@Override
	public final Iterable<IMutableMapNG> getRestrictedSubordinateMaps() {
		return Collections.unmodifiableList(subordinateMapsList);
	}

	public SimpleMultiMapModel(final IMutableMapNG map) {
		super(map);
	}

	/**
	 * TODO: This was a named constructor ({@code copyConstructor}) in
	 * Ceylon; should we make it private here and provide a static method
	 * instead?
	 */
	public SimpleMultiMapModel(final IDriverModel model) {
		super(model.getRestrictedMap());
		if (model instanceof IMultiMapModel mmm) {
			mmm.getRestrictedSubordinateMaps().forEach(subordinateMapsList::add);
		}
	}

	@Override
	public final void addSubordinateMap(final IMutableMapNG map) {
		subordinateMapsList.add(map);
	}

	@Override
	public final @Nullable IDriverModel fromSecondMap() {
		if (subordinateMapsList.isEmpty()) {
			return null;
		} else {
			return new SimpleDriverModel(subordinateMapsList.get(0));
		}
	}

	@Override
	public final int getCurrentTurn() {
		return streamAllMaps().mapToInt(IMapNG::getCurrentTurn).filter(i -> i >= 0)
			.findFirst().orElseGet(getMap()::getCurrentTurn);
	}

	@Override
	public final void setCurrentTurn(final int currentTurn) {
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			map.setCurrentTurn(currentTurn);
			map.setModified(true);
		}
	}

	@Override
	public final void setMapModified(final IMapNG map, final boolean flag) {
		for (final IMutableMapNG subMap : getRestrictedAllMaps()) {
			if (subMap == map) {
				subMap.setModified(flag);
				return;
			}
		}
		for (final IMutableMapNG subMap : getRestrictedAllMaps()) {
			if (subMap.equals(map)) {
				subMap.setModified(flag);
				return;
			}
		}
	}

	@Override
	public final void clearModifiedFlag(final IMapNG map) {
		setMapModified(map, false);
	}
}
