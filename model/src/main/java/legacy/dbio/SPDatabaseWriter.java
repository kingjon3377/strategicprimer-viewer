package legacy.dbio;

import impl.dbio.DatabaseWriter;
import impl.xmlio.SPWriter;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;

import lovelace.util.LovelaceLogger;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;

import legacy.map.HasNotes;
import legacy.map.ILegacyMap;

import java.nio.file.Path;

import lovelace.util.ThrowingConsumer;

import java.io.IOException;
import java.util.regex.Pattern;

import static io.jenetics.facilejdbc.Param.value;

public final class SPDatabaseWriter implements SPWriter {
	private static final Pattern LINEBREAK = Pattern.compile("\\R");
	private final Map<Path, Transactional> connections = new HashMap<>();

	private static DataSource getBaseConnection(final Path path) {
		final SQLiteDataSource retval = new SQLiteDataSource();
		if (path.toString().isEmpty()) {
			LovelaceLogger.info("Trying to set up an in-memory database");
			retval.setUrl("jdbc:sqlite:file::memory:");
		} else {
			LovelaceLogger.info("Setting up an SQLite database for file %s", path);
			retval.setUrl("jdbc:sqlite:" + path);
		}
		return retval;
	}

	private Transactional getDB(final Path path) {
		return connections.computeIfAbsent(path, p -> getBaseConnection(p)::getConnection);
	}

	private final DBPlayerHandler playerHandler = new DBPlayerHandler();

	private final List<DatabaseWriter<?, ?>> writers = List.of(new DBAdventureHandler(), new DBExplorableHandler(),
			new DBGroundHandler(), new DBImplementHandler(), new DBMapWriter(this, playerHandler),
			new DBAnimalHandler(), new DBImmortalHandler(), playerHandler, new DBPortalHandler(),
			new DBResourcePileHandler(), new DBCacheHandler(), new DBFieldHandler(), new DBGroveHandler(),
			new DBMineHandler(), new DBMineralHandler(), new DBShrubHandler(), new DBSimpleTerrainHandler(),
			new DBForestHandler(), new DBTextHandler(), new DBTownHandler(), new DBCommunityStatsHandler(),
			new DBVillageHandler(), new DBFortressHandler(this), new DBUnitHandler(this),
			new DBWorkerHandler());

	private static final Query NOTES_SCHEMA =
			Query.of("CREATE TABLE IF NOT EXISTS notes (" +
					"    fixture INTEGER NOT NULL," +
					"    player INTEGER NOT NULL," +
					"    note VARCHAR(255) NOT NULL" +
					");");

	// FIXME: Make an AbstractDatabaseWriter/MapContentsReader for this so
	// we don't have to keep this parallel set of DB objects here
	private final Collection<Transactional> notesInitialized = new HashSet<>();

	private static final Query INSERT_NOTE =
			Query.of("INSERT INTO notes (fixture, player, note) VALUES(:fixture, :player, :note)");

	public void writeSPObjectInContext(final Transactional sql, final Object obj, final Object context)
			throws SQLException {
		if (!notesInitialized.contains(sql)) {
			sql.transaction().accept(db -> {
				NOTES_SCHEMA.execute(db);
				LovelaceLogger.debug("Executed initializer beginning %s",
						LINEBREAK.split(NOTES_SCHEMA.rawSql())[0]);
			});
			notesInitialized.add(sql);
		}
		if (obj instanceof final HasNotes hn) {
			sql.transaction().accept(db -> {
				for (final Integer player : hn.getNotesPlayers()) {
					final String note = hn.getNote(player);
					if (!note.isEmpty()) {
						INSERT_NOTE.on(value("fixture", hn.getId()),
								value("player", player), value("note", note)).executeInsert(db);
					}
				}
			});
		}
		for (final DatabaseWriter<?, ?> writer : writers) {
			if (writer.canWrite(obj, context)) {
				writer.initialize(sql);
				writer.writeRaw(sql, obj, context);
				return;
			}
		}
		throw new IllegalStateException("No writer for %s found".formatted(obj.getClass().getName()));
	}

	@Override
	public void writeSPObject(final Path arg, final Object obj) throws IOException {
		final Transactional db = getDB(arg);
		try {
			writeSPObjectInContext(db, obj, obj);
		} catch (final SQLException except) {
			throw new IOException(except);
		}
	}

	@Override
	public void writeSPObject(final ThrowingConsumer<String, IOException> arg, final Object obj) {
		throw new UnsupportedOperationException(
				"SPDatabaseWriter can only write to a database file, not to a stream");
	}

	@Override
	public void write(final Path arg, final ILegacyMap map) throws IOException {
		writeSPObject(arg, map);
	}

	@Override
	public void write(final ThrowingConsumer<String, IOException> arg, final ILegacyMap map) {
		writeSPObject(arg, map);
	}

	public void writeToDatabase(final Transactional db, final ILegacyMap map) throws SQLException {
		writeSPObjectInContext(db, map, map);
	}
}
