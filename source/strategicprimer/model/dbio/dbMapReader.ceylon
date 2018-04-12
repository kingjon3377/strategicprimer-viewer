import ceylon.dbc {
	Sql
}

import strategicprimer.model.map {
	IMutableMapNG,
	SPMapNG,
	MapDimensionsImpl,
	IMutablePlayerCollection,
	PlayerCollection,
	PlayerImpl,
	MutablePlayer,
	Point,
	pointFactory,
	TileType,
	River
}
import strategicprimer.model.xmlio {
	Warning
}
object dbMapReader {
	{MapContentsReader*} readers = [dbPlayerHandler, dbCacheHandler, dbExplorableHandler, dbFieldHandler,
		dbFortressHandler, dbUnitHandler, dbGroundHandler, dbGroveHandler, dbImmortalHandler, dbImplementHandler,
		dbMineralHandler, dbMineHandler, dbPortalHandler, dbShrubHandler, dbSimpleTerrainHandler,
		dbTextHandler, dbTownHandler, dbVillageHandler, dbResourcePileHandler, dbAnimalHandler,
		dbCommunityStatsHandler, dbWorkerHandler];
	shared IMutableMapNG readMap(Sql db, Warning warner) {
		assert (exists metadata = db.Select("""SELECT version, rows, columns, current_turn FROM metadata LIMIT 1""")
				.execute().first, is Integer version = metadata["version"], is Integer rows = metadata["row"],
			is Integer columns = metadata["columns"], is Integer turn = metadata["current_turn"]);
		IMutablePlayerCollection players = PlayerCollection();
		for (row in db.Select("""SELECT id, codename, current FROM players""").Results()) {
			assert (is Integer id = row["id"], is String codename = row["codename"], is Boolean current = row["current"]);
			MutablePlayer player = PlayerImpl(id, codename);
			if (current) {
				player.current = true;
			}
			players.add(player);
		}
		IMutableMapNG retval = SPMapNG(MapDimensionsImpl(rows, columns, version), players, turn);
		for (dbRow in db.Select("""SELECT * FROM terrain""").Results()) {
			assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
				is String terrainString = dbRow["terrain"], is Boolean mtn = dbRow["mountainous"],
				is Boolean northR = dbRow["north_river"], is Boolean southR = dbRow["south_river"],
				is Boolean eastR = dbRow["east_river"], is Boolean westR = dbRow["west_river"],
				is Boolean lake = dbRow["lake"]);
			Point location = pointFactory(row, column);
			if (!terrainString.empty) {
				assert (is TileType terrain = TileType.parse(terrainString));
				retval.baseTerrain[location] = terrain;
			}
			retval.mountainous[location] = mtn;
			if (northR) {
				retval.addRivers(location, River.north);
			}
			if (southR) {
				retval.addRivers(location, River.south);
			}
			if (eastR) {
				retval.addRivers(location, River.east);
			}
			if (westR) {
				retval.addRivers(location, River.west);
			}
			if (lake) {
				retval.addRivers(location, River.lake);
			}
		}
		for (reader in readers) {
			reader.readMapContents(db, retval, warner);
		}
		for (reader in readers) {
			reader.readExtraMapContents(db, retval, warner);
		}
		return retval;
	}
}