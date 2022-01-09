package lovelace.util;

import org.opentest4j.AssertionFailedError;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Verify that at least one of the given assertions passes.
 *
 * TODO: Split this and any other "test support" classes into a separate
 * module, so we don't have to include JUnit as a dependency for the rest of
 * the suite.
 */
public final class AssertAny {
	private AssertAny() {
	}

	// Adapted from my memory of the class of the same name in the Ceylon SDK
	public static class MultipleFailureException extends AssertionFailedError {
		private static final long serialVersionUID = 0L;
		private final List<AssertionFailedError> failures;
		public MultipleFailureException(List<AssertionFailedError> list, String message) {
			super(message, list.get(0));
			this.failures = list;
		}
		public List<AssertionFailedError> getFailures() {
			return Collections.unmodifiableList(failures);
		}
	}

	/**
	 * Verify that at least one of the given assertions passes.
	 *
	 * @param message the message describing the problem
	 * @param assertions the gorup of assertions
	 */
	public static void assertAny(String message, Runnable... assertions) {
		List<AssertionFailedError> failures = new ArrayList<>();
		for (Runnable assertion : assertions) {
			try {
				assertion.run();
				return;
			} catch (AssertionFailedError failure) {
				failures.add(failure);
			}
		}
		throw new MultipleFailureException(failures, message);
	}

	/**
	 * Verify that at least one of the given assertions passes.
	 *
	 * @param assertions the gorup of assertions
	 */
	public static void assertAny(Runnable... assertions) {
		List<AssertionFailedError> failures = new ArrayList<>();
		for (Runnable assertion : assertions) {
			try {
				assertion.run();
				return;
			} catch (AssertionFailedError failure) {
				failures.add(failure);
			}
		}
		throw new MultipleFailureException(failures,
			String.format("%d assertions failed", failures.size()));
	}
}