import ceylon.collection {
    MutableMap,
    HashMap
}
import ceylon.language.meta {
    type
}
import ceylon.language.meta.model {
    Type
}

import lovelace.util.common {
    DelayedRemovalMap
}

import strategicprimer.model.common.map {
    IFixture,
    Point,
    MapDimensions,
    IMapNG
}
import strategicprimer.model.common.map.fixtures.mobile {
    Centaur,
    Fairy,
    Giant,
    SimpleImmortal,
    Dragon,
    Immortal,
    Ogre,
    Troll,
    Sphinx,
    Phoenix,
    Griffin,
    Djinn,
    Simurgh,
    Minotaur,
    ImmortalAnimal,
    Thunderbird,
    Kraken,
    Snowbird,
    Unicorn,
    Pegasus
}
import com.vasileff.ceylon.structures {
    MutableMultimap,
    HashMultimap,
    Multimap
}

"""A report generator for [["immortals"|Immortal]]---dragons, fairies, centaurs, and
   such."""
shared class ImmortalsReportGenerator(
        Comparison([Point, IFixture], [Point, IFixture]) comp, MapDimensions dimensions,
        Point? hq = null)
        extends AbstractReportGenerator<Immortal>(comp, dimensions, hq) {
    "Produce a report on an individual immortal."
    shared actual void produceSingle(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, Immortal item, Point loc) {
        fixtures.remove(item.id);
        ostream("At ``loc``: A(n) ``item`` ``distanceString(loc)``");
    }

    "Produce a report on all immortals."
    shared actual void produce(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream) {
        MutableMap<Type<IFixture>, Anything(String, Point)> meta =
                HashMap<Type<IFixture>, Anything(String, Point)>();
        MutableMultimap<String, Point> simples = HashMultimap<String, Point>();
        void handleSimple(Type<SimpleImmortal|ImmortalAnimal> type, String plural) =>
                meta.put(type, (_, point) => simples.put(plural, point));
        handleSimple(`Sphinx`, "Sphinx(es)");
        handleSimple(`Djinn`, "Djinn(i)");
        handleSimple(`Griffin`, "Griffin(s)");
        handleSimple(`Minotaur`, "Minotaur(s)");
        handleSimple(`Ogre`, "Ogre(s)");
        handleSimple(`Phoenix`, "Phoenix(es)");
        handleSimple(`Simurgh`, "Simurgh(s)");
        handleSimple(`Troll`, "Troll(s)");
        handleSimple(`Snowbird`, "Snowbird(s)");
        handleSimple(`Thunderbird`, "Thunderbird(s)");
        handleSimple(`Pegasus`, "Pegasi");
        handleSimple(`Unicorn`, "Unicorn(s)");
        handleSimple(`Kraken`, "Kraken(s)");
        MutableMultimap<String, Point> handleComplex(Type<Immortal> type,
                String plural = "(s)") {
            MutableMultimap<String, Point> retval = HashMultimap<String, Point>();
            meta.put(type, (kind, point) => retval.put(kind+plural, point));
            return retval;
        }
        MutableMultimap<String, Point> centaurs = handleComplex(`Centaur`);
        MutableMultimap<String, Point> giants = handleComplex(`Giant`);
        MutableMultimap<String, Point> fairies = handleComplex(`Fairy`, "");
        MutableMultimap<String, Point> dragons = handleComplex(`Dragon`);
        for ([point, immortal] in fixtures.items.narrow<[Point, Immortal]>()
                .sort(pairComparator)) {
            if (exists func = meta[type(immortal)]) {
                func(immortal.string, point);
                fixtures.remove(immortal.id);
            }
        }
        if (!centaurs.empty || !giants.empty, !fairies.empty || !dragons.empty ||
                !simples.empty) {
            ostream("""<h4>Immortals</h4>
                       <ul>""");
            for (key->list in [centaurs, giants, fairies, dragons, simples]
                    .flatMap(Multimap.asMap)) {
                if (!list.empty) {
                    ostream("<li>");
                    ostream(key);
                    ostream(": at ");
                    ostream(commaSeparatedList(list));
                    ostream("</li>``operatingSystem.newline``");
                }
            }
            ostream("</ul>``operatingSystem.newline``");
        }
    }
}
