import ceylon.collection {
    MutableList,
    MutableMap,
    HashMap,
    ArrayList
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

import strategicprimer.model.map {
    Point,
    IFixture,
    IMapNG,
    invalidPoint,
    MapDimensions
}
import strategicprimer.model.map.fixtures.mobile {
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
	Minotaur
}
import strategicprimer.report {
    IReportNode
}
import strategicprimer.report.nodes {
    SimpleReportNode,
    ListReportNode,
    SectionListReportNode,
    emptyReportNode
}
import com.vasileff.ceylon.structures {
	MutableMultimap,
	HashMultimap
}
"""A report generator for "immortals"---dragons, fairies, centaurs, and such."""
shared class ImmortalsReportGenerator(
        Comparison([Point, IFixture], [Point, IFixture]) comp, MapDimensions dimensions,
        Point hq = invalidPoint)
        extends AbstractReportGenerator<Immortal>(comp, dimensions, hq) {
    "Produce a report on an individual immortal."
    shared actual void produceSingle(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, Immortal item, Point loc) {
        fixtures.remove(item.id);
        ostream("At ``loc``: A(n) ``item`` ``distCalculator.distanceString(loc)``");
    }
    "Produce a report on all immortals."
    shared actual void produce(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream) {
        MutableList<[Point, IFixture]> values =
                ArrayList<[Point, IFixture]> { *fixtures.items
                    .sort(pairComparator) };
        MutableMap<Type<IFixture>, Anything(String, Point)> meta =
                HashMap<Type<IFixture>, Anything(String, Point)>();
        MutableMultimap<String, Point> simples = HashMultimap<String, Point>();
        void handleSimple(Type<SimpleImmortal> type, String plural) =>
                meta.put(type, (_, point) => simples.put(plural, point));
        handleSimple(`Sphinx`, "Sphinx(es)");
        handleSimple(`Djinn`, "Djinn(i)");
        handleSimple(`Griffin`, "Griffin(s)");
        handleSimple(`Minotaur`, "Minotaur(s)");
        handleSimple(`Ogre`, "Ogre(s)");
        handleSimple(`Phoenix`, "Phoenix(es)");
        handleSimple(`Simurgh`, "Simurgh(s)");
        handleSimple(`Troll`, "Troll(s)");
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
        for ([point, immortal] in values) {
            if (exists func = meta[type(immortal)]) {
                func(immortal.string, point);
                fixtures.remove(immortal.id);
            }
        }
        if (!centaurs.empty || !giants.empty, !fairies.empty || !dragons.empty ||
        !simples.empty) {
            ostream("""<h4>Immortals</h4>
                       <ul>""");
            for (coll in {centaurs, giants, fairies, dragons, simples}) {
                for (key->list in coll.asMap) {
                    if (!list.empty) {
	                    ostream("<li>");
	                    ostream(key);
	                    ostream(": at ");
	                    ostream(commaSeparatedList(list));
	                    ostream("</li>``operatingSystem.newline``");
	                }
                }
            }
            ostream("</ul>``operatingSystem.newline``");
        }
    }
    "Produce a report node on an individual immortal."
    shared actual IReportNode produceRIRSingle(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Immortal item, Point loc) {
        fixtures.remove(item.id);
        return SimpleReportNode("At ``loc``: A(n) ``item`` ``distCalculator
            .distanceString(loc)``", loc);
    }
    "Produce a report node on an individual immortal, or the intermediate-representation
     report on all immortals."
    shared actual IReportNode produceRIR(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
	        IMapNG map) {
        MutableList<[Point, IFixture]> values =
                ArrayList<[Point, IFixture]> { *fixtures.items
                    .sort(pairComparator) };
        MutableMap<String, IReportNode> simples = HashMap<String, IReportNode>();
        MutableMap<String, IReportNode> centaurs = HashMap<String, IReportNode>();
        MutableMap<String, IReportNode> giants = HashMap<String, IReportNode>();
        MutableMap<String, IReportNode> fairies = HashMap<String, IReportNode>();
        MutableMap<String, IReportNode> dragons = HashMap<String, IReportNode>();
        IReportNode separateByKind(MutableMap<String, IReportNode> mapping,
                Immortal item) {
            // For the classes we deal with here, we don't want just the kind, we want
            // the full `string`, so we use that instead of specifying HasKind and
            // using `kind`.
            if (exists node = mapping[item.string]) {
                return node;
            } else {
                IReportNode node = ListReportNode(item.string);
                mapping[item.string] = node;
                return node;
            }
        }
        for ([point, item] in values) {
            IFixture immortal = item;
            if (is Dragon immortal) {
                separateByKind(dragons, immortal)
                    .appendNode(produceRIRSingle(fixtures, map, immortal, point));
            } else if (is Fairy immortal) {
                separateByKind(fairies, immortal)
                    .appendNode(produceRIRSingle(fixtures, map, immortal, point));
            } else if (is SimpleImmortal immortal) {
                IReportNode node;
                if (exists temp = simples[immortal.plural]) {
                    node = temp;
                } else {
                    node = ListReportNode(immortal.plural);
                    simples[immortal.plural] = node;
                }
                node.appendNode(produceRIRSingle(fixtures, map, immortal, point));
            } else if (is Giant immortal) {
                separateByKind(giants, immortal)
                    .appendNode(produceRIRSingle(fixtures, map, immortal, point));
            } else if (is Centaur immortal) {
                separateByKind(centaurs, immortal)
                    .appendNode(produceRIRSingle(fixtures, map, immortal, point));
            }
        }
        IReportNode retval = SectionListReportNode(4, "Immortals");
        retval.addIfNonEmpty(*simples.items);
        IReportNode coalesce(String header, Map<String, IReportNode> mapping) {
            IReportNode retval = ListReportNode(header);
            retval.addIfNonEmpty(*mapping.items);
            return retval;
        }
        retval.addIfNonEmpty(coalesce("Dragons", dragons),
            coalesce("Fairies", fairies), coalesce("Giants", giants),
            coalesce("Centaurs", centaurs));
        if (retval.childCount == 0) {
            return emptyReportNode;
        } else {
            return retval;
        }
    }
}
