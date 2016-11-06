package model.workermgmt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import model.map.IMutableMapNG;
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
import model.map.fixtures.towns.TownSize;
import org.junit.Test;
import util.NullCleaner;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

/**
 * A class to test MapHelper methods (starting with ones that don't involve I/O).
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
		final Fortress fort = new Fortress(new Player(3, "player4"), "fort", 4,
												  TownSize.Small);
		final Player playerThree = new Player(2, "player3");
		final IUnit unit = new Unit(playerThree, "three", "unitThree", 5);
		fort.addMember(unit);
		final Collection<IUnit> listThree = new ArrayList<>();
		listThree.add(unit);
		fixtures.add(fort);
		fixtures.add(new Forest("forest", false, 10));
		fixtures.add(new Hill(7));
		addItem(new Unit(playerOne, "four", "unitFour", 6), fixtures,
				listOne);
		fixtures.add(new Oasis(8));
		Collections.shuffle(fixtures);
		final IMutableMapNG map =
				new SPMapNG(new MapDimensions(3, 3, 2), new PlayerCollection(), -1);
		for (final Point point : map.locations()) {
			map.addFixture(NullCleaner.assertNotNull(point),
					NullCleaner.assertNotNull(fixtures.remove(0)));
		}
		final IWorkerModel model = new WorkerModel(map, Optional.empty());
		final List<IUnit> listOneA = filterProxies(model.getUnits(playerOne));
		assertThat("Got all units for player 1", listOne,
				hasItems(listOneA.toArray(new IUnit[listOneA.size()])));
		assertThat("Didn't miss any for player 1", listOneA,
				hasItems(listOne.toArray(new IUnit[listOne.size()])));
		final List<IUnit> listTwoA = filterProxies(model.getUnits(playerTwo));
		assertThat("Got all units for player 2", listTwo,
				hasItems(listTwoA.toArray(new IUnit[listTwoA.size()])));
		assertThat("Didn't miss any for player 2", listTwoA,
				hasItems(listTwo.toArray(new IUnit[listTwo.size()])));
		final List<IUnit> listThreeA = filterProxies(model.getUnits(playerThree));
		assertThat("Got all units for player 3", listThree,
				hasItems(listThreeA.toArray(new IUnit[listThreeA.size()])));
		assertThat("Didn't miss any for player 3", listThreeA,
				hasItems(listThree.toArray(new IUnit[listThree.size()])));
	}

	/**
	 * @param list a list
	 * @param <T>  the type of the list
	 * @return the contents of that list, with any proxies replaced by the items they
	 * proxy
	 */
	private static <T> List<T> filterProxies(final Iterable<T> list) {
		return StreamSupport.stream(list.spliterator(), false).flatMap(item -> {
			if (item instanceof ProxyFor<?>) {
				// this wouldn't work for Skills, but ...
				//noinspection unchecked
				return StreamSupport.stream(((ProxyFor<T>) item).getProxied().spliterator(), false);
			} else {
				return Stream.of(item);
			}
		}).collect(Collectors.toList());
	}

	/**
	 * Add to multiple lists.
	 *
	 * @param <T>   the type of the item
	 * @param item  the item to add
	 * @param lists the lists to add to
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@SafeVarargs
	private static <T> void addItem(final T item, final List<? super T>... lists) {
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
