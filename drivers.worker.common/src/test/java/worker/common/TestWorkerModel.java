package worker.common;

import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Deque;

import common.map.HasKind;
import common.map.HasMutableKind;
import common.map.HasMutableName;
import common.map.HasMutableOwner;
import common.map.IFixture;
import common.map.IMapNG;
import common.map.IMutableMapNG;
import common.map.SPMapNG;
import common.map.Point;
import common.map.MapDimensionsImpl;
import common.map.Player;
import common.map.PlayerImpl;
import common.map.TileFixture;
import common.map.PlayerCollection;

import common.map.fixtures.UnitMember;

import common.map.fixtures.mobile.ProxyFor;
import common.map.fixtures.mobile.IMutableUnit;
import common.map.fixtures.mobile.IMutableWorker;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.Unit;
import common.map.fixtures.mobile.ProxyUnit;
import common.map.fixtures.mobile.AnimalImpl;
import common.map.fixtures.terrain.Oasis;
import common.map.fixtures.terrain.Hill;
import common.map.fixtures.terrain.Forest;
import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.IMutableFortress;
import common.map.fixtures.towns.TownSize;
import common.map.fixtures.towns.FortressImpl;
import drivers.common.SimpleMultiMapModel;
import drivers.common.IDriverModel;
import drivers.common.IWorkerModel;
import java.util.logging.Logger;

import common.map.fixtures.mobile.worker.IJob;
import common.map.fixtures.mobile.worker.IMutableJob;
import common.map.fixtures.mobile.worker.IMutableSkill;
import common.map.fixtures.mobile.worker.ISkill;
import common.map.fixtures.mobile.worker.Job;
import common.map.fixtures.mobile.worker.Skill;

/**
 * Test of the worker model.
 */
public class TestWorkerModel {
	/**
	 * Helper method: Flatten any proxies in the list by replacing them with what they are proxies for.
	 */
	private <T> Iterable<T> filterProxies(Iterable<T> list, Class<? extends ProxyFor> cls) {
		List<T> retval = new ArrayList<T>();
		for (T item : list) {
			if (cls.isInstance(item)) {
				((ProxyFor<? extends T>) item).getProxied().forEach(retval::add);
			} else {
				retval.add(item);
			}
		}
		return retval;
	}

	/**
	 * Helper method: Add an item to multiple lists at once.
	 */
	@SafeVarargs
	private final <T> void addItem(T item, List<? super T>... lists) {
		for (List<? super T> list : lists) {
			list.add(item);
		}
	}

	private boolean iterableEquality(Object one, Object two) {
		if (one instanceof Iterable && two instanceof Iterable) {
			List<Object> listOne = StreamSupport.stream(((Iterable<?>) one).spliterator(), false)
				.collect(Collectors.toList());
			List<Object> listTwo = StreamSupport.stream(((Iterable<?>) two).spliterator(), false)
				.collect(Collectors.toList());
			return listOne.containsAll(listTwo) && listTwo.containsAll(listOne);
		} else {
			return Objects.equals(one, two);
		}
	}

	/**
	 * Test of the {@link IWorkerModel#getUnits} method.
	 */
	@Test
	public void testGetUnits() {
		List<TileFixture> fixtures = new ArrayList<>();
		fixtures.add(new Oasis(14));
		fixtures.add(new AnimalImpl("animal", false, "wild", 1));
		List<IUnit> listOne = new ArrayList<>();
		Player playerOne = new PlayerImpl(0, "player1");
		addItem(new Unit(playerOne, "one", "unitOne", 2), fixtures, listOne);
		List<IUnit> listTwo = new ArrayList<IUnit>();
		Player playerTwo = new PlayerImpl(1, "player2");
		addItem(new Unit(playerTwo, "two", "unitTwo", 3), fixtures, listTwo);
		Player playerThree = new PlayerImpl(2, "player3");
		Player playerFour = new PlayerImpl(3, "player4");
		IMutableFortress fort = new FortressImpl(playerFour, "fort", 4, TownSize.Small);
		IUnit unit = new Unit(playerThree, "three", "unitThree", 5);
		fort.addMember(unit);
		List<IUnit> listThree = new ArrayList<>();
		listThree.add(unit);
		fixtures.add(fort);
		fixtures.add(new Forest("forest", false, 10));
		fixtures.add(new Hill(7));
		addItem(new Unit(playerOne, "four", "unitFour", 6), fixtures, listOne);
		fixtures.add(new Oasis(8));
		LinkedList<TileFixture> shuffled = new LinkedList<>(fixtures);
		Collections.shuffle(shuffled);
		IMutableMapNG map = new SPMapNG(new MapDimensionsImpl(3, 3, 2), new PlayerCollection(), -1);
		Iterator<Point> locations = map.getLocations().iterator();
		while (locations.hasNext() && !shuffled.isEmpty()) {
			Point point = locations.next();
			TileFixture fixture = shuffled.removeFirst();
			map.addFixture(point, fixture);
		}
		IWorkerModel model = new WorkerModel(map);

		// TODO: Each of these assertions passed a method reference to iterableEquality<IUnit>
		// as a fourth parameter to assertEquals(); I don't remember what that parameter did ...
		assertTrue(iterableEquality(listOne,
				filterProxies(model.getUnits(playerOne), ProxyUnit.class)),
			"Got all units for player 1");
		assertTrue(iterableEquality(listTwo,
				filterProxies(model.getUnits(playerTwo), ProxyUnit.class)),
			"Got all units for player 2");
		assertTrue(iterableEquality(listThree,
				filterProxies(model.getUnits(playerThree), ProxyUnit.class)),
			"Got all units for player 3");
	}
}