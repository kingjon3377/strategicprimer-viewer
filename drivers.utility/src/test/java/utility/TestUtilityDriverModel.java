package utility;

import common.map.IMutableMapNG;
import common.map.IMutablePlayerCollection;
import common.map.MapDimensions;
import common.map.MapDimensionsImpl;
import common.map.MutablePlayer;
import common.map.Player;
import common.map.PlayerCollection;
import common.map.PlayerImpl;
import common.map.Point;
import common.map.SPMapNG;
import common.map.TileType;
import common.map.fixtures.towns.TownStatus;
import common.map.fixtures.towns.Village;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUtilityDriverModel {
	private static IMutablePlayerCollection playerCollection(String current, String... players) {
		final IMutablePlayerCollection retval = new PlayerCollection();
		int i = 0;
		for (String name : players) {
			final MutablePlayer player = new PlayerImpl(i, name);
			if (name.equals(current)) {
				player.setCurrent(true);
			}
			retval.add(player);
			i++;
		}
		return retval;
	}

	private static void noop(String str) {
		// Noop
	}

	@Test
	public void testExpansionWithSubsets() {
		final MapDimensions dims = new MapDimensionsImpl(2, 2, 2);
		final IMutableMapNG master = new SPMapNG(dims, playerCollection("independent", "main", "second", "independent"), 0);
		final IMutableMapNG subMap = new SPMapNG(dims, playerCollection("main", "main", "second", "independent"), 0);
		for (final Point point : master.getLocations()) {
			master.setBaseTerrain(point, TileType.Plains);
			subMap.setBaseTerrain(point, TileType.Plains);
		}
		final Village villageOne = new Village(TownStatus.Active, "one", 0, subMap.getCurrentPlayer(), "human");
		final Village villageTwo = new Village(TownStatus.Active, "two", 1, master.getPlayers().getPlayer(1), "human");
		final Village villageThree = new Village(TownStatus.Active, "two", 1, master.getCurrentPlayer(), "human");
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
