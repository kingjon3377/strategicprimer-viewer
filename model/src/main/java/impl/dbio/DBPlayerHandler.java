package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.Arrays;
import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import common.map.IMapNG;
import common.map.IMutableMapNG;
import common.map.MutablePlayer;
import common.map.Player;
import common.map.PlayerImpl;

import common.xmlio.Warning;
import java.sql.Types;
import java.sql.SQLException;

final class DBPlayerHandler extends AbstractDatabaseWriter<Player, IMapNG> implements MapContentsReader {
	public DBPlayerHandler() {
		super(Player.class, IMapNG.class);
	}

	private static final Iterable<String> INITIALIZERS = Collections.singleton(
		"CREATE TABLE IF NOT EXISTS players (" +
			"    id INTEGER NOT NULL," +
			"    codename VARCHAR(64) NOT NULL," +
			"    current BOOLEAN NOT NULL," +
			"    portrait VARCHAR(256)," +
			"    country VARCHAR(64)" +
			");");

	@Override
	public Iterable<String> getInitializers() {
		return INITIALIZERS;
	}

	private static final String INSERT_SQL =
		"INSERT INTO players (id, codename, current, portrait, country) VALUES(?, ?, ?, ?, ?);";

	@Override
	public void write(DB db, Player obj, IMapNG context) {
		try {
			db.update(INSERT_SQL, obj.getPlayerId(), obj.getName(), obj.isCurrent(),
				obj.getPortrait(), obj.getCountry()).execute();
		} catch (Exception except) {
			if (except.getMessage().contains("table players has no column named country)")) {
				db.script("ALTER TABLE players ADD COLUMN country VARCHAR(64)").execute();
				write(db, obj, context);
			} else {
				throw except;
			}
		}
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readPlayer(IMutableMapNG map) {
		return (dbRow, warner) -> {
			int id = (Integer) dbRow.get("id");
			String name = (String) dbRow.get("codename");
			Boolean current = /* DBMapReader.databaseBoolean(dbRow.get("current")) */ // FIXME
				(Boolean) dbRow.get("current"); // This will compile but probably won't work
			String portrait = (String) dbRow.get("portrait");
			String country = (String) dbRow.get("country");
			MutablePlayer player = new PlayerImpl(id, name, country);
			player.setCurrent(current);
			if (portrait != null) {
				player.setPortrait(portrait);
			}
			map.addPlayer(player);
		};
	}

	@Override
	public void readMapContents(DB db, IMutableMapNG map, Warning warner) {
		try {
			handleQueryResults(db, warner, "players", readPlayer(map),
				"SELECT * FROM players");
		} catch (RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
