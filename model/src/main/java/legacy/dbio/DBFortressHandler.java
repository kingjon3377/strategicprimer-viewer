package legacy.dbio;

import legacy.map.IFixture;
import impl.dbio.AbstractDatabaseWriter;
import impl.dbio.MapContentsReader;
import impl.dbio.TryBiConsumer;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import legacy.map.Point;
import legacy.map.IMutableLegacyMap;
import legacy.map.fixtures.towns.FortressImpl;
import legacy.map.fixtures.towns.IFortress;
import legacy.map.fixtures.towns.IMutableFortress;
import common.map.fixtures.towns.TownSize;
import common.xmlio.Warning;
import legacy.map.fixtures.FortressMember;
import org.jetbrains.annotations.Nullable;

import static io.jenetics.facilejdbc.Param.value;

public final class DBFortressHandler extends AbstractDatabaseWriter<IFortress, Point> implements MapContentsReader {
    public DBFortressHandler(final @Nullable SPDatabaseWriter parent) {
        super(IFortress.class, Point.class);
        this.parent = parent;
    }

    /**
     * What we use to write members. Called "parent" because *it* actually owns *this* object.
     */
    private final @Nullable SPDatabaseWriter parent;

    private static final List<Query> INITIALIZERS = Collections.singletonList(
            Query.of("CREATE TABLE IF NOT EXISTS fortresses (" +
                    "    row INTEGER NOT NULL," +
                    "    column INTEGER NOT NULL," +
                    "    owner INTEGER NOT NULL," +
                    "    name VARCHAR(64) NOT NULL," +
                    "    size VARCHAR(6) NOT NULL" +
                    "        CHECK(size IN ('small', 'medium', 'large'))," +
                    "    id INTEGER NOT NULL," +
                    "    image VARCHAR(255)," +
                    "    portrait VARCHAR(255)" +
                    ");"));

    @Override
    public List<Query> getInitializers() {
        return INITIALIZERS;
    }

    private static final Query INSERT_SQL =
            Query.of("INSERT INTO fortresses (row, column, owner, name, size, id, image, portrait) " +
                    "VALUES(:row, :column, :owner, :name, :size, :id, :image, :portrait);");

    @Override
    public void write(final Transactional db, final IFortress obj, final Point context) throws SQLException {
        INSERT_SQL.on(value("row", context.row()), value("column", context.column()),
                        value("owner", obj.owner().getPlayerId()), value("name", obj.getName()),
                        value("size", obj.getTownSize().toString()), value("id", obj.getId()),
                        value("image", obj.getImage()), value("portrait", obj.getPortrait()))
                .execute(db.connection());
        for (final FortressMember member : obj) {
            Objects.requireNonNull(parent).writeSPObjectInContext(db, member, obj);
        }
    }

    private static TryBiConsumer<Map<String, Object>, Warning, SQLException> readFortress(final IMutableLegacyMap map,
                                                                                          final Map<Integer, IFixture> containers) {
        return (dbRow, warner) -> {
            final int row = (Integer) dbRow.get("row");
            final int column = (Integer) dbRow.get("column");
            final int ownerId = (Integer) dbRow.get("owner");
            final String name = (String) dbRow.get("name");
            final TownSize size = TownSize.parseTownSize((String) dbRow.get("size"));
            final int id = (Integer) dbRow.get("id");
            final String image = (String) dbRow.get("image");
            final String portrait = (String) dbRow.get("portrait");
            final IMutableFortress fortress = new FortressImpl(map.getPlayers().getPlayer(ownerId),
                    name, id, size);
	        if (!Objects.isNull(image)) {
                fortress.setImage(image);
            }
	        if (!Objects.isNull(portrait)) {
                fortress.setPortrait(portrait);
            }
            map.addFixture(new Point(row, column), fortress);
            containers.put(id, fortress);
        };
    }

    private static final Query SELECT = Query.of("SELECT * FROM fortresses");

    @Override
    public void readMapContents(final Connection db, final IMutableLegacyMap map, final Map<Integer, IFixture> containers,
                                final Map<Integer, List<Object>> containees, final Warning warner) throws SQLException {
        handleQueryResults(db, warner, "fortresses", readFortress(map, containers), SELECT);
    }
}
