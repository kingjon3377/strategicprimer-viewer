package utility;

import legacy.map.MutablePlayer;
import legacy.map.PlayerImpl;
import common.map.fixtures.towns.TownStatus;
import legacy.map.IMutableLegacyMap;
import legacy.map.IMutableLegacyPlayerCollection;
import legacy.map.LegacyMap;
import legacy.map.LegacyPlayerCollection;
import legacy.map.MapDimensions;
import legacy.map.MapDimensionsImpl;
import legacy.map.Point;
import legacy.map.TileFixture;
import legacy.map.TileType;
import legacy.map.fixtures.towns.Village;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class TestUtilityDriverModel {
	private static IMutableLegacyPlayerCollection playerCollection(final String current, final String... players) {
		final IMutableLegacyPlayerCollection retval = new LegacyPlayerCollection();
		int i = 0;
		for (final String name : players) {
			final MutablePlayer player = new PlayerImpl(i, name);
			if (name.equals(current)) {
				player.setCurrent(true);
			}
			retval.add(player);
			i++;
		}
		return retval;
	}

	private static void noop(final String str) {
		// Noop
	}

	@Test
	public void testExpansionWithSubsets() {
		final MapDimensions dims = new MapDimensionsImpl(2, 2, 2);
		final IMutableLegacyMap master = new LegacyMap(dims,
				playerCollection("independent", "main", "second", "independent"), 0);
		final IMutableLegacyMap subMap = new LegacyMap(dims,
				playerCollection("main", "main", "second", "independent"), 0);
		for (final Point point : master.getLocations()) {
			master.setBaseTerrain(point, TileType.Plains);
			subMap.setBaseTerrain(point, TileType.Plains);
		}
		final TileFixture villageOne = new Village(TownStatus.Active, "one", 0, subMap.getCurrentPlayer(), "human");
		final Village villageTwo = new Village(TownStatus.Active, "two", 1, master.getPlayers().getPlayer(1), "human");
		final TileFixture villageThree = new Village(TownStatus.Active, "two", 1, master.getCurrentPlayer(), "human");
		assertTrue(villageTwo.isSubset(villageThree, TestUtilityDriverModel::noop),
				"Independent village is subset of village with owner");
		final Point pointOne = new Point(0, 0);
		final Point pointTwo = new Point(1, 0);
		master.addFixture(pointOne, villageOne);
		subMap.addFixture(pointOne, villageOne);
		master.addFixture(pointTwo, villageTwo);
		subMap.addFixture(pointTwo, villageThree);
		final UtilityDriverModel model = new UtilityDriverModel(master);
		model.addSubordinateMap(subMap);
		model.expandAroundPoint(pointOne, subMap.getCurrentPlayer());
		assertTrue(subMap.getFixtures(pointTwo).contains(villageTwo), "Updated version of village got added");
		assertFalse(subMap.getFixtures(pointTwo).contains(villageThree), "Old version of village stuck around");
	}
}
