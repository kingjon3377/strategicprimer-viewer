package report.generators.tabular;

import java.util.List;

import legacy.DistanceComparatorImpl;
import org.javatuples.Pair;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;

import lovelace.util.DelayedRemovalMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import legacy.map.IFixture;
import legacy.map.MapDimensions;
import legacy.map.Point;
import legacy.map.fixtures.mobile.Immortal;

/**
 * A tabular report generator for {@link Immortal "immortals."}
 */
public final class ImmortalsTabularReportGenerator implements ITableGenerator<Immortal> {
	@Override
	public boolean canHandle(final IFixture fixture) {
		return fixture instanceof Immortal;
	}

	public ImmortalsTabularReportGenerator(final @Nullable Point hq, final MapDimensions dimensions) {
		this.hq = hq;
		this.dimensions = dimensions;
		if (Objects.isNull(hq)) {
			distanceComparator = (one, two) -> 0;
		} else {
			distanceComparator = new DistanceComparatorImpl(hq, dimensions);
		}
	}

	private final MapDimensions dimensions;
	private final @Nullable Point hq;

	/**
	 * The header row for this table.
	 */
	@Override
	public List<String> getHeaderRow() {
		return Arrays.asList("Distance", "Location", "Immortal");
	}

	/**
	 * The file-name to (by default) write this table to.
	 */
	@Override
	public String getTableName() {
		return "immortals";
	}

	private final Comparator<Point> distanceComparator;

	/**
	 * Create a table row representing the given fixture.
	 */
	@Override
	public List<List<String>> produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final Immortal item, final int key, final Point loc, final Map<Integer, Integer> parentMap) {
		fixtures.remove(key);
		return Collections.singletonList(Arrays.asList(distanceString(loc, hq, dimensions),
				locationString(loc), item.toString()));
	}

	/**
	 * Compare two Point-fixture pairs.
	 *
	 * @return the result of the comparison
	 */
	@Override
	public Comparator<Pair<Point, Immortal>> comparePairs() {
		return Comparator.<Pair<Point, Immortal>, Point>comparing(Pair::getValue0, distanceComparator)
				.thenComparing(p -> p.getValue1().getClass().hashCode())
				.thenComparing(p -> p.getValue1().hashCode());
	}

	@Override
	public Class<Immortal> getTableClass() {
		return Immortal.class;
	}
}
