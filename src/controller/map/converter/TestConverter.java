package controller.map.converter;

import static org.junit.Assert.assertTrue;

import model.map.IMutableMapNG;
import org.junit.Test;

import model.map.IMapNG;
import model.map.MapDimensions;
import model.map.Player;
import model.map.PlayerCollection;
import model.map.Point;
import model.map.PointFactory;
import model.map.SPMapNG;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.towns.Fortress;

/**
 * The test case for map-conversion code paths.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class TestConverter {

	/**
	 * Test conversion.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testConversion() {
		final IMutableMapNG start =
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(),
						0);
		final Point pointOne = PointFactory.point(0, 0);
		final Animal fixture = new Animal("animal", false, true,
				"domesticated", 1);
		start.addFixture(pointOne, fixture);
		final Point pointTwo = PointFactory.point(0, 1);
		final CacheFixture fixtureTwo = new CacheFixture("gemstones", "small",
				2);
		start.addFixture(pointTwo, fixtureTwo);
		final Point pointThree = PointFactory.point(1, 0);
		final IUnit fixtureThree = new Unit(new Player(0, "A. Player"),
				"legion", "eagles", 3);
		start.addFixture(pointThree, fixtureThree);
		final Point pointFour = PointFactory.point(1, 1);
		final Fortress fixtureFour = new Fortress(new Player(1, "B. Player"),
				"HQ", 4);
		start.addFixture(pointFour, fixtureFour);
		final IMapNG converted = ResolutionDecreaseConverter.convert(start);
		final Point zeroPoint = PointFactory.point(0, 0);
		assertTrue("Combined tile should contain fixtures from tile one",
				doesIterableContain(converted.getOtherFixtures(zeroPoint), fixture));
		assertTrue(
				"Combined tile should contain fixtures from tile two",
				doesIterableContain(converted.getOtherFixtures(zeroPoint),
						fixtureTwo));
		assertTrue(
				"Combined tile should contain fixtures from tile three",
				doesIterableContain(converted.getOtherFixtures(zeroPoint),
						fixtureThree));
		assertTrue(
				"Combined tile should contain fixtures from tile four",
				doesIterableContain(converted.getOtherFixtures(zeroPoint),
						fixtureFour));
	}

	/**
	 * Test whether an item is in an iterable. Note that the iterable's iterator
	 * will have advanced either to the item searched for or to the end.
	 *
	 * @param iter an iterable
	 * @param item an item
	 * @param <T> the type of the items in the iterator
	 * @param <U> the type of the item
	 * @return whether the iterable contains the item
	 */
	private static <T, U extends T> boolean doesIterableContain(
			final Iterable<T> iter, final U item) {
		for (final T each : iter) {
			if (each.equals(item)) {
				return true; // NOPMD
			}
		}
		return false;
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TestConverter test case";
	}
}
