package report.generators;

import java.util.function.Consumer;
import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

import lovelace.util.DelayedRemovalMap;

import common.map.IFixture;
import common.map.Point;
import common.map.MapDimensions;
import common.map.IMapNG;
import common.map.fixtures.explorable.Cave;
import common.map.fixtures.explorable.Portal;
import common.map.fixtures.explorable.Battlefield;
import common.map.fixtures.explorable.ExplorableFixture;

/**
 * A report generator for caves, battlefields, and portals.
 */
public class ExplorableReportGenerator extends AbstractReportGenerator<ExplorableFixture> {
	public ExplorableReportGenerator(final MapDimensions dimensions) {
		this(dimensions, null);
	}
	public ExplorableReportGenerator(final MapDimensions dimensions, final @Nullable Point hq) {
		super(dimensions, hq);
	}

	/**
	 * Produces a more verbose sub-report on a cave, battlefield, or portal.
	 */
	@Override
	public void produceSingle(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                          final IMapNG map, final Consumer<String> ostream, final ExplorableFixture item, final Point loc) {
		if (item instanceof Cave) {
			fixtures.remove(item.getId());
			ostream.accept("Caves beneath ");
		} else if (item instanceof Battlefield) {
			fixtures.remove(item.getId());
			ostream.accept("Signs of a long-ago battle on ");
		} else if (item instanceof Portal) {
			fixtures.remove(item.getId());
			ostream.accept("A portal to another world at ");
		} else {
			LovelaceLogger.warning("Unandled ExplorableFixture class (single item)");
			return;
		}
		if (loc.isValid()) {
			ostream.accept(loc.toString());
			ostream.accept(distanceString.apply(loc));
		} else {
			ostream.accept("an unknown location");
		}
	}

	/**
	 * Produces the report on all caves, battlefields, and portals.
	 */
	@Override
	public void produce(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                    final IMapNG map, final Consumer<String> ostream) {
		final List<Point> portals = new PointList("Portals to other worlds: ");
		final List<Point> battles = new PointList("Signs of long-ago battles on the following tiles:");
		final List<Point> caves = new PointList("Caves beneath the following tiles: ");
		for (final Pair<Point, ExplorableFixture> pair : fixtures.values().stream()
				.filter(p -> p.getValue1() instanceof ExplorableFixture)
				.sorted(pairComparator)
				.map(p -> Pair.with(p.getValue0(),
					(ExplorableFixture) p.getValue1()))
				.collect(Collectors.toList())) {
			final Point loc = pair.getValue0();
			final ExplorableFixture item = pair.getValue1();
			if (item instanceof Portal) {
				portals.add(loc);
			} else if (item instanceof Battlefield) {
				battles.add(loc);
			} else if (item instanceof Cave) {
				caves.add(loc);
			} else {
				LovelaceLogger.warning("Unandled ExplorableFixture class (in loop)");
				continue;
			}
			fixtures.remove(item.getId());
		}
		if (!caves.isEmpty() || !battles.isEmpty() || !portals.isEmpty()) {
			println(ostream, "<h4>Caves, Battlefields, and Portals</h4>");
			ostream.accept("<ul>");
			for (final List<Point> list : Arrays.asList(caves, battles, portals)) {
				if (!list.isEmpty()) {
					ostream.accept("<li>");
					ostream.accept(list.toString());
					println(ostream, "</li>");
				}
			}
			println(ostream, "</ul>");
		}
	}
}
