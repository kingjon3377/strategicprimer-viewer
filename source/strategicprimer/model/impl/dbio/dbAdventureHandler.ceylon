import ceylon.dbc {
    Sql,
    SqlNull
}

import strategicprimer.model.impl.map {
    Point,
    IMutableMapNG
}
import strategicprimer.model.impl.map.fixtures.explorable {
    AdventureFixture
}
import strategicprimer.model.impl.xmlio {
    Warning
}
object dbAdventureHandler extends AbstractDatabaseWriter<AdventureFixture, Point>()
        satisfies MapContentsReader {
    shared actual {String+} initializers =
            ["""CREATE TABLE IF NOT EXISTS adventures (
                    row INTEGER NOT NULL,
                    column INTEGER NOT NULL,
                    id INTEGER NOT NULL,
                    brief VARCHAR(255) NOT NULL,
                    full VARCHAR(512) NOT NULL,
                    owner INTEGER NOT NULL,
                    image VARCHAR(255)
                );"""];
    shared actual void write(Sql db, AdventureFixture obj, Point context) {
            db.Insert("""INSERT INTO adventures (row, column, id, brief, full, owner,
                             image)
                         VALUES(?, ?, ?, ?, ?, ?, ?);""")
                    .execute(context.row, context.column, obj.id, obj.briefDescription,
                        obj.fullDescription, obj.owner.playerId, obj.image);
    }
    void readAdventure(IMutableMapNG map, Map<String, Object> dbRow, Warning warner) {
        assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
            is Integer id = dbRow["id"], is String brief = dbRow["brief"],
            is String full = dbRow["full"], is Integer ownerId = dbRow["owner"],
            is String|SqlNull image = dbRow["image"]);
        value adventure =
                AdventureFixture(map.players.getPlayer(ownerId), brief, full, id);
        if (is String image) {
            adventure.image = image;
        }
        map.addFixture(Point(row, column), adventure);
    }
    shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) =>
            handleQueryResults(db, warner, "adventures", curry(readAdventure)(map),
                """SELECT * FROM adventures""");
}