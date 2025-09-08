package report.generators;

import java.util.function.Consumer;

import lovelace.util.LovelaceLogger;
import org.jspecify.annotations.Nullable;
import org.javatuples.Pair;

import java.util.List;
import java.util.Arrays;

import lovelace.util.DelayedRemovalMap;

import legacy.map.IFixture;
import legacy.map.Point;
import legacy.map.MapDimensions;
import legacy.map.ILegacyMap;
import legacy.map.fixtures.explorable.Cave;
import legacy.map.fixtures.explorable.Portal;
import legacy.map.fixtures.explorable.Battlefield;
import legacy.map.fixtures.explorable.ExplorableFixture;

/**
 * A report generator for caves, battlefields, and portals.
 */
public final class ExplorableReportGenerator extends AbstractReportGenerator<ExplorableFixture> {
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
	                          final ILegacyMap map, final Consumer<String> ostream, final ExplorableFixture item,
	                          final Point loc) {
		switch (item) {
			case final Cave cave -> ostream.accept("Caves beneath ");
			case final Battlefield battlefield -> ostream.accept("Signs of a long-ago battle on ");
			case final Portal portal -> ostream.accept("A portal to another world at ");
			default -> {
				LovelaceLogger.warning("Unandled ExplorableFixture class (single item)");
				return;
			}
		}
		fixtures.remove(item.getId());
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
	                    final ILegacyMap map, final Consumer<String> ostream) {
		final List<Point> portals = new PointList("Portals to other worlds: ");
		final List<Point> battles = new PointList("Signs of long-ago battles on the following tiles:");
		final List<Point> caves = new PointList("Caves beneath the following tiles: ");
		for (final Pair<Point, ExplorableFixture> pair : fixtures.values().stream()
				.filter(p -> p.getValue1() instanceof ExplorableFixture)
				.sorted(pairComparator)
				.map(p -> Pair.with(p.getValue0(),
						(ExplorableFixture) p.getValue1())).toList()) {
			final Point loc = pair.getValue0();
			final ExplorableFixture item = pair.getValue1();
			switch (item) {
				case final Portal portal -> portals.add(loc);
				case final Battlefield battlefield -> battles.add(loc);
				case final Cave cave -> caves.add(loc);
				default -> {
					LovelaceLogger.warning("Unandled ExplorableFixture class (in loop)");
					continue;
				}
			}
			fixtures.remove(item.getId());
		}
		if (!caves.isEmpty() || !battles.isEmpty() || !portals.isEmpty()) {
			ostream.accept("""
					<h4>Caves, Battlefields, and Portals</h4>
					<ul>
					""");
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
