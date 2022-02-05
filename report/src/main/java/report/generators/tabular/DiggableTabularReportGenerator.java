package report.generators.tabular;

import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import lovelace.util.DelayedRemovalMap;

import common.DistanceComparator;
import common.map.IFixture;
import common.map.MapDimensions;
import common.map.Point;
import common.map.fixtures.Ground;
import common.map.fixtures.MineralFixture;
import common.map.fixtures.resources.MineralVein;
import common.map.fixtures.resources.Mine;
import common.map.fixtures.resources.StoneDeposit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Collections;

/**
 * A tabular report generator for resources that can be mined---mines, mineral
 * veins, stone deposits, and Ground.
 */
public class DiggableTabularReportGenerator implements ITableGenerator<MineralFixture> {
	@Override
	public Class<MineralFixture> narrowedClass() {
		return MineralFixture.class;
	}

	public DiggableTabularReportGenerator(@Nullable final Point hq, final MapDimensions dimensions) {
		this.hq = hq;
		this.dimensions = dimensions;
		if (hq == null) {
			distanceComparator = (one, two) -> 0;
		} else {
			distanceComparator = new DistanceComparator(hq, dimensions);
		}
	}

	@Nullable
	private final Point hq;

	private final MapDimensions dimensions;

	/**
	 * The header row for the table.
	 */
	@Override
	public Iterable<String> getHeaderRow() {
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
	public Iterable<Iterable<String>> produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures, final MineralFixture item,
			final int key, final Point loc, final Map<Integer, Integer> parentMap) {
		String classField;
		String statusField;
		if (item instanceof Ground) {
			classField = "ground";
			statusField = (((Ground) item).isExposed()) ? "exposed" : "not exposed";
		} else if (item instanceof Mine) {
			classField = "mine";
			statusField = ((Mine) item).getStatus().toString();
		} else if (item instanceof StoneDeposit) {
			classField = "deposit";
			statusField = "exposed";
		} else if (item instanceof MineralVein) {
			classField = "vein";
			statusField = (((MineralVein) item).isExposed()) ? "exposed" : "not exposed";
		} else {
			return Collections.emptyList();
		}
		fixtures.remove(key);
		return Collections.singleton(Arrays.asList(distanceString(loc, hq, dimensions),
			locationString(loc), classField, item.getKind(), statusField));
	}

	/**
	 * Compare two Point-fixture pairs.
	 *
	 * TODO: Make this return Comparator in the interface, and not take 'one' and 'two' here?
	 */
	@Override
	public int comparePairs(final Pair<Point, MineralFixture> one, final Pair<Point, MineralFixture> two) {
		return Comparator.<Pair<Point, MineralFixture>, String>comparing(p -> p.getValue1().getKind())
			.thenComparing(Pair::getValue0, distanceComparator)
			.thenComparing(p -> p.getValue1().hashCode()).compare(one, two);
	}
}
