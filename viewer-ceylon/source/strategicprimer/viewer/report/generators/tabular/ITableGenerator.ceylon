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
    todo,
    DelayedRemovalMap
}
import strategicprimer.viewer.model.map {
    Player,
    IFixture,
    Point
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
            if (produce(ostream, fixtures, item,
                loc)) {
                fixtures.remove(num);
            }
        }
        fixtures.coalesce();
    }
    "Produce a single line of the tabular report. Return whether to remove this item from
     the collection."
    shared formal Boolean produce(
            "The stream to write the row to."
            Anything(String) ostream,
            "The set of fixtures."
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            "The item to base this line on."
            T item,
            "The location of this item in the map."
            Point loc);
    "Given two points, return a number sufficiently proportional to the distance between
     them for ordering points based on distance from a base. The default implementation
     returns the *square* of the distance, for efficiency."
    todo("Reflect the toroidal topology of the map")
    shared default Integer distance(Point first, Point second) =>
            ((first.column - second.column) * (first.column - second.column)) +
            ((first.row - second.row) * (first.row - second.row));
    "A String showing the distance between two points, suitable to be displayed, rounded
     to a tenth of a tile. This default implementation just takes the square root of
     [[distance]] and formats it."
    shared default String distanceString(Point first, Point second) =>
            Float.format(sqrt(distance(first, second).float), 1, 1);
    "The CSV header row to print at the top of the report, not including the newline."
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
"Compare an object with another object based on one field in particular."
todo("Move to lovelace.util")
shared Comparison(BaseType, BaseType) comparingOn<BaseType, FieldType>(
        "The field as a function of the object."
        FieldType(BaseType) extractor,
        "How to compare the corresponding field instances"
        Comparison(FieldType, FieldType) comparator) {
    return (BaseType first, BaseType second) =>
    comparator(extractor(first), extractor(second));
}
