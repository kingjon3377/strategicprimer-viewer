import ceylon.dbc {
	Sql
}

import strategicprimer.model.map {
	Player,
	IMapNG,
	IMutableMapNG,
	PlayerImpl
}
object dbPlayerHandler extends AbstractDatabaseWriter<Player, IMapNG>() satisfies MapContentsReader {
	shared actual {String+} initializers = [ // TODO: id should be NOT NULL, but that gave problems for some reason
		"""CREATE TABLE IF NOT EXISTS players (
			   id INTEGER,
			   codename VARCHAR(64) NOT NULL,
			   current BOOLEAN NOT NULL
		   )"""
	];
	shared actual void write(Sql db, Player obj, IMapNG context) {
		db.Insert("""INSERT INTO players (id, codename, current) VALUES(?, ?, ?)""")
				.execute(obj.playerId, obj.name, obj.current);
	}
	shared actual void readMapContents(Sql db, IMutableMapNG map) {
		for (row in db.Select("""SELECT * FROM PLAYERS""").Results()) {
			assert (is Integer id = row["id"], is String name = row["codename"], is Boolean current = row["current"]);
			value player = PlayerImpl(id, name);
			player.current = current;
			map.addPlayer(player);
		}
	}
}