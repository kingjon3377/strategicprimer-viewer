package drivers.generators;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import common.map.fixtures.mobile.worker.WorkerStats;

import java.util.function.Predicate;

public final class TestMinimumStats {
	// TODO: convert to parameterized
	@Test
	public void testLevellessRestriction() {
		final WorkerStats lowStr = WorkerStats.factory(8, 18, 18, 18, 18, 18);
		final WorkerStats lowDex = WorkerStats.factory(18, 8, 18, 18, 18, 18);
		final WorkerStats lowCon = WorkerStats.factory(18, 18, 8, 18, 18, 18);
		final WorkerStats exceptional = WorkerStats.factory(18, 18, 18, 18, 18, 18);
		for (int i = 1; i < 9; i++) {
			final Predicate<WorkerStats> pred =
					MinimumStats.suitableFor("woodcutter", i);
			assertFalse(pred.test(lowStr), "Low-strength rejected");
			assertFalse(pred.test(lowDex), "Low-dexterity rejected");
			assertFalse(pred.test(lowCon), "Low-constitution rejected");
			assertTrue(pred.test(exceptional), "Doesn't reject everything");
		}
	}

	@Test
	public void testLeveledRestriction() {
		final WorkerStats stats = WorkerStats.factory(12, 18, 18, 18, 18, 18);
		assertTrue(MinimumStats.suitableFor("woodcutter", 1).test(stats),
				"Acceptable at level 1");
		assertFalse(MinimumStats.suitableFor("woodcutter", 3).test(stats), "Not at level 3");
	}
}
