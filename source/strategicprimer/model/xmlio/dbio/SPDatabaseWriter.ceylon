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
class SPDatabaseWriter() satisfies SPWriter {
	MutableMap<Path, Sql> connections = HashMap<Path, Sql>();
	DataSource getBaseConnection(Path path) {
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
	DatabaseWriter<Anything>[] writers = [];
	shared actual void writeSPObject(Path|Anything(String) arg, Object obj) {
		"SPDatabaseWriter can only write to a database file, not to a stream"
		assert (is Path arg);
		Sql sql = getSQL(arg);
		for (writer in writers) {
			if (writer.canWrite(obj)) {
				writer.writeRaw(sql, obj);
				return;
			}
		}
		throw AssertionError("No writer for ``classDeclaration(obj).name`` found");
	}
	shared actual void write(Path|Anything(String) arg, IMapNG map) => writeSPObject(arg, map);
}