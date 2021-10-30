package common.map.fixtures.mobile.worker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A test that {@link WorkerStats#getModifier} calculates the modifier for all
 * stats correctly.
 */
public final class StatModifierTest {
	@Test
	public void testModifiers() {
		List<Integer> stats = IntStream.range(0, 20).boxed().collect(Collectors.toList());
		List<Integer> modifiers = Arrays.asList(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4);
		Iterator<Integer> statsIterator = stats.iterator();
		for (Integer modifier : modifiers) {
			assertTrue(statsIterator.hasNext());
			final int statOne = statsIterator.next();
			assertEquals(modifier.intValue(), WorkerStats.getModifier(statOne),
				"Even stat has correct modifier");
			assertTrue(statsIterator.hasNext());
			final int statTwo = statsIterator.next();
			assertEquals(modifier.intValue(), WorkerStats.getModifier(statTwo),
				"Odd stat has correct modifier");
		}
	}
}
