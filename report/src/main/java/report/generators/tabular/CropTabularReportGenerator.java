package report.generators.tabular;

import java.util.List;
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
	private static final Logger LOGGER = Logger.getLogger(CropTabularReportGenerator.class.getName());

	private static final NumberFormat NUM_FORMAT = NumberFormat.getInstance();

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
	public List<String> getHeaderRow() {
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
	public List<List<String>> produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			/*Forest|Shrub|Meadow|Grove*/ final TileFixture item, final int key, final Point loc,
			final Map<Integer, Integer> parentMap) {
		final String kind;
		final String cultivation;
		final String status;
		final String size;
		final String sizeUnit;
		final String crop = ((HasKind) item).getKind();
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
		return Collections.singletonList(Arrays.asList(distanceString(loc, hq, dimensions),
			locationString(loc), kind, size, sizeUnit, cultivation, status, crop));
	}

	/**
	 * Compare two Point-fixture pairs.
	 * @return
	 */
	@Override
	public Comparator<Pair<Point, TileFixture>> comparePairs() {
		return Comparator.<Pair<Point, TileFixture>, String>comparing(p -> ((HasKind) p.getValue1()).getKind())
			.thenComparing(Pair::getValue0, distanceComparator)
			.thenComparing(p -> p.getValue1().getClass().hashCode())
			.thenComparing(p -> p.getValue1().hashCode());
	}
}
