package drivers.exploration.old;

import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.Collections;

import common.map.Point;
import common.map.TileType;
import common.map.TileFixture;
import common.map.MapDimensions;
import common.map.MapDimensionsImpl;

/**
 * An {@link EncounterTable} where results are by quadrant of the map.
 */
class QuadrantTable implements EncounterTable {
	private static final Logger LOGGER = Logger.getLogger(QuadrantTable.class.getName());
	/**
	 * The collection of collections of results.
	 */
	Map<MapDimensions, Map<Point, String>> quadrants = new HashMap<>();

	// TODO: static?
	private Map<Point, String> valuesFor(MapDimensions dimensions, List<String> possResults,
			int quadrantRowCount) {
		// Instance variables passed in because we want to call this from the
		// second constructor.
		if (quadrants.containsKey(dimensions)) {
			return quadrants.get(dimensions);
		} else {
			Map<Point, String> retval = new HashMap<>();
			final int columns = possResults.size() / quadrantRowCount;
			int i = 0;
			final int mapColumns = dimensions.getColumns();
			final int mapRows = dimensions.getRows();
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
			Map<Point, String> temp = Collections.unmodifiableMap(retval);
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

	public QuadrantTable(int rows, String... items) {
		possibleResults = Collections.unmodifiableList(Arrays.asList(items));
		quadrantRows = rows;
	}

	private QuadrantTable (int mapRows, int mapColumns, int rows, String... items) {
		this(rows, items);
		MapDimensions dimensions = new MapDimensionsImpl(mapRows, mapColumns, 2);
		quadrants.put(dimensions, valuesFor(dimensions, possibleResults, rows));
	}

	public static QuadrantTable forDimensions(int mapRows, int mapColumns, int rows, String... items) {
		return new QuadrantTable(mapRows, mapColumns, rows, items);
	}

	/**
	 * Get the item in the table at the quadrant containing the given row
	 * and column in a map of the specified dimensions.
	 */
	public String getQuadrantValue(int row, int column, MapDimensions mapDimensions) {
		final Map<Point, String> resultsMap = valuesFor(mapDimensions, possibleResults,
			quadrantRows);
		Point bestKey = Point.INVALID_POINT;
		for (Point key : resultsMap.keySet().stream()
				.sorted(Comparator.reverseOrder())
				.collect(Collectors.toList())) {
			if (key.getRow() <= row && key.getRow() > bestKey.getRow() &&
					key.getColumn() <= column && key.getColumn() > bestKey.getColumn()) {
				bestKey = key;
			}
		}
		if (resultsMap.containsKey(bestKey)) {
			return resultsMap.get(bestKey);
		} else {
			LOGGER.severe("Best key not in map"); // FIXME: Throw an exception?
			return "";
		}
	}

	@Override
	public String generateEvent(Point point, @Nullable TileType terrain, boolean mountainous, 
			Iterable<TileFixture> fixtures, MapDimensions mapDimensions) {
		return getQuadrantValue(point.getRow(), point.getColumn(), mapDimensions);
	}

	@Override
	public Set<String> getAllEvents() {
		return new HashSet<>(possibleResults);
	}

	@Override
	public String toString() {
		return String.format("QuadrantTable in %d rows of quadrants", quadrantRows);
	}
}