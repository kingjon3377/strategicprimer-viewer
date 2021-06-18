import ceylon.dbc {
    Sql,
    SqlNull
}

import strategicprimer.model.common.map {
    IFixture,
    IMapNG,
    IMutableMapNG,
    Player,
    PlayerImpl
}

import strategicprimer.model.common.xmlio {
    Warning
}
import java.sql {
    Types,
    SQLException
}
import lovelace.util.common {
    as
}

import ceylon.collection {
    MutableMap
}

import com.vasileff.ceylon.structures {
    MutableMultimap
}

object dbPlayerHandler extends AbstractDatabaseWriter<Player, IMapNG>()
        satisfies MapContentsReader {
    shared actual {String+} initializers = [
        """CREATE TABLE IF NOT EXISTS players (
               id INTEGER NOT NULL,
               codename VARCHAR(64) NOT NULL,
               current BOOLEAN NOT NULL,
               portrait VARCHAR(256),
               country VARCHAR(64)
           );"""
    ];

    shared actual void write(Sql db, Player obj, IMapNG context) {
        try {
            db.Insert("""INSERT INTO players (id, codename, current, portrait, country)
                         VALUES(?, ?, ?, ?, ?);""")
                .execute(obj.playerId, obj.name, obj.current, obj.portrait,
                    obj.country else SqlNull(Types.varchar));
        } catch (SQLException except) {
            if (except.message.endsWith("table players has no column named country)")) {
                db.Statement("""ALTER TABLE players ADD COLUMN country VARCHAR(64)""")
                    .execute();
                write(db, obj, context);
            } else {
                throw except;
            }
        }
    }

    void readPlayer(IMutableMapNG map)(Map<String, Object> row, Warning warner) {
        assert (is Integer id = row["id"], is String name = row["codename"],
            is Boolean current = dbMapReader.databaseBoolean(row["current"]),
            is String|SqlNull portrait = row["portrait"],
            is String|SqlNull country = row["country"]);
        value player = PlayerImpl(id, name, as<String>(country));
        player.current = current;
        if (is String portrait) {
            player.portrait = portrait;
        }
        map.addPlayer(player);
    }

    shared actual void readMapContents(Sql db, IMutableMapNG map, MutableMap<Integer, IFixture> containers,
            MutableMultimap<Integer, Object> containees, Warning warner) =>
                handleQueryResults(db, warner, "players", readPlayer(map),
                    """SELECT * FROM players""");
}
