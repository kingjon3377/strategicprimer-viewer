import ceylon.dbc {
	Sql
}

import strategicprimer.model.map {
	Player,
	IMapNG,
	IMutableMapNG,
	PlayerImpl
}
import strategicprimer.model.xmlio {
	Warning
}
object dbPlayerHandler extends AbstractDatabaseWriter<Player, IMapNG>() satisfies MapContentsReader {
	shared actual {String+} initializers = [
		"""CREATE TABLE IF NOT EXISTS players (
			   id INTEGER NOT NULL,
			   codename VARCHAR(64) NOT NULL,
			   current BOOLEAN NOT NULL
		   );"""
	];
	shared actual void write(Sql db, Player obj, IMapNG context) {
		db.Insert("""INSERT INTO players (id, codename, current) VALUES(?, ?, ?);""")
				.execute(obj.playerId, obj.name, obj.current);
	}
	shared actual void readMapContents(Sql db, IMutableMapNG map, Warning warner) {
		log.trace("About to read players");
		variable Integer count = 0;
		for (row in db.Select("""SELECT * FROM PLAYERS""").Results()) {
			assert (is Integer id = row["id"], is String name = row["codename"],
				is Boolean current = dbMapReader.databaseBoolean(row["current"]));
			value player = PlayerImpl(id, name);
			player.current = current;
			map.addPlayer(player);
			count++;
			if ((count % 50) == 0) {
				log.trace("Read ``count`` players");
			}
		}
		log.trace("Finished reading players");
	}
}
