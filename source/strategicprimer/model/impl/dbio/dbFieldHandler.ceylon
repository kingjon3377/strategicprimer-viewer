import ceylon.dbc {
    Sql,
    SqlNull
}
import ceylon.decimal {
    Decimal,
    parseDecimal
}

import strategicprimer.model.common.map {
    Point,
    IFixture,
    IMutableMapNG
}
import strategicprimer.model.common.map.fixtures.resources {
    Meadow,
    FieldStatus
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

object dbFieldHandler extends AbstractDatabaseWriter<Meadow, Point>()
        satisfies MapContentsReader {
    shared actual {String+} initializers = [
        """CREATE TABLE IF NOT EXISTS fields (
               row INTEGER NOT NULL,
               column INTEGER NOT NULL,
               id INTEGER NOT NULL,
               type VARCHAR(6) NOT NULL
                   CHECK (type IN ('field', 'meadow')),
               kind VARCHAR(64) NOT NULL,
               cultivated BOOLEAN NOT NULL,
               status VARCHAR(7) NOT NULL
                   CHECK (status IN ('fallow', 'seeding', 'growing', 'bearing')),
               acres VARCHAR(128)
                   CHECK (acres NOT LIKE '%[^0-9.]%' AND acres NOT LIKE '%.%.%'),
               image VARCHAR(255)
           );"""
    ];

    shared actual void write(Sql db, Meadow obj, Point context) =>
        db.Insert("""INSERT INTO fields (row, column, id, type, kind, cultivated, status,
                         acres, image)
                     VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);""")
                .execute(context.row, context.column, obj.id,
                    (obj.field) then "field" else "meadow", obj.kind, obj.cultivated,
                    obj.status.string, obj.acres.string, obj.image);

    void readMeadow(IMutableMapNG map)(Map<String, Object> dbRow, Warning warner) {
        assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
            is Integer id = dbRow["id"], is String type = dbRow["type"],
            is String kind = dbRow["kind"],
            is Boolean cultivated = dbMapReader.databaseBoolean(dbRow["cultivated"]),
            is String statusString = dbRow["status"],
            is FieldStatus status = FieldStatus.parse(statusString),
            is String acresString = dbRow["acres"],
            is String|SqlNull image = dbRow["image"]);
        Number<out Anything> acres;
        if (is Integer num = Integer.parse(acresString)) {
            acres = num;
        } else {
            assert (is Decimal num = parseDecimal(acresString));
            acres = num;
        }
        Boolean field;
        switch (type)
        case ("meadow") {
            field = false;
        }
        case ("field") {
            field = true;
        }
        else {
            throw AssertionError("Unhandled field type");
        }
        value meadow = Meadow(kind, field, cultivated, id, status, acres);
        if (is String image) {
            meadow.image = image;
        }
        map.addFixture(Point(row, column), meadow);
    }

    shared actual void readMapContents(Sql db, IMutableMapNG map, MutableMap<Integer, IFixture> containers,
            MutableMultimap<Integer, Object> containees, Warning warner) =>
                handleQueryResults(db, warner, "meadows", readMeadow(map),
                    """SELECT * FROM fields""");
}
