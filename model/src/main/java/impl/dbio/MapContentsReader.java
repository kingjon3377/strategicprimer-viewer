package impl.dbio;

import buckelieg.jdbc.fn.DB;

import common.map.IFixture;
import common.map.IMutableMapNG;
import common.map.IMapNG;
import common.map.fixtures.FixtureIterable;
import common.xmlio.Warning;

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
	static final Logger LOGGER = Logger.getLogger(MapContentsReader.class.getName());
	/**
	 * Read map direct contents---that is, anything directly at a location on the map.
	 */
	void readMapContents(DB db, IMutableMapNG map, Warning warner);

	/**
	 * Read non-direct contents---that is, unit and fortress members and
	 * the like. Because in many cases this doesn't apply, it's by default a noop.
	 */
	default void readExtraMapContents(final DB db, final IMutableMapNG map, final Warning warner) {}

	/**
	 * Find a tile fixture or unit or fortress member within a given stream
	 * of such objects by its ID, if present.
	 */
	@Nullable
	default IFixture findByIdImpl(final Iterable<IFixture> stream, final int id) {
		for (IFixture fixture : stream) {
			if (fixture.getId() == id) {
				return fixture;
			} else if (fixture instanceof FixtureIterable) {
				IFixture retval = findByIdImpl((FixtureIterable<IFixture>) fixture, id);
				if (retval != null) {
					return retval;
				}
			}
		}
		return null;
	}

	/**
	 * Find a tile fixture or unit or fortress member by ID.
	 */
	default IFixture findById(final IMapNG map, final int id, final Warning warner) {
		return DBMemoizer.findById(map, id, this, warner);
	}

	/**
	 * Run the given method on each row returned by the given query.
	 *
	 * FIXME: Narrow TryBiConsumer exception type arg and the exception type declared to be thrown here
	 */
	default void handleQueryResults(final DB db, final Warning warner, final String description,
	                                final TryBiConsumer<Map<String, Object>, Warning, Exception> handler, final String query,
	                                final Object... args) throws Exception {
		LOGGER.fine("About to read " + description);
		Accumulator<Integer> count = new IntAccumulator(0);
		db.select(query, args).execute().forEach(handler.andThen((m, w) -> {
				count.add(1);
				if (count.getSum() % 50 == 0) {
					LOGGER.fine(String.format("Finished reading %d %s", count.getSum(),
						description));
				}
			}).wrappedPartial(warner));
		LOGGER.fine("Finished reading " + description);
	}
}
