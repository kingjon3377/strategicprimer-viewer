import ceylon.dbc {
    Sql,
    SqlNull
}

import java.sql {
    Types
}

import strategicprimer.model.common.map {
    Point
}

import strategicprimer.model.impl.map {
    IMutableMapNG
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import strategicprimer.model.impl.map.fixtures.mobile {
    Animal,
    maturityModel,
    AnimalImpl,
    AnimalTracks
}
import strategicprimer.model.common.xmlio {
    Warning
}

import lovelace.util.common {
    as
}

object dbAnimalHandler extends AbstractDatabaseWriter<Animal|AnimalTracks, Point|IUnit>()
        satisfies MapContentsReader {
    Integer|SqlNull born(Animal animal) {
        if (exists maturityAge = maturityModel.maturityAges[animal.kind],
                maturityAge <= (currentTurn - animal.born)) {
            return SqlNull(Types.integer);
        } else {
            return animal.born;
        }
    }
    shared actual {String+} initializers = [
        """CREATE TABLE IF NOT EXISTS animals (
               row INTEGER,
               column INTEGER
                   CHECK ((animals.row IS NOT NULL AND column IS NOT NULL) OR
                       (animals.row IS NULL AND column IS NULL)),
               parent INTEGER
                   CHECK ((row IS NOT NULL AND parent IS NULL) OR
                       (row IS NULL AND parent IS NOT NULL)),
               kind VARCHAR(32) NOT NULL,
               talking BOOLEAN NOT NULL,
               status VARCHAR(32) NOT NULL,
               born INTEGER,
               count INTEGER NOT NULL,
               id INTEGER NOT NULL,
               image VARCHAR(255)
           );""",
        // We assume that animal tracks can't occur inside a unit or fortress, and ignore
        // their 'domestication status', 'talking', 'born', and 'count'. We also follow
	    // the XML I/O framework in discarding their IDs.
        """CREATE TABLE IF NOT EXISTS tracks (
               row INTEGER NOT NULL,
               column INTEGER NOT NULL,
               kind VARCHAR(32) NOT NULL,
               image VARCHAR(255)
           );"""
    ];
    shared actual void write(Sql db, Animal|AnimalTracks obj, Point|IUnit context) {
        if (is AnimalTracks obj) {
            "We assume that animal tracks can't occur inside a unit."
            assert (is Point context);
            db.Insert("""INSERT INTO tracks (row, column, kind, image)
                         VALUES(?, ?, ?, ?);""")
                    .execute(context.row, context.column, obj.kind, obj.image);
        } else {
            value insertion = db.Insert(
                """INSERT INTO animals (row, column, parent, kind, talking, status,
	                   born, count, id, image)
                   VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);""");
            if (is Point context) {
                insertion.execute(context.row, context.column, SqlNull(Types.integer),
                    obj.kind, obj.talking, obj.status, born(obj), obj.population,
	                obj.id, obj.image);
            } else {
                insertion.execute(SqlNull(Types.integer), SqlNull(Types.integer),
	                context.id, obj.kind, obj.talking, obj.status, born(obj),
	                obj.population, obj.id, obj.image);
            }
        }
    }
    void readAnimal(IMutableMapNG map, Map<String, Object> dbRow, Warning warner) {
        assert (is String kind = dbRow["kind"],
            is Boolean talking = dbMapReader.databaseBoolean(dbRow["talking"]),
            is String status = dbRow["status"], is Integer|SqlNull born = dbRow["born"],
            is Integer count = dbRow["count"], is Integer id = dbRow["id"],
            is String|SqlNull image = dbRow["image"]);
        value animal = AnimalImpl(kind, talking, status, id, as<Integer>(born) else -1,
	        count);
        if (is String image) {
            animal.image = image;
        }
        if (is Integer row = dbRow["row"], is Integer column = dbRow["column"]) {
            map.addFixture(Point(row, column), animal);
        } else {
            assert (is Integer parentId = dbRow["parent"],
                is IUnit parent = findById(map, parentId, warner));
            parent.addMember(animal);
        }
    }
    void readTracks(IMutableMapNG map, Map<String, Object> dbRow, Warning warner) {
        assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
            is String kind = dbRow["kind"], is String|SqlNull image = dbRow["image"]);
        value track = AnimalTracks(kind);
        if (is String image) {
            track.image = image;
        }
        map.addFixture(Point(row, column), track);
    }
    shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {
        handleQueryResults(db, warner, "animal populations", curry(readAnimal)(map),
            """SELECT * FROM animals WHERE row IS NOT NULL""");
        handleQueryResults(db, warner, "animal tracks", curry(readTracks)(map),
            """SELECT * FROM tracks""");
    }
    shared actual void readExtraMapContents(Sql db, IMutableMapNG map, Warning warner) =>
            handleQueryResults(db, warner, "animals in units", curry(readAnimal)(map),
                """SELECT * FROM animals WHERE parent IS NOT NULL""");
}