package drivers.common;

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
		return Collections.<IMapNG>unmodifiableList(subordinateMapsList);
	}

	/**
	 * Subordinate maps and the files from which they were loaded, for use by subclasses only.
	 */
	@Override
	public final Iterable<IMutableMapNG> getRestrictedSubordinateMaps() {
		return Collections.unmodifiableList(subordinateMapsList);
	}

	public SimpleMultiMapModel(IMutableMapNG map) {
		super(map);
	}

	/**
	 * TODO: This was a named constructor ({@code copyConstructor}) in
	 * Ceylon; should we make it private here and provide a static method
	 * instead?
	 */
	public SimpleMultiMapModel(IDriverModel model) {
		super(model.getRestrictedMap());
		if (model instanceof IMultiMapModel) {
			// TODO: Condense with Iterable.forEach()?
			for (IMutableMapNG map : ((IMultiMapModel) model).getRestrictedSubordinateMaps()) {
				subordinateMapsList.add(map);
			}
		}
	}

	@Override
	public final void addSubordinateMap(IMutableMapNG map) {
		subordinateMapsList.add(map);
	}

	@Override
	@Nullable
	public final IDriverModel fromSecondMap() {
		if (subordinateMapsList.isEmpty()) {
			return null;
		} else {
			return new SimpleDriverModel(subordinateMapsList.get(0));
		}
	}

	@Override
	public final int getCurrentTurn() {
		// TODO: Once we have a streamAllMaps() method, use that to condense this (mapToInt(IMapNG::currentTurn).filter(i -> i >= 0).findFirst() or some such
		for (IMapNG map : getAllMaps()) {
			if (map.getCurrentTurn() >= 0) {
				return map.getCurrentTurn();
			}
		}
		return getRestrictedMap().getCurrentTurn();
	}

	@Override
	public final void setCurrentTurn(int currentTurn) {
		for (IMutableMapNG map : getRestrictedAllMaps()) {
			map.setCurrentTurn(currentTurn);
			map.setModified(true);
		}
	}

	@Override
	public final void setMapModified(IMapNG map, boolean flag) {
		for (IMutableMapNG subMap : getRestrictedAllMaps()) {
			if (subMap == map) {
				subMap.setModified(flag);
				return;
			}
		}
		for (IMutableMapNG subMap : getRestrictedAllMaps()) {
			if (subMap.equals(map)) {
				subMap.setModified(flag);
				return;
			}
		}
	}

	@Override
	public final void clearModifiedFlag(IMapNG map) {
		setMapModified(map, false);
	}
}
