package report.generators;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import lovelace.util.ThrowingConsumer;
import java.io.IOException;
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

import common.map.IFixture;
import common.map.HasPopulation;
import common.map.HasExtent;
import common.map.Point;
import common.map.MapDimensions;
import common.map.IMapNG;
import common.map.fixtures.resources.Meadow;
import common.map.fixtures.resources.CacheFixture;
import common.map.fixtures.resources.Mine;
import common.map.fixtures.resources.StoneDeposit;
import common.map.fixtures.resources.Shrub;
import common.map.fixtures.resources.MineralVein;
import common.map.fixtures.resources.Grove;
import common.map.fixtures.resources.HarvestableFixture;

import java.text.NumberFormat;
import java.math.BigDecimal;
import java.util.logging.Logger;
import java.math.BigInteger;

/**
 * A report generator for harvestable fixtures (other than caves and
 * battlefields, which aren't really).
 */
public class HarvestableReportGenerator extends AbstractReportGenerator<HarvestableFixture> {
	private static String populationCountString(HasPopulation<?> item, String singular) {
		return populationCountString(item, singular, singular + "s");
	}

	private static String populationCountString(HasPopulation<?> item, String singular, String plural) {
		if (item.getPopulation() <= 0) {
			return "";
		} else if (item.getPopulation() == 1) {
			return String.format(" (1 %s)", singular);
		} else {
			return String.format(" (%d %s)", item.getPopulation(), plural);
		}
	}

	private static final NumberFormat NUM_FORMAT = NumberFormat.getInstance();
	static {
		NUM_FORMAT.setMinimumFractionDigits(0);
		NUM_FORMAT.setMaximumFractionDigits(2);
		// TODO: call for comma groupings to the left of the decimal?
	}

	private static String acreageString(HasExtent<?> item) {
		if (item.getAcres().doubleValue() > 0.0) {
			return String.format(" (%s acres)", NUM_FORMAT.format(item.getAcres()));
		} else {
			return "";
		}
	}

	public HarvestableReportGenerator(Comparator<Pair<Point, IFixture>> comp,
			MapDimensions dimensions) {
		this(comp, dimensions, null);
	}

	public HarvestableReportGenerator(Comparator<Pair<Point, IFixture>> comp,
			MapDimensions dimensions, @Nullable Point hq) {
		super(comp, dimensions, hq);
	}

	/**
	 * Convert a Map (in Ceylon before the port, a Multimap) from kinds to Points to a HtmlList.
	 */
	private static HeadedList<String> mapToList(Map<String, List<Point>> map, String heading) {
		return map.entrySet().stream()
			.filter(e -> !e.getValue().isEmpty())
			.map(e -> String.format("%s: at %s", e.getKey(), commaSeparatedList(e.getValue())))
			.sorted().collect(Collectors.toCollection(
				() -> new HtmlList(heading, Collections.emptyList())));
	}

	/**
	 * Produce a sub-report(s) dealing with a single "harvestable"
	 * fixture(s). It is to be removed from the collection. {@link Cave Caves} and
	 * {@link Battlefield battlefields}, though implementing the {@link
	 * HarvestableFixture} interface, are <em>not</em> handled here.
	 */
	@Override
	public void produceSingle(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			IMapNG map, ThrowingConsumer<String, IOException> ostream, HarvestableFixture item, Point loc)
			throws IOException {
		if (!(item instanceof CacheFixture || item instanceof Grove || item instanceof Meadow
				|| item instanceof Mine || item instanceof MineralVein
				|| item instanceof Shrub || item instanceof StoneDeposit)) {
			return;
		}
		ostream.accept("At ");
		ostream.accept(loc.toString());
		ostream.accept(": ");
		if (item instanceof CacheFixture) {
			ostream.accept("A cache of ");
			ostream.accept(((CacheFixture) item).getKind());
			ostream.accept(", containing ");
			ostream.accept(((CacheFixture) item).getContents());
		} else if (item instanceof Grove) {
			ostream.accept((((Grove) item).isCultivated()) ? "cultivated " : "wild ");
			ostream.accept(((Grove) item).getKind());
			ostream.accept((((Grove) item).isOrchard()) ? " orchard " : " grove ");
			ostream.accept(populationCountString(((Grove) item), "tree"));
		} else if (item instanceof Meadow) {
			ostream.accept(((Meadow) item).getStatus().toString());
			ostream.accept((((Meadow) item).isCultivated()) ? " cultivated " :
				" wild or abandoned ");
			ostream.accept(((Meadow) item).getKind());
			ostream.accept((((Meadow) item).isField()) ? " field " : " meadow ");
			ostream.accept(acreageString(((Meadow) item)));
		} else if (item instanceof Mine) {
			ostream.accept(item.toString());
		} else if (item instanceof MineralVein) {
			ostream.accept((((MineralVein) item).isExposed()) ? "An exposed vein of " :
				"An unexposed vein of ");
			ostream.accept(((MineralVein) item).getKind());
		} else if (item instanceof Shrub) {
			ostream.accept(((Shrub) item).getKind());
			ostream.accept(" ");
			ostream.accept(populationCountString((Shrub) item, "plant"));
		} else if (item instanceof StoneDeposit) {
			ostream.accept("An exposed ");
			ostream.accept(((StoneDeposit) item).getKind());
			ostream.accept(" deposit");
		}
		ostream.accept(" ");
		ostream.accept(distanceString.apply(loc));
	}

	/**
	 * Produce the sub-report(s) dealing with "harvestable" fixtures. All
	 * fixtures referred to in this report are to be removed from the
	 * collection. {@link Cave Caves} and {@link Battlefield battlefields},
	 * though implementing {@link HarvestableFixture}, are presumed to have
	 * been handled already.
	 */
	@Override
	public void produce(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			IMapNG map, ThrowingConsumer<String, IOException> ostream) throws IOException {
		Map<String, List<Point>> stone = new HashMap<>();
		Map<String, List<Point>> shrubs = new HashMap<>();
		Map<String, List<Point>> minerals = new HashMap<>();
		HeadedMap<Mine, Point> mines = new HeadedMapImpl<>("<h5>Mines</h5>",
			Comparator.comparing(Mine::getKind).thenComparing(Mine::getStatus)
				.thenComparing(Mine::getId));
		// TODO: Group meadows and fields separately in the list since Boolean is comparable in Java?
		HeadedMap<Meadow, Point> meadows = new HeadedMapImpl<>("<h5>Meadows and Fields</h5>",
			Comparator.comparing(Meadow::getKind).thenComparing(Meadow::getStatus)
				.thenComparing(Meadow::getId));
		HeadedMap<Grove, Point> groves = new HeadedMapImpl<>("<h5>Groves and Orchards</h5>",
			Comparator.comparing(Grove::getKind).thenComparing(Grove::getId));
		HeadedMap<CacheFixture, Point> caches = new HeadedMapImpl<>(
			"<h5>Caches collected by your explorers and workers:</h5>",
			Comparator.comparing(CacheFixture::getKind).thenComparing(CacheFixture::getContents)
				.thenComparing(CacheFixture::getId));
		for (Pair<Point, HarvestableFixture> pair : fixtures.values().stream()
				.filter(p -> p.getValue1() instanceof HarvestableFixture)
				.sorted(pairComparator)
				.map(p -> Pair.with(p.getValue0(), (HarvestableFixture) p.getValue1()))
				.collect(Collectors.toList())) {
			Point point = pair.getValue0();
			HarvestableFixture item = pair.getValue1();
			// TODO: Use a Map by type
			if (item instanceof CacheFixture) {
				caches.put((CacheFixture) item, point);
				fixtures.remove(item.getId());
			} else if (item instanceof Grove) {
				groves.put((Grove) item, point);
				fixtures.remove(item.getId());
			} else if (item instanceof Meadow) {
				meadows.put((Meadow) item, point);
				fixtures.remove(item.getId());
			} else if (item instanceof Mine) {
				mines.put((Mine) item, point);
				fixtures.remove(item.getId());
			} else if (item instanceof MineralVein) {
				List<Point> list = Optional.ofNullable(minerals.get(
					((MineralVein) item).getShortDescription())).orElseGet(ArrayList::new);
				list.add(point);
				minerals.put(((MineralVein) item).getShortDescription(), list);
				fixtures.remove(item.getId());
			} else if (item instanceof Shrub) {
				List<Point> list = Optional.ofNullable(shrubs.get(
					((Shrub) item).getKind())).orElseGet(ArrayList::new);
				list.add(point);
				shrubs.put(((Shrub) item).getKind(), list);
				fixtures.remove(item.getId());
			} else if (item instanceof StoneDeposit) {
				List<Point> list = Optional.ofNullable(stone.get(
					((StoneDeposit) item).getKind())).orElseGet(ArrayList::new);
				list.add(point);
				stone.put(((StoneDeposit) item).getKind(), list);
				fixtures.remove(item.getId());
			} else {
				return;
			}
		}
		List<HeadedList<String>> all = Arrays.asList(
			mapToList(minerals, "<h5>Mineral Deposits</h5>"),
			mapToList(stone, "<h5>Exposed Stone Deposits</h5>"),
			mapToList(shrubs, "<h5>Shrubs, Small Trees, etc.</h5>"));
		List<HeadedMap<? extends HarvestableFixture, Point>> maps =
			Arrays.asList(caches, groves, meadows, mines);
		if (maps.stream().anyMatch(m -> !m.isEmpty()) || all.stream().anyMatch(l -> !l.isEmpty())) {
			ostream.accept("<h4>Resource Sources</h4>");
			ostream.accept(System.lineSeparator());
			for (HeadedMap<? extends HarvestableFixture, Point> mapping : maps) {
				super.<HarvestableFixture>writeMap(ostream, mapping, defaultFormatter(fixtures, map));
			}
			for (HeadedList<String> list : all) {
				ostream.accept(list.toString());
			}
		}
	}
}
