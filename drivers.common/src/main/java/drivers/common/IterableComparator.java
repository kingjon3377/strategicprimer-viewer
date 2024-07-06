package drivers.common;

import java.util.Comparator;

/**
 * An interface to allow a method to have a single argument that is both an Iterable and a Comparator.
 * @param <IterationType>
 * @param <ComparisonType>
 */
public interface IterableComparator<IterationType, ComparisonType>
		extends Iterable<IterationType>, Comparator<ComparisonType> {
}
