package report.generators;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;

import java.util.Map;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import lovelace.util.DelayedRemovalMap;

import common.map.Player;
import legacy.map.IFixture;
import legacy.map.Point;
import legacy.map.MapDimensions;
import legacy.map.ILegacyMap;
import common.map.fixtures.towns.TownStatus;
import legacy.map.fixtures.towns.ITownFixture;
import legacy.map.fixtures.towns.IFortress;
import legacy.map.fixtures.towns.Village;
import legacy.map.fixtures.towns.AbstractTown;

/**
 * A report generator for towns.
 *
 * TODO: Figure out some way to report what was found at any of the towns.
 *
 * TODO: Use "notes" for that?
 */
public class TownReportGenerator extends AbstractReportGenerator<ITownFixture> {

    private final Player currentPlayer;
    private final int currentTurn;
    private final MapDimensions dimensions;
    private final @Nullable Point hq;

    public TownReportGenerator(final Player currentPlayer,
                               final MapDimensions dimensions, final int currentTurn) {
        this(currentPlayer, dimensions, currentTurn, null);
    }

    public TownReportGenerator(final Player currentPlayer,
                               final MapDimensions dimensions, final int currentTurn, final @Nullable Point hq) {
        super(dimensions, hq);
        this.currentPlayer = currentPlayer;
        this.currentTurn = currentTurn;
        this.dimensions = dimensions;
        this.hq = hq;
    }

    /**
     * Separate towns by status.
     */
    private <T> void separateByStatus(final Map<TownStatus, T> mapping,
                                      final Collection<Pair<Point, IFixture>> collection,
                                      final BiConsumer<T, Pair<Point, ITownFixture>> func) {
        collection.stream().filter(p -> p.getValue1() instanceof AbstractTown)
                .sorted(pairComparator)
                .map(p -> Pair.with(p.getValue0(), (ITownFixture) p.getValue1()))
                .filter(p -> mapping.containsKey(p.getValue1().getStatus()))
                .map(p -> p.addAt0(mapping.get(p.getValue1().getStatus())))
                // TODO: Change func() to Consumer<Triplet<T, Point, ITownFixture>>?
                .forEach(t -> func.accept(t.getValue0(), t.removeFrom0()));
    }

    /**
     * Produce a report for a town. If a single fortress or village is
     * passed in, handling it is delegated to its dedicated
     * report-generating class. We remove the town from the set of fixtures.
     */
    @Override
    public void produceSingle(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
                              final ILegacyMap map, final Consumer<String> ostream, final ITownFixture item, final Point loc) {
        if (item instanceof final Village v) {
            new VillageReportGenerator(currentPlayer, dimensions, hq)
                    .produceSingle(fixtures, map, ostream, v, loc);
        } else if (item instanceof AbstractTown) {
            fixtures.remove(item.getId());
            ostream.accept("At ");
            ostream.accept(loc.toString());
            ostream.accept(": ");
            ostream.accept(item.getName());
            ostream.accept(", ");
            if (item.owner().isIndependent()) {
                ostream.accept("an independent ");
                ostream.accept(item.getTownSize().toString());
                ostream.accept(" ");
                ostream.accept(item.getStatus().toString());
                ostream.accept(" ");
                ostream.accept(item.getKind());
            } else {
                ostream.accept("a ");
                ostream.accept(item.getTownSize().toString());
                ostream.accept(" ");
                ostream.accept(item.getStatus().toString());
                if (item.owner().equals(currentPlayer)) {
                    ostream.accept(" allied with you");
                } else {
                    ostream.accept(item.owner().toString());
                }
            }
            ostream.accept(" ");
            ostream.accept(distanceString.apply(loc));
        } else if (item instanceof final IFortress f) {
            new FortressReportGenerator(currentPlayer, dimensions, currentTurn, hq)
                    .produceSingle(fixtures, map, ostream, f, loc);
        }
    }

    private static void separateByStatusInner(final Map<ITownFixture, Point> mapping,
                                              final Pair<Point, ITownFixture> pair) {
        mapping.put(pair.getValue1(), pair.getValue0());
    }

    /**
     * Produce a report on all towns. This report omits fortresses and
     * villages, and is sorted in a way that I hope is helpful. We remove
     * the town from the set of fixtures.
     */
    @Override
    public void produce(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
                        final ILegacyMap map, final Consumer<String> ostream) {
        final HeadedMap<ITownFixture, Point> abandoned =
                new HeadedMapImpl<>("<h5>Abandoned Communities</h5>");
        final HeadedMap<ITownFixture, Point> active =
                new HeadedMapImpl<>("<h5>Active Communities</h5>");
        final HeadedMap<ITownFixture, Point> burned =
                new HeadedMapImpl<>("<h5>Burned-Out Communities</h5>");
        final HeadedMap<ITownFixture, Point> ruined =
                new HeadedMapImpl<>("<h5>Ruined Communities</h5>");
        final Map<TownStatus, Map<ITownFixture, Point>> separated =
                Stream.of(Pair.with(TownStatus.Abandoned, abandoned),
                                Pair.with(TownStatus.Active, active),
                                Pair.with(TownStatus.Burned, burned),
                                Pair.with(TownStatus.Ruined, ruined))
                        .collect(Collectors.toMap(Pair::getValue0, Pair::getValue1));
        // separateByStatus() sorts using pairComparator, which should be by distance from HQ
        separateByStatus(separated, fixtures.values(), TownReportGenerator::separateByStatusInner);
        if (separated.values().stream().anyMatch(l -> !l.isEmpty())) {
            println(ostream, "<h4>Cities, towns, and/or fortifications you know about:</h4>");
            for (final HeadedMap<ITownFixture, Point> mapping :
                    Arrays.asList(abandoned, active, burned, ruined)) {
                writeMap(ostream, mapping, defaultFormatter(fixtures, map));
            }
        }
    }
}
