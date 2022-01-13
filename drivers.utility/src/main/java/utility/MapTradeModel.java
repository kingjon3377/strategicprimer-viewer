package utility;

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
	public MapTradeModel(IMutableMapNG map) {
		super(map);
	}

	public MapTradeModel(IDriverModel model) {
		super(model);
	}

	private boolean globalModifiedFlag = false;

	private void setGlobalModifiedFlag() {
		if (!globalModifiedFlag) {
			for (IMutableMapNG second : getRestrictedSubordinateMaps()) {
				second.setModified(true);
			}
			globalModifiedFlag = true;
		}
	}

	public void copyPlayers() {
		for (IMutableMapNG second : getRestrictedSubordinateMaps()) {
			getMap().getPlayers().forEach(second::addPlayer);
		}
		setGlobalModifiedFlag();
	}

	public void copyBaseTerrainAt(Point location) {
		IMapNG map = getMap();
		for (IMutableMapNG second : getRestrictedSubordinateMaps()) {
			TileType terrain = map.getBaseTerrain(location);
			if (second.getBaseTerrain(location) == null && terrain != null) {
				second.setBaseTerrain(location, terrain);
				setGlobalModifiedFlag();
			}
		}
	}

	public void maybeCopyFixturesAt(Point location, Predicate<TileFixture> condition,
			boolean zeroFixtures) {
		IMapNG map = getMap();
		for (IMutableMapNG second : getRestrictedSubordinateMaps()) {
			for (TileFixture fixture : map.getFixtures(location).stream().filter(condition)
					.collect(Collectors.toList())) {
				if (fixture.getId() >= 0 &&
						second.getFixtures(location).stream()
							.noneMatch(f -> f.getId() == fixture.getId())) {
					second.addFixture(location, fixture.copy(zeroFixtures));
					setGlobalModifiedFlag();
				}
			}
		}
	}

	public void copyRiversAt(Point location) {
		IMapNG map = getMap();
		for (IMutableMapNG second : getRestrictedSubordinateMaps()) {
			second.addRivers(location, map.getRivers(location).stream().toArray(River[]::new));
			setGlobalModifiedFlag();
		}
	}

	public void copyRoadsAt(Point location) {
		IMapNG map = getMap();
		if (!map.getRoads(location).isEmpty()) {
			Map<Direction, Integer> roads = map.getRoads(location);
			for (IMutableMapNG second : getRestrictedSubordinateMaps()) {
				Map<Direction, Integer> existingRoads = second.getRoads(location);
				for (Map.Entry<Direction, Integer> entry : roads.entrySet()) {
					Direction direction = entry.getKey();
					int quality = entry.getValue();
					int existingRoad = existingRoads.getOrDefault(direction, 0);
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
