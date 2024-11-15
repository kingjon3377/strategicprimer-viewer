package report.generators.tabular;

import java.util.List;

import legacy.DistanceComparatorImpl;
import legacy.map.HasExtent;
import legacy.map.HasPopulation;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lovelace.util.DelayedRemovalMap;

import legacy.map.IFixture;
import legacy.map.TileFixture;
import legacy.map.MapDimensions;
import legacy.map.Point;
import legacy.map.HasKind;
import legacy.map.fixtures.resources.Grove;
import legacy.map.fixtures.resources.Meadow;
import legacy.map.fixtures.resources.Shrub;
import legacy.map.fixtures.terrain.Forest;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;
import java.util.Map;
import java.text.NumberFormat;
import java.util.Objects;

/**
 * A tabular report generator for crops---forests, groves, orchards, fields,
 * meadows, and shrubs.
 */
public final class CropTabularReportGenerator implements ITableGenerator</*Forest|Shrub|Meadow|Grove*/TileFixture> {
	@Override
	public boolean canHandle(final IFixture fixture) {
		return fixture instanceof Forest || fixture instanceof Shrub || fixture instanceof Meadow ||
				fixture instanceof Grove;
	}

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

	private final @Nullable Point hq;
	private final MapDimensions dimensions;
	private final Comparator<Point> distanceComparator;

	public CropTabularReportGenerator(final @Nullable Point hq, final MapDimensions dimensions) {
		this.hq = hq;
		this.dimensions = dimensions;
		if (Objects.isNull(hq)) {
			distanceComparator = (one, two) -> 0;
		} else {
			distanceComparator = new DistanceComparatorImpl(hq, dimensions);
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
		String sizeUnit = "unknown"; // Made mutable to simplify code flow for HasPopulation below
		final String crop = ((HasKind) item).getKind();
		switch (item) {
			case final Forest f when f.isRows() -> {
				kind = "rows";
				cultivation = "---";
				status = "---";
			}
			case final Forest f -> {
				kind = "forest";
				cultivation = "---";
				status = "---";
			}
			case final Shrub s -> {
				kind = "shrub";
				cultivation = "---";
				status = "---";
				sizeUnit = "plants";
			}
			case final Meadow m -> {
				kind = m.getType().toString();
				cultivation = m.getCultivation().toString();
				status = m.getStatus().toString();
			}
			case final Grove g -> {
				kind = g.getType().toString();
				cultivation = g.getCultivation().toString();
				status = "---";
				sizeUnit = "trees";
			}
			default -> {
				return Collections.emptyList();
			}
		}
		switch (item) {
			case final HasExtent<?> he when he.getAcres().doubleValue() > 0.0 -> {
				size = truncatedNumberString(he.getAcres());
				sizeUnit = "acres";
			}
			case final HasPopulation<?> hp when hp.getPopulation() > 0 -> {
				size = Integer.toString(hp.getPopulation());
			}
			default -> {
				size = "unknown";
				sizeUnit = "---";
			}
		}
		fixtures.remove(key);
		return Collections.singletonList(Arrays.asList(distanceString(loc, hq, dimensions),
				locationString(loc), kind, size, sizeUnit, cultivation, status, crop));
	}

	/**
	 * Compare two Point-fixture pairs.
	 *
	 * @return
	 */
	@Override
	public Comparator<Pair<Point, TileFixture>> comparePairs() {
		return Comparator.<Pair<Point, TileFixture>, String>comparing(p -> ((HasKind) p.getValue1()).getKind())
				.thenComparing(Pair::getValue0, distanceComparator)
				.thenComparing(p -> p.getValue1().getClass().hashCode())
				.thenComparing(p -> p.getValue1().hashCode());
	}

	@Override
	public @NotNull Class<TileFixture> getTableClass() {
		return TileFixture.class;
	}
}
