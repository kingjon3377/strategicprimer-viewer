import ceylon.dbc {
    Sql,
    SqlNull
}

import strategicprimer.model.common.map {
    Point
}

import strategicprimer.model.impl.map {
    IMutableMapNG
}

import strategicprimer.model.common.map.fixtures.resources {
    MineralVein,
    StoneDeposit,
    StoneKind
}
import strategicprimer.model.common.xmlio {
    Warning
}
object dbMineralHandler extends AbstractDatabaseWriter<MineralVein|StoneDeposit, Point>()
        satisfies MapContentsReader {
    shared actual {String+} initializers = [
        """CREATE TABLE IF NOT EXISTS minerals (
               row INTEGER NOT NULL,
               column INTEGER NOT NULL,
               type VARCHAR(7) NOT NULL CHECK(type IN('stone', 'mineral')),
               id INTEGER NOT NULL,
               kind VARCHAR(64) NOT NULL,
               exposed BOOLEAN NOT NULL CHECK(exposed OR type IN('mineral')),
               dc INTEGER NOT NULL,
               image VARCHAR(255)
           );"""
    ];
    shared actual void write(Sql db, MineralVein|StoneDeposit obj, Point context) {
        String type;
        Boolean exposed;
        switch (obj)
        case (is MineralVein) {
            type = "mineral";
            exposed = obj.exposed;
        }
        case (is StoneDeposit) {
            type = "stone";
            exposed = true;
        }
        db.Insert(
            """INSERT INTO minerals (row, column, type, id, kind, exposed, dc, image)
               VALUES(?, ?, ?, ?, ?, ?, ?, ?);""")
            .execute(context.row, context.column, type,
                    obj.id, obj.kind, exposed, obj.dc, obj.image);
    }
    void readMineralVein(IMutableMapNG map, Map<String, Object> dbRow, Warning warner) {
        assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
            is Integer id = dbRow["id"], is String kind = dbRow["kind"],
            is Boolean exposed = dbMapReader.databaseBoolean(dbRow["exposed"]),
            is Integer dc = dbRow["dc"], is String|SqlNull image = dbRow["image"]);
        value mineral = MineralVein(kind, exposed, dc, id);
        if (is String image) {
            mineral.image = image;
        }
        map.addFixture(Point(row, column), mineral);
    }
    void readStoneDeposit(IMutableMapNG map, Map<String, Object> dbRow, Warning warner) {
        assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
            is Integer id = dbRow["id"], is String kindString = dbRow["kind"],
            is StoneKind kind = StoneKind.parse(kindString), is Integer dc = dbRow["dc"],
            is String|SqlNull image = dbRow["image"]);
        value stone = StoneDeposit(kind, dc, id);
        if (is String image) {
            stone.image = image;
        }
        map.addFixture(Point(row, column), stone);
    }
    shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {
        handleQueryResults(db, warner, "stone deposits", curry(readStoneDeposit)(map),
            """SELECT row, column, id, kind, dc, image FROM minerals
               WHERE type = 'stone'""");
        handleQueryResults(db, warner, "mineral veins", curry(readMineralVein)(map),
            """SELECT row, column, id, kind, exposed, dc, image FROM minerals
               WHERE type = 'mineral'""");
    }
}