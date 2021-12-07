package impl.dbio;

import buckelieg.jdbc.fn.DB;

import impl.xmlio.SPWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.logging.Logger;
import org.sqlite.SQLiteDataSource;
import javax.sql.DataSource;
import common.map.HasNotes;
import common.map.IMapNG;
import java.nio.file.Path;
import static impl.xmlio.SPWriter.IOConsumer;
import java.util.Collections;
import java.util.Arrays;

public final class SPDatabaseWriter implements SPWriter {
	/**
	 * A logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(SPDatabaseWriter.class.getName());

	private final Map<Path, DB> connections = new HashMap<>();

	private DataSource getBaseConnection(Path path) {
		SQLiteDataSource retval = new SQLiteDataSource();
		if (path.toString().isEmpty()) {
			LOGGER.info("Trying to set up an in-memory database");
			retval.setUrl("jdbc:sqlite:file::memory:");
		} else {
			LOGGER.info("Setting up an SQLite database for file " + path);
			retval.setUrl("jdbc:sqlite:" + path);
		}
		return retval;
	}

	// TODO: Rename to getDB
	private DB getSQL(Path path) {
		if (connections.containsKey(path)) {
			return connections.get(path);
		} else {
			DB retval = new DB(getBaseConnection(path));
			connections.put(path, retval);
			return retval;
		}
	}

	private final DBPlayerHandler playerHandler = new DBPlayerHandler();

	private final List<DatabaseWriter<?, ?>> writers = Collections.unmodifiableList(Arrays.asList(
		new DBAdventureHandler(), new DBExplorableHandler(), new DBGroundHandler(),
		new DBImplementHandler(), new DBMapWriter(this, playerHandler), new DBAnimalHandler(),
		new DBImmortalHandler(), playerHandler, new DBPortalHandler(),
		new DBResourcePileHandler(), new DBCacheHandler(), new DBFieldHandler(),
		new DBGroveHandler(), new DBMineHandler(), new DBMineralHandler(), new DBShrubHandler(),
		new DBSimpleTerrainHandler(), new DBForestHandler(), new DBTextHandler(),
		new DBTownHandler(), new DBCommunityStatsHandler(), new DBVillageHandler(),
		new DBFortressHandler(this), new DBUnitHandler(this), new DBWorkerHandler()));

	private static final String NOTES_SCHEMA =
		"CREATE TABLE IF NOT EXISTS notes (" +
			"    fixture INTEGER NOT NULL," +
			"    player INTEGER NOT NULL," +
			"    note VARCHAR(255) NOT NULL" +
			");";

	// FIXME: Make an AbstractDatabaseWriter/MapContentsReader for this so
	// we don't have to keep this parallel set of DB objects here
	private final Set<DB> notesInitialized = new HashSet<>();

	private static final String INSERT_NOTE =
		"INSERT INTO notes (fixture, player, note) VALUES(?, ?, ?)";

	public void writeSPObjectInContext(DB sql, Object obj, Object context) {
		if (!notesInitialized.contains(sql)) {
			sql.transaction(db -> {
					db.script(NOTES_SCHEMA).execute();
					LOGGER.fine("Executed initializer beginning " +
						NOTES_SCHEMA.split("\\R")[0]);
					notesInitialized.add(sql);
					return true;
				});
		}
		if (obj instanceof HasNotes) {
			sql.transaction(db -> {
					for (Integer player : ((HasNotes) obj).getNotesPlayers()) {
						String note = ((HasNotes) obj).getNote(player);
						if (note != null) {
							db.update(INSERT_NOTE, ((HasNotes) obj).getId(),
								player, note)
								.execute();
						}
					}
					return true;
				});
		}
		for (DatabaseWriter<?, ?> writer : writers) {
			if (writer.canWrite(obj, context)) {
				writer.initialize(sql);
				writer.writeRaw(sql, obj, context);
				return;
			}
		}
		throw new IllegalStateException(String.format("No writer for %s found",
			obj.getClass().getName()));
	}

	@Override
	public void writeSPObject(Path arg, Object obj) {
		DB db = getSQL(arg);
		writeSPObjectInContext(db, obj, obj);
	}

	@Override
	public void writeSPObject(IOConsumer<String> arg, Object obj) {
		throw new UnsupportedOperationException(
			"SPDatabaseWriter can only write to a database file, not to a stream");
	}

	@Override
	public void write(Path arg, IMapNG map) {
		writeSPObject(arg, map);
	}

	@Override
	public void write(IOConsumer<String> arg, IMapNG map) {
		writeSPObject(arg, map);
	}

	public void writeToDatabase(DB db, IMapNG map) {
		writeSPObjectInContext(db, map, map);
	}
}
