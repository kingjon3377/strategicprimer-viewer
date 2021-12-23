package report.generators;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import lovelace.util.DelayedRemovalMap;
import lovelace.util.IOConsumer;
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

	public AdventureReportGenerator(Comparator<Pair<Point, IFixture>> comp, Player currentPlayer,
			MapDimensions dimensions) {
		this(comp, currentPlayer, dimensions, null);
	}

	public AdventureReportGenerator(Comparator<Pair<Point, IFixture>> comp, Player currentPlayer,
			MapDimensions dimensions, @Nullable Point hq) {
		super(comp, dimensions, hq);
		this.currentPlayer = currentPlayer;
	}

	private final Player currentPlayer;

	/**
	 * Produce the report on all adventure hooks in the map.
	 */
	@Override
	public void produce(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map,
			IOConsumer<String> ostream) throws IOException {
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
	public void produceSingle(DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			IMapNG map, IOConsumer<String> ostream, AdventureFixture item, Point loc) 
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
