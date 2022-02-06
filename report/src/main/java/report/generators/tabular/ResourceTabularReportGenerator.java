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
import common.map.TileFixture;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A tabular report generator for resources, including {@link CacheFixture
 * caches}, {@link IResourcePile resource piles}, and {@link Implement equipment}.
 */
public class ResourceTabularReportGenerator
		implements ITableGenerator</*Implement|CacheFixture|IResourcePile*/TileFixture> {
	@Override
	public Class<TileFixture> narrowedClass() {
		return TileFixture.class;
	}

	public ResourceTabularReportGenerator(@Nullable final Point hq, final MapDimensions dimensions) {
		this.hq = hq;
		this.dimensions = dimensions;
		if (hq == null) {
			distanceComparator = (one, two) -> 0;
		} else {
			distanceComparator = new DistanceComparator(hq, dimensions);
		}
	}

	private final MapDimensions dimensions;
	@Nullable
	private final Point hq;

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
			/*Implement|CacheFixture|IResourcePile*/final TileFixture item, final int key, final Point loc,
			final Map<Integer, Integer> parentMap) {
		String kind;
		String quantity;
		String specifics;
		if (item instanceof Implement) {
			kind = "equipment";
			quantity = Integer.toString(((Implement) item).getCount());
			specifics = ((Implement) item).getKind();
		} else if (item instanceof CacheFixture) {
			kind = ((CacheFixture) item).getKind();
			quantity = "---";
			specifics = ((CacheFixture) item).getContents();
		} else if (item instanceof IResourcePile) {
			kind = ((IResourcePile) item).getKind();
			quantity = ((IResourcePile) item).getQuantity().toString();
			specifics = ((IResourcePile) item).getContents();
		} else {
			return Collections.emptyList();
		}
		fixtures.remove(key);
		return Collections.singletonList(Arrays.asList(distanceString(loc, hq, dimensions),
			locationString(loc), kind, quantity, specifics));
	}

	private static int compareItems(/*Implement|CacheFixture|IResourcePile*/final TileFixture first,
			/*Implement|CacheFixture|IResourcePile*/final TileFixture second) {
		if (first instanceof Implement) {
			if (second instanceof Implement) {
				return Comparator.comparing(Implement::getKind)
					.thenComparing(Implement::getCount, Comparator.reverseOrder())
					.compare((Implement) first, (Implement) second);
			} else if (second instanceof IResourcePile) {
				return 1;
			} else {
				return -1;
			}
		} else if (first instanceof CacheFixture) {
			if (second instanceof CacheFixture) {
				return Comparator.comparing(CacheFixture::getKind)
					.thenComparing(CacheFixture::getContents)
					.compare((CacheFixture) first, (CacheFixture) second);
			} else {
				return 1;
			}
		} else if (first instanceof IResourcePile) {
			if (second instanceof IResourcePile) {
				return Comparator.comparing(IResourcePile::getKind)
					.thenComparing(IResourcePile::getContents)
					.thenComparing(IResourcePile::getQuantity, Comparator.reverseOrder())
					.compare((IResourcePile) first, (IResourcePile) second);
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
	 */
	@Override
	public int comparePairs(
			final Pair<Point, /*Implement|CacheFixture|IResourcePile*/TileFixture> one,
			final Pair<Point, /*Implement|CacheFixture|IResourcePile*/TileFixture> two) {
		return Comparator.<Pair<Point, TileFixture>, Point>comparing(Pair::getValue0, distanceComparator)
			.thenComparing(Pair::getValue1,
				ResourceTabularReportGenerator::compareItems)
			.compare(one, two);
	}

	/**
	 * Write rows for equipment, counting multiple identical Implements in one line.
	 */
	@Override
	public void produceTable(final ThrowingConsumer<String, IOException> ostream,
	                         final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                         final Map<Integer, Integer> parentMap) throws IOException {
		Iterable<Triplet<Integer, Point, TileFixture>> values = fixtures.entrySet().stream()
			.filter(e -> e instanceof CacheFixture || e instanceof Implement ||
				e instanceof IResourcePile)
			.map(e -> Triplet.with(e.getKey(), e.getValue().getValue0(),
				(TileFixture) e.getValue().getValue1()))
			.sorted(Comparator.comparing(Triplet::removeFrom0, this::comparePairs))
			.collect(Collectors.toList());
		writeRow(ostream, getHeaderRow().toArray(new String[0]));
		Map<Pair<Point, String>, Integer> implementCounts = new HashMap<>();
		for (Triplet<Integer, Point, TileFixture> triplet : values) {
			int key = triplet.getValue0();
			Point loc = triplet.getValue1();
			TileFixture fixture = triplet.getValue2();
			if (fixture instanceof Implement) {
				int num;
				if (implementCounts.containsKey(Pair.with(loc, ((Implement) fixture).getKind()))) {
					num = implementCounts.get(Pair.with(loc, ((Implement) fixture).getKind()));
				} else {
					num = 0;
				}
				implementCounts.put(Pair.with(loc, ((Implement) fixture).getKind()),
					num + ((Implement) fixture).getCount());
				fixtures.remove(key);
			} else {
				for (List<String> row : produce(fixtures, fixture, key, loc, parentMap)) {
					writeRow(ostream, row.toArray(new String[0]));
					fixtures.remove(key);
				}
			}
		}
		for (Map.Entry<Pair<Point, String>, Integer> entry : implementCounts.entrySet()) {
			Point loc = entry.getKey().getValue0();
			String key = entry.getKey().getValue1();
			int count = entry.getValue();
			writeRow(ostream, distanceString(loc, hq, dimensions), locationString(loc),
				"equipment", Integer.toString(count), key);
		}
		fixtures.coalesce();
	}
}
