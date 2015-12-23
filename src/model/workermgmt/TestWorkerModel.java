package model.workermgmt;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import model.map.MapDimensions;
import model.map.Player;
import model.map.PlayerCollection;
import model.map.Point;
import model.map.SPMapNG;
import model.map.TileFixture;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.ProxyFor;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.towns.Fortress;
import org.junit.Test;
import util.NullCleaner;

import static org.junit.Assert.assertTrue;

/**
 * A class to test MapHelper methods (starting with ones that don't involve I/O).
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
public final class TestWorkerModel {
	/**
	 * Test for getUnits().
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetUnits() {
		final List<TileFixture> fixtures = new ArrayList<>();
		fixtures.add(new Mountain());
		fixtures.add(new Animal("animal", false, false, "wild", 1));
		final List<IUnit> listOne = new ArrayList<>();
		final Player playerOne = new Player(0, "player1");
		addItem(new Unit(playerOne, "one", "unitOne", 2), fixtures,
				listOne);
		final List<IUnit> listTwo = new ArrayList<>();
		final Player playerTwo = new Player(1, "player2");
		addItem(new Unit(playerTwo, "two", "unitTwo", 3), fixtures,
				listTwo);
		final Fortress fort = new Fortress(new Player(3, "player4"), "fort", 4);
		final Player playerThree = new Player(2, "player3");
		final IUnit unit = new Unit(playerThree, "three", "unitThree", 5);
		fort.addMember(unit);
		final Collection<IUnit> listThree = new ArrayList<>();
		listThree.add(unit);
		fixtures.add(fort);
		fixtures.add(new Forest("forest", false));
		fixtures.add(new Hill(7));
		addItem(new Unit(playerOne, "four", "unitFour", 6), fixtures,
				listOne);
		fixtures.add(new Oasis(8));
		Collections.shuffle(fixtures);
		final SPMapNG map =
				new SPMapNG(new MapDimensions(3, 3, 2), new PlayerCollection(),
						           -1);
		for (final Point point : map.locations()) {
			map.addFixture(NullCleaner.assertNotNull(point),
					NullCleaner.assertNotNull(fixtures.remove(0)));
		}
		final IWorkerModel model = new WorkerModel(map, new File(""));
		final List<IUnit> listOneA = filterProxies(model.getUnits(playerOne));
		assertTrue("Got all units for player 1", listOneA.containsAll(listOne));
		assertTrue("And didn't miss any for player 1",
				listOne.containsAll(listOneA));
		final List<IUnit> listTwoA = filterProxies(model.getUnits(playerTwo));
		assertTrue("Got all units for player 2", listTwoA.containsAll(listTwo));
		assertTrue("And didn't miss any for player 2",
				listTwo.containsAll(listTwoA));
		final List<IUnit> listThreeA = filterProxies(model.getUnits(playerThree));
		assertTrue("Got all units for player 3",
				listThreeA.containsAll(listThree));
		assertTrue("And didn't miss any for player 3",
				listThree.containsAll(listThreeA));
	}

	/**
	 * @param list a list
	 * @param <T>  the type of the list
	 * @return the contents of that list, with any proxies replaced by the items they
	 * proxy
	 */
	private static <T> List<T> filterProxies(final Iterable<T> list) {
		// FIXME: There ought to be a way of doing this using the Streams API
		final List<T> retval = new ArrayList<>();
		for (final T item : list) {
			if (item instanceof ProxyFor<?>) {
				// this wouldn't work for Skills, but ...
				((ProxyFor<T>) item).getProxied().forEach(retval::add);
			} else {
				retval.add(item);
			}
		}
		return retval;
	}

	/**
	 * Add to multiple lists.
	 *
	 * @param <T>   the type of the item
	 * @param item  the item to add
	 * @param lists the lists to add to
	 */
	@SafeVarargs
	private static <T> void addItem(final T item,
	                                final List<? super T>... lists) {
		for (final List<? super T> list : lists) {
			list.add(item);
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TestWorkerModel";
	}
}
