package report.generators;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import lovelace.util.DelayedRemovalMap;
import lovelace.util.ThrowingConsumer;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.io.IOException;

import common.map.Player;
import common.map.IFixture;
import common.map.Point;
import common.map.MapDimensions;
import common.map.IMapNG;
import common.map.fixtures.explorable.AdventureFixture;

/**
 * A report generator for adventure hooks.
 */
public class AdventureReportGenerator extends AbstractReportGenerator<AdventureFixture> {

	public AdventureReportGenerator(final Comparator<Pair<Point, IFixture>> comp, final Player currentPlayer,
	                                final MapDimensions dimensions) {
		this(comp, currentPlayer, dimensions, null);
	}

	public AdventureReportGenerator(final Comparator<Pair<Point, IFixture>> comp, final Player currentPlayer,
	                                final MapDimensions dimensions, @Nullable final Point hq) {
		super(comp, dimensions, hq);
		this.currentPlayer = currentPlayer;
	}

	private final Player currentPlayer;

	/**
	 * Produce the report on all adventure hooks in the map.
	 */
	@Override
	public void produce(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures, final IMapNG map,
	                    final ThrowingConsumer<String, IOException> ostream) throws IOException {
		super.<AdventureFixture>writeMap(ostream, fixtures.values().stream()
				.filter(p -> p.getValue1() instanceof AdventureFixture)
				.sorted(pairComparator)
				.collect(Collectors.toMap(p -> (AdventureFixture) p.getValue1(),
					Pair::getValue0, (u, v) -> {
						throw new IllegalStateException("Duplicates in stream"); },
					() -> new HeadedMapImpl<AdventureFixture, Point>(
						"<h4>Possible Adventures</h4>"))),
			defaultFormatter(fixtures, map));
	}

	/**
	 * Produce a more verbose sub-report on an adventure hook.
	 */
	@Override
	public void produceSingle(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                          final IMapNG map, final ThrowingConsumer<String, IOException> ostream, final AdventureFixture item, final Point loc)
			throws IOException {
		fixtures.remove(item.getId());
		ostream.accept(item.getBriefDescription());
		ostream.accept(" at ");
		ostream.accept(loc.toString());
		ostream.accept(": ");
		ostream.accept(item.getFullDescription());
		ostream.accept(" ");
		ostream.accept(distanceString.apply(loc));
		if (!item.getOwner().isIndependent()) {
			ostream.accept(" (already investigated by ");
			if (item.getOwner().equals(currentPlayer)) {
				ostream.accept(" you)");
			} else {
				ostream.accept(" another player)");
			}
		}
	}
}
