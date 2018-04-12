import strategicprimer.model.xmlio {
	IMapReader,
	Warning
}
import strategicprimer.model.map {
	IMutableMapNG
}
import java.nio.file {
	Path
}
import java.io {
	Reader
}
import ceylon.dbc {
	newConnectionFromDataSource,
	Sql
}
import org.sqlite {
	SQLiteDataSource
}
import ceylon.collection {
	MutableMap,
	HashMap
}
import javax.sql {
	DataSource
}
shared object spDatabaseReader satisfies IMapReader {
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
			Sql retval = Sql(newConnectionFromDataSource(getBaseConnection(path)));
			connections[path] = retval;
			return retval;
		}
	}
	shared actual IMutableMapNG readMap(Path file, Warning warner) { // FIXME: Allow caller to pass in an Sql, for use in tests
		Sql sql = getSQL(file);
		return dbMapReader.readMap(sql, warner);
	}

	shared actual IMutableMapNG readMapFromStream(Path file, Reader istream, Warning warner) {
		throw AssertionError("Can't read a database from a stream");
	}
}