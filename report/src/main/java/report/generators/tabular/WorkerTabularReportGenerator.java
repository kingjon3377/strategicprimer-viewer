package report.generators.tabular;

import java.util.List;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import lovelace.util.DelayedRemovalMap;

import common.DistanceComparator;
import common.map.IFixture;
import common.map.MapDimensions;
import common.map.Point;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.worker.WorkerStats;

import java.util.Comparator;
import java.util.Map;
import java.util.Collections;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

/**
 * A report generator for workers. We do not cover Jobs or Skills; see {@link
 * SkillTabularReportGenerator} for that.
 */
public class WorkerTabularReportGenerator implements ITableGenerator<IWorker> {
	@Override
	public Class<IWorker> narrowedClass() {
		return IWorker.class;
	}

	@Nullable
	private final Point hq;
	private final MapDimensions dimensions;
	public WorkerTabularReportGenerator(@Nullable final Point hq, final MapDimensions dimensions) {
		this.hq = hq;
		this.dimensions = dimensions;
		if (hq == null) {
			distanceComparator = (one, two) -> 0;
		} else {
			distanceComparator = new DistanceComparator(hq, dimensions);
		}
	}

	/**
	 * The header row of the table.
	 */
	@Override
	public List<String> getHeaderRow() {
		return Arrays.asList("Distance", "Location", "Name", "Race",
			"HP", "Max HP", "Str", "Dex", "Con", "Int", "Wis", "Cha");
	}

	/**
	 * The file-name to (by default) write this table to.
	 */
	@Override
	public String getTableName() {
		return "workers";
	}

	private final Comparator<Point> distanceComparator;

	/**
	 * Create a GUI table row representing a worker.
	 */
	@Override
	public List<List<String>> produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures, final IWorker item,
			final int key, final Point loc, final Map<Integer, Integer> parentMap) {
		fixtures.remove(key);
		WorkerStats stats = item.getStats();
		if (stats == null) {
			return Collections.singletonList(Stream.concat(Stream.of(
					distanceString(loc, hq, dimensions), locationString(loc),
					item.getName(), item.getRace()),
				Stream.generate(() -> "---").limit(9)).collect(Collectors.toList()));
		} else {
			return Collections.singletonList(Stream.concat(Stream.of(
					distanceString(loc, hq, dimensions), locationString(loc),
					item.getName(), item.getRace(),
					Integer.toString(stats.getHitPoints()),
					Integer.toString(stats.getMaxHitPoints())),
				IntStream.of(stats.array()).mapToObj(WorkerStats::getModifierString))
					.collect(Collectors.toList()));
		}
	}

	/**
	 * Compare two worker-location pairs.
	 */
	@Override
	public int comparePairs(final Pair<Point, IWorker> one, final Pair<Point, IWorker> two) {
		return Comparator.<Pair<Point, IWorker>, Point>comparing(Pair::getValue0, distanceComparator)
			.thenComparing(p -> p.getValue1().getName()).compare(one, two);
	}
}
