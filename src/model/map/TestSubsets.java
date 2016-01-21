package model.map;

import java.io.IOException;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.towns.AbstractTown;
import model.map.fixtures.towns.Fortification;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.TownStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
	 *
	 * @throws IOException on I/O error writing to the null stream
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
		assertTrue("Empty is subset of self", zero.isSubset(zero, DEV_NULL, ""));
		assertTrue("Empty is subset of one",
				firstCollection.isSubset(zero, DEV_NULL, ""));
		assertTrue("Empty is subset of two",
				secondCollection.isSubset(zero, DEV_NULL, ""));
		assertFalse("One is not subset of empty",
				zero.isSubset(firstCollection, DEV_NULL, ""));
		assertTrue("One is subset of self",
				firstCollection.isSubset(firstCollection, DEV_NULL, ""));
		assertTrue("One is subset of two",
				secondCollection.isSubset(firstCollection, DEV_NULL, ""));
		assertFalse("Two is not subset of empty",
				zero.isSubset(secondCollection, DEV_NULL, ""));
		assertFalse("Two is not subset of one",
				firstCollection.isSubset(secondCollection, DEV_NULL, ""));
		assertTrue("Two is subset of self",
				secondCollection.isSubset(secondCollection, DEV_NULL, ""));
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
		final RiverFixture thirdCollection = new RiverFixture(River.Lake, River.South,
				                                                     River.East,
				                                                     River.North,
				                                                     River.West);
		assertTrue("None is a subset of all",
				thirdCollection.isSubset(zero, DEV_NULL, ""));
		final RiverFixture firstRivers = new RiverFixture(River.Lake, River.South,
				                                                 River.East);
		assertTrue("Three are a subset of all",
				thirdCollection.isSubset(firstRivers, DEV_NULL, ""));
		final RiverFixture secondRivers = new RiverFixture(River.West, River.North);
		assertTrue("Two are a subset of all",
				thirdCollection.isSubset(secondRivers, DEV_NULL, ""));
		assertTrue("All is a subset of all",
				thirdCollection.isSubset(thirdCollection, DEV_NULL, ""));
		assertTrue("None is a subset of two", secondRivers.isSubset(zero, DEV_NULL, ""));
		assertFalse("All is not a subset of two",
				secondRivers.isSubset(thirdCollection, DEV_NULL, ""));
		assertFalse("Three are not a subset of two",
				secondRivers.isSubset(firstRivers, DEV_NULL, ""));
		assertTrue("Two are a subset of themselves",
				secondRivers.isSubset(secondRivers, DEV_NULL, ""));
		assertTrue("None is a subset of three", firstRivers.isSubset(zero, DEV_NULL,
				""));
		assertFalse("All is not a subset of three",
				firstRivers.isSubset(thirdCollection, DEV_NULL, ""));
		assertFalse("A different two are not a subset of three",
				firstRivers.isSubset(secondRivers, DEV_NULL, ""));
		assertTrue("Three are a subset of themselves",
				firstRivers.isSubset(firstRivers, DEV_NULL, ""));
		assertTrue("None is a subset of itself",
				zero.isSubset(zero, DEV_NULL, ""));
		assertFalse("All are not a subset of none",
				zero.isSubset(thirdCollection, DEV_NULL, ""));
		assertFalse("Three are not a subset of none",
				zero.isSubset(firstRivers, DEV_NULL, ""));
		assertFalse("Two are not a subset of none",
				zero.isSubset(secondRivers, DEV_NULL, ""));
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
		assertFalse("Subset requires same owner, first test",
				firstFort.isSubset(secondFort, DEV_NULL, ""));
		assertFalse("Subset requires same owner, second test",
				secondFort.isSubset(firstFort, DEV_NULL, ""));
		final Fortress thirdFort = new Fortress(new Player(1, ONE_STR), "fTwo", 2);
		assertFalse("Subset requires same name, first test",
				firstFort.isSubset(thirdFort, DEV_NULL, ""));
		assertFalse("Subset requires same name, second test",
				thirdFort.isSubset(firstFort, DEV_NULL, ""));
		final Fortress fourthFort = new Fortress(new Player(1, ONE_STR), "fOne", 3);
		assertTrue("Subset doesn't require identiy or ID equality",
				firstFort.isSubset(fourthFort, DEV_NULL, ""));
		assertTrue("Subset doesn't require identiy or ID equality",
				fourthFort.isSubset(firstFort, DEV_NULL, ""));
		fourthFort.addMember(new Unit(new Player(2, "two"), "unit_type", "unit_name",
				                             4));
		assertTrue("Fortress without is a subset of fortress with unit",
				fourthFort.isSubset(firstFort, DEV_NULL, ""));
		assertFalse("Fortress with is not a subset of fortress without unit",
				firstFort.isSubset(fourthFort, DEV_NULL, ""));
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
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(),
						           -1);
		final Point pointOne = PointFactory.point(0, 0);
		firstMap.setBaseTerrain(pointOne, TileType.Jungle);
		final IMutableMapNG secondMap =
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(),
						           -1);
		secondMap.setBaseTerrain(pointOne, TileType.Jungle);
		final Point pointTwo = PointFactory.point(1, 1);
		secondMap.setBaseTerrain(pointTwo, TileType.Ocean);
		final IMapNG zero =
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(),
						           -1);
		assertTrue("None is a subset of itself",
				zero.isSubset(zero, DEV_NULL, ""));
		assertTrue("None is a subset of one", firstMap.isSubset(zero, DEV_NULL, ""));
		assertTrue("None is a subset of two", secondMap.isSubset(zero, DEV_NULL, ""));
		assertFalse("One is not a subset of none",
				zero.isSubset(firstMap, DEV_NULL, ""));
		assertTrue("One is a subset of itself",
				firstMap.isSubset(firstMap, DEV_NULL, ""));
		assertTrue("One is a subset of two", secondMap.isSubset(firstMap, DEV_NULL, ""));
		assertFalse("TWo is not a subset of none",
				zero.isSubset(secondMap, DEV_NULL, ""));
		assertFalse("Two is not a subset of one",
				firstMap.isSubset(secondMap, DEV_NULL, ""));
		assertTrue("Two is a subset of itself",
				secondMap.isSubset(secondMap, DEV_NULL, ""));
		firstMap.setBaseTerrain(pointTwo, TileType.Plains);
		assertFalse("Corresponding but non-matching tile breaks subset",
				secondMap.isSubset(firstMap, DEV_NULL, ""));
		assertFalse("Corresponding but non-matching tile breaks subset",
				firstMap.isSubset(secondMap, DEV_NULL, ""));
		secondMap.setBaseTerrain(pointTwo, TileType.Plains);
		assertTrue("Subset again after resetting terrain",
				secondMap.isSubset(firstMap, DEV_NULL, ""));
		firstMap.addFixture(pointTwo, new CacheFixture("category", "contents", 3));
		assertTrue("Subset calculation ignores caches",
				secondMap.isSubset(firstMap, DEV_NULL, ""));
		firstMap.addFixture(pointTwo, new TextFixture("text", -1));
		assertTrue("Subset calculation ignores text fixtures",
				secondMap.isSubset(firstMap, DEV_NULL, ""));
		firstMap.addFixture(pointTwo, new Animal("animal", true, false, "status", 5));
		assertTrue("Subset calculation ignores animal tracks",
				secondMap.isSubset(firstMap, DEV_NULL, ""));
	}

	/**
	 * Test subsets' interaction with copy().
	 *
	 * @throws IOException on I/O writing to bit bucket
	 */
	@Test
	public void testSubsetsAndCopy() throws IOException {
		final IMutableMapNG firstMap =
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(),
						           -1);
		final Point pointOne = PointFactory.point(0, 0);
		firstMap.setBaseTerrain(pointOne, TileType.Jungle);
		final IMapNG zero =
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(),
						           -1);
		assertTrue("zero is a subset of one before copy",
				firstMap.isSubset(zero, DEV_NULL, ""));
		final IMutableMapNG secondMap =
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(),
						           -1);
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
		assertEquals("Cloned map equals original", firstMap, firstMap.copy(false));
		final IMapNG clone = firstMap.copy(true);
		assertTrue("unfilled map is still a subset of zeroed clone",
				clone.isSubset(zero, DEV_NULL, ""));
		// DCs, the only thing zeroed out in *map* copy() at the moment, are ignored in
		// equals().
		for (final TileFixture fix : clone.getOtherFixtures(pointOne)) {
			if (fix instanceof AbstractTown) {
				assertEquals("Copied map didn't copy DCs", 0, ((IEvent) fix).getDC());
			}
		}
		final Unit uOne = new Unit(new Player(0, ""), "type", "name", 7);
		uOne.addMember(new Worker("worker", "dwarf", 8, new Job("job", 1)));
		assertEquals("clone equals original", uOne, uOne.copy(false));
		assertFalse("zeroed clone doesn't equal original", uOne.equals(uOne.copy(true)));
		assertTrue("zeroed clone is subset of original",
				uOne.isSubset(uOne.copy(true), DEV_NULL, ""));
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
