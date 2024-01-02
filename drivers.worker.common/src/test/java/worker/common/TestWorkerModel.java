package worker.common;

import java.util.Set;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import legacy.map.LegacyPlayerCollection;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import legacy.map.IMutableLegacyMap;
import legacy.map.LegacyMap;
import legacy.map.Point;
import legacy.map.MapDimensionsImpl;
import legacy.map.Player;
import legacy.map.PlayerImpl;
import legacy.map.TileFixture;
import common.map.PlayerCollection;

import legacy.map.fixtures.mobile.ProxyFor;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.Unit;
import legacy.map.fixtures.mobile.ProxyUnit;
import legacy.map.fixtures.mobile.AnimalImpl;
import legacy.map.fixtures.terrain.Oasis;
import legacy.map.fixtures.terrain.Hill;
import legacy.map.fixtures.terrain.Forest;
import legacy.map.fixtures.towns.IMutableFortress;
import common.map.fixtures.towns.TownSize;
import legacy.map.fixtures.towns.FortressImpl;
import drivers.common.IWorkerModel;

/**
 * Test of the worker model.
 */
public class TestWorkerModel {
    /**
     * Helper method: Flatten any proxies in the list by replacing them with what they are proxies for.
     */
    private static <T> Iterable<T> filterProxies(final Iterable<T> list, final Class<? extends ProxyFor<?>> cls) {
        final List<T> retval = new ArrayList<>();
        for (final T item : list) {
            if (cls.isInstance(item)) {
                retval.addAll(((ProxyFor<? extends T>) item).getProxied());
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
    private static <T> void addItem(final T item, final List<? super T>... lists) {
        for (final List<? super T> list : lists) {
            list.add(item);
        }
    }

    /**
     * Test whether two collections contain the same elements (ignoring order and duplications!), or for
     * non-iterable objects test object equality.
     */
    private static boolean iterableEquality(final Object one, final Object two) {
        if (one instanceof Iterable && two instanceof Iterable) {
            final Set<Object> listOne = StreamSupport.stream(((Iterable<?>) one).spliterator(), false)
                    .collect(Collectors.toSet());
            final Set<Object> listTwo = StreamSupport.stream(((Iterable<?>) two).spliterator(), false)
                    .collect(Collectors.toSet());
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
        final List<TileFixture> fixtures = new ArrayList<>();
        fixtures.add(new Oasis(14));
        fixtures.add(new AnimalImpl("animal", false, "wild", 1));
        final List<IUnit> listOne = new ArrayList<>();
        final Player playerOne = new PlayerImpl(0, "player1");
        addItem(new Unit(playerOne, "one", "unitOne", 2), fixtures, listOne);
        final List<IUnit> listTwo = new ArrayList<>();
        final Player playerTwo = new PlayerImpl(1, "player2");
        addItem(new Unit(playerTwo, "two", "unitTwo", 3), fixtures, listTwo);
        final Player playerThree = new PlayerImpl(2, "player3");
        final Player playerFour = new PlayerImpl(3, "player4");
        final IMutableFortress fort = new FortressImpl(playerFour, "fort", 4, TownSize.Small);
        final IUnit unit = new Unit(playerThree, "three", "unitThree", 5);
        fort.addMember(unit);
        final List<IUnit> listThree = new ArrayList<>();
        listThree.add(unit);
        fixtures.add(fort);
        fixtures.add(new Forest("forest", false, 10));
        fixtures.add(new Hill(7));
        addItem(new Unit(playerOne, "four", "unitFour", 6), fixtures, listOne);
        fixtures.add(new Oasis(8));
        final LinkedList<TileFixture> shuffled = new LinkedList<>(fixtures);
        Collections.shuffle(shuffled);
        final IMutableLegacyMap map = new LegacyMap(new MapDimensionsImpl(3, 3, 2), new LegacyPlayerCollection(), -1);
        final Iterator<Point> locations = map.getLocations().iterator();
        while (locations.hasNext() && !shuffled.isEmpty()) {
            final Point point = locations.next();
            final TileFixture fixture = shuffled.removeFirst();
            map.addFixture(point, fixture);
        }
        final IWorkerModel model = new WorkerModel(map);

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
