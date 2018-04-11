import strategicprimer.model.xmlio {
	SPWriter
}
import ceylon.collection {
	MutableMap,
	HashMap
}
import ceylon.file {
	Path,
	File,
	Nil
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
import strategicprimer.model.map {
	IMapNG
}
import ceylon.language.meta {
	classDeclaration
}
shared object spDatabaseWriter satisfies SPWriter {
	MutableMap<Path, Sql> connections = HashMap<Path, Sql>();
	DataSource getBaseConnection(Path path) { // TODO: Figure out how to use Derby/JavaDB for an empty Path
		SQLiteDataSource retval = SQLiteDataSource();
		retval.url = "jdbc:sqlite:``path``";
		return retval;
	}
	Sql getSQL(Path path) {
		if (exists connection = connections[path]) {
			return connection;
		} else {
			assert (is File|Nil res = path.resource);
			Sql retval = Sql(newConnectionFromDataSource(getBaseConnection(path)));
			connections[path] = retval;
			return retval;
		}
	}
	DatabaseWriter<Nothing, Nothing>[] writers = [dbAdventureHandler, dbExplorableHandler,
		dbGroundHandler, dbImplementHandler, dbMapWriter, dbAnimalHandler, dbImmortalHandler,
		dbPlayerHandler, dbPortalHandler, dbResourcePileHandler, dbCacheHandler, dbFieldHandler,
		dbGroveHandler, dbMineHandler, dbMineralHandler, dbShrubHandler, dbSimpleTerrainHandler,
		dbForestHandler, dbTextWriter, dbTownHandler, dbCommunityStatsHandler, dbVillageHandler,
		dbFortressHandler, dbUnitHandler, dbWorkerHandler];
	shared void writeSPObjectInContext(Sql sql, Object obj, Object context) {
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
	shared actual void write(Path|Anything(String) arg, IMapNG map) => writeSPObject(arg, map); // FIXME: Allow caller to pass in an Sql, for use in tests
}