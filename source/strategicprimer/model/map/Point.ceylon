"A structure encapsulating two coordinates: a row and column in the map."
shared interface Point satisfies Comparable<Point> {
	"The first coordinate, the point's row."
	shared formal Integer row;
	"The second coordinate, the point's column."
	shared formal Integer column;
	"Compare to another point, by first row and then column."
	shared actual default Comparison compare(Point point) {
		Comparison rowComparison = row <=> point.row;
		if (rowComparison == equal) {
			return column <=> point.column;
		} else {
			return rowComparison;
		}
	}
	"""A point is "valid" if neither row nor column is negative."""
	shared default Boolean valid => row >= 0 && column >= 0;
}