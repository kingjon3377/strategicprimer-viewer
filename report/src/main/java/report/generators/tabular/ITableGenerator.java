package report.generators.tabular;

import java.io.IOException;
import java.util.List;
import lovelace.util.ThrowingConsumer;
import org.jetbrains.annotations.Nullable;

import lovelace.util.DelayedRemovalMap;

import common.map.Player;
import common.map.IFixture;
import common.map.MapDimensions;
import common.map.Point;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;

import java.util.regex.Pattern;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.logging.Logger;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * An interface for tabular-report generators. It's expected that implementers
 * will take the current player and the location of his or her HQ as
 * constructor parameters.
 */
public interface ITableGenerator<T extends IFixture> {
	/**
	 * A regular expression to match quote characters.
	 */
	static final Pattern QUOTE_PATTERN = Pattern.compile("\"");

	static final Logger LOGGER = Logger.getLogger(ITableGenerator.class.getName());

	// TODO: Should actually be a Predicate<IFixture> canHandle(), most
	// likely, in the absence of reified union types.
	Class<T> narrowedClass();

	/**
	 * Produce a tabular report on a particular category of fixtures in the
	 * map, and remove all fixtures covered in the table from the collection.
	 */
	default void produceTable(final ThrowingConsumer<String, IOException> ostream, final DelayedRemovalMap<Integer,
			Pair<Point, IFixture>> fixtures, final Map<Integer, Integer> parentMap)
			throws IOException {
		Class<T> cls = narrowedClass();
		Iterable<Triplet<Integer, Point, T>> values = fixtures.entrySet().stream()
			.filter(e -> cls.isInstance(e.getValue().getValue1()))
			.map(e -> Triplet.with(e.getKey(), e.getValue().getValue0(),
				(T) e.getValue().getValue1()))
			.sorted(Comparator.comparing(Triplet::removeFrom0, this::comparePairs))
			.collect(Collectors.toList());
		writeRow(ostream, getHeaderRow().toArray(new String[0]));
		for (Triplet<Integer, Point, T> triplet : values) {
			for (List<String> row : produce(fixtures, triplet.getValue2(),
					triplet.getValue0(), triplet.getValue1(), parentMap)) {
				writeRow(ostream, row.toArray(new String[0]));
			}
		}
		fixtures.coalesce();
	}

	/**
	 * Produce a tabular report on a particular category of fixtures in the
	 * map, in the format of a model for a Swing JTable, and remove all
	 * fixtures covered in the table from the collection.
	 */
	default TableModel produceTableModel(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                                     final Map<Integer, Integer> parentMap) {
		Class<T> cls = narrowedClass();
		Iterable<Triplet<Integer, Point, T>> values = fixtures.entrySet().stream()
			.filter(e -> cls.isInstance(e.getValue().getValue1()))
			.map(e -> Triplet.with(e.getKey(), e.getValue().getValue0(),
				(T) e.getValue().getValue1()))
			.sorted(Comparator.comparing(Triplet::removeFrom0, this::comparePairs))
			.collect(Collectors.toList());
		DefaultTableModel retval = new DefaultTableModel(getHeaderRow().toArray(), 0);
		int count = 0;
		for (Triplet<Integer, Point, T> triplet : values) {
			for (List<String> row : produce(fixtures, triplet.getValue2(),
					triplet.getValue0(), triplet.getValue1(), parentMap)) {
				// TODO: We'd like special handling of numeric fields ...
				// TODO: Offer a version of addRow() that takes a List?
				retval.addRow(row.toArray(new String[0]));
				count++;
			}
		}
		LOGGER.finer(String.format("Added %d rows in %s", count, getTableName()));
		fixtures.coalesce();
		return retval;
	}

	/**
	 * Produce lines (usually only one line) of the tabular report. Returns
	 * an empty iterable if not handled by this generator. Because not all
	 * lines should remove items from the collection, implementations must
	 * do that removal themselves.
	 *
	 * @param fixtures The set of fixtures.
	 * @param item The item to base this line or these lines on.
	 * @param key Its key in the collection (usually its ID, but not always)
	 * @param loc The location of this item in the map.
	 * @param parentMap The mapping from children's to parents' IDs
	 */
	List<List<String>> produce(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                           T item, int key, Point loc, Map<Integer, Integer> parentMap);

	/**
	 * Given two points, return a number sufficiently proportional to the
	 * distance between them for ordering points based on distance from a
	 * base. The default implementation returns the <em>square</em> of the
	 * distance, for efficiency.
	 */
	default int distance(final Point first, final Point second, final MapDimensions dimensions) {
		int colDistRaw = Math.abs(first.getColumn() - second.getColumn());
		int rowDistRaw = Math.abs(first.getRow() - second.getRow());
		int colDist;
		int rowDist;
		if (colDistRaw > dimensions.getColumns() / 2) {
			colDist = dimensions.getColumns() - colDistRaw;
		} else {
			colDist = colDistRaw;
		}
		if (rowDistRaw > dimensions.getRows() / 2) {
			rowDist = dimensions.getRows() - rowDistRaw;
		} else {
			rowDist = rowDistRaw;
		}
		return (colDist * colDist) + (rowDist * rowDist);
	}

	/**
	 * A String showing the distance between two points, suitable to be
	 * displayed, rounded to a tenth of a tile. This default implementation
	 * just takes the square root of {@link distance} and formats it,
	 * unless one or both of the points is null or invalid, in which case it prints "unknown" instead.
	 */
	default String distanceString(@Nullable final Point first, @Nullable final Point second,
	                              final MapDimensions dimensions) {
		if (first != null && second != null && first.isValid() && second.isValid()) {
			return String.format("%1.1f", Math.sqrt(distance(first, second, dimensions)));
		} else {
			return "unknown";
		}
	}

	/**
	 * If the given {@link location point} is valid, return its {@link
	 * Point#toString} string representation}; otherwise, return "unknown".
	 */
	default String locationString(final Point location) {
		return (location.isValid()) ? location.toString() : "unknown";
	}

	/**
	 * The header row to print at the top of the report, listing what the fields represent.
	 */
	List<String> getHeaderRow();

	/**
	 * Compare two Point-fixture pairs.
	 */
	int comparePairs(Pair<Point, T> one, Pair<Point, T> two);

	/**
	 * The Point in a Point-fixture pair. TODO: Remove this, right?
	 */
	default Point pairPoint(final Pair<Point, T> pair) {
		return pair.getValue0();
	}

	/**
	 * The fixture in a Point-fixture pair. TODO: Remove this, right?
	 */
	default T pairFixture(final Pair<Point, T> pair) {
		return pair.getValue1();
	}

	/**
	 * A String representing the owner of a fixture: "You" if equal to
	 * currentPlayer, "Independent" if an independent player, or otherwise
	 * the player's name.
	 */
	default String ownerString(final Player currentPlayer, final Player owner) {
		if (currentPlayer.equals(owner)) {
			return "You";
		} else if (owner.isIndependent()) {
			return "Independent";
		} else {
			return owner.getName();
		}
	}

	/**
	 * The field delimiter; provided to limit "magic character" warnings
	 * and allow us to change it.
	 */
	default char getFieldDelimiter() {
		return ',';
	}

	/**
	 * The row delimiter; used to limit "magic character" warnings and
	 * allow us to change it.
	 */
	default String getRowDelimiter() {
		return System.lineSeparator();
	}

	/**
	 * Write multiple fields to a row, quoting as necessary, separated by
	 * the field delimiter, with the last field followed by the row
	 * delimiter.
	 */
	default void writeRow(final ThrowingConsumer<String, IOException> ostream, final String... fields) throws IOException {
		boolean firstField = true;
		for (String field : fields) {
			if (firstField) {
				firstField = false;
			} else {
				ostream.accept(Character.toString(getFieldDelimiter()));
			}
			String quotesQuoted = QUOTE_PATTERN.matcher(field).replaceAll("\"\"");
			if (quotesQuoted.contains(Character.toString(getFieldDelimiter())) ||
					quotesQuoted.contains(getRowDelimiter()) ||
					quotesQuoted.contains(" ")) {
				ostream.accept(String.format("\"%s\"", quotesQuoted));
			} else {
				ostream.accept(quotesQuoted);
			}
		}
		ostream.accept(getRowDelimiter());
	}

	/**
	 * The file-name to (by default) write this table to.
	 */
	String getTableName();
}
