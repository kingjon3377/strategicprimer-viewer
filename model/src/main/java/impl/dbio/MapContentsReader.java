package impl.dbio;

import lovelace.util.LovelaceLogger;
import legacy.map.IFixture;
import legacy.map.IMutableLegacyMap;
import common.xmlio.Warning;

import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Row;
import io.jenetics.facilejdbc.RowParser;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import java.util.stream.Stream;

import lovelace.util.Accumulator;
import lovelace.util.IntAccumulator;

/**
 * An interface for code to read map contents from an SQL database.
 */
public interface MapContentsReader {
	int READ_LOG_INTERVAL = 50;

	/**
	 * Read map contents---that is, anything directly at a location on the map.
	 *
	 * @param db         The database to read from.
	 * @param map        The map we're reading
	 * @param containers A map by ID of fixtures that can contain others, to connect later.
	 * @param containees A multimap (TODO: use one) by container ID of fixtures that are contained in other fixtures,
	 *                   to be added to their containers later.
	 * @param warner     Warning instance to use
	 */
	void readMapContents(Connection db, IMutableLegacyMap map, Map<Integer, IFixture> containers,
						 Map<Integer, List<Object>> containees, Warning warner) throws SQLException;

	private static Map<String, Object> parseToMap(final Row rs, final Connection conn) throws SQLException {
		final ResultSetMetaData rsm = rs.getMetaData();
		final Map<String, Object> retval = new HashMap<>(rsm.getColumnCount());
		for (int i = 1; i <= rsm.getColumnCount(); i++) {
			retval.put(rsm.getColumnLabel(i), rs.getObject(i));
		}
		return retval;
	}

	/**
	 * Run the given method on each row returned by the given query.
	 *
	 * FIXME: Provide a version taking a RowParser, for the more common case of 1:1 object-to-row mapping
	 */
	default void handleQueryResults(final Connection db, final Warning warner, final String description,
									final TryBiConsumer<Map<String, Object>, Warning, SQLException> handler,
									final Query query, final Object... args) throws SQLException {
		LovelaceLogger.debug("About to read %s", description);
		final Accumulator<Integer> count = new IntAccumulator(0);
		try (final Stream<Map<String, Object>> stream = query
				.as(((RowParser<Map<String, Object>>) MapContentsReader::parseToMap).stream(), db)) {
			stream.forEach(handler.andThen((m, w) -> {
				count.add(1);
				if (count.getSum() % READ_LOG_INTERVAL == 0) {
					LovelaceLogger.debug("Finished reading %d %s", count.getSum(), description);
				}
			}).wrappedPartial(warner));
		}
		LovelaceLogger.debug("Finished reading %s", description);
	}

	default void multimapPut(final Map<Integer, List<Object>> mapping, final Integer key, final Object val) {
		if (mapping.containsKey(key)) {
			mapping.get(key).add(val);
		} else {
			final List<Object> list = new ArrayList<>();
			list.add(val);
			mapping.put(key, list);
		}
	}

	/**
	 * Read a Boolean value from a field. This helper is necessary because
	 * SQLite stores booleans as integers, and the JDBC library unhelpfully
	 * exposes that "feature".
	 *
	 * TODO: If needed, make a version that returns null (via boxed Boolean) if not found instead of throwing
	 */
	default boolean getBooleanValue(final Map<String, Object> dbRow, final String key) throws SQLException {
		if (!dbRow.containsKey(key)) {
			throw new SQLException("Expected key '%s' not in the schema".formatted(key));
		}
		final Object val = dbRow.get(key);
		return switch (val) {
			case null -> throw new SQLException("No value for key " + key);
			case final Boolean b -> b; // Can't happen in SQLite, but we can dream ...
			case final Integer i when i == 0 -> false;
			case final Integer i when i == 1 -> true;
			case final Integer _ -> throw new SQLException("Invalid Boolean value",
					new IllegalArgumentException("Outside range of Boolean"));
			default -> throw new SQLException("Invalid Boolean value",
					new IllegalArgumentException("Field maps to non-Boolean value"));
		};
	}
}
