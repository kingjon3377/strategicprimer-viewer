package utility;

import legacy.map.IFixture;

import java.util.Map;

import legacy.map.TileType;
import drivers.common.IDriverModel;
import drivers.common.SimpleMultiMapModel;

import java.util.Objects;
import java.util.function.Predicate;

import legacy.map.River;
import legacy.map.Direction;
import legacy.map.ILegacyMap;
import legacy.map.IMutableLegacyMap;
import legacy.map.Point;
import legacy.map.TileFixture;

public final class MapTradeModel extends SimpleMultiMapModel {
	public MapTradeModel(final IMutableLegacyMap map) {
		super(map);
	}

	public MapTradeModel(final IDriverModel model) {
		super(model);
	}

	private boolean globalModifiedFlag = false;

	private void setGlobalModifiedFlag() {
		if (!globalModifiedFlag) {
			for (final IMutableLegacyMap second : getRestrictedSubordinateMaps()) {
				second.setModified(true);
			}
			globalModifiedFlag = true;
		}
	}

	public void copyPlayers() {
		for (final IMutableLegacyMap second : getRestrictedSubordinateMaps()) {
			getMap().getPlayers().forEach(second::addPlayer);
		}
		setGlobalModifiedFlag();
	}

	public void copyBaseTerrainAt(final Point location) {
		final ILegacyMap map = getMap();
		for (final IMutableLegacyMap second : getRestrictedSubordinateMaps()) {
			final TileType terrain = map.getBaseTerrain(location);
			if (Objects.isNull(second.getBaseTerrain(location)) && !Objects.isNull(terrain)) {
				second.setBaseTerrain(location, terrain);
				setGlobalModifiedFlag();
			}
		}
	}

	public void maybeCopyFixturesAt(final Point location, final Predicate<TileFixture> condition,
									final IFixture.CopyBehavior zeroFixtures) {
		final ILegacyMap map = getMap();
		for (final IMutableLegacyMap second : getRestrictedSubordinateMaps()) {
			for (final TileFixture fixture : map.getFixtures(location)) {
				if (condition.test(fixture) && fixture.getId() >= 0 &&
						second.getFixtures(location).stream().noneMatch(f -> f.getId() == fixture.getId())) {
					second.addFixture(location, fixture.copy(zeroFixtures));
					setGlobalModifiedFlag();
				}
			}
		}
	}

	public void copyRiversAt(final Point location) {
		final ILegacyMap map = getMap();
		for (final IMutableLegacyMap second : getRestrictedSubordinateMaps()) {
			second.addRivers(location, map.getRivers(location).toArray(River[]::new));
			setGlobalModifiedFlag();
		}
	}

	public void copyRoadsAt(final Point location) {
		final ILegacyMap map = getMap();
		if (!map.getRoads(location).isEmpty()) {
			final Map<Direction, Integer> roads = map.getRoads(location);
			for (final IMutableLegacyMap second : getRestrictedSubordinateMaps()) {
				final Map<Direction, Integer> existingRoads = second.getRoads(location);
				for (final Map.Entry<Direction, Integer> entry : roads.entrySet()) {
					final Direction direction = entry.getKey();
					final int quality = entry.getValue();
					final int existingRoad = existingRoads.getOrDefault(direction, 0);
					if (existingRoad < quality) {
						second.setRoadLevel(location, direction, quality);
						setGlobalModifiedFlag();
					}
				}
			}
		}
	}
}
