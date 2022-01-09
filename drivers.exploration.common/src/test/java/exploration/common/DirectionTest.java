package exploration.common;

import org.javatuples.Pair;

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

import common.map.Point;
import common.map.PlayerCollection;
import common.map.MapDimensionsImpl;
import common.map.Direction;
import common.map.SPMapNG;

/**
 * Tests that the movement code gets its most basic functionality, namely
 * finding adjacent tiles, right.
 */
public class DirectionTest {
	private static interface DirectionAssertion {
		void call(Point source, Point destination, String extraMessage);
	}

	/**
	 * A custom assertion for these tests.
	 */
	private static DirectionAssertion directionAssert(IExplorationModel model, Direction direction) {
		return (source, destination, extraMessage) ->
			assertEquals(destination, model.getDestination(source, direction),
				String.format("%s of %s%s is %s", direction, source,
					extraMessage, destination));
	}

	/**
	 * Test that wrapping to the east works properly.
	 */
	@Test
	public void testEast() {
		IExplorationModel model = new ExplorationModel(new SPMapNG(new MapDimensionsImpl(5, 5, 2),
			new PlayerCollection(), 0));
		DirectionAssertion localAssert = directionAssert(model, Direction.East);
		localAssert.call(new Point(0, 0), new Point(0, 1), "");
		localAssert.call(new Point(1, 1), new Point(1, 2), "");
		localAssert.call(new Point(3, 4), new Point(3, 0), " in a 5x5 map");
		localAssert.call(new Point(4, 3), new Point(4, 4), "");
	}

	/**
	 * Test that wrapping to the north works properly.
	 */
	@Test
	public void testNorth() {
		IExplorationModel model = new ExplorationModel(new SPMapNG(new MapDimensionsImpl(5, 5, 2),
			new PlayerCollection(), 0));
		DirectionAssertion localAssert = directionAssert(model, Direction.North);
		localAssert.call(new Point(0, 0), new Point(4, 0), " in a 5x5 map");
		localAssert.call(new Point(1, 1), new Point(0, 1), "");
		localAssert.call(new Point(3, 4), new Point(2, 4), "");
		localAssert.call(new Point(4, 3), new Point(3, 3), "");
	}

	/**
	 * Test that wrapping to the south works properly.
	 */
	@Test
	public void testSouth() {
		IExplorationModel model = new ExplorationModel(new SPMapNG(new MapDimensionsImpl(5, 5, 2),
			new PlayerCollection(), 0));
		DirectionAssertion localAssert = directionAssert(model, Direction.South);
		localAssert.call(new Point(0, 0), new Point(1, 0), "");
		localAssert.call(new Point(1, 1), new Point(2, 1), "");
		localAssert.call(new Point(3, 4), new Point(4, 4), "");
		localAssert.call(new Point(4, 3), new Point(0, 3), " in a 5x5 map");
	}

	/**
	 * Test that wrapping to the west works properly.
	 */
	@Test
	public void testWest() {
		IExplorationModel model = new ExplorationModel(new SPMapNG(new MapDimensionsImpl(5, 5, 2),
			new PlayerCollection(), 0));
		DirectionAssertion localAssert = directionAssert(model, Direction.West);
		localAssert.call(new Point(0, 0), new Point(0, 4), " in a 5x5 map");
		localAssert.call(new Point(1, 1), new Point(1, 0), "");
		localAssert.call(new Point(3, 4), new Point(3, 3), "");
		localAssert.call(new Point(4, 3), new Point(4, 2), "");
	}

	/**
	 * Test that wrapping to the northeast works properly.
	 */
	@Test
	public void testNortheast() {
		IExplorationModel model = new ExplorationModel(new SPMapNG(new MapDimensionsImpl(5, 5, 2),
			new PlayerCollection(), 0));
		DirectionAssertion localAssert = directionAssert(model, Direction.Northeast);
		localAssert.call(new Point(0, 0), new Point(4, 1), " in a 5x5 map");
		localAssert.call(new Point(1, 1), new Point(0, 2), "");
		localAssert.call(new Point(3, 4), new Point(2, 0), "");
		localAssert.call(new Point(4, 3), new Point(3, 4), "");
	}

	/**
	 * Test that wrapping to the northwest works properly.
	 */
	@Test
	public void testNorthwest() {
		IExplorationModel model = new ExplorationModel(new SPMapNG(new MapDimensionsImpl(5, 5, 2),
			new PlayerCollection(), 0));
		DirectionAssertion localAssert = directionAssert(model, Direction.Northwest);
		localAssert.call(new Point(0, 0), new Point(4, 4), " in a 5x5 map");
		localAssert.call(new Point(1, 1), new Point(0, 0), "");
		localAssert.call(new Point(3, 4), new Point(2, 3), "");
		localAssert.call(new Point(4, 3), new Point(3, 2), "");
	}

	/**
	 * Test that wrapping to the southeast works properly.
	 */
	@Test
	public void testSoutheast() {
		IExplorationModel model = new ExplorationModel(new SPMapNG(new MapDimensionsImpl(5, 5, 2),
			new PlayerCollection(), 0));
		DirectionAssertion localAssert = directionAssert(model, Direction.Southeast);
		localAssert.call(new Point(0, 0), new Point(1, 1), "");
		localAssert.call(new Point(1, 1), new Point(2, 2), "");
		localAssert.call(new Point(3, 4), new Point(4, 0), " in a 5x5 map");
		localAssert.call(new Point(4, 3), new Point(0, 4), " in a 5x5 map");
	}

	/**
	 * Test that wrapping to the south works properly.
	 */
	@Test
	public void testSouthwest() {
		IExplorationModel model = new ExplorationModel(new SPMapNG(new MapDimensionsImpl(5, 5, 2),
			new PlayerCollection(), 0));
		DirectionAssertion localAssert = directionAssert(model, Direction.Southwest);
		localAssert.call(new Point(0, 0), new Point(1, 4), " in a 5x5 map");
		localAssert.call(new Point(1, 1), new Point(2, 0), "");
		localAssert.call(new Point(3, 4), new Point(4, 3), "");
		localAssert.call(new Point(4, 3), new Point(0, 2), " in a 5x5 map");
	}

	/**
	 * Test that "movement" to "nowhere" works properly.
	 */
	@Test
	public void testNowhere() {
		IExplorationModel model = new ExplorationModel(new SPMapNG(new MapDimensionsImpl(5, 5, 2),
			new PlayerCollection(), 0));
		DirectionAssertion localAssert = directionAssert(model, Direction.Nowhere);
		localAssert.call(new Point(0, 0), new Point(0, 0), "");
		localAssert.call(new Point(1, 1), new Point(1, 1), "");
		localAssert.call(new Point(3, 4), new Point(3, 4), "");
		localAssert.call(new Point(4, 3), new Point(4, 3), "");
	}
}