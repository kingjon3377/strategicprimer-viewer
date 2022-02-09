package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import common.map.Point;
import common.map.IMutableMapNG;

import common.map.fixtures.TextFixture;
import common.xmlio.Warning;

final class DBTextHandler extends AbstractDatabaseWriter<TextFixture, Point> implements MapContentsReader {
	public DBTextHandler() {
		super(TextFixture.class, Point.class);
	}

	private static final List<String> INITIALIZERS = Collections.singletonList(
		"CREATE TABLE IF NOT EXISTS text_notes (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    turn INTEGER," +
			"    text VARCHAR(1024) NOT NULL," +
			"    image VARCHAR(255)" +
			");");

	@Override
	public List<String> getInitializers() {
		return INITIALIZERS;
	}

	@Override
	public void write(final DB db, final TextFixture obj, final Point context) {
		final Integer turn;
		if (obj.getTurn() >= 0) {
			turn = obj.getTurn();
		} else {
			turn = null;
		}
		db.update("INSERT INTO text_notes (row, column, turn, text, image) VALUES(?, ?, ?, ?, ?);",
				context.getRow(), context.getColumn(), turn, obj.getText(), obj.getImage())
			.execute();
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readTextNote(final IMutableMapNG map) {
		return (dbRow, warner) -> {
			final int row = (Integer) dbRow.get("row");
			final int column = (Integer) dbRow.get("column");
			final Integer turn = (Integer) dbRow.get("turn");
			final String text = (String) dbRow.get("text");
			final String image = (String) dbRow.get("image");
			final TextFixture fixture = new TextFixture(text, turn == null ? -1 : turn);
			if (image != null) {
				fixture.setImage(image);
			}
			map.addFixture(new Point(row, column), fixture);
		};
	}

	@Override
	public void readMapContents(final DB db, final IMutableMapNG map, final Warning warner) {
		try {
			handleQueryResults(db, warner, "text notes", readTextNote(map),
				"SELECT * FROM text_notes");
		} catch (final RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (final Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
