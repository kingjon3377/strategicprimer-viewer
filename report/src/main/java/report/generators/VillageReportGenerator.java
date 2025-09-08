package report.generators;

import java.util.function.Consumer;

import lovelace.util.TriConsumer;
import org.jspecify.annotations.Nullable;
import org.javatuples.Pair;

import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;

import lovelace.util.DelayedRemovalMap;

import legacy.map.Player;
import legacy.map.IFixture;
import legacy.map.Point;
import legacy.map.MapDimensions;
import legacy.map.ILegacyMap;
import legacy.map.fixtures.towns.Village;

/**
 * A report generator for Villages.
 */
public final class VillageReportGenerator extends AbstractReportGenerator<Village> {

	private final Player currentPlayer;

	public VillageReportGenerator(final Player currentPlayer, final MapDimensions dimensions) {
		this(currentPlayer, dimensions, null);
	}

	public VillageReportGenerator(final Player currentPlayer, final MapDimensions dimensions,
	                              final @Nullable Point hq) {
		super(dimensions, hq);
		this.currentPlayer = currentPlayer;
	}

	/**
	 * Produce the report on all known villages.
	 */
	public void produce(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                    final ILegacyMap map, final Consumer<String> ostream) {
		final Comparator<Village> villageComparator = Comparator.comparing(Village::getName)
				.thenComparing(Village::getRace)
				.thenComparing(Village::getId);
		final HeadedMap<Village, Point> own = new HeadedMapImpl<>(
				"<h4>Villages pledged to your service:</h4>", villageComparator);
		final HeadedMap<Village, Point> independents = new HeadedMapImpl<>(
				"<h4>Villages you think are independent:</h4>", villageComparator);
		final Map<Player, HeadedMap<Village, Point>> others = new HashMap<>();
		for (final Pair<Point, Village> pair : fixtures.values().stream()
				.filter(p -> p.getValue1() instanceof Village)
				.sorted(pairComparator)
				.map(p -> Pair.with(p.getValue0(), (Village) p.getValue1())).toList()) {
			final Point loc = pair.getValue0();
			final Village village = pair.getValue1();
			if (village.owner().equals(currentPlayer)) {
				own.put(village, loc);
			} else if (village.owner().isIndependent()) {
				independents.put(village, loc);
			} else {
				final HeadedMap<Village, Point> mapping = others.computeIfAbsent(village.owner(),
						p -> new HeadedMapImpl<>("<h5>Villages sworn to %s</h5>%n"
								.formatted(p.getName()), villageComparator));
				mapping.put(village, loc);
			}
		}
		final Comparator<Pair<? super Village, Point>> byDistance = Comparator.comparing(Pair::getValue1,
				distComparator);
		final TriConsumer<Village, Point, Consumer<String>> writer = defaultFormatter(fixtures, map);
		writeMap(ostream, own, writer, byDistance);
		writeMap(ostream, independents, writer, byDistance);
		if (!others.isEmpty()) {
			println(ostream, "<h4>Other villages you know about:</h4>");
			for (final HeadedMap<Village, Point> mapping : others.values()) {
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
	                          final ILegacyMap map, final Consumer<String> ostream, final Village item,
	                          final Point loc) {
		fixtures.remove(item.getId());
		atPoint(ostream, loc, ": ");
		ostream.accept(item.getName());
		ostream.accept(", a(n) ");
		ostream.accept(item.getRace());
		ostream.accept(" village, ");
		if (item.owner().isIndependent()) {
			ostream.accept("independent ");
		} else if (item.owner().equals(currentPlayer)) {
			ostream.accept("sworn to you ");
		} else {
			ostream.accept("sworn to ");
			ostream.accept(item.owner().getName());
			ostream.accept(" ");
		}
		ostream.accept(distanceString.apply(loc));
	}
}
