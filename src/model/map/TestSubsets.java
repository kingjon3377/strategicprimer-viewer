package model.map;

import java.util.function.Consumer;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.towns.AbstractTown;
import model.map.fixtures.towns.Fortification;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.TownStatus;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static util.NullStream.DEV_NULL;

/**
 * Tests for Subsettable functionality.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class TestSubsets {
	/**
	 * The string "static-method", used in an annotation on each method.
	 */
	private static final String ST_MET = "static-method";
	/**
	 * A commonly-used string.
	 */
	private static final String ONE_STR = "one";

	/**
	 * A test of PlayerCollection's subset feature.
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testPlayerCollectionSubset() {
		final IMutablePlayerCollection firstCollection = new PlayerCollection();
		firstCollection.add(new Player(1, ONE_STR));
		final IMutablePlayerCollection secondCollection = new PlayerCollection();
		secondCollection.add(new Player(1, ONE_STR));
		secondCollection.add(new Player(2, "two"));
		final IPlayerCollection zero = new PlayerCollection();
		assertThat("Empty is subset of self",
				Boolean.valueOf(zero.isSubset(zero, DEV_NULL, "")), equalTo(
						Boolean.TRUE));
		assertThat("Empty is subset of one",
				Boolean.valueOf(firstCollection.isSubset(zero, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		assertThat("Empty is subset of two",
				Boolean.valueOf(secondCollection.isSubset(zero, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		assertThat("One is not subset of empty",
				Boolean.valueOf(zero.isSubset(firstCollection, DEV_NULL, "")),
				equalTo(Boolean.FALSE));
		assertThat("One is subset of self",
				Boolean.valueOf(firstCollection.isSubset(firstCollection, DEV_NULL, "")),
				equalTo(
						Boolean.TRUE));
		assertThat("One is subset of two",
				Boolean.valueOf(secondCollection.isSubset(firstCollection, DEV_NULL,
						"")),
				equalTo(
						Boolean.TRUE));
		assertThat("Two is not subset of empty",
				Boolean.valueOf(zero.isSubset(secondCollection, DEV_NULL, "")),
				equalTo(Boolean.FALSE));
		assertThat("Two is not subset of one",
				Boolean.valueOf(firstCollection.isSubset(secondCollection, DEV_NULL,
						"")),
				equalTo(
						Boolean.FALSE));
		assertThat("Two is subset of self",
				Boolean.valueOf(
						secondCollection.isSubset(secondCollection, DEV_NULL, "")),
				equalTo(
						Boolean.TRUE));
	}

	/**
	 * A test of RiverFixture's subset feature.
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testRiverSubset() {
		final RiverFixture zero = new RiverFixture();
		final RiverFixture thirdCollection =
				new RiverFixture(River.Lake, River.South, River.East, River.North,
										River.West);
		assertThat("None is a subset of all",
				Boolean.valueOf(thirdCollection.isSubset(zero, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		final RiverFixture firstRivers =
				new RiverFixture(River.Lake, River.South, River.East);
		assertThat("Three are a subset of all",
				Boolean.valueOf(thirdCollection.isSubset(firstRivers, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		final RiverFixture secondRivers = new RiverFixture(River.West, River.North);
		assertThat("Two are a subset of all",
				Boolean.valueOf(thirdCollection.isSubset(secondRivers, DEV_NULL, "")),
				equalTo(
						Boolean.TRUE));
		assertThat("All is a subset of all",
				Boolean.valueOf(thirdCollection.isSubset(thirdCollection, DEV_NULL, "")),
				equalTo(
						Boolean.TRUE));
		assertThat("None is a subset of two",
				Boolean.valueOf(secondRivers.isSubset(zero, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		assertThat("All is not a subset of two",
				Boolean.valueOf(secondRivers.isSubset(thirdCollection, DEV_NULL, "")),
				equalTo(
						Boolean.FALSE));
		assertThat("Three are not a subset of two",
				Boolean.valueOf(secondRivers.isSubset(firstRivers, DEV_NULL, "")),
				equalTo(Boolean.FALSE));
		assertThat("Two are a subset of themselves",
				Boolean.valueOf(secondRivers.isSubset(secondRivers, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		assertThat("None is a subset of three",
				Boolean.valueOf(firstRivers.isSubset(zero, DEV_NULL,
						"")), equalTo(Boolean.TRUE));
		assertThat("All is not a subset of three",
				Boolean.valueOf(firstRivers.isSubset(thirdCollection, DEV_NULL, "")),
				equalTo(
						Boolean.FALSE));
		assertThat("A different two are not a subset of three",
				Boolean.valueOf(firstRivers.isSubset(secondRivers, DEV_NULL, "")),
				equalTo(Boolean.FALSE));
		assertThat("Three are a subset of themselves",
				Boolean.valueOf(firstRivers.isSubset(firstRivers, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		assertThat("None is a subset of itself",
				Boolean.valueOf(zero.isSubset(zero, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		assertThat("All are not a subset of none",
				Boolean.valueOf(zero.isSubset(thirdCollection, DEV_NULL, "")),
				equalTo(Boolean.FALSE));
		assertThat("Three are not a subset of none",
				Boolean.valueOf(zero.isSubset(firstRivers, DEV_NULL, "")),
				equalTo(Boolean.FALSE));
		assertThat("Two are not a subset of none",
				Boolean.valueOf(zero.isSubset(secondRivers, DEV_NULL, "")),
				equalTo(Boolean.FALSE));
	}

	/**
	 * A test of Fortress's subset feature.
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testFortressSubset() {
		final Fortress firstFort = new Fortress(new Player(1, ONE_STR), "fOne", 1,
													   TownSize.Small);
		final Fortress secondFort = new Fortress(new Player(2, "two"), "fOne", 1,
														TownSize.Small);
		assertThat("Subset requires same owner, first test",
				Boolean.valueOf(firstFort.isSubset(secondFort, DEV_NULL, "")),
				equalTo(Boolean.FALSE));
		assertThat("Subset requires same owner, second test",
				Boolean.valueOf(secondFort.isSubset(firstFort, DEV_NULL, "")),
				equalTo(Boolean.FALSE));
		final Fortress thirdFort = new Fortress(new Player(1, ONE_STR), "fTwo", 2,
													   TownSize.Small);
		assertThat("Subset requires same name, first test",
				Boolean.valueOf(firstFort.isSubset(thirdFort, DEV_NULL, "")),
				equalTo(Boolean.FALSE));
		assertThat("Subset requires same name, second test",
				Boolean.valueOf(thirdFort.isSubset(firstFort, DEV_NULL, "")),
				equalTo(Boolean.FALSE));
		final Fortress fourthFort = new Fortress(new Player(1, ONE_STR), "fOne", 3,
														TownSize.Small);
		assertThat("Subset doesn't require identity or ID equality",
				Boolean.valueOf(firstFort.isSubset(fourthFort, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		assertThat("Subset doesn't require identity or ID equality",
				Boolean.valueOf(fourthFort.isSubset(firstFort, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		fourthFort.addMember(new Unit(new Player(2, "two"), "unit_type", "unit_name",
											 4));
		assertThat("Fortress without is a subset of fortress with unit",
				Boolean.valueOf(fourthFort.isSubset(firstFort, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		assertThat("Fortress with is not a subset of fortress without unit",
				Boolean.valueOf(firstFort.isSubset(fourthFort, DEV_NULL, "")),
				equalTo(Boolean.FALSE));
		final Fortress fifthFort =
				new Fortress(new Player(1, ONE_STR), "unknown", 5, TownSize.Small);
		assertThat("Fortress named 'unknown' can be subset, first test",
				Boolean.valueOf(firstFort.isSubset(fifthFort, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		assertThat("'unknown' is not commutative",
				Boolean.valueOf(fifthFort.isSubset(firstFort, DEV_NULL, "")),
				equalTo(Boolean.FALSE));
	}

	/**
	 * Test the MapNG subset feature.
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testMapSubset() {
		final IMutableMapNG firstMap =
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(), -1);
		final Point pointOne = PointFactory.point(0, 0);
		firstMap.setBaseTerrain(pointOne, TileType.Jungle);
		final IMutableMapNG secondMap =
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(), -1);
		secondMap.setBaseTerrain(pointOne, TileType.Jungle);
		final Point pointTwo = PointFactory.point(1, 1);
		secondMap.setBaseTerrain(pointTwo, TileType.Ocean);
		final IMapNG zero =
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(), -1);
		assertThat("None is a subset of itself",
				Boolean.valueOf(zero.isSubset(zero, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		assertThat("None is a subset of one",
				Boolean.valueOf(firstMap.isSubset(zero, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		assertThat("None is a subset of two",
				Boolean.valueOf(secondMap.isSubset(zero, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		assertThat("One is not a subset of none",
				Boolean.valueOf(zero.isSubset(firstMap, DEV_NULL, "")),
				equalTo(Boolean.FALSE));
		assertThat("One is a subset of itself",
				Boolean.valueOf(firstMap.isSubset(firstMap, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		assertThat("One is a subset of two",
				Boolean.valueOf(secondMap.isSubset(firstMap, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		assertThat("TWo is not a subset of none",
				Boolean.valueOf(zero.isSubset(secondMap, DEV_NULL, "")),
				equalTo(Boolean.FALSE));
		assertThat("Two is not a subset of one",
				Boolean.valueOf(firstMap.isSubset(secondMap, DEV_NULL, "")),
				equalTo(Boolean.FALSE));
		assertThat("Two is a subset of itself",
				Boolean.valueOf(secondMap.isSubset(secondMap, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		firstMap.setBaseTerrain(pointTwo, TileType.Plains);
		assertThat("Corresponding but non-matching tile breaks subset",
				Boolean.valueOf(secondMap.isSubset(firstMap, DEV_NULL, "")),
				equalTo(Boolean.FALSE));
		assertThat("Corresponding but non-matching tile breaks subset",
				Boolean.valueOf(firstMap.isSubset(secondMap, DEV_NULL, "")),
				equalTo(Boolean.FALSE));
		secondMap.setBaseTerrain(pointTwo, TileType.Plains);
		assertThat("Subset again after resetting terrain",
				Boolean.valueOf(secondMap.isSubset(firstMap, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		firstMap.addFixture(pointTwo, new CacheFixture("category", "contents", 3));
		assertThat("Subset calculation ignores caches",
				Boolean.valueOf(secondMap.isSubset(firstMap, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		firstMap.addFixture(pointTwo, new TextFixture("text", -1));
		assertThat("Subset calculation ignores text fixtures",
				Boolean.valueOf(secondMap.isSubset(firstMap, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		firstMap.addFixture(pointTwo, new Animal("animal", true, false, "status", 5));
		assertThat("Subset calculation ignores animal tracks",
				Boolean.valueOf(secondMap.isSubset(firstMap, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
	}

	/**
	 * Test map subsets to ensure off-by-one errors are caught.
	 */
	@Test
	public void testMapOffByOne() {
		final IMutableMapNG baseMap =
				new SPMapNG(new MapDimensions(3, 3, 2), new PlayerCollection(), -1);
		baseMap.locationStream()
				.forEach(point -> baseMap.setBaseTerrain(point, TileType.Plains));
		baseMap.setForest(PointFactory.point(1, 1), new Forest("elm", false, 1));
		baseMap.addFixture(PointFactory.point(1, 1),
				new Animal("skunk", false, false, "wild", 1));
		baseMap.addRivers(PointFactory.point(1, 1), River.East);

		final IMutableMapNG testMap =
				new SPMapNG(baseMap.dimensions(), new PlayerCollection(), -1);
		testMap.locationStream()
				.forEach(point -> testMap.setBaseTerrain(point, TileType.Plains));
		final Forest forest = new Forest("elm", false, 1);
		final TileFixture animal = new Animal("skunk", false, false, "wild", 1);
		final Consumer<IMutableMapNG> testTrue =
				map -> assertThat("Subset holds when fixture(s) placed correctly",
						Boolean.valueOf(baseMap.isSubset(map, DEV_NULL, "")),
						equalTo(Boolean.TRUE));
		final Consumer<IMutableMapNG> testFalse =
				map -> assertThat("Subset fails when fixture(s) off by one",
						Boolean.valueOf(baseMap.isSubset(map, DEV_NULL, "")),
						equalTo(Boolean.FALSE));
		for (final Point point : testMap.locations()) {
			assertThat("Subset invariant before attempt using " + point,
					Boolean.valueOf(baseMap.isSubset(testMap, DEV_NULL, "")),
					equalTo(Boolean.TRUE));
			final Consumer<IMutableMapNG> test;
			if (PointFactory.point(1, 1).equals(point)) {
				test = testTrue;
			} else {
				test = testFalse;
			}
			testMap.setForest(point, forest);
			test.accept(testMap);
			testMap.setForest(point, null);
			testMap.addFixture(point, animal);
			test.accept(testMap);
			testMap.removeFixture(point, animal);
			testMap.addRivers(point, River.East);
			test.accept(testMap);
			testMap.removeRivers(point, River.East);
			assertThat("Subset invariant after attempt using " + point,
					Boolean.valueOf(baseMap.isSubset(testMap, DEV_NULL, "")),
					equalTo(Boolean.TRUE));
		}
	}

	/**
	 * Test subsets' interaction with copy().
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSubsetsAndCopy() {
		final IMutableMapNG firstMap =
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(), -1);
		final Point pointOne = PointFactory.point(0, 0);
		firstMap.setBaseTerrain(pointOne, TileType.Jungle);
		final IMapNG zero =
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(), -1);
		assertThat("zero is a subset of one before copy",
				Boolean.valueOf(firstMap.isSubset(zero, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		final IMutableMapNG secondMap =
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(), -1);
		secondMap.setBaseTerrain(pointOne, TileType.Jungle);
		final Point pointTwo = PointFactory.point(1, 1);
		secondMap.setBaseTerrain(pointTwo, TileType.Ocean);
		firstMap.setBaseTerrain(pointTwo, TileType.Plains);
		secondMap.setBaseTerrain(pointTwo, TileType.Plains);
		firstMap.addFixture(pointTwo, new CacheFixture("category", "contents", 3));
		firstMap.addFixture(pointTwo, new TextFixture("text", -1));
		firstMap.addFixture(pointTwo, new Animal("animal", true, false, "status", 5));
		firstMap.addFixture(pointOne,
				new Fortification(TownStatus.Burned, TownSize.Large, 15, "fortification",
										 6, new Player(0, "")));
		assertThat("Cloned map equals original", firstMap.copy(false, null),
				equalTo(firstMap));
		final IMapNG clone = firstMap.copy(true, null);
		assertThat("unfilled map is still a subset of zeroed clone",
				Boolean.valueOf(clone.isSubset(zero, DEV_NULL, "")),
				equalTo(Boolean.TRUE));
		// DCs, the only thing zeroed out in *map* copy() at the moment, are ignored in
		// equals().
		clone.streamOtherFixtures(pointOne).filter(AbstractTown.class::isInstance)
				.forEach(fix -> assertThat("Copied map didn't copy DCs",
						Integer.valueOf(fix.getDC()), equalTo(
								Integer.valueOf(0))));
		final Unit uOne = new Unit(new Player(0, ""), "type", "name", 7);
		uOne.addMember(new Worker("worker", "dwarf", 8, new Job("job", 1)));
		assertThat("clone equals original", uOne.copy(false), equalTo(uOne));
		assertThat("zeroed clone doesn't equal original",
				Boolean.valueOf(uOne.equals(uOne.copy(true))),
				equalTo(Boolean.FALSE));
		assertThat("zeroed clone is subset of original",
				Boolean.valueOf(uOne.isSubset(uOne.copy(true), DEV_NULL, "")),
				equalTo(Boolean.TRUE));
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TestSubsets";
	}
}
