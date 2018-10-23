import ceylon.dbc {
    Sql,
    SqlNull
}

import java.sql {
    Types
}

import strategicprimer.model.common.map {
    IMutableMapNG,
    Point,
    HasKind,
    HasMutableImage
}
import strategicprimer.model.common.map.fixtures.mobile {
    Immortal,
    IUnit,
    SimpleImmortal,
    Centaur,
    Dragon,
    Fairy,
    Giant,
    Sphinx,
    Djinn,
    Griffin,
    Minotaur,
    Ogre,
    Phoenix,
    Simurgh,
    Troll
}
import strategicprimer.model.common.xmlio {
    Warning
}

object dbImmortalHandler extends AbstractDatabaseWriter<Immortal, Point|IUnit>()
        satisfies MapContentsReader {
    shared actual {String+} initializers = [
        """CREATE TABLE IF NOT EXISTS simple_immortals (
               row INTEGER,
               column INTEGER
                   CHECK ((row IS NOT NULL AND column IS NOT NULL)
                       OR (row IS NULL AND column IS NULL)),
               parent INTEGER
                   CHECK ((row IS NOT NULL AND parent IS NULL)
                       OR (row IS NULL AND parent IS NOT NULL)),
               type VARCHAR(16) NOT NULL
                   CHECK (type IN('sphinx', 'djinn', 'griffin', 'minotaur', 'ogre',
                       'phoenix', 'simurgh', 'troll')),
               id INTEGER NOT NULL,
               image VARCHAR(255)
           );""",
        """CREATE TABLE IF NOT EXISTS kinded_immortals (
               row INTEGER,
               column INTEGER
                   CHECK ((row IS NOT NULL AND column IS NOT NULL)
                       OR (row IS NULL AND column IS NULL)),
               parent INTEGER
                   CHECK ((row IS NOT NULL AND parent IS NULL)
                       OR (row IS NULL AND parent IS NOT NULL)),
               type VARCHAR(16) NOT NULL
                   CHECK (type IN ('centaur', 'dragon', 'fairy', 'giant')),
               kind VARCHAR(32) NOT NULL,
               id INTEGER NOT NULL,
               image VARCHAR(255)
           );"""
    ];

    shared actual void write(Sql db, Immortal obj, Point|IUnit context) {
        if (is SimpleImmortal obj) {
            value insertion = db.Insert("""INSERT INTO simple_immortals (row, column,
                                               parent, type, id, image)
                                           VALUES(?, ?, ?, ?, ?, ?);""");
            if (is Point context) {
                insertion.execute(context.row, context.column, SqlNull(Types.integer),
                    obj.kind, obj.id, obj.image);
            } else {
                insertion.execute(SqlNull(Types.integer), SqlNull(Types.integer),
                    context.id, obj.kind, obj.id, obj.image);
            }
        } else {
            assert (is HasKind obj);
            assert (is Centaur|Dragon|Fairy|Giant obj);
            String type;
            switch (obj)
            case (is Centaur) {
                type = "centaur";
            }
            case (is Dragon) {
                type = "dragon";
            }
            case (is Fairy) {
                type = "fairy";
            }
            case (is Giant) {
                type = "giant";
            }
            value insertion = db.Insert(
                """INSERT INTO kinded_immortals (row, column, parent, type, kind, id,
                       image)
                   VALUES(?, ?, ?, ?, ?, ?, ?);""");
            if (is Point context) {
                insertion.execute(context.row, context.column, SqlNull(Types.integer),
                    type, obj.kind, obj.id, obj.image);
            } else {
                insertion.execute(SqlNull(Types.integer), SqlNull(Types.integer),
                    context.id, type, obj.kind, obj.id, obj.image);
            }
        }
    }

    void readSimpleImmortal(IMutableMapNG map, Map<String, Object> dbRow,
            Warning warner) {
        assert (is String type = dbRow["type"], is Integer id = dbRow["id"],
            is String|SqlNull image = dbRow["image"]);
        SimpleImmortal immortal;
        switch (type)
        case ("sphinx") {
            immortal = Sphinx(id);
        }
        case ("djinn") {
            immortal = Djinn(id);
        }
        case ("griffin") {
            immortal = Griffin(id);
        }
        case ("minotaur") {
            immortal = Minotaur(id);
        }
        case ("ogre") {
            immortal = Ogre(id);
        }
        case ("phoenix") {
            immortal = Phoenix(id);
        }
        case ("simurgh") {
            immortal = Simurgh(id);
        }
        case ("troll") {
            immortal = Troll(id);
        }
        else {
            throw AssertionError("Unhandled 'simple immortal' type");
        }
        if (is String image) {
            immortal.image = image;
        }
        if (is Integer row = dbRow["row"], is Integer column = dbRow["column"]) {
            map.addFixture(Point(row, column), immortal);
        } else {
            assert (is Integer parentId = dbRow["parent"],
                is IUnit parent = findById(map, parentId, warner));
            parent.addMember(immortal);
        }
    }

    void readKindedImmortal(IMutableMapNG map, Map<String, Object> dbRow,
            Warning warner) {
        assert (is String type = dbRow["type"], is String kind = dbRow["kind"],
            is Integer id = dbRow["id"], is String|SqlNull image = dbRow["image"]);
        Immortal&HasMutableImage immortal;
        switch (type)
        case ("centaur") {
            immortal = Centaur(kind, id);
        }
        case ("dragon") {
            immortal = Dragon(kind, id);
        }
        case ("fairy") {
            immortal = Fairy(kind, id);
        }
        case ("giant") {
            immortal = Giant(kind, id);
        }
        else {
            throw AssertionError("Unexpected immortal kind");
        }
        if (is String image) {
            immortal.image = image;
        }
        if (is Integer row = dbRow["row"], is Integer column = dbRow["column"]) {
            map.addFixture(Point(row, column), immortal);
        } else {
            assert (is Integer parentId = dbRow["parent"],
                is IUnit parent = findById(map, parentId, warner));
            parent.addMember(immortal);
        }
    }

    shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {
        handleQueryResults(db, warner, "simple immortals", curry(readSimpleImmortal)(map),
            """SELECT * FROM simple_immortals WHERE row IS NOT NULL""");
        handleQueryResults(db, warner, "immortals with kinds",
            curry(readKindedImmortal)(map),
            """SELECT * FROM kinded_immortals WHERE row IS NOT NULL""");
    }

    shared actual void readExtraMapContents(Sql db, IMutableMapNG map, Warning warner) {
        handleQueryResults(db, warner, "simple immortals in units",
            curry(readSimpleImmortal)(map),
            """SELECT * FROM simple_immortals WHERE parent IS NOT NULL""");
        handleQueryResults(db, warner, "immortals with kinds in units",
            curry(readKindedImmortal)(map),
            """SELECT * FROM kinded_immortals WHERE parent IS NOT NULL""");
    }
}
