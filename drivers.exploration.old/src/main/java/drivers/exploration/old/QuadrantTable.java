package drivers.exploration.old;

import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Collections;

import legacy.map.Point;
import legacy.map.TileType;
import legacy.map.TileFixture;
import legacy.map.MapDimensions;
import legacy.map.MapDimensionsImpl;

/**
 * An {@link EncounterTable} where results are by quadrant of the map.
 */
class QuadrantTable implements EncounterTable {
	/**
	 * The collection of collections of results.
	 */
	private final Map<MapDimensions, Map<Point, String>> quadrants = new HashMap<>();

	// TODO: static?
	private Map<Point, String> valuesFor(final MapDimensions dimensions, final List<String> possResults,
										 final int quadrantRowCount) {
		// Instance variables passed in because we want to call this from the
		// second constructor.
		if (quadrants.containsKey(dimensions)) {
			return quadrants.get(dimensions);
		} else {
			final Map<Point, String> retval = new HashMap<>();
			final int columns = possResults.size() / quadrantRowCount;
			int i = 0;
			final int mapColumns = dimensions.columns();
			final int mapRows = dimensions.rows();
			final int columnRemainder = mapColumns % columns;
			final int rowRemainder = mapRows % quadrantRowCount;
			final int columnStep = mapColumns / columns;
			final int rowStep = mapRows / quadrantRowCount;
			for (int row = 0; row < (mapRows - rowRemainder); row += rowStep) {
				for (int column = 0; column < (mapColumns - columnRemainder);
					 column += columnStep) {
					retval.put(new Point(row, column), possResults.get(i));
					i++;
				}
			}
			final Map<Point, String> temp = Collections.unmodifiableMap(retval);
			quadrants.put(dimensions, temp);
			return temp;
		}
	}

	/**
	 * The items to allocate by quadrant.
	 */
	private final List<String> possibleResults;

	/**
	 * How many rows of quadrants there should be.
	 */
	private final int quadrantRows;

	public QuadrantTable(final int rows, final String... items) {
		possibleResults = List.of(items);
		quadrantRows = rows;
	}

	private QuadrantTable(final int mapRows, final int mapColumns, final int rows, final String... items) {
		this(rows, items);
		final MapDimensions dimensions = new MapDimensionsImpl(mapRows, mapColumns, 2);
		quadrants.put(dimensions, valuesFor(dimensions, possibleResults, rows));
	}

	public static QuadrantTable forDimensions(final int mapRows, final int mapColumns, final int rows,
											  final String... items) {
		return new QuadrantTable(mapRows, mapColumns, rows, items);
	}

	/**
	 * Get the item in the table at the quadrant containing the given row
	 * and column in a map of the specified dimensions.
	 */
	public String getQuadrantValue(final int row, final int column, final MapDimensions mapDimensions) {
		final Map<Point, String> resultsMap = valuesFor(mapDimensions, possibleResults,
				quadrantRows);
		Point bestKey = Point.INVALID_POINT;
		for (final Point key : resultsMap.keySet().stream()
				.sorted(Comparator.reverseOrder()).toList()) {
			if (key.row() <= row && key.row() > bestKey.row() &&
					key.column() <= column && key.column() > bestKey.column()) {
				bestKey = key;
			}
		}
		if (resultsMap.containsKey(bestKey)) {
			return resultsMap.get(bestKey);
		} else {
			LovelaceLogger.error("Best key not in map"); // FIXME: Throw an exception?
			return "";
		}
	}

	@Override
	public String generateEvent(final Point point, final @Nullable TileType terrain, final boolean mountainous,
								final Iterable<TileFixture> fixtures, final MapDimensions mapDimensions) {
		return getQuadrantValue(point.row(), point.column(), mapDimensions);
	}

	@Override
	public Set<String> getAllEvents() {
		return new HashSet<>(possibleResults);
	}

	@Override
	public String toString() {
		return "QuadrantTable in %d rows of quadrants".formatted(quadrantRows);
	}
}
