package report.generators.tabular;

import java.util.List;

import legacy.DistanceComparatorImpl;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lovelace.util.DelayedRemovalMap;

import legacy.map.Player;
import legacy.map.IFixture;
import legacy.map.MapDimensions;
import legacy.map.Point;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.Animal;
import legacy.map.fixtures.UnitMember;

import java.util.Comparator;
import java.util.Map;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Collections;

/**
 * A tabular report generator for units.
 */
public final class UnitTabularReportGenerator implements ITableGenerator<IUnit> {
	@Override
	public boolean canHandle(final IFixture fixture) {
		return fixture instanceof IUnit;
	}

	private final Player player;
	private final @Nullable Point hq;
	private final MapDimensions dimensions;

	public UnitTabularReportGenerator(final Player player, final @Nullable Point hq, final MapDimensions dimensions) {
		this.player = player;
		this.hq = hq;
		this.dimensions = dimensions;
		if (Objects.isNull(hq)) {
			distanceComparator = (one, two) -> 0;
		} else {
			distanceComparator = new DistanceComparatorImpl(hq, dimensions);
		}
	}

	/**
	 * The header row for this table.
	 */
	@Override
	public List<String> getHeaderRow() {
		return Arrays.asList("Distance", "Location", "Owner", "Kind/Category", "Name", "Orders",
				"ID #");
	}

	/**
	 * The file-name to (by default) write this table to.
	 */
	@Override
	public String getTableName() {
		return "units";
	}

	private final Comparator<Point> distanceComparator;

	/**
	 * Create a GUI table row representing the unit.
	 */
	@Override
	public List<List<String>> produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures, final IUnit item,
			final int key, final Point loc, final Map<Integer, Integer> parentMap) {
		final List<String> retval = Arrays.asList(distanceString(loc, hq, dimensions),
				locationString(loc), ownerString(player, item.owner()), item.getKind(),
				item.getName(),
				Optional.ofNullable(item.getAllOrders().lastEntry())
						.map(Map.Entry::getValue).orElse(""),
				(player.equals(item.owner())) ? Integer.toString(item.getId()) : "---");
		for (final UnitMember member : item) {
			if (member instanceof Animal) {
				// We don't want animals inside a unit showing up in the wild-animal report
				fixtures.remove(member.getId());
			} else if (!player.equals(item.owner())) {
				// A player shouldn't be able to see the details of another player's units.
				fixtures.remove(member.getId());
			}
		}
		fixtures.remove(key);
		return Collections.singletonList(retval);
	}

	/**
	 * Compare two location-unit pairs.
	 *
	 * @return
	 */
	@Override
	public Comparator<Pair<Point, IUnit>> comparePairs() {
		return Comparator.<Pair<Point, IUnit>, Point>comparing(Pair::getValue0, distanceComparator)
				.thenComparing(Pair::getValue1,
						Comparator.comparing(IUnit::owner)
								.thenComparing(IUnit::getKind)
								.thenComparing(IUnit::getName));
	}

	@Override
	public @NotNull Class<IUnit> getTableClass() {
		return IUnit.class;
	}
}
