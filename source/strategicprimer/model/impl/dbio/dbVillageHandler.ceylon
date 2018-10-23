import ceylon.dbc {
    Sql,
    SqlNull
}

import java.sql {
    Types
}

import strategicprimer.model.common.map {
    Point,
    IMutableMapNG
}

import strategicprimer.model.common.map.fixtures.towns {
    Village,
    TownStatus,
    CommunityStats
}
import strategicprimer.model.common.xmlio {
    Warning
}

object dbVillageHandler extends AbstractDatabaseWriter<Village, Point>()
        satisfies MapContentsReader {
    shared actual {String+} initializers = [
        """CREATE TABLE IF NOT EXISTS villages (
               row INTEGER NOT NULL,
               column INTEGER NOT NULL,
               status VARCHAR(9) NOT NULL
                   CHECK(status IN ('abandoned', 'active', 'burned', 'ruined')),
               name VARCHAR(128) NOT NULL,
               id INTEGER NOT NULL,
               owner INTEGER NOT NULL,
               race VARCHAR(32) NOT NULL,
               image VARCHAR(255),
               portrait VARCHAR(255),
               population INTEGER
           );"""
    ];

    shared actual void write(Sql db, Village obj, Point context) {
        db.Insert("""INSERT INTO villages (row, column, status, name, id, owner, race,
                         image, portrait, population)
                     VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);""")
                .execute(context.row, context.column, obj.status.string, obj.name, obj.id,
                    obj.owner.playerId, obj.race, obj.image, obj.portrait,
                    obj.population?.population else SqlNull(Types.integer));
        if (exists stats = obj.population) {
            dbCommunityStatsHandler.initialize(db);
            dbCommunityStatsHandler.write(db, stats, obj);
        }
    }

    void readVillage(IMutableMapNG map, Map<String, Object> dbRow, Warning warner) {
        assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
            is String statusString = dbRow["status"],
            is TownStatus status = TownStatus.parse(statusString),
            is String name = dbRow["name"], is Integer id = dbRow["id"],
            is Integer ownerId = dbRow["owner"],
            is String race = dbRow["race"], is String|SqlNull image = dbRow["image"],
            is String|SqlNull portrait = dbRow["portrait"],
            is Integer|SqlNull population = dbRow["population"]);
        value village = Village(status, name, id, map.players.getPlayer(ownerId), race);
        if (is String image) {
            village.image = image;
        }
        if (is String portrait) {
            village.portrait = portrait;
        }
        if (is Integer population) {
            village.population = CommunityStats(population);
        }
        map.addFixture(Point(row, column), village);
    }

    shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) =>
            handleQueryResults(db, warner, "villages", curry(readVillage)(map),
                """SELECT * from villages""");
}
