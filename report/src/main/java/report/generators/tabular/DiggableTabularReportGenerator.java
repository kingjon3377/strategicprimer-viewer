package report.generators.tabular;

import java.util.List;

import legacy.DistanceComparatorImpl;
import legacy.map.fixtures.resources.ExposureStatus;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lovelace.util.DelayedRemovalMap;

import legacy.map.IFixture;
import legacy.map.MapDimensions;
import legacy.map.Point;
import legacy.map.fixtures.Ground;
import legacy.map.fixtures.MineralFixture;
import legacy.map.fixtures.resources.MineralVein;
import legacy.map.fixtures.resources.Mine;
import legacy.map.fixtures.resources.StoneDeposit;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Collections;
import java.util.Objects;

/**
 * A tabular report generator for resources that can be mined---mines, mineral
 * veins, stone deposits, and Ground.
 */
public final class DiggableTabularReportGenerator implements ITableGenerator<MineralFixture> {
	@Override
	public boolean canHandle(final IFixture fixture) {
		return fixture instanceof MineralFixture;
	}

	public DiggableTabularReportGenerator(final @Nullable Point hq, final MapDimensions dimensions) {
		this.hq = hq;
		this.dimensions = dimensions;
		if (Objects.isNull(hq)) {
			distanceComparator = (one, two) -> 0;
		} else {
			distanceComparator = new DistanceComparatorImpl(hq, dimensions);
		}
	}

	private final @Nullable Point hq;

	private final MapDimensions dimensions;

	/**
	 * The header row for the table.
	 */
	@Override
	public List<String> getHeaderRow() {
		return Arrays.asList("Distance", "Location", "Kind", "Product", "Status");
	}

	/**
	 * The file-name to (by default) write this table to.
	 */
	@Override
	public String getTableName() {
		return "minerals";
	}

	private final Comparator<Point> distanceComparator;

	/**
	 * Create a table row representing a fixture.
	 */
	@Override
	public List<List<String>> produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures, final MineralFixture item,
			final int key, final Point loc, final Map<Integer, Integer> parentMap) {
		final String classField;
		final String statusField;
		switch (item) {
			case final Ground g when g.getExposure() == ExposureStatus.EXPOSED -> {
				classField = "ground";
				statusField = "exposed";
			}
			case final Ground g when g.getExposure() == ExposureStatus.HIDDEN -> {
				classField = "ground";
				statusField = "not exposed";
			}
			case final Mine m -> {
				classField = "mine";
				statusField = m.getStatus().toString();
			}
			case final StoneDeposit stoneDeposit -> {
				classField = "deposit";
				statusField = "exposed";
			}
			case final MineralVein mv when mv.getExposure() == ExposureStatus.EXPOSED -> {
				classField = "vein";
				statusField = "exposed";
			}
			case final MineralVein mv when mv.getExposure() == ExposureStatus.HIDDEN -> {
				classField = "vein";
				statusField = "not exposed";
			}
			default -> {
				return Collections.emptyList();
			}
		}
		fixtures.remove(key);
		return Collections.singletonList(Arrays.asList(distanceString(loc, hq, dimensions),
				locationString(loc), classField, item.getKind(), statusField));
	}

	/**
	 * Compare two Point-fixture pairs.
	 */
	@Override
	public Comparator<Pair<Point, MineralFixture>> comparePairs() {
		return Comparator.<Pair<Point, MineralFixture>, String>comparing(p -> p.getValue1().getKind())
				.thenComparing(Pair::getValue0, distanceComparator)
				.thenComparing(p -> p.getValue1().hashCode());
	}

	@Override
	public @NotNull Class<MineralFixture> getTableClass() {
		return MineralFixture.class;
	}
}
