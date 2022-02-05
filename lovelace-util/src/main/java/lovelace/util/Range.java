package lovelace.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;

/**
 * A class encapsulating a range of integers, to fluently test whether other
 * integers are in the range. (Replacement for Ceylon's {@code Range<Integer>}.)
 */
public class Range implements Iterable<Integer> {
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

	@NotNull
	@Override
	public Iterator<Integer> iterator() {
		return new RangeIterator(this);
	}

	private static class RangeIterator implements Iterator<Integer> {
		int current;
		int upperBound;
		public RangeIterator(Range range) {
			this.current = range.lowerBound - 1;
			this.upperBound = range.upperBound;
		}

		@Override
		public boolean hasNext() {
			return current < upperBound;
		}

		@Override
		public Integer next() {
			if (current < upperBound) {
				current++;
				return current;
			} else {
				throw new NoSuchElementException();
			}
		}
	}
}
