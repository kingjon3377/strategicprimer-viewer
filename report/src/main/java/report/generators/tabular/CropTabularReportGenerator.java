package report.generators.tabular;

import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import lovelace.util.DelayedRemovalMap;

import common.DistanceComparator;
import common.map.IFixture;
import common.map.TileFixture;
import common.map.MapDimensions;
import common.map.Point;
import common.map.HasKind;
import common.map.fixtures.resources.Grove;
import common.map.fixtures.resources.Meadow;
import common.map.fixtures.resources.Shrub;
import common.map.fixtures.terrain.Forest;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;
import java.util.Map;
import java.text.NumberFormat;

/**
 * A tabular report generator for crops---forests, groves, orchards, fields,
 * meadows, and shrubs.
 */
public class CropTabularReportGenerator implements ITableGenerator</*Forest|Shrub|Meadow|Grove*/TileFixture> {
	@Override
	public Class<TileFixture> narrowedClass() {
		return TileFixture.class;
	}

	/**
	 * A logger.
	 */
	private static Logger LOGGER = Logger.getLogger(CropTabularReportGenerator.class.getName());

	private static NumberFormat NUM_FORMAT = NumberFormat.getInstance();

	static {
		NUM_FORMAT.setMinimumFractionDigits(0);
		NUM_FORMAT.setMaximumFractionDigits(2);
	}

	/**
	 * Produce a {@link String} representation of a {@link Number}, limiting it to two decimal places.
	 */
	private static String truncatedNumberString(final Number number) {
		return NUM_FORMAT.format(number);
	}

	@Nullable
	private final Point hq;
	private final MapDimensions dimensions;
	private final Comparator<Point> distanceComparator;

	public CropTabularReportGenerator(@Nullable final Point hq, final MapDimensions dimensions) {
		this.hq = hq;
		this.dimensions = dimensions;
		if (hq == null) {
			distanceComparator = (one, two) -> 0;
		} else {
			distanceComparator = new DistanceComparator(hq, dimensions)::compare;
		}
	}

	/**
	 * The header row for the table.
	 */
	@Override
	public Iterable<String> getHeaderRow() {
		return Arrays.asList("Distance", "Location", "Kind", "Size", "Size Unit", "Cultivation",
			"Status", "Crop");
	}

	/**
	 * The file-name to (by default) write this table to.
	 */
	@Override
	public String getTableName() {
		return "crops";
	}

	/**
	 * Create a table row representing the crop.
	 */
	@Override
	public Iterable<Iterable<String>> produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			/*Forest|Shrub|Meadow|Grove*/ final TileFixture item, final int key, final Point loc,
			final Map<Integer, Integer> parentMap) {
		if (!(item instanceof Forest || item instanceof Shrub || item instanceof Meadow ||
				item instanceof Grove)) {
			return Collections.emptyList();
		}
		String kind;
		String cultivation;
		String status;
		String size;
		String sizeUnit;
		String crop = ((HasKind) item).getKind();
		if (item instanceof Forest) {
			kind = (((Forest) item).isRows()) ? "rows" : "forest";
			cultivation = "---";
			status = "---";
			if (((Forest) item).getAcres().doubleValue() > 0.0) {
				size = truncatedNumberString(((Forest) item).getAcres());
				sizeUnit = "acres";
			} else {
				size = "unknown";
				sizeUnit = "---";
			}
		} else if (item instanceof Shrub) {
			kind = "shrub";
			cultivation = "---";
			status = "---";
			if (((Shrub) item).getPopulation() > 0) {
				size = Integer.toString(((Shrub) item).getPopulation());
				sizeUnit = "plants";
			} else {
				size = "unknown";
				sizeUnit = "---";
			}
		} else if (item instanceof Meadow) {
			kind = (((Meadow) item).isField()) ? "field" : "meadow";
			cultivation = (((Meadow) item).isCultivated()) ? "cultivated" : "wild";
			status = ((Meadow) item).getStatus().toString();
			if (((Meadow) item).getAcres().doubleValue() > 0.0) {
				size = truncatedNumberString(((Meadow) item).getAcres());
				sizeUnit = "acres";
			} else {
				size = "unknown";
				sizeUnit = "---";
			}
		} else if (item instanceof Grove) {
			kind = (((Grove) item).isOrchard()) ? "orchard" : "grove";
			cultivation = (((Grove) item).isCultivated()) ? "cultivated" : "wild";
			status = "---";
			if (((Grove) item).getPopulation() > 0) {
				size = Integer.toString(((Grove) item).getPopulation());
				sizeUnit = "trees";
			} else {
				size = "unknown";
				sizeUnit = "---";
			}
		} else {
			return Collections.emptyList();
		}
		fixtures.remove(key);
		return Collections.singleton(Arrays.asList(distanceString(loc, hq, dimensions),
			locationString(loc), kind, size, sizeUnit, cultivation, status, crop));
	}

	/**
	 * Compare two Point-fixture pairs.
	 */
	@Override
	public int comparePairs(final Pair<Point, /*Forest|Shrub|Meadow|Grove*/TileFixture> one,
	                        final Pair<Point, /*Forest|Shrub|Meadow|Grove*/TileFixture> two) {
		return Comparator.<Pair<Point, TileFixture>, String>comparing(p -> ((HasKind) p.getValue1()).getKind())
			.thenComparing(Comparator.comparing(Pair::getValue0, distanceComparator))
			.thenComparing(p -> p.getValue1().getClass().hashCode())
			.thenComparing(p -> p.getValue1().hashCode()).compare(one, two);
	}
}
