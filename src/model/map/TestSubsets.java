package model.map;

import java.io.IOException;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static util.NullStream.DEV_NULL;

/**
 * Tests for Subsettable functionality.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
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
		final PlayerCollection firstCollection = new PlayerCollection();
		firstCollection.add(new Player(1, ONE_STR));
		final PlayerCollection secondCollection = new PlayerCollection();
		secondCollection.add(new Player(1, ONE_STR));
		secondCollection.add(new Player(2, "two"));
		final PlayerCollection zero = new PlayerCollection();
		assertThat("Empty is subset of self", zero.isSubset(zero, DEV_NULL, ""), equalTo(true));
		assertThat("Empty is subset of one",
				firstCollection.isSubset(zero, DEV_NULL, ""), equalTo(true));
		assertThat("Empty is subset of two",
				secondCollection.isSubset(zero, DEV_NULL, ""), equalTo(true));
		assertThat("One is not subset of empty",
				zero.isSubset(firstCollection, DEV_NULL, ""), equalTo(false));
		assertThat("One is subset of self",
				firstCollection.isSubset(firstCollection, DEV_NULL, ""), equalTo(true));
		assertThat("One is subset of two",
				secondCollection.isSubset(firstCollection, DEV_NULL, ""), equalTo(true));
		assertThat("Two is not subset of empty",
				zero.isSubset(secondCollection, DEV_NULL, ""), equalTo(false));
		assertThat("Two is not subset of one",
				firstCollection.isSubset(secondCollection, DEV_NULL, ""), equalTo(false));
		assertThat("Two is subset of self",
				secondCollection.isSubset(secondCollection, DEV_NULL, ""), equalTo(true));
	}

	/**
	 * A test of RiverFixture's subset feature.
	 *
	 * @throws IOException on I/O error writing to the null stream
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testRiverSubset() throws IOException {
		final RiverFixture zero = new RiverFixture();
		final RiverFixture thirdCollection =
				new RiverFixture(River.Lake, River.South, River.East, River.North,
										River.West);
		assertThat("None is a subset of all",
				thirdCollection.isSubset(zero, DEV_NULL, ""), equalTo(true));
		final RiverFixture firstRivers =
				new RiverFixture(River.Lake, River.South, River.East);
		assertThat("Three are a subset of all",
				thirdCollection.isSubset(firstRivers, DEV_NULL, ""), equalTo(true));
		final RiverFixture secondRivers = new RiverFixture(River.West, River.North);
		assertThat("Two are a subset of all",
				thirdCollection.isSubset(secondRivers, DEV_NULL, ""), equalTo(true));
		assertThat("All is a subset of all",
				thirdCollection.isSubset(thirdCollection, DEV_NULL, ""), equalTo(true));
		assertThat("None is a subset of two", secondRivers.isSubset(zero, DEV_NULL, ""),
				equalTo(true));
		assertThat("All is not a subset of two",
				secondRivers.isSubset(thirdCollection, DEV_NULL, ""), equalTo(false));
		assertThat("Three are not a subset of two",
				secondRivers.isSubset(firstRivers, DEV_NULL, ""), equalTo(false));
		assertThat("Two are a subset of themselves",
				secondRivers.isSubset(secondRivers, DEV_NULL, ""), equalTo(true));
		assertThat("None is a subset of three", firstRivers.isSubset(zero, DEV_NULL,
				""), equalTo(true));
		assertThat("All is not a subset of three",
				firstRivers.isSubset(thirdCollection, DEV_NULL, ""), equalTo(false));
		assertThat("A different two are not a subset of three",
				firstRivers.isSubset(secondRivers, DEV_NULL, ""), equalTo(false));
		assertThat("Three are a subset of themselves",
				firstRivers.isSubset(firstRivers, DEV_NULL, ""), equalTo(true));
		assertThat("None is a subset of itself",
				zero.isSubset(zero, DEV_NULL, ""), equalTo(true));
		assertThat("All are not a subset of none",
				zero.isSubset(thirdCollection, DEV_NULL, ""), equalTo(false));
		assertThat("Three are not a subset of none",
				zero.isSubset(firstRivers, DEV_NULL, ""), equalTo(false));
		assertThat("Two are not a subset of none",
				zero.isSubset(secondRivers, DEV_NULL, ""), equalTo(false));
	}

	/**
	 * A test of Fortress's subset feature.
	 *
	 * @throws IOException on I/O error writing to the null stream
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testFortressSubset() throws IOException {
		final Fortress firstFort = new Fortress(new Player(1, ONE_STR), "fOne", 1);
		final Fortress secondFort = new Fortress(new Player(2, "two"), "fOne", 1);
		assertThat("Subset requires same owner, first test",
				firstFort.isSubset(secondFort, DEV_NULL, ""), equalTo(false));
		assertThat("Subset requires same owner, second test",
				secondFort.isSubset(firstFort, DEV_NULL, ""), equalTo(false));
		final Fortress thirdFort = new Fortress(new Player(1, ONE_STR), "fTwo", 2);
		assertThat("Subset requires same name, first test",
				firstFort.isSubset(thirdFort, DEV_NULL, ""), equalTo(false));
		assertThat("Subset requires same name, second test",
				thirdFort.isSubset(firstFort, DEV_NULL, ""), equalTo(false));
		final Fortress fourthFort = new Fortress(new Player(1, ONE_STR), "fOne", 3);
		assertThat("Subset doesn't require identity or ID equality",
				firstFort.isSubset(fourthFort, DEV_NULL, ""), equalTo(true));
		assertThat("Subset doesn't require identity or ID equality",
				fourthFort.isSubset(firstFort, DEV_NULL, ""), equalTo(true));
		fourthFort.addMember(new Unit(new Player(2, "two"), "unit_type", "unit_name",
											4));
		assertThat("Fortress without is a subset of fortress with unit",
				fourthFort.isSubset(firstFort, DEV_NULL, ""), equalTo(true));
		assertThat("Fortress with is not a subset of fortress without unit",
				firstFort.isSubset(fourthFort, DEV_NULL, ""), equalTo(false));
	}

	/**
	 * Test the MapNG subset feature.
	 *
	 * @throws IOException on I/O error writing to the null stream
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testMapSubset() throws IOException {
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
				zero.isSubset(zero, DEV_NULL, ""), equalTo(true));
		assertThat("None is a subset of one", firstMap.isSubset(zero, DEV_NULL, ""),
				equalTo(true));
		assertThat("None is a subset of two", secondMap.isSubset(zero, DEV_NULL, ""),
				equalTo(true));
		assertThat("One is not a subset of none",
				zero.isSubset(firstMap, DEV_NULL, ""), equalTo(false));
		assertThat("One is a subset of itself",
				firstMap.isSubset(firstMap, DEV_NULL, ""), equalTo(true));
		assertThat("One is a subset of two", secondMap.isSubset(firstMap, DEV_NULL, ""),
				equalTo(true));
		assertThat("TWo is not a subset of none",
				zero.isSubset(secondMap, DEV_NULL, ""), equalTo(false));
		assertThat("Two is not a subset of one",
				firstMap.isSubset(secondMap, DEV_NULL, ""), equalTo(false));
		assertThat("Two is a subset of itself",
				secondMap.isSubset(secondMap, DEV_NULL, ""), equalTo(true));
		firstMap.setBaseTerrain(pointTwo, TileType.Plains);
		assertThat("Corresponding but non-matching tile breaks subset",
				secondMap.isSubset(firstMap, DEV_NULL, ""), equalTo(false));
		assertThat("Corresponding but non-matching tile breaks subset",
				firstMap.isSubset(secondMap, DEV_NULL, ""), equalTo(false));
		secondMap.setBaseTerrain(pointTwo, TileType.Plains);
		assertThat("Subset again after resetting terrain",
				secondMap.isSubset(firstMap, DEV_NULL, ""), equalTo(true));
		firstMap.addFixture(pointTwo, new CacheFixture("category", "contents", 3));
		assertThat("Subset calculation ignores caches",
				secondMap.isSubset(firstMap, DEV_NULL, ""), equalTo(true));
		firstMap.addFixture(pointTwo, new TextFixture("text", -1));
		assertThat("Subset calculation ignores text fixtures",
				secondMap.isSubset(firstMap, DEV_NULL, ""), equalTo(true));
		firstMap.addFixture(pointTwo, new Animal("animal", true, false, "status", 5));
		assertThat("Subset calculation ignores animal tracks",
				secondMap.isSubset(firstMap, DEV_NULL, ""));
	}
	/**
	 * Test map subsets to ensure off-by-one errors are caught.
	 * @throws IOException on I/O error writing to bit bucket
	 */
	@Test
	public void testMapOffByOne() throws IOException {
		final SPMapNG baseMap =
				new SPMapNG(new MapDimensions(3, 3, 2), new PlayerCollection(), -1);
		baseMap.locationStream()
				.forEach(point -> baseMap.setBaseTerrain(point, TileType.Plains));
		baseMap.setForest(PointFactory.point(1, 1), new Forest("elm", false));
		baseMap.addFixture(PointFactory.point(1, 1),
				new Animal("skunk", false, false, "wild", 1));
		baseMap.addRivers(PointFactory.point(1, 1), River.East);

		final SPMapNG testMap =
				new SPMapNG(baseMap.dimensions(), new PlayerCollection(), -1);
		testMap.locationStream()
				.forEach(point -> testMap.setBaseTerrain(point, TileType.Plains));
		final Forest forest = new Forest("elm", false);
		final TileFixture animal = new Animal("skunk", false, false, "wild", 1);
		final Consumer<SPMapNG> testTrue =
				map -> {
					try {
						assertThat("Subset holds when fixture(s) placed correctly",
								baseMap.isSubset(map, DEV_NULL, ""), equalTo(true));
					} catch (final IOException ignored) {

					}
				};
		final Consumer<SPMapNG> testFalse = map -> {
			try {
				assertThat("Subset fails when fixture(s) off by one",
						baseMap.isSubset(map, DEV_NULL, ""), equalTo(false));
			} catch (final IOException ignored) {

			}
		};
		for (final Point point : testMap.locations()) {
			assertThat("Subset invariant before attempt using " + point,
					baseMap.isSubset(testMap, DEV_NULL, ""));
			final Consumer<SPMapNG> test;
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
					baseMap.isSubset(testMap, DEV_NULL, ""), equalTo(true));
		}
	}
	/**
	 * Test subsets' interaction with copy().
	 *
	 * @throws IOException on I/O writing to bit bucket
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSubsetsAndCopy() throws IOException {
		final IMutableMapNG firstMap =
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(), -1);
		final Point pointOne = PointFactory.point(0, 0);
		firstMap.setBaseTerrain(pointOne, TileType.Jungle);
		final IMapNG zero =
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(), -1);
		assertThat("zero is a subset of one before copy",
				firstMap.isSubset(zero, DEV_NULL, ""), equalTo(true));
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
		assertThat("Cloned map equals original", firstMap.copy(false), equalTo(firstMap));
		final IMapNG clone = firstMap.copy(true);
		assertThat("unfilled map is still a subset of zeroed clone",
				clone.isSubset(zero, DEV_NULL, ""), equalTo(true));
		// DCs, the only thing zeroed out in *map* copy() at the moment, are ignored in
		// equals().
		clone.streamOtherFixtures(pointOne).filter(AbstractTown.class::isInstance)
				.map(IEvent.class::cast).forEach(
				fix -> assertThat("Copied map didn't copy DCs", fix.getDC(), equalTo(0)));
		final Unit uOne = new Unit(new Player(0, ""), "type", "name", 7);
		uOne.addMember(new Worker("worker", "dwarf", 8, new Job("job", 1)));
		assertThat("clone equals original", uOne.copy(false), equalTo(uOne));
		assertThat("zeroed clone doesn't equal original", uOne.equals(uOne.copy(true)), equalTo(false));
		assertThat("zeroed clone is subset of original",
				uOne.isSubset(uOne.copy(true), DEV_NULL, ""), equalTo(true));
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TestSubsets";
	}
}
