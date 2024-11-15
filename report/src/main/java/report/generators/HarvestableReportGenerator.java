package report.generators;

import java.util.function.Consumer;

import legacy.map.fixtures.explorable.Battlefield;
import legacy.map.fixtures.explorable.Cave;
import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;

import java.util.Comparator;

import lovelace.util.DelayedRemovalMap;

import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Collections;

import legacy.map.IFixture;
import legacy.map.HasPopulation;
import legacy.map.HasExtent;
import legacy.map.Point;
import legacy.map.MapDimensions;
import legacy.map.ILegacyMap;
import legacy.map.fixtures.resources.Meadow;
import legacy.map.fixtures.resources.CacheFixture;
import legacy.map.fixtures.resources.Mine;
import legacy.map.fixtures.resources.StoneDeposit;
import legacy.map.fixtures.resources.Shrub;
import legacy.map.fixtures.resources.MineralVein;
import legacy.map.fixtures.resources.Grove;
import legacy.map.fixtures.resources.HarvestableFixture;

import java.text.NumberFormat;

/**
 * A report generator for harvestable fixtures (other than caves and
 * battlefields, which aren't really).
 */
public final class HarvestableReportGenerator extends AbstractReportGenerator<HarvestableFixture> {
	private static String populationCountString(final HasPopulation<?> item, final String singular) {
		return populationCountString(item, singular, singular + "s");
	}

	private static String populationCountString(final HasPopulation<?> item, final String singular,
	                                            final String plural) {
		if (item.getPopulation() <= 0) {
			return "";
		} else if (item.getPopulation() == 1) {
			return " (1 %s)".formatted(singular);
		} else {
			return " (%d %s)".formatted(item.getPopulation(), plural);
		}
	}

	private static final NumberFormat NUM_FORMAT = NumberFormat.getInstance();

	static {
		NUM_FORMAT.setMinimumFractionDigits(0);
		NUM_FORMAT.setMaximumFractionDigits(2);
		// TODO: call for comma groupings to the left of the decimal?
	}

	private static String acreageString(final HasExtent<?> item) {
		if (item.getAcres().doubleValue() > 0.0) {
			return " (%s acres)".formatted(NUM_FORMAT.format(item.getAcres()));
		} else {
			return "";
		}
	}

	public HarvestableReportGenerator(final MapDimensions dimensions) {
		this(dimensions, null);
	}

	public HarvestableReportGenerator(final MapDimensions dimensions, final @Nullable Point hq) {
		super(dimensions, hq);
	}

	/**
	 * Convert a Map (in Ceylon before the port, a Multimap) from kinds to Points to a HtmlList.
	 */
	private static HeadedList<String> mapToList(final Map<String, List<Point>> map, final String heading) {
		return map.entrySet().stream()
				.filter(e -> !e.getValue().isEmpty())
				.map(e -> "%s: at %s".formatted(e.getKey(), commaSeparatedList(e.getValue())))
				.sorted().collect(Collectors.toCollection(
						() -> new HtmlList(heading, Collections.emptyList())));
	}

	/**
	 * Produce a sub-report(s) dealing with a single "harvestable"
	 * fixture(s). It is to be removed from the collection.
	 * {@link Cave Caves} and
	 * {@link Battlefield battlefields}, though
	 * implementing the {@link HarvestableFixture} interface, are <em>not</em> handled here.
	 */
	@Override
	public void produceSingle(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                          final ILegacyMap map, final Consumer<String> ostream, final HarvestableFixture item,
	                          final Point loc) {
		if (!(item instanceof CacheFixture || item instanceof Grove || item instanceof Meadow
				|| item instanceof Mine || item instanceof MineralVein
				|| item instanceof Shrub || item instanceof StoneDeposit)) {
			return;
		}
		atPoint(ostream, loc, ": ");
		switch (item) {
			case final CacheFixture cf -> {
				ostream.accept("A cache of ");
				ostream.accept(item.getKind());
				ostream.accept(", containing ");
				ostream.accept(cf.getContents());
			}
			case final Grove g -> {
				ostream.accept(g.getCultivation().toString());
				ostream.accept(item.getKind());
				ostream.accept(" %s ".formatted(g.getType()));
				ostream.accept(populationCountString(g, "tree"));
			}
			case final Meadow m -> {
				ostream.accept(m.getStatus().toString());
				ostream.accept(switch (m.getCultivation()) {
					case CULTIVATED -> " cultivated ";
					case WILD -> " wild or abandoned ";
				});
				ostream.accept(item.getKind());
				ostream.accept(" %s ".formatted(m.getType()));
				ostream.accept(acreageString(m));
			}
			case final Mine mine -> ostream.accept(item.toString());
			case final MineralVein mv -> {
				ostream.accept(mv.isExposed() ? "An exposed vein of " : "An unexposed vein of ");
				ostream.accept(item.getKind());
			}
			case final Shrub s -> {
				ostream.accept(item.getKind());
				ostream.accept(" ");
				ostream.accept(populationCountString(s, "plant"));
			}
			default -> {
				/*if (item instanceof StoneDeposit)*/

				ostream.accept("An exposed ");
				ostream.accept(item.getKind());
				ostream.accept(" deposit");
			}
		}
		ostream.accept(" ");
		ostream.accept(distanceString.apply(loc));
	}

	/**
	 * Produce the sub-report(s) dealing with "harvestable" fixtures. All
	 * fixtures referred to in this report are to be removed from the
	 * collection. {@link Cave Caves} and {@link
	 * Battlefield battlefields},
	 * though implementing {@link HarvestableFixture}, are presumed to have
	 * been handled already.
	 */
	@Override
	public void produce(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                    final ILegacyMap map, final Consumer<String> ostream) {
		final Map<String, List<Point>> stone = new HashMap<>();
		final Map<String, List<Point>> shrubs = new HashMap<>();
		final Map<String, List<Point>> minerals = new HashMap<>();
		final HeadedMap<Mine, Point> mines = new HeadedMapImpl<>("<h5>Mines</h5>",
				Comparator.comparing(Mine::getKind).thenComparing(Mine::getStatus)
						.thenComparing(Mine::getId));
		// TODO: Group meadows and fields separately in the list since Boolean is comparable in Java?
		final HeadedMap<Meadow, Point> meadows = new HeadedMapImpl<>("<h5>Meadows and Fields</h5>",
				Comparator.comparing(Meadow::getKind).thenComparing(Meadow::getStatus)
						.thenComparing(Meadow::getId));
		final HeadedMap<Grove, Point> groves = new HeadedMapImpl<>("<h5>Groves and Orchards</h5>",
				Comparator.comparing(Grove::getKind).thenComparing(Grove::getId));
		final HeadedMap<CacheFixture, Point> caches = new HeadedMapImpl<>(
				"<h5>Caches collected by your explorers and workers:</h5>",
				Comparator.comparing(CacheFixture::getKind).thenComparing(CacheFixture::getContents)
						.thenComparing(CacheFixture::getId));
		for (final Pair<Point, HarvestableFixture> pair : fixtures.values().stream()
				.filter(p -> p.getValue1() instanceof HarvestableFixture)
				.sorted(pairComparator)
				.map(p -> Pair.with(p.getValue0(), (HarvestableFixture) p.getValue1())).toList()) {
			final Point point = pair.getValue0();
			final HarvestableFixture item = pair.getValue1();
			// TODO: Use a Map by type
			switch (item) {
				case final CacheFixture c -> {
					caches.put(c, point);
					fixtures.remove(item.getId());
				}
				case final Grove g -> {
					groves.put(g, point);
					fixtures.remove(item.getId());
				}
				case final Meadow m -> {
					meadows.put(m, point);
					fixtures.remove(item.getId());
				}
				case final Mine m -> {
					mines.put(m, point);
					fixtures.remove(item.getId());
				}
				case final MineralVein mineralVein -> {
					final List<Point> list = Optional.ofNullable(minerals.get(
							item.getShortDescription())).orElseGet(ArrayList::new);
					list.add(point);
					minerals.put(item.getShortDescription(), list);
					fixtures.remove(item.getId());
				}
				case final Shrub shrub -> {
					final List<Point> list = Optional.ofNullable(shrubs.get(
							item.getKind())).orElseGet(ArrayList::new);
					list.add(point);
					shrubs.put(item.getKind(), list);
					fixtures.remove(item.getId());
				}
				case final StoneDeposit stoneDeposit -> {
					final List<Point> list = Optional.ofNullable(stone.get(
							item.getKind())).orElseGet(ArrayList::new);
					list.add(point);
					stone.put(item.getKind(), list);
					fixtures.remove(item.getId());
				}
				default -> {
					return;
				}
			}
		}
		final List<HeadedList<String>> all = Arrays.asList(
				mapToList(minerals, "<h5>Mineral Deposits</h5>"),
				mapToList(stone, "<h5>Exposed Stone Deposits</h5>"),
				mapToList(shrubs, "<h5>Shrubs, Small Trees, etc.</h5>"));
		final List<HeadedMap<? extends HarvestableFixture, Point>> maps =
				Arrays.asList(caches, groves, meadows, mines);
		if (maps.stream().anyMatch(m -> !m.isEmpty()) || all.stream().anyMatch(l -> !l.isEmpty())) {
			println(ostream, "<h4>Resource Sources</h4>");
			for (final HeadedMap<? extends HarvestableFixture, Point> mapping : maps) {
				writeMap(ostream, mapping, defaultFormatter(fixtures, map));
			}
			for (final HeadedList<String> list : all) {
				ostream.accept(list.toString());
			}
		}
	}
}
