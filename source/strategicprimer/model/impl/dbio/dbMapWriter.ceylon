import strategicprimer.model.common.map {
    IMutableMapNG,
    IMapNG,
    River
}
import ceylon.dbc {
    Sql
}

variable Integer currentTurn = -1;

object dbMapWriter extends AbstractDatabaseWriter<IMutableMapNG, IMapNG>() {
    Boolean[5] riverFlags(River* rivers) =>
        [rivers.contains(River.north), rivers.contains(River.south),
            rivers.contains(River.east), rivers.contains(River.west),
            rivers.contains(River.lake)];

    shared actual {String+} initializers = [
        """CREATE TABLE IF NOT EXISTS metadata (
               version INTEGER NOT NULL,
               rows INTEGER NOT NULL,
               columns INTEGER NOT NULL,
               current_turn INTEGER NOT NULL
           );""",
    """CREATE TABLE IF NOT EXISTS terrain (
           row INTEGER NOT NULL,
           column INTEGER NOT NULL,
           terrain VARCHAR(16) NOT NULL
               CHECK (terrain IN ('', 'tundra', 'desert', 'mountain', 'boreal_forest',
                   'temperate_forest', 'ocean', 'plains', 'jungle', 'steppe', 'swamp')),
           mountainous BOOLEAN NOT NULL,
           north_river BOOLEAN NOT NULL,
           south_river BOOLEAN NOT NULL,
           east_river BOOLEAN NOT NULL,
           west_river BOOLEAN NOT NULL,
           lake BOOLEAN NOT NULL
       );""",
        """CREATE TABLE IF NOT EXISTS bookmarks (
               row INTEGER NOT NULL,
               column INTEGER NOT NULL,
               player INTEGER NOT NULL
           );""",
        """CREATE TABLE IF NOT EXISTS roads (
               row INTEGER NOT NULL,
               column INTEGER NOT NULL,
               direction VARCHAR(9) NOT NULL
                  CHECK (direction IN ('north', 'south', 'east', 'west', 'northeast',
                        'southeast', 'northwest', 'southwest')),
               quality INTEGER NOT NULL

           )"""
    ];

    shared actual void write(Sql db, IMutableMapNG obj, IMapNG context) {
        db.Insert("""INSERT INTO metadata (version, rows, columns, current_turn)
                     VALUES(?, ?, ?, ?);""")
            .execute(obj.dimensions.version, obj.dimensions.rows,
                        obj.dimensions.columns, obj.currentTurn);
        currentTurn = obj.currentTurn;
        dbPlayerHandler.initialize(db);
        for (player in obj.players) {
            dbPlayerHandler.write(db, player, obj);
        }
        value terrainInsertion = db.Insert(
            """INSERT INTO terrain (row, column, terrain, mountainous, north_river,
                   south_river, east_river, west_river, lake)
               VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);""");
        value bookmarkInsertion = db.Insert(
            """INSERT INTO bookmarks (row, column, player) VALUES(?, ?, ?)""");
        value roadsInsertion = db.Insert(
            """INSERT INTO roads (row, column, direction, quality) VALUES(?, ?, ?, ?)""");
        variable Integer count = 0;
        variable Integer fixtureCount = 0;
        for (location in obj.locations) {
            terrainInsertion.execute(location.row, location.column,
                obj.baseTerrain[location]?.xml else "",
                //obj.mountainous[location], *riverFlags(obj.rivers[location])); // TODO: syntax sugar
                obj.mountainous.get(location), *riverFlags(*obj.rivers.get(location)));
            //for (fixture in obj.fixtures[location]) { // TODO: syntax sugar
            for (fixture in obj.fixtures.get(location)) {
                spDatabaseWriter.writeSPObjectInContext(db, fixture, location);
                fixtureCount++;
            }
            for (player in obj.allBookmarks.get(location)) {
                bookmarkInsertion.execute(location.row, location.column, player.playerId);
            }
            for (direction->quality in obj.roads.getOrDefault(location, [])) {
                roadsInsertion.execute(location.row, location.column, direction.string, quality);
            }
            count++;
            if (25.divides(count)) {
                log.trace(
                    "Wrote ``count`` points with ``fixtureCount`` fixtures so far");
            }
        }
    }
}
