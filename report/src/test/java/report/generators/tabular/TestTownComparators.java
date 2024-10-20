package report.generators.tabular;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import legacy.map.Player;
import legacy.map.PlayerImpl;
import common.map.fixtures.towns.TownStatus;
import common.map.fixtures.towns.TownSize;
import legacy.map.fixtures.towns.FortressImpl;
import legacy.map.fixtures.towns.ITownFixture;
import legacy.map.fixtures.towns.Village;
import legacy.map.fixtures.towns.City;
import legacy.map.fixtures.towns.Town;
import legacy.map.fixtures.towns.Fortification;

import java.util.Collections;
import java.util.Arrays;

/**
 * Test for {@link TownComparators}.
 */
public class TestTownComparators {
	/**
	 * Test that the town-comparison algorithms work as expected.
	 */
	@SuppressWarnings("ValueOfIncrementOrDecrementUsed")
	@Test
	public void testComparison() {
		// TODO: How can we condense this initialization?
		final List<ITownFixture> input = new ArrayList<>();
		int id = 0;
		final Player owner = new PlayerImpl(1, "player");
		for (final TownStatus status : TownStatus.values()) {
			for (final TownSize size : TownSize.values()) {
				input.add(new Town(status, size, -1, "inputTown", id++, owner));
				input.add(new City(status, size, -1, "inputCity", id++, owner));
				input.add(new Fortification(status, size, -1, "inputFortification",
						id++, owner));
			}
			input.add(new Village(status, "inputVillage", id++, owner, "inputRace"));
		}
		input.add(new FortressImpl(owner, "inputFortress", id++, TownSize.Small));
		input.add(new City(TownStatus.Active, TownSize.Large, -1, "inputCityTwo", id++, owner));
		input.add(new FortressImpl(owner, "inputFortressTwo", id++, TownSize.Small));
		input.add(new Town(TownStatus.Ruined, TownSize.Medium, -1, "inputTownTwo", id++, owner));
		input.add(new Fortification(TownStatus.Burned, TownSize.Small, -1,
				"inputFortificationTwo", id++, owner));
		input.add(new Village(TownStatus.Abandoned, "inputVillageTwo", id++, owner, "inputRace"));
		input.add(new FortressImpl(owner, "inputFortressThree", id++, TownSize.Medium));
		input.add(new FortressImpl(owner, "inputFortressFour", id, TownSize.Large));
		final List<ITownFixture> shuffled = new ArrayList<>(input);
		Collections.shuffle(shuffled);
		final List<ITownFixture> expected = Arrays.asList(
				new FortressImpl(owner, "inputFortressFour", 47, TownSize.Large),
				new City(TownStatus.Active, TownSize.Large, -1, "inputCity", 7, owner),
				new City(TownStatus.Active, TownSize.Large, -1, "inputCityTwo", 41, owner),
				new Town(TownStatus.Active, TownSize.Large, -1, "inputTown", 6, owner),
				new Fortification(TownStatus.Active, TownSize.Large, -1, "inputFortification", 8,
						owner),
				new FortressImpl(owner, "inputFortressThree", 46, TownSize.Medium),
				new City(TownStatus.Active, TownSize.Medium, -1, "inputCity", 4, owner),
				new Town(TownStatus.Active, TownSize.Medium, -1, "inputTown", 3, owner),
				new Fortification(TownStatus.Active, TownSize.Medium, -1, "inputFortification", 5,
						owner),
				new FortressImpl(owner, "inputFortress", 40, TownSize.Small),
				new FortressImpl(owner, "inputFortressTwo", 42, TownSize.Small),
				new City(TownStatus.Active, TownSize.Small, -1, "inputCity", 1, owner),
				new Town(TownStatus.Active, TownSize.Small, -1, "inputTown", 0, owner),
				new Fortification(TownStatus.Active, TownSize.Small, -1, "inputFortification", 2,
						owner),
				new Village(TownStatus.Active, "inputVillage", 9, owner, "inputRace"),
				new City(TownStatus.Abandoned, TownSize.Large, -1, "inputCity", 17, owner),
				new Town(TownStatus.Abandoned, TownSize.Large, -1, "inputTown", 16, owner),
				new Fortification(TownStatus.Abandoned, TownSize.Large, -1, "inputFortification", 18,
						owner),
				new City(TownStatus.Abandoned, TownSize.Medium, -1, "inputCity", 14, owner),
				new Town(TownStatus.Abandoned, TownSize.Medium, -1, "inputTown", 13, owner),
				new Fortification(TownStatus.Abandoned, TownSize.Medium, -1, "inputFortification", 15,
						owner),
				new City(TownStatus.Abandoned, TownSize.Small, -1, "inputCity", 11, owner),
				new Town(TownStatus.Abandoned, TownSize.Small, -1, "inputTown", 10, owner),
				new Fortification(TownStatus.Abandoned, TownSize.Small, -1, "inputFortification", 12,
						owner),
				new Village(TownStatus.Abandoned, "inputVillage", 19, owner, "inputRace"),
				new Village(TownStatus.Abandoned, "inputVillageTwo", 45, owner, "inputRace"),
				new City(TownStatus.Ruined, TownSize.Large, -1, "inputCity", 27, owner),
				new Town(TownStatus.Ruined, TownSize.Large, -1, "inputTown", 26, owner),
				new Fortification(TownStatus.Ruined, TownSize.Large, -1, "inputFortification", 28,
						owner),
				new City(TownStatus.Ruined, TownSize.Medium, -1, "inputCity", 24, owner),
				new Town(TownStatus.Ruined, TownSize.Medium, -1, "inputTown", 23, owner),
				new Town(TownStatus.Ruined, TownSize.Medium, -1, "inputTownTwo", 43, owner),
				new Fortification(TownStatus.Ruined, TownSize.Medium, -1, "inputFortification", 25,
						owner),
				new City(TownStatus.Ruined, TownSize.Small, -1, "inputCity", 21, owner),
				new Town(TownStatus.Ruined, TownSize.Small, -1, "inputTown", 20, owner),
				new Fortification(TownStatus.Ruined, TownSize.Small, -1, "inputFortification", 22,
						owner),
				new Village(TownStatus.Ruined, "inputVillage", 29, owner, "inputRace"),
				new City(TownStatus.Burned, TownSize.Large, -1, "inputCity", 37, owner),
				new Town(TownStatus.Burned, TownSize.Large, -1, "inputTown", 36, owner),
				new Fortification(TownStatus.Burned, TownSize.Large, -1, "inputFortification", 38,
						owner),
				new City(TownStatus.Burned, TownSize.Medium, -1, "inputCity", 34, owner),
				new Town(TownStatus.Burned, TownSize.Medium, -1, "inputTown", 33, owner),
				new Fortification(TownStatus.Burned, TownSize.Medium, -1, "inputFortification", 35,
						owner),
				new City(TownStatus.Burned, TownSize.Small, -1, "inputCity", 31, owner),
				new Town(TownStatus.Burned, TownSize.Small, -1, "inputTown", 30, owner),
				new Fortification(TownStatus.Burned, TownSize.Small, -1, "inputFortification", 32,
						owner),
				new Fortification(TownStatus.Burned, TownSize.Small, -1, "inputFortificationTwo", 44,
						owner),
				new Village(TownStatus.Burned, "inputVillage", 39, owner, "inputRace"));
		shuffled.sort(TownComparators::compareTowns);
//		for (int i = 0; i < shuffled.size(); i++) {
//			assertEquals(expected.get(i), shuffled.get(i),
//				"%dth element in sorted list of towns is as expected".formatted(i));
//		}
		assertEquals(expected, shuffled, "Sorted list of towns is in the order we expect");
	}
}
