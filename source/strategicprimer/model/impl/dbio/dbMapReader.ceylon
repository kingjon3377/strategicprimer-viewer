import ceylon.dbc {
    Sql
}

import java.sql {
    SQLException
}

import strategicprimer.model.common.map {
    IMutableMapNG,
    SPMapNG,
    MapDimensionsImpl,
    PlayerImpl,
    MutablePlayer,
    Point,
    TileType,
    River,
    IMutablePlayerCollection,
    PlayerCollection
}
import strategicprimer.model.common.xmlio {
    Warning
}

object dbMapReader {
    {MapContentsReader*} readers = [dbPlayerHandler, dbCacheHandler, dbExplorableHandler,
        dbFieldHandler, dbFortressHandler, dbUnitHandler, dbGroundHandler, dbGroveHandler,
        dbImmortalHandler, dbImplementHandler, dbMineralHandler, dbMineHandler,
        dbPortalHandler, dbShrubHandler, dbSimpleTerrainHandler, dbTextHandler,
        dbTownHandler, dbVillageHandler, dbResourcePileHandler, dbAnimalHandler,
        dbCommunityStatsHandler, dbWorkerHandler, dbAdventureHandler, dbForestHandler];

    "If [[field]] is is an Integer and either 0 or 1, which is how SQLite stores Boolean
     values, convert to the equivalent Boolean and return that; otherwise, return the
     original value."
    shared Anything databaseBoolean(Anything field) {
        if (is Integer field) {
            switch (field)
            case (0) {
                return false;
            }
            case (1) {
                return true;
            }
            else {
                return field;
            }
        } else {
            return field;
        }
    }

    shared IMutableMapNG readMap(Sql db, Warning warner) {
        assert (exists metadata = db.Select(
            """SELECT version, rows, columns, current_turn FROM metadata LIMIT 1""")
                .execute().first, is Integer version = metadata["version"],
            is Integer rows = metadata["rows"], is Integer columns = metadata["columns"],
            is Integer turn = metadata["current_turn"]);
        IMutablePlayerCollection players = PlayerCollection();
        log.trace("About to read players");
        for (row in db.Select("""SELECT id, codename, current FROM players""")
                .Results()) {
            assert (is Integer id = row["id"], is String codename = row["codename"],
                is Boolean current = databaseBoolean(row["current"]));
            MutablePlayer player = PlayerImpl(id, codename);
            if (current) {
                player.current = true;
            }
            players.add(player);
        }
        log.trace("Finished reading players, about to start on terrain");
        IMutableMapNG retval =
                SPMapNG(MapDimensionsImpl(rows, columns, version), players, turn);
        variable Integer count = 0;
        for (dbRow in db.Select("""SELECT * FROM terrain""").Results()) {
            assert (is Integer row = dbRow["row"], is Integer column = dbRow["column"],
                is String terrainString = dbRow["terrain"],
                is Boolean mtn = databaseBoolean(dbRow["mountainous"]),
                is Boolean northR = databaseBoolean(dbRow["north_river"]),
                is Boolean southR = databaseBoolean(dbRow["south_river"]),
                is Boolean eastR = databaseBoolean(dbRow["east_river"]),
                is Boolean westR = databaseBoolean(dbRow["west_river"]),
                is Boolean lake = databaseBoolean(dbRow["lake"]));
            Point location = Point(row, column);
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
            count++;
            if (50.divides(count)) {
                log.trace("Read terrain for ``count`` tiles");
            }
        }
        log.trace("Finished reading terrain");
        for (reader in readers) {
            try {
                reader.readMapContents(db, retval, warner);
            } catch (SQLException exception) {
                if (exception.message.contains("no such table")) {
                    continue;
                } else {
                    throw exception;
                }
            }
        }
        for (reader in readers) {
            try {
                reader.readExtraMapContents(db, retval, warner);
            } catch (SQLException exception) {
                if (exception.message.contains("no such table")) {
                    continue;
                } else {
                    throw exception;
                }
            }
        }
        log.trace("Finished reading the map");
        return retval;
    }
}
