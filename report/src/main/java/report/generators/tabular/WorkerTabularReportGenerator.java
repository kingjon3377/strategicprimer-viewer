package report.generators.tabular;

import java.util.List;

import legacy.DistanceComparatorImpl;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lovelace.util.DelayedRemovalMap;

import legacy.map.IFixture;
import legacy.map.MapDimensions;
import legacy.map.Point;
import legacy.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.worker.WorkerStats;

import java.util.Comparator;
import java.util.Map;
import java.util.Collections;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

/**
 * A report generator for workers. We do not cover Jobs or Skills; see {@link
 * SkillTabularReportGenerator} for that.
 *
 * TODO: Should probably include something about mount and/or equipment
 */
public final class WorkerTabularReportGenerator implements ITableGenerator<IWorker> {
	@Override
	public boolean canHandle(final IFixture fixture) {
		return fixture instanceof IWorker;
	}

	private final @Nullable Point hq;
	private final MapDimensions dimensions;

	public WorkerTabularReportGenerator(final @Nullable Point hq, final MapDimensions dimensions) {
		this.hq = hq;
		this.dimensions = dimensions;
		if (Objects.isNull(hq)) {
			distanceComparator = (one, two) -> 0;
		} else {
			distanceComparator = new DistanceComparatorImpl(hq, dimensions);
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
		final WorkerStats stats = item.getStats();
		if (Objects.isNull(stats)) {
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
	 *
	 * @return the result of the comparison
	 */
	@Override
	public Comparator<Pair<Point, IWorker>> comparePairs() {
		return Comparator.<Pair<Point, IWorker>, Point>comparing(Pair::getValue0, distanceComparator)
				.thenComparing(p -> p.getValue1().getName());
	}

	@Override
	public @NotNull Class<IWorker> getTableClass() {
		return IWorker.class;
	}
}
