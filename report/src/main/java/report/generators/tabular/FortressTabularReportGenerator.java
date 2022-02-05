package report.generators.tabular;

import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import lovelace.util.DelayedRemovalMap;

import common.DistanceComparator;
import common.map.Player;
import common.map.IFixture;
import common.map.MapDimensions;
import common.map.Point;
import common.map.fixtures.towns.IFortress;

import java.util.Comparator;
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;

/**
 * A tabular report generator for fortresses.
 */
public class FortressTabularReportGenerator implements ITableGenerator<IFortress> {
	@Override
	public Class<IFortress> narrowedClass() {
		return IFortress.class;
	}

	private final Player player;
	@Nullable
	private final Point hq;
	private final MapDimensions dimensions;
	public FortressTabularReportGenerator(final Player player, @Nullable final Point hq, final MapDimensions dimensions) {
		this.player = player;
		this.hq = hq;
		this.dimensions = dimensions;
		if (hq == null) {
			distanceComparator = (one, two) -> 0;
		} else {
			distanceComparator = new DistanceComparator(hq, dimensions);
		}
	}

	/**
	 * The header fields are Distance, Location, Owner, and Name.
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
		return "fortresses";
	}

	private final Comparator<Point> distanceComparator;

	/**
	 * Create a table row representing the fortress.
	 */
	@Override
	public Iterable<Iterable<String>> produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final IFortress item, final int key, final Point loc, final Map<Integer, Integer> parentMap) {
		Iterable<String> retval = Arrays.asList(distanceString(loc, hq, dimensions),
			locationString(loc), ownerString(player, item.getOwner()), item.getName());
		// Players shouldn't be able to see the contents of others' fortresses
		// in other tables.
		if (!player.equals(item.getOwner())) {
			item.stream().map(IFixture::getId).forEach(fixtures::remove);
		}
		fixtures.remove(key);
		return Collections.singleton(retval);
	}

	/**
	 * Compare two fortresses based on whether they are owned by the player
	 * for whom the report is being produced.
	 */
	private int compareOwners(final IFortress one, final IFortress two) {
		if (player.equals(one.getOwner()) && !player.equals(two.getOwner())) {
			return -1;
		} else if (player.equals(two.getOwner()) && !player.equals(one.getOwner())) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * Compare two fortresses' names, with a special case so HQ goes at the top.
	 */
	private static int compareNames(final IFortress one, final IFortress two) {
		if ("HQ".equals(one.getName()) && !"HQ".equals(two.getName())) {
			return -1;
		} else if ("HQ".equals(two.getName()) && !"HQ".equals(one.getName())) {
			return 1;
		} else {
			return one.getName().compareTo(two.getName());
		}
	}

	/**
	 * Compare two Point-IFortress pairs.
	 */
	@Override
	public int comparePairs(final Pair<Point, IFortress> one, final Pair<Point, IFortress> two) {
		return Comparator.<Pair<Point, IFortress>, IFortress>comparing(Pair::getValue1, this::compareOwners)
			.thenComparing(Pair::getValue0, distanceComparator)
			.thenComparing(Pair::getValue1, FortressTabularReportGenerator::compareNames)
			.thenComparing(p -> p.getValue1().getOwner()).compare(one, two);
	}
}
