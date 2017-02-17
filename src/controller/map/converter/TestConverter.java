package controller.map.converter;

import java.util.regex.Pattern;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.MapDimensionsImpl;
import model.map.PlayerCollection;
import model.map.PlayerImpl;
import model.map.Point;
import model.map.PointFactory;
import model.map.River;
import model.map.SPMapNG;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.Ground;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.TownSize;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * The test case for map-conversion code paths.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class TestConverter {
	/**
	 * Extracted constant: the type of rock used in the test-data tables.
	 */
	private static final String ROCK_TYPE = "rock1";
	/**
	 * Extracted constant: the type of tree used for boreal forests in the test-data
	 * tables.
	 */
	private static final String BOREAL_TREE = "btree1";
	/**
	 * Extracted constant: the type of field used in the test-data tables.
	 */
	private static final String FIELD_TYPE = "grain1";
	/**
	 * Extracted constant: the type of tree used for temperate forests in the test-data
	 * tables.
	 */
	private static final String TEMP_TREE = "ttree1";
	/**
	 * A pattern to match all positive IDs in XML.
	 */
	private static final Pattern POSITIVE_IDS =
			Pattern.compile("id=\"[0-9]*\"");
	/**
	 * Initialize a point in the map with its terrain and a list of fixtures.
	 * @param map a map
	 * @param point a point
	 * @param terrain the terrain type there
	 * @param fixtures fixtures to put there
	 */
	private static void initialize(final IMutableMapNG map, final Point point,
								   final TileType terrain,
								   final TileFixture... fixtures) {
		if (TileType.NotVisible != terrain) {
			map.setBaseTerrain(point, terrain);
		}
		for (final TileFixture fixture : fixtures) {
			if (fixture instanceof Ground && map.getGround(point) == null) {
				map.setGround(point, (Ground) fixture);
			} else if (fixture instanceof Forest && map.getForest(point) == null) {
				map.setForest(point, (Forest) fixture);
			} else {
				map.addFixture(point, fixture);
			}
		}
	}
	/**
	 * Test conversion.
	 */
	@Test
	public void testConversion() {
		final IMutableMapNG start =
				new SPMapNG(new MapDimensionsImpl(2, 2, 2), new PlayerCollection(), 0);
		final Animal fixture = new Animal("animal", false, true, "domesticated", 1);
		initialize(start, PointFactory.point(0, 0), TileType.Desert, fixture);
		final CacheFixture fixtureTwo = new CacheFixture("gemstones", "small",
																2);
		initialize(start, PointFactory.point(0, 1), TileType.Desert, fixtureTwo);
		final IUnit fixtureThree =
				new Unit(new PlayerImpl(0, "A. Player"), "legion", "eagles", 3);
		initialize(start, PointFactory.point(1, 0), TileType.Desert, fixtureThree);
		final Fortress fixtureFour = new Fortress(new PlayerImpl(1, "B. Player"), "HQ", 4,
														 TownSize.Small);
		initialize(start, PointFactory.point(1, 1), TileType.Plains, fixtureFour);

		final IMapNG converted = ResolutionDecreaseConverter.convert(start);
		final Point zeroPoint = PointFactory.point(0, 0);
		assertThat("Combined tile should contain fixtures from tile one",
				converted.getOtherFixtures(zeroPoint), hasItem(fixture));
		assertThat("Combined tile should contain fixtures from tile two",
				converted.getOtherFixtures(zeroPoint), hasItem(fixtureTwo));
		assertThat("Combined tile should contain fixtures from tile three",
				converted.getOtherFixtures(zeroPoint), hasItem(fixtureThree));
		assertThat("Combined tile should contain fixtures from tile four",
				converted.getOtherFixtures(zeroPoint), hasItem(fixtureFour));
		assertThat("Combined tile has type of most of input tiles",
				converted.getBaseTerrain(zeroPoint), equalTo(TileType.Desert));
	}

	/**
	 * Test more corners of resolution-decrease conversion.
	 */
	@Test
	public void testMoreConversion() {
		final IMutableMapNG start =
				new SPMapNG(new MapDimensionsImpl(2, 2, 2), new PlayerCollection(), 0);
		final Point pointOne = PointFactory.point(0, 0);
		start.setMountainous(pointOne, true);
		start.addRivers(pointOne, River.East, River.South);
		final Ground groundOne = new Ground(-1, "groundOne", false);
		initialize(start, pointOne, TileType.Steppe, groundOne);
		final Point pointTwo = PointFactory.point(0, 1);
		start.addRivers(pointTwo, River.Lake);
		final Ground groundTwo = new Ground(-1, "groundTwo", false);
		initialize(start, pointTwo, TileType.Steppe, groundTwo);
		final Point pointThree = PointFactory.point(1, 0);
		final Forest forestOne = new Forest("forestOne", false, 1);
		initialize(start, pointThree, TileType.Plains, forestOne);
		final Point pointFour = PointFactory.point(1, 1);
		final Forest forestTwo = new Forest("forestTwo", false, 2);
		initialize(start, pointFour, TileType.Desert, forestTwo);
		final IMapNG converted = ResolutionDecreaseConverter.convert(start);
		final Point zeroPoint = PointFactory.point(0, 0);
		assertThat("One mountainous point makes the reduced point mountainous",
				Boolean.valueOf(converted.isMountainous(zeroPoint)),
				equalTo(Boolean.TRUE));
		assertThat("Ground carries over", converted.getGround(zeroPoint),
				equalTo(groundOne));
		assertThat("Ground carries over even when already set",
				converted.getOtherFixtures(zeroPoint), hasItem(groundTwo));
		assertThat("Forest carries over", converted.getForest(zeroPoint),
				equalTo(forestOne));
		assertThat("Forest carries over even when already set",
				converted.getOtherFixtures(zeroPoint), hasItem(forestTwo));
		assertThat("Non-interior rivers carry over", converted.getRivers(zeroPoint),
				hasItem(River.Lake));
		assertThat("Non-interior rivers carry over", converted.getRivers(zeroPoint),
				not(hasItems(River.East, River.South)));
		assertThat("Combined tile has most common terrain type among inputs",
				converted.getBaseTerrain(zeroPoint), equalTo(TileType.Steppe));
	}

	/**
	 * Test that resolution-decrease conversion fails fast.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testResolutionDecreaseRequirement() {
		ResolutionDecreaseConverter.convert(
				new SPMapNG(new MapDimensionsImpl(3, 3, 2), new PlayerCollection(), -1));
		fail("Shouldn't accept non-even dimensions");
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TestConverter test case";
	}
}
