package impl.dbio;

import buckelieg.jdbc.fn.DB;

import java.util.Collections;
import java.util.Arrays;
import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import java.math.BigDecimal;

import java.sql.Types;

import common.map.Point;
import common.map.IMutableMapNG;

import common.map.fixtures.TextFixture;
import common.xmlio.Warning;

final class DBTextHandler extends AbstractDatabaseWriter<TextFixture, Point> implements MapContentsReader {
	public DBTextHandler() {
		super(TextFixture.class, Point.class);
	}

	private static final Iterable<String> INITIALIZERS = Collections.singleton(
		"CREATE TABLE IF NOT EXISTS text_notes (" +
			"    row INTEGER NOT NULL," +
			"    column INTEGER NOT NULL," +
			"    turn INTEGER," +
			"    text VARCHAR(1024) NOT NULL," +
			"    image VARCHAR(255)" +
			");");

	@Override
	public Iterable<String> getInitializers() {
		return INITIALIZERS;
	}

	@Override
	public void write(DB db, TextFixture obj, Point context) {
		Integer turn;
		if (obj.getTurn() >= 0) {
			turn = obj.getTurn();
		} else {
			turn = null;
		}
		db.update("INSERT INTO text_notes (row, column, turn, text, image) VALUES(?, ?, ?, ?, ?);",
				context.getRow(), context.getColumn(), turn, obj.getText(), obj.getImage())
			.execute();
	}

	private TryBiConsumer<Map<String, Object>, Warning, Exception> readTextNote(IMutableMapNG map) {
		return (dbRow, warner) -> {
			int row = (Integer) dbRow.get("row");
			int column = (Integer) dbRow.get("column");
			Integer turn = (Integer) dbRow.get("turn");
			String text = (String) dbRow.get("text");
			String image = (String) dbRow.get("image");
			TextFixture fixture = new TextFixture(text, turn == null ? -1 : turn);
			if (image != null) {
				fixture.setImage(image);
			}
			map.addFixture(new Point(row, column), fixture);
		};
	}

	@Override
	public void readMapContents(DB db, IMutableMapNG map, Warning warner) {
		try {
			handleQueryResults(db, warner, "text notes", readTextNote(map),
				"SELECT * FROM text_notes");
		} catch (RuntimeException except) {
			// Don't wrap RuntimeExceptions in RuntimeException
			throw except;
		} catch (Exception except) {
			// FIXME Antipattern
			throw new RuntimeException(except);
		}
	}
}
