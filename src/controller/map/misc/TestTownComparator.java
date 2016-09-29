package controller.map.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import model.map.Player;
import model.map.fixtures.towns.City;
import model.map.fixtures.towns.Fortification;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.ITownFixture;
import model.map.fixtures.towns.Town;
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.TownStatus;
import model.map.fixtures.towns.Village;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for the TownComparator class.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class TestTownComparator {
	/**
	 * Test that the comparator works as expected.
	 */
	@SuppressWarnings({"ValueOfIncrementOrDecrementUsed", "ObjectAllocationInLoop"})
	@Test
	public void testComparison() {
		final List<ITownFixture> input = new ArrayList<>();
		int id = 0;
		final Player owner = new Player(1, "player");
		for (final TownStatus status : TownStatus.values()) {
			for (final TownSize size : TownSize.values()) {
				input.add(new Town(status, size, -1, "inputTown", id++, owner));
				input.add(new City(status, size, -1, "inputCity", id++, owner));
				input.add(new Fortification(status, size, -1, "inputFortification", id++,
												   owner));
			}
			input.add(new Village(status, "inputVillage", id++, owner, "inputRace"));
		}
		// TODO: Cover other sizes of fortress
		input.add(new Fortress(owner, "inputFortress", id++, TownSize.Small));
		input.add(new City(TownStatus.Active, TownSize.Large, -1, "inputCityTwo", id++,
								  owner));
		input.add(new Fortress(owner, "inputFortressTwo", id++, TownSize.Small));
		input.add(new Town(TownStatus.Ruined, TownSize.Medium, -1, "inputTownTwo", id++,
								  owner));
		input.add(new Fortification(TownStatus.Burned, TownSize.Small, -1,
										   "inputFortificationTwo", id++, owner));
		input.add(new Village(TownStatus.Abandoned, "inputVillageTwo", id, owner,
									 "inputRace"));
		Collections.shuffle(input);
		final List<ITownFixture> expected = Arrays.asList(
				new City(TownStatus.Active, TownSize.Large, -1, "inputCity", 7, owner),
				new City(TownStatus.Active, TownSize.Large, -1, "inputCityTwo", 41, owner),
				new Town(TownStatus.Active, TownSize.Large, -1, "inputTown", 6, owner),
				new Fortification(TownStatus.Active, TownSize.Large, -1,
										 "inputFortification", 8, owner),
				new City(TownStatus.Active, TownSize.Medium, -1, "inputCity", 4, owner),
				new Town(TownStatus.Active, TownSize.Medium, -1, "inputTown", 3, owner),
				new Fortification(TownStatus.Active, TownSize.Medium, -1,
										 "inputFortification", 5, owner),
				new Fortress(owner, "inputFortress", 40, TownSize.Small),
				new Fortress(owner, "inputFortressTwo", 42, TownSize.Small),
				new City(TownStatus.Active, TownSize.Small, -1, "inputCity", 1, owner),
				new Town(TownStatus.Active, TownSize.Small, -1, "inputTown", 0, owner),
				new Fortification(TownStatus.Active, TownSize.Small, -1,
										 "inputFortification", 2, owner),
				new Village(TownStatus.Active, "inputVillage", 9, owner, "inputRace"),
				new City(TownStatus.Abandoned, TownSize.Large, -1, "inputCity", 17,
								owner),
				new Town(TownStatus.Abandoned, TownSize.Large, -1, "inputTown", 16,
								owner),
				new Fortification(TownStatus.Abandoned, TownSize.Large, -1,
										 "inputFortification", 18, owner),
				new City(TownStatus.Abandoned, TownSize.Medium, -1, "inputCity", 14,
								owner),
				new Town(TownStatus.Abandoned, TownSize.Medium, -1, "inputTown", 13,
								owner),
				new Fortification(TownStatus.Abandoned, TownSize.Medium, -1,
										 "inputFortification", 15, owner),
				new City(TownStatus.Abandoned, TownSize.Small, -1, "inputCity", 11,
								owner),
				new Town(TownStatus.Abandoned, TownSize.Small, -1, "inputTown", 10,
								owner),
				new Fortification(TownStatus.Abandoned, TownSize.Small, -1,
										 "inputFortification", 12, owner),
				new Village(TownStatus.Abandoned, "inputVillage", 19, owner,
								   "inputRace"),
				new Village(TownStatus.Abandoned, "inputVillageTwo", 45, owner,
								   "inputRace"),
				new City(TownStatus.Ruined, TownSize.Large, -1, "inputCity", 37, owner),
				new Town(TownStatus.Ruined, TownSize.Large, -1, "inputTown", 36, owner),
				new Fortification(TownStatus.Ruined, TownSize.Large, -1,
										 "inputFortification", 38, owner),
				new City(TownStatus.Ruined, TownSize.Medium, -1, "inputCity", 34, owner),
				new Town(TownStatus.Ruined, TownSize.Medium, -1, "inputTown", 33, owner),
				new Town(TownStatus.Ruined, TownSize.Medium, -1, "inputTownTwo", 43, owner),
				new Fortification(TownStatus.Ruined, TownSize.Medium, -1,
										 "inputFortification", 35, owner),
				new City(TownStatus.Ruined, TownSize.Small, -1, "inputCity", 31, owner),
				new Town(TownStatus.Ruined, TownSize.Small, -1, "inputTown", 30, owner),
				new Fortification(TownStatus.Ruined, TownSize.Small, -1,
										 "inputFortification", 32, owner),
				new Village(TownStatus.Ruined, "inputVillage", 39, owner, "inputRace"),
				new City(TownStatus.Burned, TownSize.Large, -1, "inputCity", 27, owner),
				new Town(TownStatus.Burned, TownSize.Large, -1, "inputTown", 26, owner),
				new Fortification(TownStatus.Burned, TownSize.Large, -1,
										 "inputFortification", 28, owner),
				new City(TownStatus.Burned, TownSize.Medium, -1, "inputCity", 24, owner),
				new Town(TownStatus.Burned, TownSize.Medium, -1, "inputTown", 23, owner),
				new Fortification(TownStatus.Burned, TownSize.Medium, -1,
										 "inputFortification", 25, owner),
				new City(TownStatus.Burned, TownSize.Small, -1, "inputCity", 21, owner),
				new Town(TownStatus.Burned, TownSize.Small, -1, "inputTown", 20, owner),
				new Fortification(TownStatus.Burned, TownSize.Small, -1,
										 "inputFortification", 22, owner),
				new Fortification(TownStatus.Burned, TownSize.Small, -1,
										 "inputFortificationTwo", 44, owner),
				new Village(TownStatus.Burned, "inputVillage", 29, owner, "inputRace"));
		Collections.sort(input, new TownComparator());
		assertThat("Sorted list of towns is the order we expect", input,
				equalTo(expected));
	}
}
