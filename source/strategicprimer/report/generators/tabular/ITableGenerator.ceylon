import ceylon.collection {
    ArrayList,
    MutableList
}
import ceylon.math.float {
    sqrt
}
import ceylon.regex {
    regex,
    Regex
}

import lovelace.util.common {
    DelayedRemovalMap,
    comparingOn
}

import strategicprimer.model.map {
    Player,
    IFixture,
    Point,
    MapDimensions
}
import javax.swing.table {
    TableModel,
    DefaultTableModel
}
import ceylon.interop.java {
	createJavaStringArray
}

"A regular expression to mtch quote characters."
Regex quotePattern = regex("\"", true);
"An interface for tabular-report generators. It's expected that implementers will take the
 current player and the location of his or her HQ as constructor parameters."
shared interface ITableGenerator<T> given T satisfies IFixture {
    "Produce a tabular report on a particular category of fixtures in the map, and remove
      all fixtures covered in the table from the collection."
    shared default void produceTable(Anything(String) ostream, DelayedRemovalMap<Integer,
    [Point, IFixture]> fixtures) {
        MutableList<[Integer, [Point, T]]> temp =
                ArrayList<[Integer, [Point, T]]>();
        for (key->val in fixtures) {
            if (is T fixture = val.rest.first) {
                temp.add([key, [val.first, fixture]]);
            }
        }
        {[Integer, [Point, T]]*} values = temp
            .sort(comparingOn(([Integer, [Point, T]] pair) => pair.rest.first,
            comparePairs));
        writeRow(ostream, headerRow.first, *headerRow.rest);
        for ([num, [loc, item]] in values) {
            for (row in produce(fixtures, item, num, loc)) {
                writeRow(ostream, row.first, *row.rest);
            }
        }
        fixtures.coalesce();
    }
    "Produce a tabular report on a particular category of fixtures in the map, in the
     format of a model for a Swing JTable, and remove all fixtures covered in the table
     from the collection."
    shared default TableModel produceTableModel(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures) {
        MutableList<[Integer, [Point, T]]> temp =
                ArrayList<[Integer, [Point, T]]>();
        for (key->val in fixtures) {
            if (is T fixture = val.rest.first) {
                temp.add([key, [val.first, fixture]]);
            }
        }
        {[Integer, [Point, T]]*} values = temp
            .sort(comparingOn(([Integer, [Point, T]] pair) => pair.rest.first,
            comparePairs));
        DefaultTableModel retval = DefaultTableModel(
            createJavaStringArray(headerRow), 0);
        for ([num, [loc, item]] in values) {
            for (row in produce(fixtures, item, num, loc)) {
                retval.addRow(createJavaStringArray(row));
            }
        }
        fixtures.coalesce();
        return retval;
    }
    "Produce lines (usually only one line) of the tabular report. Returns the empty
     iterable if not handled by this generator. Because not all lines should remove
     items from the collection, implementations must do that removal themselves."
    shared formal {{String+}*} produce(
            "The set of fixtures."
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            "The item to base this line on."
            T item,
            "Its key in the colleciton (usually its ID, but not always)"
            Integer key,
            "The location of this item in the map."
            Point loc);
    "Given two points, return a number sufficiently proportional to the distance between
     them for ordering points based on distance from a base. The default implementation
     returns the *square* of the distance, for efficiency."
    shared default Integer distance(Point first, Point second, MapDimensions dimensions) {
        Integer colDistRaw = (first.column - second.column).magnitude;
        Integer rowDistRaw = (first.row - second.row).magnitude;
        Integer colDist;
        Integer rowDist;
        if (colDistRaw > dimensions.columns / 2) {
            colDist = dimensions.columns - colDistRaw;
        } else {
            colDist = colDistRaw;
        }
        if (rowDistRaw > dimensions.rows / 2) {
            rowDist = dimensions.rows - rowDistRaw;
        } else {
            rowDist = rowDistRaw;
        }
        return (colDist * colDist) + (rowDist * rowDist);
    }
    "A String showing the distance between two points, suitable to be displayed, rounded
     to a tenth of a tile. This default implementation just takes the square root of
     [[distance]] and formats it."
    shared default String distanceString(Point first, Point second,
                MapDimensions dimensions) =>
            Float.format(sqrt(distance(first, second, dimensions).float),
                1, 1);
    "The header row to print at the top of the report, listing what the fields represent."
    shared formal [String+] headerRow;
    "Compare two Point-fixture pairs."
    shared formal Comparison comparePairs([Point, T] one, [Point, T] two);
    """"A String representing the owner of a fixture: "You" if equal to currentPlayer,
       "Independent" if an independent player, or otherwise the player's name."""
    shared default String ownerString(Player currentPlayer, Player owner) {
        if (currentPlayer == owner) {
            return "You";
        } else if (owner.independent) {
            return "Independent";
        } else {
            return owner.name;
        }
    }
    """"The field delimiter; provided to limit "magic character" warnings and allow us to
       change it."""
    shared default Character fieldDelimiter => ',';
    """The row delimiter; used to limit "magic character" warnings and allow us to change
       it."""
    shared default String rowDelimiter => operatingSystem.newline;
    "Write multiple fields to a row, quoting as necessary, separated by the field
     delimiter, with the last field followed by the row delimiter."
    shared default void writeRow(Anything(String) ostream, String firstField,
            String* fields) {
        void writeField(Anything(String) ostream, String field) {
            String quotesQuoted = quotePattern.replace(field, "\"\"");
            if ({"\"", fieldDelimiter.string, rowDelimiter, " "}
                .any(quotesQuoted.contains)) {
                ostream("\"``quotesQuoted``\"");
            } else {
                ostream(quotesQuoted);
            }
        }
        writeField(ostream, firstField);
        for (field in fields) {
            ostream(fieldDelimiter.string);
            writeField(ostream, field);
        }
        ostream(rowDelimiter);
    }
    "The file-name to (by default) write this table to."
    shared formal String tableName;
}
