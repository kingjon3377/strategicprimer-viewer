package model.workermgmt;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.map.MapDimensions;
import model.map.MapView;
import model.map.Player;
import model.map.Point;
import model.map.SPMap;
import model.map.TileFixture;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.towns.Fortress;
import model.viewer.PointIterator;

import org.junit.Test;

import util.IteratorWrapper;

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
		final SPMap map = new SPMap(new MapDimensions(3, 3, 2));
		final Player playerOne = new Player(0, "player1");
		final Player playerTwo = new Player(1, "player2");
		final Player playerThree = new Player(2, "player3");
		final List<TileFixture> fixtures = new ArrayList<>();
		final List<Unit> listOne = new ArrayList<>();
		final List<Unit> listTwo = new ArrayList<>();
		final List<Unit> listThree = new ArrayList<>();
		fixtures.add(new Mountain());
		fixtures.add(new Animal("animal", false, false, "wild", 1));
		addItem(new Unit(playerOne, "one", "unitOne", 2), fixtures,
				listOne);
		addItem(new Unit(playerTwo, "two", "unitTwo", 3), fixtures,
				listTwo);
		final Fortress fort = new Fortress(new Player(3, "player4"), "fort", 4);
		final Unit unit = new Unit(playerThree, "three", "unitThree", 5);
		fort.addUnit(unit);
		listThree.add(unit);
		fixtures.add(fort);
		fixtures.add(new Forest("forest", false));
		fixtures.add(new Hill(7));
		addItem(new Unit(playerOne, "four", "unitFour", 6), fixtures,
				listOne);
		fixtures.add(new Oasis(8));
		Collections.shuffle(fixtures);
		final IWorkerModel model = new WorkerModel(new MapView(map, 0, 0),
				"string");
		final Iterable<Point> iter = new IteratorWrapper<>(new PointIterator(
				model, false, true, true));
		for (final Point point : iter) {
			final TileFixture fix = fixtures.remove(0);
			assert point != null && fix != null;
			map.getTile(point).addFixture(fix);
		}
		final List<Unit> listOneA = model.getUnits(playerOne);
		assertTrue("Got all units for player 1", listOneA.containsAll(listOne));
		assertTrue("And didn't miss any for player 1",
				listOne.containsAll(listOneA));
		final List<Unit> listTwoA = model.getUnits(playerTwo);
		assertTrue("Got all units for player 2", listTwoA.containsAll(listTwo));
		assertTrue("And didn't miss any for player 2",
				listTwo.containsAll(listTwoA));
		final List<Unit> listThreeA = model.getUnits(playerThree);
		assertTrue("Got all units for player 3",
				listThreeA.containsAll(listThree));
		assertTrue("And didn't miss any for player 3",
				listThree.containsAll(listThreeA));
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
}
