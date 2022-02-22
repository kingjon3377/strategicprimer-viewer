package impl.dbio;

import buckelieg.jdbc.fn.DB;

import common.map.IFixture;
import common.map.IMutableMapNG;
import common.map.IMapNG;
import common.map.fixtures.FixtureIterable;
import common.xmlio.Warning;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import java.util.logging.Logger;

import lovelace.util.Accumulator;
import lovelace.util.IntAccumulator;

/**
 * An interface for code to read map contents from an SQL database.
 */
interface MapContentsReader {
	/**
	 * Logger for default methods.
	 */
	Logger LOGGER = Logger.getLogger(MapContentsReader.class.getName());
	/**
	 * Read map contents---that is, anything directly at a location on the map.
	 *
	 * @param db The database to read from.
	 * @param map The map we're reading
	 * @param containers A map by ID of fixtures that can contain others, to connect later.
	 * @param containees A multimap (TODO: use one) by container ID of fixtures that are contained in other fixtures,
	 *                      to be added to their containers later.
	 * @param warner Warning instance to use
	 */
	void readMapContents(DB db, IMutableMapNG map, Map<Integer, IFixture> containers,
			Map<Integer, List<Object>> containees, Warning warner);

	/**
	 * Run the given method on each row returned by the given query.
	 *
	 * FIXME: Narrow TryBiConsumer exception type arg and the exception type declared to be thrown here
	 */
	default void handleQueryResults(final DB db, final Warning warner, final String description,
	                                final TryBiConsumer<Map<String, Object>, Warning, Exception> handler, final String query,
	                                final Object... args) throws Exception {
		LOGGER.fine("About to read " + description);
		final Accumulator<Integer> count = new IntAccumulator(0);
		db.select(query, args).execute().forEach(handler.andThen((m, w) -> {
				count.add(1);
				if (count.getSum() % 50 == 0) {
					LOGGER.fine(String.format("Finished reading %d %s", count.getSum(),
						description));
				}
			}).wrappedPartial(warner));
		LOGGER.fine("Finished reading " + description);
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
}
