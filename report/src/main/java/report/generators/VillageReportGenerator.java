package report.generators;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import lovelace.util.ThrowingConsumer;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import lovelace.util.ThrowingTriConsumer;

import lovelace.util.DelayedRemovalMap;

import common.map.Player;
import common.map.IFixture;
import common.map.Point;
import common.map.MapDimensions;
import common.map.IMapNG;
import common.map.fixtures.towns.Village;

/**
 * A report generator for Villages.
 */
public class VillageReportGenerator extends AbstractReportGenerator<Village> {

	private final Player currentPlayer;

	public VillageReportGenerator(final Comparator<Pair<Point, IFixture>> comp, final Player currentPlayer,
	                              final MapDimensions dimensions) {
		this(comp, currentPlayer, dimensions, null);
	}

	public VillageReportGenerator(final Comparator<Pair<Point, IFixture>> comp, final Player currentPlayer,
	                              final MapDimensions dimensions, @Nullable final Point hq) {
		super(comp, dimensions, hq);
		this.currentPlayer = currentPlayer;
	}

	/**
	 * Produce the report on all known villages.
	 */
	public void produce(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                    final IMapNG map, final ThrowingConsumer<String, IOException> ostream) throws IOException {
		Comparator<Village> villageComparator = Comparator.comparing(Village::getName)
			.thenComparing(Comparator.comparing(Village::getRace))
			.thenComparing(Comparator.comparing(Village::getId));
		HeadedMap<Village, Point> own = new HeadedMapImpl<>(
			"<h4>Villages pledged to your service:</h4>", villageComparator);
		HeadedMap<Village, Point> independents = new HeadedMapImpl<>(
			"<h4>Villages you think are independent:</h4>", villageComparator);
		Map<Player, HeadedMap<Village, Point>> others = new HashMap<>();
		for (Pair<Point, Village> pair : fixtures.values().stream()
				.filter(p -> p.getValue1() instanceof Village)
				.sorted(pairComparator)
				.map(p -> Pair.with(p.getValue0(), (Village) p.getValue1()))
				.collect(Collectors.toList())) {
			Point loc = pair.getValue0();
			Village village = pair.getValue1();
			if (village.getOwner().equals(currentPlayer)) {
				own.put(village, loc);
			} else if (village.getOwner().isIndependent()) {
				independents.put(village, loc);
			} else {
				HeadedMap<Village, Point> mapping;
				if (others.containsKey(village.getOwner())) {
					mapping = others.get(village.getOwner());
				} else {
					mapping = new HeadedMapImpl<>(String.format(
							"<h5>Villages sworn to %s</h5>%n",
							village.getOwner().getName()),
						villageComparator);
					others.put(village.getOwner(), mapping);
				}
				mapping.put(village, loc);
			}
		}
		Comparator<Pair<? super Village, Point>> byDistance = Comparator.comparing(Pair::getValue1,
			distComparator);
		ThrowingTriConsumer<Village, Point, ThrowingConsumer<String, IOException>, IOException> writer =
			defaultFormatter(fixtures, map);
		writeMap(ostream, own, writer, byDistance);
		writeMap(ostream, independents, writer, byDistance);
		if (!others.isEmpty()) {
			ostream.accept("<h4>Other villages you know about:</h4>");
			ostream.accept(System.lineSeparator());
			for (HeadedMap<Village, Point> mapping : others.values()) {
				writeMap(ostream, mapping, writer, byDistance);
			}
		}
	}

	/**
	 * Produce the (very brief) report for a particular village (we're
	 * probably in the middle of a bulleted list, but we don't assume
	 * that).
	 */
	@Override
	public void produceSingle(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                          final IMapNG map, final ThrowingConsumer<String, IOException> ostream, final Village item, final Point loc)
			throws IOException {
		fixtures.remove(item.getId());
		ostream.accept("At ");
		ostream.accept(loc.toString());
		ostream.accept(": ");
		ostream.accept(item.getName());
		ostream.accept(", a(n) ");
		ostream.accept(item.getRace());
		ostream.accept(" village, ");
		if (item.getOwner().isIndependent()) {
			ostream.accept("independent ");
		} else if (item.getOwner().equals(currentPlayer)) {
			ostream.accept("sworn to you ");
		} else {
			ostream.accept("sworn to ");
			ostream.accept(item.getOwner().getName());
			ostream.accept(" ");
		}
		ostream.accept(distanceString.apply(loc));
	}
}
