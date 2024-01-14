package legacy.dbio;

import legacy.map.IFixture;
import impl.dbio.AbstractDatabaseWriter;
import impl.dbio.MapContentsReader;
import impl.dbio.TryBiConsumer;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import legacy.map.Point;
import legacy.map.IMutableLegacyMap;

import legacy.map.fixtures.TextFixture;
import common.xmlio.Warning;

import static io.jenetics.facilejdbc.Param.value;

public final class DBTextHandler extends AbstractDatabaseWriter<TextFixture, Point> implements MapContentsReader {
    public DBTextHandler() {
        super(TextFixture.class, Point.class);
    }

    private static final List<Query> INITIALIZERS = Collections.singletonList(
            Query.of("CREATE TABLE IF NOT EXISTS text_notes (" +
                    "    row INTEGER NOT NULL," +
                    "    column INTEGER NOT NULL," +
                    "    turn INTEGER," +
                    "    text VARCHAR(1024) NOT NULL," +
                    "    image VARCHAR(255)" +
                    ");"));

    @Override
    public List<Query> getInitializers() {
        return INITIALIZERS;
    }

    private static final Query INSERT = Query.of(
            "INSERT INTO text_notes (row, column, turn, text, image) VALUES(:row, :column, :turn, :text, :image);");

    @Override
    public void write(final Transactional db, final TextFixture obj, final Point context) throws SQLException {
        final List<Param> params = new ArrayList<>();
        params.add(value("row", context.row()));
        params.add(value("column", context.column()));
        if (obj.getTurn() >= 0) {
            params.add(value("turn", obj.getTurn()));
        }
        params.add(value("text", obj.getText()));
        params.add(value("image", obj.getImage()));
        INSERT.on(params).execute(db.connection());
    }

    private static TryBiConsumer<Map<String, Object>, Warning, SQLException> readTextNote(final IMutableLegacyMap map) {
        return (dbRow, warner) -> {
            final int row = (Integer) dbRow.get("row");
            final int column = (Integer) dbRow.get("column");
            final Integer turn = (Integer) dbRow.get("turn");
            final String text = (String) dbRow.get("text");
            final String image = (String) dbRow.get("image");
            final TextFixture fixture = new TextFixture(text, Objects.requireNonNullElse(turn, -1));
	        if (!Objects.isNull(image)) {
                fixture.setImage(image);
            }
            map.addFixture(new Point(row, column), fixture);
        };
    }

    private static final Query SELECT = Query.of("SELECT * FROM text_notes");

    @Override
    public void readMapContents(final Connection db, final IMutableLegacyMap map, final Map<Integer, IFixture> containers,
                                final Map<Integer, List<Object>> containees, final Warning warner) throws SQLException {
        handleQueryResults(db, warner, "text notes", readTextNote(map), SELECT);
    }
}
