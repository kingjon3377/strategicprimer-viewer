package legacy.map;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests that {@link PointIterable} works properly for each possible configuration.
 */
public final class TestPointIterable {
	/**
	 * Test iteration forwards, horizontally, from the beginning of the map.
	 */
	@Test
	public void testFromBeginning() {
		assertIterableEquals(Arrays.asList(new Point(0, 0), new Point(0, 1),
						new Point(0, 2), new Point(1, 0), new Point(1, 1), new Point(1, 2),
						new Point(2, 0), new Point(2, 1), new Point(2, 2)),
				new PointIterable(new MapDimensionsImpl(3, 3, 1), true, true),
				"Iterator should produce points in the expected order when given no starting point and iterating forwards horizontally.");
	}

	/**
	 * Test iteration forwards, horizontally, from a selected point.
	 */
	@Test
	public void testFromSelection() {
		assertIterableEquals(Arrays.asList(new Point(1, 2), new Point(2, 0), new Point(2, 1),
						new Point(2, 2), new Point(0, 0), new Point(0, 1), new Point(0, 2),
						new Point(1, 0), new Point(1, 1)),
				new PointIterable(new MapDimensionsImpl(3, 3, 1), true, true, new Point(1, 1)),
				"Iterator should produce points in the expected order when given a starting point and iterating forwards horizontally.");
	}

	/**
	 * Test searching forwards, vertically, from the "selection" the viewer starts with.
	 */
	@Test
	public void testInitialSelection() {
		assertIterableEquals(Arrays.asList(new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(0, 1),
						new Point(1, 1), new Point(2, 1), new Point(0, 2), new Point(1, 2), new Point(2, 2)),
				new PointIterable(new MapDimensionsImpl(3, 3, 1), true, false, Point.INVALID_POINT),
				"Iterator should produce points in the expected order when starting at {@link Point#invalidPoint} and iterating forwards vertically.");
	}

	/**
	 * Test searching backwards, horizontally.
	 */
	@Test
	public void testReverse() {
		assertIterableEquals(Arrays.asList(new Point(2, 2), new Point(2, 1), new Point(2, 0),
						new Point(1, 2), new Point(1, 1), new Point(1, 0), new Point(0, 2), new Point(0, 1),
						new Point(0, 0)), new PointIterable(new MapDimensionsImpl(3, 3, 1), false, true),
				"Iterator should produce points in the expected order when iterating backwards horizontally.");
	}

	/**
	 * Test searching vertically, backwards.
	 */
	@Test
	public void testVerticalReverse() {
		assertIterableEquals(Arrays.asList(new Point(2, 2), new Point(1, 2), new Point(0, 2),
						new Point(2, 1), new Point(1, 1), new Point(0, 1), new Point(2, 0),
						new Point(1, 0), new Point(0, 0)), new PointIterable(new MapDimensionsImpl(3, 3, 1), false, false),
				"Iterator should produce points in the expected order when iterating backwards vertically.");
	}
}
