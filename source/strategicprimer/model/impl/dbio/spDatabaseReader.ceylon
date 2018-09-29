import strategicprimer.model.impl.xmlio {
    IMapReader
}
import strategicprimer.model.common.xmlio {
    Warning
}
import strategicprimer.model.impl.map {
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
    DataSource getBaseConnection(Path path) {
        SQLiteDataSource retval = SQLiteDataSource();
        if (path.string.empty) {
            log.warn("Setting up an (empty) in-memory database for reading");
            retval.url = "jdbc:sqlite:file::memory:";
        } else {
            retval.url = "jdbc:sqlite:``path``";
        }
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
    shared actual IMutableMapNG readMap(Path file, Warning warner) {
        Sql sql = getSQL(file);
        return dbMapReader.readMap(sql, warner);
    }

    shared actual IMutableMapNG readMapFromStream(Path file, Reader istream,
            Warning warner) {
        throw AssertionError("Can't read a database from a stream");
    }

    shared IMutableMapNG readMapFromDatabase(Sql db, Warning warner) =>
            dbMapReader.readMap(db, warner);
}