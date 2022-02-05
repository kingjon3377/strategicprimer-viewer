package impl.dbio;

import buckelieg.jdbc.fn.DB;

import impl.xmlio.IMapReader;
import common.xmlio.Warning;
import common.map.IMutableMapNG;
import java.io.Reader;
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
	private static final Logger LOGGER = Logger.getLogger(SPDatabaseWriter.class.getName());

	private final Map<Path, DB> connections = new HashMap<>();

	private DataSource getBaseConnection(final Path path) {
		SQLiteDataSource retval = new SQLiteDataSource();
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
	private DB getSQL(final Path path) {
		if (connections.containsKey(path)) {
			return connections.get(path);
		} else {
			DB retval = new DB(getBaseConnection(path));
			connections.put(path, retval);
			return retval;
		}
	}

	private final DBMapReader dbMapReader = new DBMapReader();

	@Override
	public IMutableMapNG readMap(final Path file, final Warning warner) {
		DB db = getSQL(file);
		return dbMapReader.readMap(db, warner);
	}

	@Override
	public IMutableMapNG readMapFromStream(final Path file, final Reader istream, final Warning warner) {
		throw new UnsupportedOperationException("Can't read a database from a stream");
	}

	public IMutableMapNG readMapFromDatabase(final DB db, final Warning warner) {
		return dbMapReader.readMap(db, warner);
	}
}
