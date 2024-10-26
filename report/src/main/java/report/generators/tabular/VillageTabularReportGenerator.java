package report.generators.tabular;

import java.util.List;

import legacy.DistanceComparatorImpl;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import lovelace.util.DelayedRemovalMap;

import legacy.map.Player;
import legacy.map.IFixture;
import legacy.map.MapDimensions;
import legacy.map.Point;
import legacy.map.fixtures.towns.Village;

import java.util.Map;
import java.util.Collections;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * A tabular report generator for villages.
 */
public class VillageTabularReportGenerator implements ITableGenerator<Village> {
	@Override
	public boolean canHandle(final IFixture fixture) {
		return fixture instanceof Village;
	}

	public VillageTabularReportGenerator(final Player player, final @Nullable Point hq,
	                                     final MapDimensions dimensions) {
		this.player = player;
		this.hq = hq;
		this.dimensions = dimensions;
		if (Objects.isNull(hq)) {
			distanceComparator = (one, two) -> 0;
		} else {
			distanceComparator = new DistanceComparatorImpl(hq, dimensions);
		}
	}

	private final Player player;
	private final @Nullable Point hq;
	private final MapDimensions dimensions;

	/**
	 * The header of this table.
	 */
	@Override
	public List<String> getHeaderRow() {
		return Arrays.asList("Distance", "Location", "Owner", "Name");
	}

	/**
	 * The file-name to (by default) write this table to.
	 */
	@Override
	public String getTableName() {
		return "villages";
	}

	private final Comparator<Point> distanceComparator;

	/**
	 * Create a table row representing the village.
	 */
	@Override
	public List<List<String>> produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures, final Village item,
			final int key, final Point loc, final Map<Integer, Integer> parentMap) {
		fixtures.remove(key);
		return Collections.singletonList(Arrays.asList(distanceString(loc, hq, dimensions),
				locationString(loc), ownerString(player, item.owner()), item.getName()));
	}

	/**
	 * Compare two location-and-village pairs.
	 *
	 * @return
	 */
	@Override
	public Comparator<Pair<Point, Village>> comparePairs() {
		return Comparator.<Pair<Point, Village>, Point>comparing(Pair::getValue0, distanceComparator)
				.thenComparing(Pair::getValue1, Comparator.comparing(Village::owner)
						.thenComparing(Village::getName));
	}
}
