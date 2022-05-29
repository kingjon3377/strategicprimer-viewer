package report.generators.tabular;

import java.io.IOException;
import java.util.List;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;

import lovelace.util.DelayedRemovalMap;
import lovelace.util.ThrowingConsumer;

import common.DistanceComparator;

import common.map.MapDimensions;
import common.map.IFixture;
import common.map.Point;
import common.map.fixtures.Implement;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.resources.CacheFixture;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * A tabular report generator for resources, including {@link CacheFixture
 * caches}, {@link IResourcePile resource piles}, and {@link Implement equipment}.
 */
public class ResourceTabularReportGenerator
		implements ITableGenerator</*Implement|CacheFixture|IResourcePile*/IFixture> {
	@Override
	public boolean canHandle(final IFixture fixture) {
		return fixture instanceof Implement || fixture instanceof CacheFixture || fixture instanceof  IResourcePile;
	}

	public ResourceTabularReportGenerator(final @Nullable Point hq, final MapDimensions dimensions) {
		this.hq = hq;
		this.dimensions = dimensions;
		if (hq == null) {
			distanceComparator = (one, two) -> 0;
		} else {
			distanceComparator = new DistanceComparator(hq, dimensions);
		}
	}

	private final MapDimensions dimensions;
	private final @Nullable Point hq;

	/**
	 * The file-name to (by default) write this table to.
	 */
	@Override
	public String getTableName() {
		return "resources";
	}

	/**
	 * The header row for this table.
	 */
	@Override
	public List<String> getHeaderRow() {
		return Arrays.asList("Distance", "Location", "Kind", "Quantity", "Specifics");
	}

	private final Comparator<Point> distanceComparator;

	/**
	 * Create a table row representing the given fixture.
	 */
	@Override
	public List<List<String>> produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			/*Implement|CacheFixture|IResourcePile*/final IFixture item, final int key, final Point loc,
			final Map<Integer, Integer> parentMap) {
		final String kind;
		final String quantity;
		final String specifics;
		if (item instanceof Implement i) {
			kind = "equipment";
			quantity = Integer.toString(i.getCount());
			specifics = i.getKind();
		} else if (item instanceof CacheFixture cf) {
			kind = cf.getKind();
			quantity = "---";
			specifics = cf.getContents();
		} else if (item instanceof IResourcePile rp) {
			kind = rp.getKind();
			quantity = rp.getQuantity().toString();
			specifics = rp.getContents();
		} else {
			return Collections.emptyList();
		}
		fixtures.remove(key);
		return Collections.singletonList(Arrays.asList(distanceString(loc, hq, dimensions),
			locationString(loc), kind, quantity, specifics));
	}

	private static int compareItems(/*Implement|CacheFixture|IResourcePile*/final IFixture first,
			/*Implement|CacheFixture|IResourcePile*/final IFixture second) {
		if (first instanceof Implement one) {
			if (second instanceof Implement two) {
				return Comparator.comparing(Implement::getKind)
					.thenComparing(Implement::getCount, Comparator.reverseOrder())
					.compare(one, two);
			} else if (second instanceof IResourcePile) {
				return 1;
			} else {
				return -1;
			}
		} else if (first instanceof CacheFixture one) {
			if (second instanceof CacheFixture two) {
				return Comparator.comparing(CacheFixture::getKind)
					.thenComparing(CacheFixture::getContents)
					.compare(one, two);
			} else {
				return 1;
			}
		} else if (first instanceof IResourcePile one) {
			if (second instanceof IResourcePile two) {
				return Comparator.comparing(IResourcePile::getKind)
					.thenComparing(IResourcePile::getContents)
					.thenComparing(IResourcePile::getQuantity, Comparator.reverseOrder())
					.compare(one, two);
			} else {
				return -1;
			}
		} else {
			// give up
			return 0;
		}
	}

	/**
	 * Compare two Point-fixture pairs.
	 * @return
	 */
	@Override
	public Comparator<Pair<Point, IFixture>> comparePairs() {
		return Comparator.<Pair<Point, IFixture>, Point>comparing(Pair::getValue0, distanceComparator)
			.thenComparing(Pair::getValue1,
				ResourceTabularReportGenerator::compareItems);
	}

	/**
	 * Write rows for equipment, counting multiple identical Implements in one line.
	 */
	@Override
	public void produceTable(final ThrowingConsumer<String, IOException> ostream,
	                         final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                         final Map<Integer, Integer> parentMap) throws IOException {
		final Iterable<Triplet<Integer, Point, IFixture>> values = fixtures.entrySet().stream()
			.filter(e -> e instanceof CacheFixture || e instanceof Implement ||
				e instanceof IResourcePile)
			.map(e -> Triplet.with(e.getKey(), e.getValue().getValue0(),
					e.getValue().getValue1()))
			.sorted(Comparator.comparing(Triplet::removeFrom0, comparePairs()))
			.collect(Collectors.toList());
		writeRow(ostream, getHeaderRow().toArray(new String[0]));
		final Map<Pair<Point, String>, Integer> implementCounts = new HashMap<>();
		for (final Triplet<Integer, Point, IFixture> triplet : values) {
			final int key = triplet.getValue0();
			final Point loc = triplet.getValue1();
			final IFixture fixture = triplet.getValue2();
			if (fixture instanceof Implement i) {
				final int num;
				num = implementCounts.getOrDefault(Pair.with(loc, i.getKind()), 0);
				implementCounts.put(Pair.with(loc, i.getKind()), num + i.getCount());
				fixtures.remove(key);
			} else {
				for (final List<String> row : produce(fixtures, fixture, key, loc, parentMap)) {
					writeRow(ostream, row.toArray(new String[0]));
					fixtures.remove(key);
				}
			}
		}
		for (final Map.Entry<Pair<Point, String>, Integer> entry : implementCounts.entrySet()) {
			final Point loc = entry.getKey().getValue0();
			final String key = entry.getKey().getValue1();
			final int count = entry.getValue();
			writeRow(ostream, distanceString(loc, hq, dimensions), locationString(loc),
				"equipment", Integer.toString(count), key);
		}
		fixtures.coalesce();
	}
}
