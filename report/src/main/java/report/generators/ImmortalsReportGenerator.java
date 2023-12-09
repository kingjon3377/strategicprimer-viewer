package report.generators;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;

import java.util.Map;
import java.util.HashMap;

import lovelace.util.ConcatIterable;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import lovelace.util.DelayedRemovalMap;

import legacy.map.IFixture;
import legacy.map.Point;
import legacy.map.MapDimensions;
import legacy.map.ILegacyMap;
import legacy.map.fixtures.mobile.Centaur;
import legacy.map.fixtures.mobile.Fairy;
import legacy.map.fixtures.mobile.Giant;
import legacy.map.fixtures.mobile.Dragon;
import legacy.map.fixtures.mobile.Immortal;
import legacy.map.fixtures.mobile.Ogre;
import legacy.map.fixtures.mobile.Troll;
import legacy.map.fixtures.mobile.Sphinx;
import legacy.map.fixtures.mobile.Phoenix;
import legacy.map.fixtures.mobile.Griffin;
import legacy.map.fixtures.mobile.Djinn;
import legacy.map.fixtures.mobile.Simurgh;
import legacy.map.fixtures.mobile.Minotaur;
import legacy.map.fixtures.mobile.Thunderbird;
import legacy.map.fixtures.mobile.Kraken;
import legacy.map.fixtures.mobile.Snowbird;
import legacy.map.fixtures.mobile.Unicorn;
import legacy.map.fixtures.mobile.Pegasus;

/**
 * A report generator for {@link Immortal "immortals"}---dragons, fairies, centaurs, and such.
 */
public class ImmortalsReportGenerator extends AbstractReportGenerator<Immortal> {

    public ImmortalsReportGenerator(final MapDimensions dimensions) {
        this(dimensions, null);
    }

    public ImmortalsReportGenerator(final MapDimensions dimensions, final @Nullable Point hq) {
        super(dimensions, hq);
    }

    /**
     * Produce a report on an individual immortal.
     */
    @Override
    public void produceSingle(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
                              final ILegacyMap map, final Consumer<String> ostream, final Immortal item, final Point loc) {
        fixtures.remove(item.getId());
        ostream.accept("At ");
        ostream.accept(loc.toString());
        ostream.accept(": A(n) ");
        ostream.accept(item.toString());
        ostream.accept(" ");
        ostream.accept(distanceString.apply(loc));
    }

    /**
     * Produce a report on all immortals.
     */
    @Override
    public void produce(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
                        final ILegacyMap map, final Consumer<String> ostream) {
        final Map<Class<? extends IFixture>, BiConsumer<String, Point>> meta = new HashMap<>();
        final Map<String, List<Point>> simples = new HashMap<>();
        final BiConsumer<Class<? extends Immortal>, String> handleSimple =
                (type, plural) -> meta.put(type, (ignored, point) -> {
                    final List<Point> list = Optional.ofNullable(simples.get(plural))
                            .orElseGet(ArrayList::new);
                    list.add(point);
                    simples.put(plural, list);
                });
        handleSimple.accept(Sphinx.class, "Sphinx(es)");
        handleSimple.accept(Djinn.class, "Djinn(i)");
        handleSimple.accept(Griffin.class, "Griffin(s)");
        handleSimple.accept(Minotaur.class, "Minotaur(s)");
        handleSimple.accept(Ogre.class, "Ogre(s)");
        handleSimple.accept(Phoenix.class, "Phoenix(es)");
        handleSimple.accept(Simurgh.class, "Simurgh(s)");
        handleSimple.accept(Troll.class, "Troll(s)");
        handleSimple.accept(Snowbird.class, "Snowbird(s)");
        handleSimple.accept(Thunderbird.class, "Thunderbird(s)");
        handleSimple.accept(Pegasus.class, "Pegasi");
        handleSimple.accept(Unicorn.class, "Unicorn(s)");
        handleSimple.accept(Kraken.class, "Kraken(s)");
        final BiFunction<Class<? extends Immortal>, String, Map<String, List<Point>>> handleComplex =
                (type, plural) -> {
                    final Map<String, List<Point>> retval = new HashMap<>();
                    meta.put(type, (kind, point) -> {
                        final String pluraled = kind + plural;
                        final List<Point> list = Optional.ofNullable(
                                retval.get(pluraled)).orElseGet(ArrayList::new);
                        list.add(point);
                        retval.put(pluraled, list);
                    });
                    return retval;
                };
        final Map<String, List<Point>> centaurs = handleComplex.apply(Centaur.class, "(s)");
        final Map<String, List<Point>> giants = handleComplex.apply(Giant.class, "(s)");
        final Map<String, List<Point>> fairies = handleComplex.apply(Fairy.class, "");
        final Map<String, List<Point>> dragons = handleComplex.apply(Dragon.class, "(s)");
        for (final Pair<Point, Immortal> pair : fixtures.values().stream()
                .filter(p -> p.getValue1() instanceof Immortal)
                .filter(p -> meta.containsKey(p.getValue1().getClass()))
                .sorted(pairComparator)
                .map(p -> Pair.with(p.getValue0(), (Immortal) p.getValue1())).toList()) {
            final BiConsumer<String, Point> func = meta.get(pair.getValue1().getClass());
            func.accept(pair.getValue1().toString(), pair.getValue0());
            fixtures.remove(pair.getValue1().getId());
        }
        if (!centaurs.isEmpty() || !giants.isEmpty() || !fairies.isEmpty() || !dragons.isEmpty() ||
                !simples.isEmpty()) {
            println(ostream, "<h4>Immortals</h4>");
            for (final Map.Entry<String, List<Point>> entry : new ConcatIterable<>(
                    centaurs.entrySet(), giants.entrySet(), fairies.entrySet(),
                    simples.entrySet())) {
                final String key = entry.getKey();
                final List<Point> list = entry.getValue();
                if (!list.isEmpty()) {
                    ostream.accept("<li>");
                    ostream.accept(key);
                    ostream.accept(": at ");
                    ostream.accept(commaSeparatedList(list));
                    println(ostream, "</li>");
                }
            }
            println(ostream, "</ul>");
        }
    }
}
