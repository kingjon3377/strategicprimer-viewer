package lovelace.util;

/**
 * A class encapsulating a range of integers, to fluently test whether other
 * integers are in the range. (Replacement for Ceylon's <code>Range&lt;Integer&gt;</code>.)
 */
public class Range {
	public Range(int lowerBound, int upperBound) {
		if (upperBound < lowerBound) {
			throw new IllegalArgumentException(
				"Upper bound must be greater than or equal to lower bound");
		}
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	private final int lowerBound;
	private final int upperBound;

	public int getLowerBound() {
		return lowerBound;
	}

	public int getUpperBound() {
		return upperBound;
	}

	public boolean contains(final int num) {
		return num >= lowerBound && num <= upperBound;
	}

	public int size() {
		return upperBound - lowerBound + 1;
	}
}
