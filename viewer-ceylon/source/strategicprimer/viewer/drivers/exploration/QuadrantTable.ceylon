import ceylon.collection {
    MutableMap,
    HashMap
}

import model.map {
    MapDimensions,
    Point,
    MapDimensionsImpl,
    PointFactory,
    TileType,
    TileFixture
}
"An [[EncounterTable]] where results are by quadrant of the map."
class QuadrantTable satisfies EncounterTable {
    "The collection of collections of results."
    MutableMap<MapDimensions, Map<Point, String>> quadrants =
            HashMap<MapDimensions, Map<Point, String>>();
    Map<Point, String> valuesFor(MapDimensions dimensions, String[] possResults,
            Integer quadrantRowCount) {
        // Instance variables passed in because we want to call this from the
        // second constructor.
        if (exists retval = quadrants.get(dimensions)) {
            return retval;
        } else {
            MutableMap<Point, String> retval = HashMap<Point, String>();
            Integer columns = possResults.size / quadrantRowCount;
            variable Integer i = 0;
            Integer mapColumns = dimensions.columns;
            Integer mapRows = dimensions.rows;
            Integer columnRemainder = mapColumns % columns;
            Integer rowRemainder = mapRows % quadrantRowCount;
            Integer columnStep = mapColumns / columns;
            Integer rowStep = mapRows / quadrantRowCount;
            for (row in (0..(mapRows - rowRemainder)).by(rowStep)) {
                for (column in (0..(mapColumns - columnRemainder)).by(columnStep)) {
                    assert (exists temp = possResults[i]);
                    retval.put(PointFactory.point(row, column), temp);
                    i++;
                }
            }
            Map<Point,String> temp = map { *retval };
            quadrants.put(dimensions, temp);
            return temp;
        }
    }
    "The items to allocate by quadrant."
    String[] possibleResults;
    "How many rows of quadrants there should be."
    Integer quadrantRows;
    shared new (Integer rows, String* items) {
        possibleResults = items.sequence();
        quadrantRows = rows;
    }
    shared new forDimensions(Integer mapRows, Integer mapColumns, Integer rows,
            String* items) extends QuadrantTable(rows, *items) {
        MapDimensions dimensions = MapDimensionsImpl(mapRows, mapColumns, 2);
        Map<Point, String> firstQuadrants = valuesFor(dimensions, possibleResults, rows);
        quadrants.put(dimensions, firstQuadrants);
    }
    "Get the item in the table at the quadrant containing the given row and column in a
     map of the specified dimensions."
    shared String getQuadrantValue(Integer row, Integer column,
            MapDimensions mapDimensions) {
        Map<Point, String> resultsMap = valuesFor(mapDimensions, possibleResults,
            quadrantRows);
        variable Point bestKey = PointFactory.invalidPoint;
        for (key in resultsMap.keys) {
            if (key.row <= row, key.row > bestKey.row, key.col <= column,
                    key.col > bestKey.col) {
                bestKey = key;
            }
        }
        if (exists retval = resultsMap.get(bestKey)) {
            return retval;
        } else {
            return "";
        }
    }
    shared actual String generateEvent(Point point, TileType terrain,
            {TileFixture*} fixtures, MapDimensions mapDimensions) =>
                getQuadrantValue(point.row, point.col, mapDimensions);
    shared actual Set<String> allEvents => set { *possibleResults };
    shared actual String string =>
            "QuadrantTable in ``quadrantRows`` rows of quadrants";
}