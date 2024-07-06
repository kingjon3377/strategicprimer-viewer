package report.generators;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import lovelace.util.DelayedRemovalMap;

import java.util.stream.Collectors;

import legacy.map.Player;
import legacy.map.IFixture;
import legacy.map.Point;
import legacy.map.MapDimensions;
import legacy.map.ILegacyMap;
import legacy.map.fixtures.explorable.AdventureFixture;

/**
 * A report generator for adventure hooks.
 */
public class AdventureReportGenerator extends AbstractReportGenerator<AdventureFixture> {

	public AdventureReportGenerator(final Player currentPlayer, final MapDimensions dimensions) {
		this(currentPlayer, dimensions, null);
	}

	public AdventureReportGenerator(final Player currentPlayer, final MapDimensions dimensions,
	                                final @Nullable Point hq) {
		super(dimensions, hq);
		this.currentPlayer = currentPlayer;
	}

	private final Player currentPlayer;

	/**
	 * Produce the report on all adventure hooks in the map.
	 */
	@Override
	public void produce(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures, final ILegacyMap map,
	                    final Consumer<String> ostream) {
		writeMap(ostream, fixtures.values().stream()
						.filter(p -> p.getValue1() instanceof AdventureFixture)
						.sorted(pairComparator)
						.collect(Collectors.toMap(p -> (AdventureFixture) p.getValue1(),
								Pair::getValue0, (u, v) -> {
									throw new IllegalStateException("Duplicates in stream");
								},
								() -> new HeadedMapImpl<>(
										"<h4>Possible Adventures</h4>"))),
				defaultFormatter(fixtures, map));
	}

	/**
	 * Produce a more verbose sub-report on an adventure hook.
	 */
	@Override
	public void produceSingle(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                          final ILegacyMap map, final Consumer<String> ostream, final AdventureFixture item,
	                          final Point loc) {
		fixtures.remove(item.getId());
		ostream.accept(item.getBriefDescription());
		ostream.accept(" at ");
		ostream.accept(loc.toString());
		ostream.accept(": ");
		ostream.accept(item.getFullDescription());
		ostream.accept(" ");
		ostream.accept(distanceString.apply(loc));
		if (!item.owner().isIndependent()) {
			ostream.accept(" (already investigated by ");
			if (item.owner().equals(currentPlayer)) {
				ostream.accept(" you)");
			} else {
				ostream.accept(" another player)");
			}
		}
	}
}
