package utility;

import common.map.IFixture;
import java.util.Map;
import common.map.TileType;
import drivers.common.IDriverModel;
import drivers.common.SimpleMultiMapModel;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import common.map.River;
import common.map.Direction;
import common.map.IMapNG;
import common.map.IMutableMapNG;
import common.map.Point;
import common.map.TileFixture;

public class MapTradeModel extends SimpleMultiMapModel {
	public MapTradeModel(final IMutableMapNG map) {
		super(map);
	}

	public MapTradeModel(final IDriverModel model) {
		super(model);
	}

	private boolean globalModifiedFlag = false;

	private void setGlobalModifiedFlag() {
		if (!globalModifiedFlag) {
			for (final IMutableMapNG second : getRestrictedSubordinateMaps()) {
				second.setModified(true);
			}
			globalModifiedFlag = true;
		}
	}

	public void copyPlayers() {
		for (final IMutableMapNG second : getRestrictedSubordinateMaps()) {
			getMap().getPlayers().forEach(second::addPlayer);
		}
		setGlobalModifiedFlag();
	}

	public void copyBaseTerrainAt(final Point location) {
		final IMapNG map = getMap();
		for (final IMutableMapNG second : getRestrictedSubordinateMaps()) {
			final TileType terrain = map.getBaseTerrain(location);
			if (second.getBaseTerrain(location) == null && terrain != null) {
				second.setBaseTerrain(location, terrain);
				setGlobalModifiedFlag();
			}
		}
	}

	public void maybeCopyFixturesAt(final Point location, final Predicate<TileFixture> condition,
	                                final IFixture.CopyBehavior zeroFixtures) {
		final IMapNG map = getMap();
		for (final IMutableMapNG second : getRestrictedSubordinateMaps()) {
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
		final IMapNG map = getMap();
		for (final IMutableMapNG second : getRestrictedSubordinateMaps()) {
			second.addRivers(location, map.getRivers(location).toArray(new River[0]));
			setGlobalModifiedFlag();
		}
	}

	public void copyRoadsAt(final Point location) {
		final IMapNG map = getMap();
		if (!map.getRoads(location).isEmpty()) {
			final Map<Direction, Integer> roads = map.getRoads(location);
			for (final IMutableMapNG second : getRestrictedSubordinateMaps()) {
				final Map<Direction, Integer> existingRoads = second.getRoads(location);
				for (final Map.Entry<Direction, Integer> entry : roads.entrySet()) {
					final Direction direction = entry.getKey();
					final int quality = entry.getValue();
					final int existingRoad = existingRoads.getOrDefault(direction, 0);
					if (existingRoad >= quality) {
						continue;
					} else {
						second.setRoadLevel(location, direction, quality);
						setGlobalModifiedFlag();
					}
				}
			}
		}
	}
}
