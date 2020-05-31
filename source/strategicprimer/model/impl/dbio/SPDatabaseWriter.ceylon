import strategicprimer.model.impl.xmlio {
    SPWriter
}
import ceylon.collection {
    MutableMap,
    MutableSet,
    HashSet,
    HashMap
}
import ceylon.file {
    Path,
    File,
    Nil
}
import ceylon.logging {
    Logger,
    logger
}
import ceylon.dbc {
    Sql,
    newConnectionFromDataSource
}
import org.sqlite {
    SQLiteDataSource
}
import javax.sql {
    DataSource
}
import strategicprimer.model.common.map {
    HasNotes,
    IMapNG
}
import ceylon.language.meta {
    classDeclaration
}

"A logger."
Logger log = logger(`module strategicprimer.model.impl`);

shared object spDatabaseWriter satisfies SPWriter {
    MutableMap<Path, Sql> connections = HashMap<Path, Sql>();

    DataSource getBaseConnection(Path path) {
        SQLiteDataSource retval = SQLiteDataSource();
        if (path.string.empty) {
            log.debug("Trying to set up an in-memory database");
            retval.url = "jdbc:sqlite:file::memory:";
        } else {
            log.debug("Setting up an SQLite database for file ``path``");
            retval.url = "jdbc:sqlite:``path``";
        }
        return retval;
    }

    Sql getSQL(Path path) {
        if (exists connection = connections[path]) {
            return connection;
        } else {
            if (!path.string.empty) {
                assert (is File|Nil res = path.resource);
            }
            Sql retval = Sql(newConnectionFromDataSource(getBaseConnection(path)));
            connections[path] = retval;
            return retval;
        }
    }

    DatabaseWriter<Nothing, Nothing>[] writers = [dbAdventureHandler, dbExplorableHandler,
        dbGroundHandler, dbImplementHandler, dbMapWriter, dbAnimalHandler,
        dbImmortalHandler, dbPlayerHandler, dbPortalHandler, dbResourcePileHandler,
        dbCacheHandler, dbFieldHandler, dbGroveHandler, dbMineHandler, dbMineralHandler,
        dbShrubHandler, dbSimpleTerrainHandler, dbForestHandler, dbTextHandler,
        dbTownHandler, dbCommunityStatsHandler, dbVillageHandler, dbFortressHandler,
        dbUnitHandler, dbWorkerHandler];

    String notesSchema = """CREATE TABLE IF NOT EXISTS notes (
                                fixture INTEGER NOT NULL,
                                player INTEGER NOT NULL,
                                note VARCHAR(255) NOT NULL
                            );""";

    MutableSet<Sql> notesInitialized = HashSet<Sql>();

    shared void writeSPObjectInContext(Sql sql, Object obj, Object context) {
        if (!sql in notesInitialized) {
            sql.transaction(() {
                sql.Statement(notesSchema).execute();
                log.trace("Executed initializer beginning ``notesSchema.lines.first``");
                notesInitialized.add(sql);
                return true;
            });
        }
        if (is HasNotes obj) {
            sql.transaction(() {
                for (player in obj.notesPlayers) {
                    if (exists note = obj.notes.get(player)) {
                        sql.Insert("INSERT INTO notes (fixture, player, note)
                                    VALUES(?, ?, ?)").execute(obj.id, player, note);
                    }
                }
                return true;
            });
        }
        for (writer in writers) {
            if (writer.canWrite(obj, context)) {
                writer.initialize(sql);
                writer.writeRaw(sql, obj, context);
                return;
            }
        }
        throw AssertionError("No writer for ``classDeclaration(obj).name`` found");
    }

    shared actual void writeSPObject(Path|Anything(String) arg, Object obj) {
        "SPDatabaseWriter can only write to a database file, not to a stream"
        assert (is Path arg);
        Sql sql = getSQL(arg);
        writeSPObjectInContext(sql, obj, obj);
    }

    shared actual void write(Path|Anything(String) arg, IMapNG map) =>
            writeSPObject(arg, map);

    shared void writeToDatabase(Sql db, IMapNG map) =>
            writeSPObjectInContext(db, map, map);
}
