package impl.dbio;

import impl.xmlio.IMapReader;
import common.xmlio.Warning;
import common.map.IMutableMapNG;
import io.jenetics.facilejdbc.Transactional;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import org.sqlite.SQLiteDataSource;
import java.util.Map;
import java.util.HashMap;
import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.logging.Logger;

public class SPDatabaseReader implements IMapReader {
	/**
	 * A logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(SPDatabaseReader.class.getName());

	private final Map<Path, Transactional> connections = new HashMap<>();

	private static DataSource getBaseConnection(final Path path) {
		final SQLiteDataSource retval = new SQLiteDataSource();
		if (path.toString().isEmpty()) {
			LOGGER.info("Trying to set up an (empty) in-memory database for reading");
			retval.setUrl("jdbc:sqlite:file::memory:");
		} else {
			LOGGER.info("Setting up an SQLite database for file " + path);
			retval.setUrl("jdbc:sqlite:" + path);
		}
		return retval;
	}

	// TODO: Rename to getDB
	private Transactional getSQL(final Path path) {
		if (connections.containsKey(path)) {
			return connections.get(path);
		} else {
			final Transactional retval = getBaseConnection(path)::getConnection;
			connections.put(path, retval);
			return retval;
		}
	}

	private final DBMapReader dbMapReader = new DBMapReader();

	@Override
	public IMutableMapNG readMap(final Path file, final Warning warner) throws IOException {
		final Transactional db = getSQL(file);
		try {
			return dbMapReader.readMap(db, warner);
		} catch (final SQLException except) {
			throw new IOException(except);
		}
	}

	@Override
	public IMutableMapNG readMapFromStream(final Path file, final Reader istream, final Warning warner) {
		throw new UnsupportedOperationException("Can't read a database from a stream");
	}

	public IMutableMapNG readMapFromDatabase(final Transactional db, final Warning warner) throws IOException {
		try {
			return dbMapReader.readMap(db, warner);
		} catch (SQLException except) {
			throw new IOException(except);
		}
	}

	// TODO: Find a way to restrict access, or to make the two collections have their lifespan tied to a particular database
	public void clearCache() {
		dbMapReader.clearCache();
	}
}
