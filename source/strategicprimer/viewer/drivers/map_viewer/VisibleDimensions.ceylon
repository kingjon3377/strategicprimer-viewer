import lovelace.util.common {
    todo
}

"The minimum and maximum rows and columns drawn."
todo("Tests")
shared class VisibleDimensions(minimumRow, maximumRow, minimumColumn, maximumColumn) {
    "The lowest(-numbered) (top-most) row that we draw."
    shared Integer minimumRow;

    "The highest(-numbered) (bottom-most) row that we draw."
    shared Integer maximumRow;

    "The lowest (left-most) column we draw."
    shared Integer minimumColumn;

    "The highest (right-most) column we draw."
    shared Integer maximumColumn;

    "The rows that we draw."
    shared Range<Integer> rows = minimumRow..maximumRow;

    "The columns that we draw."
    shared Range<Integer> columns = minimumColumn..maximumColumn;

    shared actual String string =>
            "VisibleDimensions: (``minimumRow``, ``minimumColumn``) to (``maximumRow``, ``
                maximumColumn``)";

    // if this fails, the Span docs are wrong and we're off-by-one
    assert ((2..4).size == 3);

    "The number of columns visible."
    shared Integer width => (minimumColumn..maximumColumn).size;

    "The number of rows visible."
    shared Integer height => (minimumRow..maximumRow).size;
}
