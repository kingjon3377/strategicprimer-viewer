package model.workermgmt;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

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
import util.NullCleaner;

/**
 * A class to test MapHelper methods (starting with ones that don't involve
 * I/O).
 *
 * @author Jonathan Lovelace
 *
 */
public class TestWorkerModel {
	/**
	 * Test for getUnits().
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetUnits() {
		final SPMapNG map =
				new SPMapNG(new MapDimensions(3, 3, 2), new PlayerCollection(),
						-1);
		final Player playerOne = new Player(0, "player1");
		final Player playerTwo = new Player(1, "player2");
		final Player playerThree = new Player(2, "player3");
		final List<TileFixture> fixtures = new ArrayList<>();
		final List<IUnit> listOne = new ArrayList<>();
		final List<IUnit> listTwo = new ArrayList<>();
		final List<IUnit> listThree = new ArrayList<>();
		fixtures.add(new Mountain());
		fixtures.add(new Animal("animal", false, false, "wild", 1));
		addItem(new Unit(playerOne, "one", "unitOne", 2), fixtures,
				listOne);
		addItem(new Unit(playerTwo, "two", "unitTwo", 3), fixtures,
				listTwo);
		final Fortress fort = new Fortress(new Player(3, "player4"), "fort", 4);
		final IUnit unit = new Unit(playerThree, "three", "unitThree", 5);
		fort.addUnit(unit);
		listThree.add(unit);
		fixtures.add(fort);
		fixtures.add(new Forest("forest", false));
		fixtures.add(new Hill(7));
		addItem(new Unit(playerOne, "four", "unitFour", 6), fixtures,
				listOne);
		fixtures.add(new Oasis(8));
		Collections.shuffle(fixtures);
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
	 * @param <T> the type of the list
	 * @return the contents of that list, with any proxies replaced by the items they proxy
	 */
	private static <T> List<T> filterProxies(final List<T> list) {
		final List<T> retval = new ArrayList<>();
		for (T item : list) {
			if (item instanceof ProxyFor<?>) {
				@SuppressWarnings("unchecked") // this wouldn't work for Skills, but ...
				ProxyFor<T> proxy = (ProxyFor<T>) item;
				for (T proxied : proxy.getProxied()) {
					retval.add(proxied);
				}
			} else {
				retval.add(item);
			}
		}
		return retval;
	}
	/**
	 * Add to multiple lists.
	 *
	 * @param <T> the type of the item
	 * @param item the item to add
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
	@Override
	public String toString() {
		return "TestWorkerModel";
	}
}
