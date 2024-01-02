package legacy.dbio;

import legacy.map.IFixture;
import impl.dbio.AbstractDatabaseWriter;
import impl.dbio.MapContentsReader;
import impl.dbio.TryBiConsumer;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import legacy.map.ILegacyMap;
import legacy.map.IMutableLegacyMap;
import legacy.map.MutablePlayer;
import legacy.map.Player;
import legacy.map.PlayerImpl;

import common.xmlio.Warning;

import static io.jenetics.facilejdbc.Param.value;

public final class DBPlayerHandler extends AbstractDatabaseWriter<Player, ILegacyMap> implements MapContentsReader {
	public DBPlayerHandler() {
		super(Player.class, ILegacyMap.class);
	}

	private static final List<Query> INITIALIZERS = Collections.singletonList(
		Query.of("CREATE TABLE IF NOT EXISTS players (" +
			"    id INTEGER NOT NULL," +
			"    codename VARCHAR(64) NOT NULL," +
			"    current BOOLEAN NOT NULL," +
			"    portrait VARCHAR(256)," +
			"    country VARCHAR(64)" +
			");"));

	@Override
	public List<Query> getInitializers() {
		return INITIALIZERS;
	}

	private static final Query INSERT_SQL =
		Query.of("INSERT INTO players (id, codename, current, portrait, country) " +
			"VALUES(:id, :codename, :current, :portrait, :country);");

	private static final Query UPDATE_SCHEMA = Query.of("ALTER TABLE players ADD COLUMN country VARCHAR(64)");

	@Override
	public void write(final Transactional db, final Player obj, final ILegacyMap context) throws SQLException {
		final List<Param> params = new ArrayList<>();
		params.add(value("id", obj.getPlayerId()));
		params.add(value("codename", obj.getName()));
		params.add(value("current", obj.isCurrent()));
		if (!obj.getPortrait().isEmpty()) {
			params.add(value("portrait", obj.getPortrait()));
		}
		if (obj.getCountry() != null && !obj.getPortrait().isEmpty()) {
			params.add(value("country", obj.getCountry()));
		}
		try {
			// FIXME: Throughout this module, use executeUpdate or equivalent for non-schema-changing queries.
			INSERT_SQL.on(params).execute(db.connection());
		} catch (final SQLException except) {
			if (except.getMessage().contains("table players has no column named country)")) {
				UPDATE_SCHEMA.execute(db.connection());
				INSERT_SQL.on(params).execute(db.connection());
			} else {
				throw except;
			}
		}
	}

	private TryBiConsumer<Map<String, Object>, Warning, SQLException> readPlayer(final IMutableLegacyMap map) {
		return (dbRow, warner) -> {
			final int id = (Integer) dbRow.get("id");
			final String name = (String) dbRow.get("codename");
			final boolean current = getBooleanValue(dbRow, "current");
			final String portrait = (String) dbRow.get("portrait");
			final String country = (String) dbRow.get("country");
			final MutablePlayer player = new PlayerImpl(id, name, country);
			player.setCurrent(current);
			if (portrait != null) {
				player.setPortrait(portrait);
			}
			map.addPlayer(player);
		};
	}

	private static final Query SELECT = Query.of("SELECT * FROM players");

	@Override
	public void readMapContents(final Connection db, final IMutableLegacyMap map, final Map<Integer, IFixture> containers,
								final Map<Integer, List<Object>> containees, final Warning warner) throws SQLException {
		handleQueryResults(db, warner, "players", readPlayer(map), SELECT);
	}
}
