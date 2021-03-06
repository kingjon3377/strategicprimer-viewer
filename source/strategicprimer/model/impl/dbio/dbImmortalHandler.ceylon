import ceylon.dbc {
    Sql,
    SqlNull
}

import java.sql {
    Types,
    SQLException
}

import strategicprimer.model.common.map {
    IMutableMapNG,
    Point,
    IFixture,
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
    Troll,
    ImmortalAnimal
}
import strategicprimer.model.common.xmlio {
    Warning
}

import ceylon.collection {
    MutableMap
}

import com.vasileff.ceylon.structures {
    MutableMultimap
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
                       'phoenix', 'simurgh', 'troll', 'snowbird', 'thunderbird',
                       'pegasus', 'unicorn', 'kraken')),
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

    Boolean containsSimpleImmortals(String s) => s.contains("simple_immortals");

    shared actual void write(Sql db, Immortal obj, Point|IUnit context) {
        if (is SimpleImmortal|ImmortalAnimal obj) {
            try {
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
            } catch (SQLException except) {
                if (except.message.endsWith("constraint failed: simple_immortals)")) {
                    try (tx = db.Transaction()) {
                        assert (exists initializer = initializers
                            .find(containsSimpleImmortals)?.replace(
                            "simple_immortals ", "simple_immortals_replacement "));
                        db.Statement(initializer).execute();
                        db.Statement("INSERT INTO simple_immortals_replacement
                                      SELECT * FROM simple_immortals").execute();
                        db.Statement("DROP TABLE simple_immortals").execute();
                        db.Statement("ALTER TABLE simple_immortals_replacement
                                      RENAME TO simple_immortals").execute();
                    }
                    write(db, obj, context);
                } else {
                    throw except;
                }
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

    void readSimpleImmortal(IMutableMapNG map,
            MutableMultimap<Integer, Object> containees)(Map<String, Object> dbRow,
            Warning warner) {
        assert (is String type = dbRow["type"], is Integer id = dbRow["id"],
            is String|SqlNull image = dbRow["image"]);
        SimpleImmortal|ImmortalAnimal immortal;
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
            try {
                immortal = ImmortalAnimal.parse(type)(id);
            } catch (ParseException exception) {
                throw AssertionError("Unhandled 'simple immortal' type");
            }
        }
        if (is String image) {
            immortal.image = image;
        }
        if (is Integer row = dbRow["row"], is Integer column = dbRow["column"]) {
            map.addFixture(Point(row, column), immortal);
        } else {
            assert (is Integer parentId = dbRow["parent"]);
            containees.put(parentId, immortal);
        }
    }

    void readKindedImmortal(IMutableMapNG map,
            MutableMultimap<Integer, Object> containees)(Map<String, Object> dbRow,
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
            assert (is Integer parentId = dbRow["parent"]);
            containees.put(parentId, immortal);
        }
    }

    shared actual void readMapContents(Sql db, IMutableMapNG map, MutableMap<Integer, IFixture> containers,
            MutableMultimap<Integer, Object> containees, Warning warner) {
        handleQueryResults(db, warner, "simple immortals", readSimpleImmortal(map, containees),
            """SELECT * FROM simple_immortals""");
        handleQueryResults(db, warner, "immortals with kinds", readKindedImmortal(map, containees),
            """SELECT * FROM kinded_immortals""");
    }
}
