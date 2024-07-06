package legacy.dbio;

import common.xmlio.SPFormatException;
import legacy.xmlio.IMapReader;
import common.xmlio.Warning;
import legacy.map.IMutableLegacyMap;
import io.jenetics.facilejdbc.Transactional;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.NoSuchFileException;
import java.sql.SQLException;
import javax.xml.stream.XMLStreamException;

import lovelace.util.LovelaceLogger;
import org.sqlite.SQLiteDataSource;

import java.util.Map;
import java.util.HashMap;
import javax.sql.DataSource;
import java.nio.file.Path;

public class SPDatabaseReader implements IMapReader {
	private final Map<Path, Transactional> connections = new HashMap<>();

	private static DataSource getBaseConnection(final Path path) {
		final SQLiteDataSource retval = new SQLiteDataSource();
		if (path.toString().isEmpty()) {
			LovelaceLogger.info("Trying to set up an (empty) in-memory database for reading");
			retval.setUrl("jdbc:sqlite:file::memory:");
		} else {
			LovelaceLogger.info("Setting up an SQLite database for file %s", path);
			retval.setUrl("jdbc:sqlite:" + path);
		}
		return retval;
	}

	private Transactional getDB(final Path path) {
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
	public IMutableLegacyMap readMap(final Path file, final Warning warner)
			throws IOException {
		final Transactional db = getDB(file);
		try {
			return dbMapReader.readMap(db, warner);
		} catch (final SQLException except) {
			throw new IOException(except);
		}
	}

	@Override
	public IMutableLegacyMap readMapFromStream(final Path file, final Reader istream, final Warning warner) {
		throw new UnsupportedOperationException("Can't read a database from a stream");
	}

	public IMutableLegacyMap readMapFromDatabase(final Transactional db, final Warning warner) throws IOException {
		try {
			return dbMapReader.readMap(db, warner);
		} catch (final SQLException except) {
			throw new IOException(except);
		}
	}

	// TODO: Find a way to restrict access, or to make the two collections have their lifespan tied to a particular DB
	public void clearCache() {
		dbMapReader.clearCache();
	}
}
