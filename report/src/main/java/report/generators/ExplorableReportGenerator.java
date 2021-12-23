package report.generators;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import lovelace.util.IOConsumer;
import java.util.List;
import java.util.Comparator;
import java.io.IOException;
import lovelace.util.IOConsumer;
import java.util.stream.Collectors;
import java.util.Arrays;

import lovelace.util.DelayedRemovalMap;

import common.map.Player;
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
	private final Player currentPlayer;
	public ExplorableReportGenerator(Comparator<Pair<Point, IFixture>> comp, Player currentPlayer,
			MapDimensions dimensions) {
		this(comp, currentPlayer, dimensions, null);
	}
	public ExplorableReportGenerator(Comparator<Pair<Point, IFixture>> comp, Player currentPlayer,
			MapDimensions dimensions, @Nullable Point hq) {
		super(comp, dimensions, hq);
		this.currentPlayer = currentPlayer;
	}

	/**
	 * Produces a more verbose sub-report on a cave, battlefield, or portal.
	 */
	@Override
	public void produceSingle(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			IMapNG map, IOConsumer<String> ostream, ExplorableFixture item, Point loc) 
			throws IOException {
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
			// FIXME: Log a warning?
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
	public void produce(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			IMapNG map, IOConsumer<String> ostream) throws IOException {
		List<Point> portals = new PointList("Portals to other worlds: ");
		List<Point> battles = new PointList("Signs of long-ago battles on the following tiles:");
		List<Point> caves = new PointList("Caves beneath the following tiles: ");
		for (Pair<Point, ExplorableFixture> pair : fixtures.values().stream()
				.filter(p -> p.getValue1() instanceof ExplorableFixture)
				.sorted(pairComparator)
				.map(p -> Pair.<Point, ExplorableFixture>with(p.getValue0(),
					(ExplorableFixture) p.getValue1()))
				.collect(Collectors.toList())) {
			Point loc = pair.getValue0();
			ExplorableFixture item = pair.getValue1();
			if (item instanceof Portal) {
				portals.add(loc);
			} else if (item instanceof Battlefield) {
				battles.add(loc);
			} else if (item instanceof Cave) {
				caves.add(loc);
			} else {
				// TODO: log a warning?
				continue;
			}
			fixtures.remove(item.getId());
		}
		if (!caves.isEmpty() || !battles.isEmpty() || !portals.isEmpty()) {
			ostream.accept("<h4>Caves, Battlefields, and Portals</h4>");
			ostream.accept(System.lineSeparator()); // TODO: Add println(os, str) to superclass
			ostream.accept("<ul>");
			for (List<Point> list : Arrays.asList(caves, battles, portals)) {
				if (!list.isEmpty()) {
					ostream.accept("<li>");
					ostream.accept(list.toString());
					ostream.accept("</li>"); // TODO: add newline? Wasn't in Ceylon ...
				}
			}
			ostream.accept("</ul>");
			ostream.accept(System.lineSeparator());
		}
	}
}
