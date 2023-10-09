package report.generators;

import java.util.function.Consumer;

import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;

import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;

import lovelace.util.DelayedRemovalMap;

import common.map.Player;
import common.map.IFixture;
import common.map.Point;
import common.map.MapDimensions;
import common.map.IMapNG;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.Implement;
import common.map.fixtures.FortressMember;
import common.map.fixtures.mobile.IUnit;

/**
 * A report generator for equipment and resources.
 */
public class FortressMemberReportGenerator extends AbstractReportGenerator<FortressMember> {

    private final Player currentPlayer;
    private final MapDimensions dimensions;
    private final int currentTurn;
    private final @Nullable Point hq;

    public FortressMemberReportGenerator(final Player currentPlayer, final MapDimensions dimensions, final int currentTurn) {
        this(currentPlayer, dimensions, currentTurn, null);
    }

    public FortressMemberReportGenerator(final Player currentPlayer,
                                         final MapDimensions dimensions, final int currentTurn, final @Nullable Point hq) {
        super(dimensions, hq);
        this.currentPlayer = currentPlayer;
        this.dimensions = dimensions;
        this.currentTurn = currentTurn;
        this.hq = hq;
    }

    /**
     * Produces a sub-report on a resource or piece of equipment. All
     * fixtures referred to in this report are removed from the collection.
     */
    @Override
    public void produceSingle(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
                              final IMapNG map, final Consumer<String> ostream, final FortressMember item, final Point loc) {
        //	assert (is IUnit|IResourcePile|Implement item);
        switch (item) {
            case IUnit u -> // TODO: Should be a field, right? Or else a constructor parameter?
                    new UnitReportGenerator(currentPlayer, dimensions,
                            currentTurn, hq).produceSingle(fixtures, map, ostream, u, loc);
            case Implement i -> {
                fixtures.remove(item.getId());
                ostream.accept("Equipment: ");
                ostream.accept(i.getKind());
                if (i.getCount() > 1) {
                    ostream.accept(" (");
                    ostream.accept(Integer.toString(i.getCount()));
                    ostream.accept(")");
                }
            }
            case IResourcePile r -> {
                fixtures.remove(item.getId());
                ostream.accept("A pile of ");
                ostream.accept(r.getQuantity().toString());
                if (r.getQuantity().units().isEmpty()) {
                    ostream.accept(" ");
                } else {
                    ostream.accept(" of ");
                }
                ostream.accept(r.getContents());
                ostream.accept(" (");
                ostream.accept(r.getKind());
                ostream.accept(")");
                if (r.getCreated() >= 0) {
                    ostream.accept(" from turn ");
                    ostream.accept(Integer.toString(r.getCreated()));
                }
            }
            default -> {
				LovelaceLogger.warning("Unhandled case in FortressMemberReportGenerator.produceSingle()");
            }
        }
    }

    /**
     * Produces a sub-report on all fortress members. All fixtures referred
     * to in this report are removed from the collection. This method
     * should probably never actually be called, since nearly all resources
     * will be in fortresses and should be reported as such, but we'll
     * handle this properly anyway.
     */
    @Override
    public void produce(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
                        final IMapNG map, final Consumer<String> ostream) {
        final HeadedMap<Implement, Point> equipment = new HeadedMapImpl<>("<li>Equipment:",
                Comparator.comparing(Implement::getKind)
                        .thenComparing(Implement::getCount,
                                Comparator.reverseOrder())
                        .thenComparing(Implement::getId));
        final Map<String, HeadedMap<IResourcePile, Point>> resources = new HashMap<>();
        for (final Pair<Point, FortressMember> pair : fixtures.values().stream()
                .filter(p -> p.getValue1() instanceof IResourcePile ||
                        p.getValue1() instanceof Implement)
                .sorted(pairComparator)
                .map(p -> Pair.with(p.getValue0(), (FortressMember) p.getValue1())).toList()) {
            final Point loc = pair.getValue0();
            final FortressMember item = pair.getValue1();
            if (item instanceof final IResourcePile resource) {
                final HeadedMap<IResourcePile, Point> pileMap;
                if (resources.containsKey(resource.getKind())) {
                    pileMap = resources.get(resource.getKind());
                } else {
                    pileMap = new HeadedMapImpl<>(resource.getKind() + ":",
                            Comparator.comparing(IResourcePile::getKind)
                                    .thenComparing(IResourcePile::getContents)
                                    .thenComparing(IResourcePile::getQuantity,
                                            Comparator.reverseOrder())
                                    .thenComparing(IResourcePile::getCreated)
                                    .thenComparing(IResourcePile::getId));
                    resources.put(resource.getKind(), pileMap);
                }
                pileMap.put(resource, loc);
                fixtures.remove(resource.getId());
            } else if (item instanceof final Implement implement) {
                equipment.put(implement, loc); // TODO: Ensure it's not displacing anything (i.e. no duplicate equipment fixtures)
                fixtures.remove(implement.getId());
            }
        }
        if (!equipment.isEmpty() || !resources.isEmpty()) {
            println(ostream, "<h4>Resources and Equipment</h4>");
            println(ostream, "<ul>");
            writeMap(ostream, equipment, defaultFormatter(fixtures, map));
            if (!resources.isEmpty()) {
                println(ostream, "<li>Resources:<ul>");
                for (final Map.Entry<String, HeadedMap<IResourcePile, Point>> entry :
                        resources.entrySet()) {
                    final String kind = entry.getKey();
                    final HeadedMap<IResourcePile, Point> mapping = entry.getValue();
                    ostream.accept("<li>");
                    writeMap(ostream, mapping, defaultFormatter(fixtures, map));
                    println(ostream, "</li>");
                }
                println(ostream, "</ul>");
                println(ostream, "</li>");
            }
            println(ostream, "</ul>");
        }
    }
}
