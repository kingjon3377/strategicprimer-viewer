package report.generators.tabular;

import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import lovelace.util.DelayedRemovalMap;

import common.DistanceComparator;
import common.map.Player;
import common.map.IFixture;
import common.map.MapDimensions;
import common.map.Point;
import common.map.fixtures.towns.Village;
import java.util.Map;
import java.util.Collections;
import java.util.Arrays;
import java.util.Comparator;

/**
 * A tabular report generator for villages.
 */
public class VillageTabularReportGenerator implements ITableGenerator<Village> {
	@Override
	public Class<Village> narrowedClass() {
		return Village.class;
	}

	public VillageTabularReportGenerator(Player player, @Nullable Point hq, MapDimensions dimensions) {
		this.player = player;
		this.hq = hq;
		this.dimensions = dimensions;
		if (hq == null) {
			distanceComparator = (one, two) -> 0;
		} else {
			distanceComparator = new DistanceComparator(hq, dimensions);
		}
	}

	private final Player player;
	@Nullable
	private final Point hq;
	private final MapDimensions dimensions;

	/**
	 * The header of this table.
	 */
	@Override
	public Iterable<String> getHeaderRow() {
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
	public Iterable<Iterable<String>> produce(
			DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures, Village item,
			int key, Point loc, Map<Integer, Integer> parentMap) {
		fixtures.remove(key);
		return Collections.singleton(Arrays.asList(distanceString(loc, hq, dimensions),
			locationString(loc), ownerString(player, item.getOwner()), item.getName()));
	}

	/**
	 * Compare two location-and-village pairs.
	 */
	@Override
	public int comparePairs(Pair<Point, Village> one, Pair<Point, Village> two) {
		return Comparator.<Pair<Point, Village>, Point>comparing(Pair::getValue0, distanceComparator)
			.thenComparing(Comparator.comparing(Pair::getValue1,
				Comparator.comparing(Village::getOwner)
					.thenComparing(Village::getName)))
			.compare(one, two);
	}
}
