package report.generators.tabular;

import java.util.List;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import lovelace.util.DelayedRemovalMap;
import java.util.Arrays;
import java.util.Collections;

import common.DistanceComparator;
import common.map.IFixture;
import common.map.MapDimensions;
import common.map.Point;
import common.map.fixtures.mobile.Immortal;

/**
 * A tabular report generator for {@link Immortal "immortals."}
 */
public class ImmortalsTabularReportGenerator implements ITableGenerator<Immortal> {
	@Override
	public Class<Immortal> narrowedClass() {
		return Immortal.class;
	}

	public ImmortalsTabularReportGenerator(@Nullable final Point hq, final MapDimensions dimensions) {
		this.hq = hq;
		this.dimensions = dimensions;
		if (hq == null) {
			distanceComparator = (one, two) -> 0;
		} else {
			distanceComparator = new DistanceComparator(hq, dimensions);
		}
	}
	private final MapDimensions dimensions;
	@Nullable
	private final Point hq;

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
	 */
	@Override
	public int comparePairs(final Pair<Point, Immortal> one, final Pair<Point, Immortal> two) {
		return Comparator.<Pair<Point, Immortal>, Point>comparing(Pair::getValue0, distanceComparator)
			.thenComparing(p -> p.getValue1().getClass().hashCode())
			.thenComparing(p -> p.getValue1().hashCode()).compare(one, two);
	}
}
