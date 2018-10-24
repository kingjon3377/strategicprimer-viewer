import ceylon.dbc {
    Sql,
    SqlNull
}

import strategicprimer.model.common.map {
    IMapNG,
    IMutableMapNG,
    Player,
    PlayerImpl
}

import strategicprimer.model.common.xmlio {
    Warning
}

object dbPlayerHandler extends AbstractDatabaseWriter<Player, IMapNG>()
        satisfies MapContentsReader {
    shared actual {String+} initializers = [
        """CREATE TABLE IF NOT EXISTS players (
               id INTEGER NOT NULL,
               codename VARCHAR(64) NOT NULL,
               current BOOLEAN NOT NULL,
               portrait VARCHAR(256)
           );"""
    ];

    shared actual void write(Sql db, Player obj, IMapNG context) =>
        db.Insert("""INSERT INTO players (id, codename, current, portrait)
                     VALUES(?, ?, ?, ?);""")
                .execute(obj.playerId, obj.name, obj.current, obj.portrait);

    void readPlayer(IMutableMapNG map, Map<String, Object> row, Warning warner) {
        assert (is Integer id = row["id"], is String name = row["codename"],
            is Boolean current = dbMapReader.databaseBoolean(row["current"]),
            is String|SqlNull portrait = row["portrait"]);
        value player = PlayerImpl(id, name);
        player.current = current;
        if (is String portrait) {
            player.portrait = portrait;
        }
        map.addPlayer(player);
    }

    shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) =>
            handleQueryResults(db, warner, "players", curry(readPlayer)(map),
                """SELECT * FROM players""");
}
