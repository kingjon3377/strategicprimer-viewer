package report.generators;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import lovelace.util.ThrowingConsumer;
import java.io.IOException;
import java.util.Comparator;
import lovelace.util.DelayedRemovalMap;
import java.util.List;
import java.util.stream.Collectors;

import common.map.IFixture;
import common.map.Point;
import common.map.MapDimensions;
import common.map.IMapNG;
import common.map.fixtures.TextFixture;

/**
 * A report generator for arbitrary-text notes.
 */
public class TextReportGenerator extends AbstractReportGenerator<TextFixture> {

	public TextReportGenerator(final Comparator<Pair<Point, IFixture>> comp, final MapDimensions dimensions) {
		this(comp, dimensions, null);
	}

	public TextReportGenerator(final Comparator<Pair<Point, IFixture>> comp, final MapDimensions dimensions,
	                           @Nullable final Point hq) {
		super(comp, dimensions, hq);
	}

	/**
	 * Produce the part of the report dealing with an arbitrary-text note.
	 * This does <em>not</em> remove it from the collection, because this method
	 * doesn't know the synthetic ID number that was assigned to it.
	 */
	@Override
	public void produceSingle(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                          final IMapNG map, final ThrowingConsumer<String, IOException> ostream, final TextFixture item, final Point loc)
			throws IOException {
		ostream.accept("At ");
		ostream.accept(loc.toString());
		ostream.accept(" ");
		ostream.accept(distanceString.apply(loc));
		if (item.getTurn() >= 0) {
			ostream.accept(": On turn ");
			ostream.accept(Integer.toString(item.getTurn()));
		}
		ostream.accept(": ");
		ostream.accept(item.getText());
	}

	private static int compareTurn(final Triplet<Integer, Point, TextFixture> one,
	                               final Triplet<Integer, Point, TextFixture> two) {
		return Integer.compare(one.getValue2().getTurn(), two.getValue2().getTurn());
	}

	/**
	 * Produce the part of the report dealing with arbitrary-text notes.
	 */
	@Override
	public void produce(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                    final IMapNG map, final ThrowingConsumer<String, IOException> ostream) throws IOException {
		List<Triplet<Integer, Point, TextFixture>> items =
			fixtures.entrySet().stream()
				.filter(e -> e.getValue().getValue1() instanceof TextFixture)
				.map(e -> Triplet.with(e.getKey(), e.getValue().getValue0(),
					(TextFixture) e.getValue().getValue1()))
				.sorted(((Comparator<Triplet<Integer, Point, TextFixture>>)
						TextReportGenerator::compareTurn)
					.thenComparing(t -> Pair.with(t.getValue1(), (IFixture) t.getValue2()),
						pairComparator)
					.thenComparing(t -> t.getValue2().getText()))
				.collect(Collectors.toList());
		if (!items.isEmpty()) {
			ostream.accept("<h4>Miscellaneous Notes</h4>");
			ostream.accept(System.lineSeparator());
			ostream.accept("<ul>");
			ostream.accept(System.lineSeparator());
			for (Triplet<Integer, Point, TextFixture> triplet : items) {
				int key = triplet.getValue0();
				Point location = triplet.getValue1();
				TextFixture item = triplet.getValue2();
				fixtures.remove(key);
				ostream.accept("<li>");
				produceSingle(fixtures, map, ostream, item, location);
				ostream.accept("</li>");
				ostream.accept(System.lineSeparator());
			}
			ostream.accept("</ul>");
				ostream.accept(System.lineSeparator());
		}
	}
}
