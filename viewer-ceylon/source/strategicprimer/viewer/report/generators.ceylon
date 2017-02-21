import util {
    PatientMap,
    Pair
}
import model.map {
    Point,
    IFixture,
    IMapNG,
    Player,
    River,
    DistanceComparator,
    PointFactory
}
import model.map.fixtures {
    TextFixture,
    ResourcePile,
    Implement,
    FortressMember,
    UnitMember
}
import java.lang {
    IllegalStateException,
    IllegalArgumentException
}
import java.util {
    JCollection=Collection,
    JComparator=Comparator
}
import ceylon.collection {
    MutableList,
    ArrayList,
    MutableMap,
    HashMap,
    TreeMap
}
import model.report {
    IReportNode,
    SectionListReportNode,
    SimpleReportNode,
    EmptyReportNode,
    ListReportNode,
    SectionReportNode,
    ComplexReportNode,
    SortedSectionListReportNode
}
import model.map.fixtures.mobile {
    Animal,
    IUnit,
    IWorker,
    Immortal,
    SimpleImmortal,
    Fairy,
    Centaur,
    Giant,
    Dragon
}
import model.map.fixtures.mobile.worker {
    WorkerStats { modifierString=getModifierString },
    ISkill,
    IJob
}
import ceylon.interop.java {
    CeylonCollection,
    CeylonIterable,
    CeylonSet
}
import lovelace.util.jvm {
    ceylonComparator
}
import lovelace.util.common {
    todo
}
import model.map.fixtures.towns {
    TownStatus,
    ITownFixture,
    AbstractTown,
    Village,
    Fortress
}
import model.map.fixtures.terrain {
    Forest,
    Hill,
    Oasis
}
import model.map.fixtures.explorable {
    Portal,
    ExplorableFixture,
    Battlefield,
    Cave,
    AdventureFixture
}
import ceylon.language.meta.model {
    Type
}
import ceylon.language.meta {
    type
}
import model.map.fixtures.resources {
    HarvestableFixture,
    Grove,
    CacheFixture,
    Meadow,
    Mine,
    MineralVein,
    Shrub,
    StoneDeposit
}
"An interface for a class that is both a Pair of Comparators and a Comparator of Pairs."
todo("Figure out some way to get the compiler to accept `Comparable(T,T)` and
      `Comparable(U, U)` instead of `JComparator` as arguments.")
interface PairComparator<T, U>
        satisfies Pair<JComparator<T>, JComparator<U>>
        given T satisfies Object {
    shared formal Comparison compare(Pair<T, U> first, Pair<T, U> second);
}
"A comparator for Pairs that uses provided comparators to compare first the first item in
 the pair, then the second."
class PairComparatorImpl<T, U>(JComparator<T> firstItem, JComparator<U> secondItem)
        satisfies PairComparator<T, U> given T satisfies Object {
    Comparison(T, T) firstComparator = ceylonComparator(firstItem);
    Comparison(U, U) secondComparator = ceylonComparator(secondItem);
    shared actual Comparison compare(Pair<T, U> first, Pair<T, U> second) {
        Comparison firstResult = firstComparator(first.first(), second.first());
        if (firstResult == equal) {
            return secondComparator(first.second(), second.second());
        } else {
            return firstResult;
        }
    }
    shared actual JComparator<T> first() => firstItem;

    shared actual JComparator<U> second() => secondItem;

}
"An interface for report generators."
interface IReportGenerator<T> given T satisfies IFixture {
    "A list that knows what its title should be when its contents are written to HTML."
    shared /* static */ interface HeadedList<Element> satisfies List<Element> {
        "The header text."
        shared formal String header;
    }
    "A Map that knows what its title should be when its contents are written to HTML."
    shared /* static */ interface HeadedMap<Key, Value> satisfies Map<Key, Value>
            given Key satisfies Object{
        "The header text."
        shared formal String header;
    }
    "Write a (sub-)report to a stream. All fixtures that this report references should
     be removed from the set before returning."
    shared formal void produce(
        "The set of fixtures in the map."
        PatientMap<Integer, Pair<Point, IFixture>> fixtures,
        "The map. (Needed to get terrain type for some reports.)"
        IMapNG map,
        "The player for whom the report is being produced."
        todo("Pass as constructor parameter, not here")
        Player currentPlayer,
        "The stream to write to"
        Anything(String) ostream,
        "The specific item to write about and its location; if null, write about all
         matching items."
        [T, Point]? entry = null);
    "Produce an intermediate-representation form of the report representing an item or a
     group of items. All fixtures that this report references should be removed from the
     set before returning."
    shared formal IReportNode produceRIR(
        "The set of fixtures in the map."
        PatientMap<Integer, Pair<Point, IFixture>> fixtures,
        "The map. (Needed to get terrain type for some reports.)"
        IMapNG map,
        "The player for whom the report is being produced."
        todo("Pass as constructor parameter, not here")
        Player currentPlayer,
        "The specific item to write about and its location; if null, write about all
         matching items."
        [T, Point]? entry = null);
    "Write the contents of a Map to a stream as a list, but don't write anything
     if it is empty."
    shared default void writeMap<out Key>(
            "The stream to write to."
            Anything(String) ostream,
            "The map to write. Has to be a [[HeadedMap]] so we can get its heading."
            HeadedMap<Key, Point> map,
            "The method to write each item."
            Anything(Key->Point, Anything(String)) lambda) given Key satisfies Object {
        if (!map.empty) {
            ostream("``map.header``
                     <ul>
                     ");
            for (entry in map) {
                ostream("<li>");
                lambda(entry, ostream);
                ostream("""</li>
                           """);
            }
            ostream("""</ul>
                       """);
        }
    }
}
"An abstract superclass for classes that generate reports for particular kinds of SP
 objects. It's mostly interface and helper methods, but contains a couple of bits of
 shared state."
todo("Take current player as constructor parameter instead of method parameter",
    "Make as many methods static as possible")
abstract class AbstractReportGenerator<T>(
        shared PairComparator<Point, IFixture> pairComparator)
        satisfies IReportGenerator<T> given T satisfies IFixture {
    shared DistanceComparator distCalculator;
    if (is DistanceComparator temp = pairComparator.first()) {
        distCalculator = temp;
    } else {
        distCalculator = DistanceComparator(PointFactory.invalidPoint);
    }
    deprecated shared String playerNameOrYou(Player player) {
        if (player.current) {
            return "you";
        } else {
            return player.string;
        }
    }
    "A list that produces HTML in its [[string]] attribute."
    shared class HtmlList(shared actual String header, {String*} initial = {})
            extends ArrayList<String>(0, 1.5, initial)
            satisfies IReportGenerator<T>.HeadedList<String> {
        "If there's nothing in the list, return the empty string, but otherwise produce an
         HTML list of our contents."
        shared actual String string {
            if (empty) {
                return "";
            } else {
                StringBuilder builder = StringBuilder();
                builder.append("``header``
                                <ul>
                                ");
                for (item in this) {
                    builder.append("<li>``item``</li>
                                    ");
                }
                builder.append("""</ul>
                                  """);
                return builder.string;
            }
        }
    }
    """A list of Points that produces a comma-separated list in its `string` and has a
       "header"."""
    shared class PointList(shared actual String header) extends ArrayList<Point>()
            satisfies IReportGenerator<T>.HeadedList<Point> {
        shared actual String string {
            if (empty) {
                return "";
            } else {
                StringBuilder builder = StringBuilder();
                builder.append(header);
                builder.append(" ");
                assert (exists firstItem = first);
                builder.append(firstItem.string);
                if (exists third = rest.rest.first) {
                    variable {Point*} temp = rest;
                    while (exists current = temp.first) {
                        if (temp.rest.first exists) {
                            builder.append(", ``current``");
                        } else {
                            builder.append(", and ``current``");
                        }
                        temp = temp.rest;
                    }
                } else if (exists second = rest.first) {
                    builder.append(" and ``second``");
                }
                return builder.string;
            }
        }
    }
    deprecated shared MutableList<Point> pointsListAt(String desc) =>
            PointList("``desc``: at ");
    "An implementation of HeadedMap."
    todo("Switch to Ceylon collections interfaces")
    shared class HeadedMapImpl<Key, Value>(shared actual String header,
            Comparison(Key, Key)? comparator = null, {<Key->Value>*} initial = {})
            satisfies IReportGenerator<T>.HeadedMap<Key, Value>&MutableMap<Key, Value>
            given Key satisfies Object {
        MutableMap<Key, Value> wrapped;
        if (exists comparator) {
            wrapped = TreeMap<Key, Value>(comparator, initial);
        } else {
            wrapped = HashMap<Key, Value> { entries = initial; };
        }
        shared actual Integer size => wrapped.size;
        shared actual Boolean empty => wrapped.empty;
        shared actual Integer hash => wrapped.hash;
        shared actual Boolean equals(Object that) {
            if (is Map<Key, Value> that) {
                return that.containsEvery(this) && containsEvery(that);
            } else {
                return false;
            }
        }
        shared actual void clear() => wrapped.clear();
        shared actual MutableMap<Key,Value> clone() => HeadedMapImpl<Key, Value>(header,
            comparator, initial);
        shared actual Boolean defines(Object key) => wrapped.defines(key);
        shared actual Value? get(Object key) => wrapped.get(key);
        shared actual Iterator<Key->Value> iterator() => wrapped.iterator();
        shared actual Value? put(Key key, Value item) => wrapped.put(key, item);
        shared actual Value? remove(Key key) => wrapped.remove(key);
    }
}
"A report generator for arbitrary-text notes."
class TextReportGenerator(PairComparator<Point, IFixture> comp)
        extends AbstractReportGenerator<TextFixture>(comp) {
    "Produce the part of the report dealing with arbitrary-text notes. If an individual
     note is specified, this does *not* remove it from the collection, because this
     method doesn't know the synthetic ID # that was assigned to it."
    shared actual void produce(PatientMap<Integer, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player currentPlayer, Anything(String) ostream,
            [TextFixture, Point]? entry) {
        if (exists entry) {
            TextFixture item = entry.first;
            Point loc = entry.rest.first;
            ostream("At ``loc`` ``distCalculator.distanceString(loc)``");
            if (item.turn>=0) {
                ostream(": On turn ``item.turn``");
            }
            ostream(": ``item.text``");
        } else {
            MutableList<[Point, TextFixture]> items = ArrayList<[Point, TextFixture]>();
            for (inner in fixtures.entrySet()) {
                value pair = inner.\ivalue;
                if (is TextFixture fixture = pair.second()) {
                    items.add([pair.first(), fixture]);
                    fixtures.remove(inner.key);
                }
            }
            List<[Point, TextFixture]> retItems = items.sort(
                ([Point, TextFixture] x, [Point, TextFixture] y) =>
                    x[1].turn <=> y[1].turn);
            if (!retItems.empty) {
                ostream("""<h4>Miscellaneous Notes</h4>
                           <ul>
                           """);
                for ([location, item] in retItems) {
                    ostream("<li>");
                    produce(fixtures, map, currentPlayer, ostream, [item, location]);
                    ostream("""</li>
                           """);
                }
                ostream("""</ul>
                       """);
            }
        }
    }
    "Produce the part of the report dealing with arbitrary-text note(s), in
     report intermediate representation. If an individual note is specified, this does
     *not* remove it from the collection, because this method doesn't know the synthetic
     ID # that was assigned to it."
    shared actual IReportNode produceRIR(
            PatientMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map,
            Player currentPlayer, [TextFixture, Point]? entry) {
        if (exists entry) {
            TextFixture item = entry.first;
            Point loc = entry.rest.first;
            if (item.turn>=0) {
                return SimpleReportNode("At ``loc`` ``distCalculator.distanceString(loc)`` On turn ``item
                    .turn``: ``item.text``");
            } else {
                return SimpleReportNode("At ``loc`` ``distCalculator.distanceString(loc)``: ``item.text``");
            }
        } else {
            IReportNode retval = SectionListReportNode(4, "Miscellaneous Notes");
            for (inner in fixtures.entrySet()) {
                value pair = inner.\ivalue;
                if (is TextFixture fixture = pair.second()) {
                    retval.add(produceRIR(fixtures, map, currentPlayer, [fixture,
                        pair.first()]));
                    fixtures.remove(inner.key);
                }
            }
            if (retval.childCount > 0) {
                return retval;
            } else {
                return EmptyReportNode.nullNode;
            }
        }
    }
}
"A report generator for sightings of animals."
todo("Ensure that animal-tracks' synthetic IDs are used to remove them")
class AnimalReportGenerator(PairComparator<Point, IFixture> comp)
        extends AbstractReportGenerator<Animal>(comp) {
    "Produce the sub-report about animals or an individual Animal."
    shared actual void produce(PatientMap<Integer, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player currentPlayer, Anything(String) ostream,
            [Animal, Point]? entry) {
        if (exists entry) {
            Animal item = entry.first;
            Point loc = entry.rest.first;
            ostream("At ``loc``:");
            if (item.traces) {
                ostream(" tracks or traces of");
            } else if (item.talking) {
                ostream(" talking");
            }
            ostream(" ``item.kind`` ``distCalculator.distanceString(loc)``");
        } else {
            MutableList<Pair<Point, IFixture>> values =
                    ArrayList<Pair<Point, IFixture>> { *CeylonCollection(fixtures.values())
                        .sort(pairComparator.compare) };
            MutableMap<String, MutableList<Point>> items =
                    HashMap<String, MutableList<Point>>();
            for (pair in values) {
                if (is Animal animal = pair.second()) {
                    String desc;
                    if (animal.traces) {
                        desc = "tracks or traces of ``animal.kind``";
                    } else if (animal.talking) {
                        desc = "talking ``animal.kind``";
                    } else {
                        desc = animal.kind;
                    }
                    MutableList<Point> list;
                    if (exists temp = items.get(desc)) {
                        list = temp;
                    } else {
                        list = pointsListAt(desc);
                        items.put(desc, list);
                    }
                    list.add(pair.first());
                    if (animal.id > 0) {
                        fixtures.remove(animal.id);
                    } else {
                        for (inner in fixtures.entrySet()) {
                            if (inner.\ivalue == pair) {
                                fixtures.remove(inner.key);
                            }
                        }
                    }
                }
            }
            if (!items.empty) {
                ostream("""<h4>Animal sightings or encounters</h4>
                            <ul>
                            """);
                for (key->list in items) {
                    ostream("<li>``key``: ``list.string``</li>
                         ");
                }
                ostream("""</ul>
                       """);
            }
        }
    }
    "Produce the sub-report about animals or an individual Animal."
    shared actual IReportNode produceRIR(
            PatientMap<Integer,Pair<Point,IFixture>> fixtures, IMapNG map,
            Player currentPlayer, [Animal, Point]? entry) {
        if (exists entry) {
            Animal item = entry.first;
            Point loc = entry.rest.first;
            if (item.traces) {
                return SimpleReportNode(loc, "At ``loc``: tracks or traces of ``item
                    .kind`` ``distCalculator.distanceString(loc)``");
            } else if (item.talking) {
                return SimpleReportNode(loc, "At ``loc``: talking ``item
                    .kind`` ``distCalculator.distanceString(loc)``");
            } else {
                return SimpleReportNode(loc, "At ``loc``: ``item.kind`` ``distCalculator
                    .distanceString(loc)``");
            }
        } else {
            MutableList<Pair<Point, IFixture>> values =
                    ArrayList<Pair<Point, IFixture>> { *CeylonCollection(fixtures.values())
                        .sort(pairComparator.compare) };
            MutableMap<String, IReportNode> items = HashMap<String, IReportNode>();
            for (pair in values) {
                if (is Animal animal = pair.second()) {
                    IReportNode node;
                    if (exists temp = items.get(animal.kind)) {
                        node = temp;
                    } else {
                        node = ListReportNode(animal.kind);
                        items.put(animal.kind, node);
                    }
                    node.add(produceRIR(fixtures, map, currentPlayer, [animal,
                        pair.first()]));
                    if (animal.id > 0) {
                        fixtures.remove(animal.id);
                    } else {
                        for (inner in fixtures.entrySet()) {
                            if (inner.\ivalue == pair) {
                                fixtures.remove(inner.key);
                            }
                        }
                    }
                }
            }
            if (items.empty) {
                return EmptyReportNode.nullNode;
            } else {
                IReportNode retval = SectionListReportNode(4,
                    "Animal sightings or encounters");
                for (node in items.items) {
                    retval.add(node);
                }
                return retval;
            }
        }
    }
}
"A report generator for towns."
todo("Figure out some way to report what was found at any of the towns.")
class TownReportGenerator(PairComparator<Point, IFixture> comp)
        extends AbstractReportGenerator<ITownFixture>(comp) {
    {TownStatus+} statuses = {TownStatus.active, TownStatus.abandoned, TownStatus.ruined,
        TownStatus.burned};
    "Separate towns by status."
    void separateByStatus<T>(Map<TownStatus, T> mapping,
            JCollection<Pair<Point, IFixture>> collection,
            Anything(T, Pair<Point, IFixture>) func) {
        MutableList<Pair<Point, IFixture>> list = ArrayList<Pair<Point, IFixture>>();
        for (pair in collection) {
            if (pair.second() is AbstractTown) {
                list.add(pair);
            }
        }
        for (pair in list.sort(pairComparator.compare)) {
            if (is ITownFixture item = pair.second(),
                    exists result = mapping.get(item.status)) {
                func(result, pair);
            }
        }
    }
    "Produce a report for a town, or all towns. If a single fortress or village is passed
     in, handling it is delegated to its dedicated report-generating classes. The
     all-towns report omits fortresses and villages, and is sorted in a way that I hope
     is helpful. We remove the town(s) from the set of fixtures."
    shared actual void produce(PatientMap<Integer,Pair<Point,IFixture>> fixtures,
            IMapNG map, Player currentPlayer, Anything(String) ostream,
            [ITownFixture, Point]? entry) {
        if (exists entry) {
            ITownFixture item = entry.first;
            Point loc = entry.rest.first;
            if (is Village item) {
                VillageReportGenerator(comp)
                    .produce(fixtures, map, currentPlayer, ostream, [item, loc]);
            } else if (is Fortress item) {
                FortressReportGenerator(comp)
                    .produce(fixtures, map, currentPlayer, ostream, [item, loc]);
            } else if (is AbstractTown item) {
                fixtures.remove(item.id);
                ostream("At ``loc``: ``item.name``, ");
                if (item.owner.independent) {
                    ostream(
                        "an independent ``item.size()`` ``item.status()`` ``item.kind()``");
                } else {
                    ostream(
                        "a ``item.size()`` ``item.status()`` allied with ``playerNameOrYou(
                            item.owner)``");
                }
                ostream(" ``distCalculator.distanceString(loc)``");
            } else {
                throw IllegalStateException("Unhandled ITownFixture subclass");
            }
        } else {
            HeadedMap<ITownFixture, Point>&MutableMap<ITownFixture, Point> abandoned =
                    HeadedMapImpl<ITownFixture, Point>("<h5>Abandoned Communities</h5>");
            HeadedMap<ITownFixture, Point>&MutableMap<ITownFixture, Point> active =
                    HeadedMapImpl<ITownFixture, Point>("<h5>Active Communities</h5>");
            HeadedMap<ITownFixture, Point>&MutableMap<ITownFixture, Point> burned =
                    HeadedMapImpl<ITownFixture, Point>("<h5>Burned-Out Communities</h5>");
            HeadedMap<ITownFixture, Point>&MutableMap<ITownFixture, Point> ruined =
                    HeadedMapImpl<ITownFixture, Point>("<h5>Ruined Communities</h5>");
            Map<TownStatus, MutableMap<ITownFixture, Point>> separated =
                    HashMap<TownStatus, MutableMap<ITownFixture, Point>> {
                            *{ TownStatus.abandoned->abandoned, TownStatus.active->active,
                                TownStatus.burned->burned, TownStatus.ruined->ruined }
                    };
            // separateByStatus() sorts using pairComparator, which should be by distance
            // from HQ
            separateByStatus(separated, fixtures.values(),
                (MutableMap<ITownFixture, Point> mapping, pair) {
                    assert (is ITownFixture town = pair.second());
                    mapping.put(town, pair.first());
                });
            if (separated.items.any((mapping) => !mapping.empty)) {
                ostream("""<h4>Cities, towns, and/or fortifications you know about:</h4>
                       """);
                for (mapping in {abandoned, active, burned, ruined}) {
                    writeMap(ostream, mapping,
                        (ITownFixture key->Point val, formatter) =>
                        produce(fixtures, map, currentPlayer, formatter,
                            [key, val]));
                }
            }
        }
    }
    "Produce a report for a town or towns. Handling of fortresses and villages passed as
     [[entry]] is delegated to their dedicated report-generating classes. We remove the
     town from the set of fixtures."
    shared actual IReportNode produceRIR(
            PatientMap<Integer,Pair<Point,IFixture>> fixtures, IMapNG map,
            Player currentPlayer, [ITownFixture, Point]? entry) {
        if (exists entry) {
            ITownFixture item = entry.first;
            Point loc = entry.rest.first;
            if (is Village item) {
                return VillageReportGenerator(comp)
                    .produceRIR(fixtures, map, currentPlayer, [item, loc]);
            } else if (is Fortress item) {
                return FortressReportGenerator(comp)
                    .produceRIR(fixtures, map, currentPlayer, [item, loc]);
            } else if (is AbstractTown item) {
                fixtures.remove(item.id);
                if (item.owner.independent) {
                    return SimpleReportNode(loc,
                        "At ``loc``: ``item.name``, an independent ``item.size()`` ``item
                            .status()`` ``item.kind()`` ``distCalculator
                            .distanceString(loc)``");
                } else {
                    return SimpleReportNode(loc,
                        "At ``loc``: ``item.name``, a ``item.size()`` ``item.status()`` ``item
                            .kind()`` allied with ``playerNameOrYou(item.owner)`` ``
                            distCalculator.distanceString(loc)``");
                }
            } else {
                throw IllegalStateException("Unhandled ITownFixture subclass");
            }
        } else {
            Map<TownStatus, IReportNode> separated = HashMap<TownStatus, IReportNode> {
                    *{TownStatus.abandoned -> SectionListReportNode(5, "Abandoned Communities"),
                        TownStatus.active->SectionListReportNode(5, "Active Communities"),
                        TownStatus.burned->SectionListReportNode(5, "Burned-Out Communities"),
                        TownStatus.ruined->SectionListReportNode(5, "Ruined Communities") }
            };
            separateByStatus(separated, fixtures.values(),
                (IReportNode node, pair) {
                    assert (is ITownFixture town = pair.second());
                    node.add(produceRIR(fixtures, map, currentPlayer, [town, pair.first()]));
                });
            IReportNode retval = SectionListReportNode(4,
                "Cities, towns, and/or fortifications you know about:");
            for (status in statuses) {
                if (exists node = separated.get(status)) {
                    retval.addIfNonEmpty(node);
                }
            }
            if (retval.childCount == 0) {
                return EmptyReportNode.nullNode;
            } else {
                return retval;
            }
        }
    }
}
"A report generator for fortresses."
class FortressReportGenerator(PairComparator<Point, IFixture> comp)
        extends AbstractReportGenerator<Fortress>(comp) {
    IReportGenerator<IUnit> urg = UnitReportGenerator(comp);
    IReportGenerator<FortressMember> memberReportGenerator =
            FortressMemberReportGenerator(comp);
    String terrain(IMapNG map, Point point,
            PatientMap<Integer, Pair<Point, IFixture>> fixtures) {
        StringBuilder builder = StringBuilder();
        builder.append("Surrounding terrain: ``map.getBaseTerrain(point).toXML()
            .replace("_", " ")``");
        variable Boolean unforested = true;
        if (exists forest = map.getForest(point)) {
            builder.append(", forested with ``forest.kind``");
            fixtures.remove(forest.id);
            unforested = false;
        }
        if (map.isMountainous(point)) {
            builder.append(", mountainous");
        }
        for (fixture in map.getOtherFixtures(point)) {
            if (unforested, is Forest fixture) {
                unforested = false;
                builder.append(", forested with ``fixture.kind``");
                fixtures.remove(fixture.id);
            } else if (is Hill fixture) {
                builder.append(", hilly");
                fixtures.remove(fixture.id);
            } else if (is Oasis fixture) {
                builder.append(", with a nearby oasis");
                fixtures.remove(fixture.id);
            }
        }
        return builder.string;
    }
    "Write HTML representing a collection of rivers."
    void riversToString(Anything(String) formatter, River* rivers) {
        if (rivers.contains(River.lake)) {
            formatter("""<li>There is a nearby lake.</li>
                         """);
        }
        value temp = rivers.filter((river) => river != River.lake);
        if (exists first = temp.first) {
            formatter(
                "<li>There is a river on the tile, flowing through the following borders: ");
            formatter(first.description);
            for (river in temp.rest) {
                formatter(", ``river.description``");
            }
            formatter("""</li>
                         """);
        }
    }
    "Add nodes representing rivers to a parent."
    void riversToNode(Point loc, IReportNode parent, River* rivers) {
        if (rivers.contains(River.lake)) {
            parent.add(SimpleReportNode(loc,"There is a nearby lake."));
        }
        value temp = rivers.filter((river) => river != River.lake);
        if (exists first = temp.first) {
            StringBuilder builder = StringBuilder();
            builder.append(
                "There is a river on the tile, flowing through the following borders: ");
            builder.append(first.description);
            for (river in temp.rest) {
                builder.append(", ``river.description``");
            }
            parent.add(SimpleReportNode(loc, builder.string));
        }
    }
    "Produces a sub-report on a fortress, or all fortresses. All fixtures referred to in
     this report are removed from the collection."
    shared actual void produce(PatientMap<Integer, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player currentPlayer, Anything(String) ostream,
            [Fortress, Point]? entry) {
        if (exists entry) {
            Fortress item = entry.first;
            Point loc = entry.rest.first;
            ostream("<h5>Fortress ``item.name`` belonging to ``playerNameOrYou(
                        item.owner)``</h5>
                     <ul>
                     <li>Located at ``loc`` ``distCalculator.distanceString(loc)``</li>
                     <li>``terrain(map, loc, fixtures)``</li>
                     ");
            riversToString(ostream, *CeylonIterable(map.getRivers(loc)));
            MutableList<IUnit> units = ArrayList<IUnit>();
            MutableList<Implement> equipment = ArrayList<Implement>();
            MutableMap<String, MutableList<ResourcePile>> resources =
                    HashMap<String, MutableList<ResourcePile>>();
            MutableList<FortressMember> contents = ArrayList<FortressMember>();
            for (member in item) {
                if (is IUnit member) {
                    units.add(member);
                } else if (is Implement member) {
                    equipment.add(member);
                } else if (is ResourcePile member) {
                    String kind = member.kind;
                    if (exists list = resources.get(kind)) {
                        list.add(member);
                    } else {
                        MutableList<ResourcePile> list = ArrayList<ResourcePile>();
                        resources.put(kind, list);
                        list.add(member);
                    }
                } else {
                    contents.add(member);
                }
                fixtures.remove(item.id);
            }
            if (!units.empty) {
                ostream("""Units on the tile:<ul>
                           """);
                for (unit in units) {
                    ostream("<li>");
                    urg.produce(fixtures, map, currentPlayer, ostream, [unit, loc]);
                    ostream("""</li>
                               """);
                }
                ostream("""</ul>
                           """);
            }
            if (!equipment.empty) {
                ostream("""Equipment:<ul>
                           """);
                for (implement in equipment) {
                    ostream("<li>");
                    memberReportGenerator.produce(fixtures, map, currentPlayer, ostream,
                        [implement, loc]);
                    ostream("""</li>
                               """);
                }
                ostream("""</ul>
                           """);
            }
            if (!resources.empty) {
                ostream("""Resources:<ul>
                           """);
                for (kind->list in resources) {
                    ostream("<li>``kind``
                             <ul>
                             ");
                    for (pile in list) {
                        ostream("<li>");
                        memberReportGenerator.produce(fixtures, map, currentPlayer,
                            ostream, [pile, loc]);
                        ostream("""</li>
                                   """);
                    }
                    ostream("""</ul>
                               </li>
                               """);
                }
                ostream("""</ul>
                           """);
            }
            if (!contents.empty) {
                ostream("""Other fortress contents:<ul>
                           """);
                for (member in contents) {
                    ostream("<li>");
                    memberReportGenerator.produce(fixtures, map, currentPlayer, ostream,
                        [member, loc]);
                    ostream("""</li>
                               """);
                }
                ostream("""</ul>
                           """);
            }
            fixtures.remove(item.id);
        } else {
            MutableMap<Fortress, Point> ours = HashMap<Fortress, Point>();
            MutableMap<Fortress, Point> others = HashMap<Fortress, Point>();
            MutableList<Pair<Point, IFixture>> values =
                    ArrayList<Pair<Point, IFixture>> { *CeylonCollection(fixtures.values())
                        .sort(pairComparator.compare) };
            for (pair in values) {
                if (is Fortress fort = pair.second()) {
                    if (currentPlayer == fort.owner) {
                        ours.put(fort, pair.first());
                    } else {
                        others.put(fort, pair.first());
                    }
                }
            }
            if (!ours.empty) {
                ostream("""<h4>Your fortresses in the map:</h4>
                       """);
                for (fort->loc in ours) {
                    produce(fixtures, map, currentPlayer, ostream, [fort, loc]);
                }
            }
            if (!others.empty) {
                ostream("""<h4>Other fortresses in the map:</h4>
                       """);
                for (fort->loc in others) {
                    produce(fixtures, map, currentPlayer, ostream, [fort, loc]);
                }
            }
        }
    }
    "Produces a sub-report on a fortress, or all fortresses. All fixtures referred to in
     this report are removed from the collection."
    shared actual IReportNode produceRIR(
            PatientMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map,
            Player currentPlayer, [Fortress, Point]? entry) {
        if (exists entry) {
            Fortress item = entry.first;
            Point loc = entry.rest.first;
            IReportNode retval = SectionListReportNode(loc, 5,
                "Fortress ``item.name`` belonging to ``playerNameOrYou(item.owner)``");
            retval.add(SimpleReportNode(loc, "Located at ``loc`` ``distCalculator
                .distanceString(loc)``"));
            // This is a no-op if no rivers, so avoid an if
            riversToNode(loc, retval, *CeylonIterable(map.getRivers(loc)));
            IReportNode units = ListReportNode("Units on the tile:");
            IReportNode resources = ListReportNode(loc, "Resources:");
            MutableMap<String,IReportNode> resourceKinds = HashMap<String,IReportNode>();
            IReportNode equipment = ListReportNode(loc, "Equipment:");
            IReportNode contents = ListReportNode(loc, "Other Contents of Fortress:");
            for (member in item) {
                if (is IUnit member) {
                    units.add(urg.produceRIR(fixtures, map, currentPlayer, [member, loc]));
                } else if (is Implement member) {
                    equipment.add(memberReportGenerator.produceRIR(fixtures, map,
                        currentPlayer, [member, loc]));
                } else if (is ResourcePile member) {
                    IReportNode node;
                    if (exists temp = resourceKinds.get(member.kind)) {
                        node = temp;
                    } else {
                        node = ListReportNode("``member.kind``:");
                        resourceKinds.put(member.kind, node);
                    }
                    node.add(memberReportGenerator.produceRIR(fixtures, map, currentPlayer,
                        [member, loc]));
                } else {
                    contents.add(memberReportGenerator.produceRIR(fixtures, map,
                        currentPlayer, [member, loc]));
                }
            }
            for (node in resourceKinds.items) {
                resources.addIfNonEmpty(node);
            }
            retval.addIfNonEmpty(units, resources, equipment, contents);
            fixtures.remove(item.id);
            return retval;
        } else {
            MutableList<Pair<Point, IFixture>> values =
                    ArrayList<Pair<Point, IFixture>> { *CeylonCollection(fixtures.values())
                        .sort(pairComparator.compare) };
            IReportNode foreign = SectionReportNode(4, "Foreign fortresses in the map:");
            IReportNode ours = SectionReportNode(4, "Your fortresses in the map:");
            for (pair in values) {
                if (is Fortress fort = pair.second) {
                    if (currentPlayer == fort.owner) {
                        ours.add(produceRIR(fixtures, map, currentPlayer, [fort,
                            pair.first()]));
                    } else {
                        foreign.add(produceRIR(fixtures, map, currentPlayer, [fort,
                            pair.first()]));
                    }
                }
            }
            if (ours.childCount == 0) {
                if (foreign.childCount == 0) {
                    return EmptyReportNode.nullNode;
                } else {
                    return foreign;
                }
            } else if (foreign.childCount == 0) {
                return ours;
            } else {
                IReportNode retval = ComplexReportNode();
                retval.add(ours);
                retval.add(foreign);
                return retval;
            }
        }
    }
}
"A report generator for units."
todo("Extract a WorkerReportGenerator class?")
class UnitReportGenerator(PairComparator<Point, IFixture> comp)
        extends AbstractReportGenerator<IUnit>(comp) {
    IReportGenerator<FortressMember> memberReportGenerator =
            FortressMemberReportGenerator(comp);
    IReportGenerator<Animal> animalReportGenerator = AnimalReportGenerator(comp);
    "Produce text describing the given Skills."
    String skills(ISkill* job) {
        StringBuilder builder = StringBuilder();
        if (exists first = job.first) {
            builder.append(" ``first.name`` ``first.level``");
            for (skill in job.rest) {
                builder.append(", ``skill.name`` ``skill.level``");
            }
            builder.append(")");
        }
        return builder.string;
    }
    "Produce the sub-sub-report on a worker's stats."
    String statsString(WorkerStats stats) {
        return "He or she has the following stats: ``stats.hitPoints`` / ``stats
            .maxHitPoints`` Hit Points, Strength ``modifierString(stats.strength)
        ``, Dexterity ``modifierString(stats.dexterity)``, Constitution ``
        modifierString(stats.constitution)``, Intelligence ``modifierString(stats
            .intelligence)``, Wisdom ``modifierString(stats.wisdom)``, Charisma ``
        modifierString(stats.charisma)``";
    }
    "Write the report on a Worker."
    void workerReport(IWorker worker,
            "Whether we should give details of the worker's stats and experience---true
             iff the current player owns the worker."
            Boolean details, Anything(String) ostream) {
        ostream("``worker.name``, a ``worker.race``.");
        if (details, exists stats = worker.stats) {
            ostream(
                "
                 <p>``statsString(stats)``</p>
                 ");
        }
        if (details, !CeylonIterable(worker).empty) {
            ostream(
                """(S)he has training or experience in the following Jobs (Skills):
                    <ul>
                    """);
            for (job in worker) {
                ostream("<li>``job.level`` levels in ``job
                    .name`` ``skills(*job)``</li>
                    ");
            }
            ostream("""</ul>
                       """);
        }
    }
    "Produce the report-intermediate-representation sub-sub-report on a Job."
    IReportNode produceJobRIR(IJob job, Point loc) {
        return SimpleReportNode(loc,
            "``job.level`` levels in ``job.name`` ``skills(*job)``");
    }
    "Produce the report-intermediate-representation sub-report on a worker."
    IReportNode produceWorkerRIR(Point loc, IWorker worker,
            "Whether we should give details of the worker's stats and experience---true
             only if the current player owns the worker"
            Boolean details) {
        if (details) {
            IReportNode retval = ComplexReportNode(loc,
                "``worker.name``, a ``worker.race``");
            if (exists stats = worker.stats) {
                retval.add(SimpleReportNode(statsString(stats)));
            }
            if (!CeylonIterable(worker).empty) {
                IReportNode jobs = ListReportNode(loc,
                    "(S)he has training or experience in the following Jobs (Skills):");
                for (job in worker) {
                    jobs.add(produceJobRIR(job, loc));
                }
                retval.add(jobs);
            }
            return retval;
        } else {
            return SimpleReportNode(loc, "``worker.name``, a ``worker.race``");
        }
    }
    "Produce the sub-sub-report about a unit's orders and results."
    void produceOrders(IUnit item, Anything(String) formatter) {
        if (!item.allOrders.empty || !item.allResults.empty) {
            formatter("""Orders and Results:<ul>
                         """);
            for (turn in CeylonSet(item.allOrders.keySet())
                    .union(CeylonSet(item.allResults.keySet())).map((val) => val.intValue())
                    .sort((x, y) => x <=> y)) {
                formatter("<li>Turn ``turn``:<ul>
                           ");
                if (exists orders = item.getOrders(turn)) {
                    formatter("<li>Orders: ``orders``</li>
                               ");
                }
                if (exists results = item.getResults(turn)) {
                    formatter("<li>Results: ``results``</li>
                               ");
                }
                formatter("""</ul>
                             </li>
                             """);
            }
            formatter("""</ul>
                         """);
        }
    }
    "Produce a sub-sub-report on a unit (we assume we're already in the middle of a
     paragraph or bullet point), or the part of the report on all units not covered
     as part of fortresses."
    shared actual void produce(PatientMap<Integer, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player currentPlayer, Anything(String) ostream,
            [IUnit, Point]? entry) {
        if (exists entry) {
            IUnit item = entry.first;
            Point loc = entry.rest.first;
            ostream("Unit of type ``item.kind``, named ``item.name``, ");
            if (item.owner.independent) {
                ostream("independent");
            } else {
                ostream("owned by ``playerNameOrYou(item.owner)``");
            }
            if (!CeylonIterable(item).empty) {
                MutableList<IWorker> workers = ArrayList<IWorker>();
                MutableList<Implement> equipment = ArrayList<Implement>();
                // TODO: separate out by kind?
                MutableList<ResourcePile> resources = ArrayList<ResourcePile>();
                // TODO: condense like animals somehow ("2 tame donkeys, 3 wild sheep, etc.")
                MutableList<Animal> animals = ArrayList<Animal>();
                MutableList<UnitMember> others = ArrayList<UnitMember>();
                for (member in item) {
                    if (is IWorker member) {
                        workers.add(member);
                    } else if (is Implement member) {
                        equipment.add(member);
                    } else if (is ResourcePile member) {
                        resources.add(member);
                    } else if (is Animal member) {
                        animals.add(member);
                    } else {
                        others.add(member);
                    }
                }
                ostream(""". Members of the unit:
                           <ul>
                           """);
                void produceInner<T>(String heading, List<T> collection, Anything(T) generator)
                        given T satisfies UnitMember {
                    if (!collection.empty) {
                        ostream("<li>``heading``:
                                 <ul>
                                 ");
                        for (member in collection) {
                            ostream("<li>");
                            generator(member);
                            ostream("""</li>
                                       """);
                            fixtures.remove(member.id);
                        }
                        ostream("""</ul>
                                   </li>
                                   """);
                    }
                }
                produceInner<IWorker>("Workers", workers, (worker) => workerReport(worker,
                    item.owner == currentPlayer, ostream));
                produceInner<Animal>("Animals", animals, (animal) => animalReportGenerator
                    .produce(fixtures, map, currentPlayer, ostream, [animal, loc]));
                produceInner<Implement>("Equipment", equipment, (member) => memberReportGenerator
                    .produce(fixtures, map, currentPlayer, ostream, [member, loc]));
                produceInner<ResourcePile>("Resources", resources, (member) => memberReportGenerator
                    .produce(fixtures, map, currentPlayer, ostream, [member, loc]));
                produceInner<UnitMember>("Others", others, (obj) => ostream(obj.string));
                ostream("""</ul>
                           """);
            }
            produceOrders(item, ostream);
            fixtures.remove(item.id);
        } else {
            MutableList<Pair<Point, IFixture>> values =
                    ArrayList<Pair<Point, IFixture>> { *CeylonCollection(fixtures.values())
                        .sort(pairComparator.compare) };
            HeadedMap<IUnit, Point>&MutableMap<IUnit, Point> foreign =
                    HeadedMapImpl<IUnit, Point>("<h5>Foreign Units</h5>");
            HeadedMap<IUnit, Point>&MutableMap<IUnit, Point> ours =
                    HeadedMapImpl<IUnit, Point>("<h5>Your units</h5>");
            for (pair in values) {
                if (is IUnit unit = pair.second()) {
                    if (currentPlayer == unit.owner) {
                        ours.put(unit, pair.first());
                    } else {
                        foreign.put(unit, pair.first());
                    }
                }
            }
            if (!ours.empty || !foreign.empty) {
                ostream("""<h4>Units in the map</h4>
                           <p>(Any units listed above are not described again.)</p>
                           """);
                Anything(IUnit->Point, Anything(String)) unitFormatter =
                        (IUnit key->Point val, Anything(String) formatter) {
                            formatter("At ``val````distCalculator
                                .distanceString(val)``");
                            produce(fixtures, map, currentPlayer, formatter,
                                [key, val]);
                        };
                writeMap(ostream, ours, unitFormatter);
                writeMap(ostream, foreign, unitFormatter);
            }
        }
    }
    "Produce a sub-sub-report on a unit (we assume we're already in the middle of a
     paragraph or bullet point), or the part of the report dealing with all units not
     already covered."
    shared actual IReportNode produceRIR(
            PatientMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map,
            Player currentPlayer, [IUnit, Point]? entry) {
        if (exists entry) {
            IUnit item = entry.first;
            Point loc = entry.rest.first;
            String base;
            if (item.owner.independent) {
                base = "Unit of type ``item.kind``, named ``item
                    .name``, independent.";
            } else {
                base = "Unit of type ``item.kind``, named ``item.name``, owned by ``
                    playerNameOrYou(item.owner)``.";
            }
            fixtures.remove(item.id);
            ListReportNode workers = ListReportNode("Workers:");
            ListReportNode animals = ListReportNode("Animals:");
            ListReportNode equipment = ListReportNode("Equipment:");
            ListReportNode resources = ListReportNode("Resources:");
            ListReportNode others = ListReportNode("Others:");
            IReportNode retval = ListReportNode(loc, "``base`` Members of the unit:");
            for (member in item) {
                if (is IWorker member) {
                    workers.add(produceWorkerRIR(loc, member, currentPlayer == item.owner));
                } else if (is Animal member) {
                    animals.add(animalReportGenerator
                        .produceRIR(fixtures, map, currentPlayer, [member, loc]));
                } else if (is Implement member) {
                    equipment.add(memberReportGenerator.produceRIR(fixtures, map,
                        currentPlayer, [member, loc]));
                } else if (is ResourcePile member) {
                    resources.add(memberReportGenerator.produceRIR(fixtures, map,
                        currentPlayer, [member, loc]));
                } else {
                    others.add(SimpleReportNode(loc, member.string));
                }
                fixtures.remove(member.id);
            }
            retval.addIfNonEmpty(workers, animals, equipment, resources, others);
            ListReportNode ordersNode = ListReportNode("Orders and Results:");
            for (turn in CeylonSet(item.allOrders.keySet())
                    .union(CeylonSet(item.allResults.keySet())).map((val) => val.intValue())
                    .sort((x, y) => x <=> y)) {
                ListReportNode current = ListReportNode("Turn ``turn``:");
                if (exists orders = item.getOrders(turn)) {
                    current.add(SimpleReportNode("Orders: ``orders``"));
                }
                if (exists results = item.getResults(turn)) {
                    current.add(SimpleReportNode("Results: ``results``"));
                }
                ordersNode.addIfNonEmpty(current);
            }
            retval.addIfNonEmpty(ordersNode);
            if (retval.childCount == 0) {
                return SimpleReportNode(loc, base);
            } else {
                return retval;
            }
        } else {
            MutableList<Pair<Point, IFixture>> values =
                    ArrayList<Pair<Point, IFixture>> { *CeylonCollection(fixtures.values())
                        .sort(pairComparator.compare) };
            IReportNode theirs = SectionListReportNode(5, "Foreign Units");
            IReportNode ours = SectionListReportNode(5, "Your Units");
            for (pair in values) {
                if (is IUnit unit = pair.second()) {
                    IReportNode unitNode = produceRIR(fixtures, map, currentPlayer, [unit,
                        pair.first()]);
                    unitNode.text = "At ``pair.first()``: ``unitNode.text`` ``distCalculator
                        .distanceString(pair.first())``";
                    if (currentPlayer == unit.owner) {
                        ours.add(unitNode);
                    } else {
                        theirs.add(unitNode);
                    }
                }
            }
            IReportNode textNode = SimpleReportNode(
                "(Any units reported above are not described again.)");
            if (ours.childCount == 0) {
                if (theirs.childCount == 0) {
                    return EmptyReportNode.nullNode;
                } else {
                    theirs.addAsFirst(textNode);
                    theirs.text = "Foreign units in the map:";
                    return theirs;
                }
            } else if (theirs.childCount == 0) {
                ours.addAsFirst(textNode);
                ours.text = "Your units in the map:";
                return ours;
            } else {
                IReportNode retval = SectionReportNode(4, "Units in the map:");
                retval.add(textNode);
                retval.add(ours);
                retval.add(theirs);
                return retval;
            }
        }
    }
}
"A report generator for equipment and resources."
class FortressMemberReportGenerator(PairComparator<Point, IFixture> comp)
        extends AbstractReportGenerator<FortressMember>(comp) {
    "Produces a sub-report on a resource or piece of equipment, or on all fortress
     members. All fixtures referred to in this report are removed from the collection.
     This method should probably never actually be called and do anything without an
     [[entry]], since nearly all resources will be in fortresses and should be reported
     as such, but we'll handle this properly anyway."
    shared actual void produce(PatientMap<Integer, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player  currentPlayer, Anything(String) ostream,
            [FortressMember, Point]? entry) {
        if (exists entry) {
            FortressMember item = entry.first;
            Point loc = entry.rest.first;
            if (is IUnit item) {
                UnitReportGenerator(pairComparator).produce(fixtures, map, currentPlayer,
                    ostream, [item, loc]);
            } else if (is ResourcePile item) {
                fixtures.remove(item.id);
                if (item.quantity.units.empty) {
                    ostream("A pile of ``item.quantity`` ``item.contents`` (``item.kind``)");
                } else {
                    ostream(
                        "A pile of ``item.quantity`` of ``item.contents`` (``item.kind``)");
                }
                if (item.created >= 0) {
                    ostream(" from turn ``item.created``");
                }
            } else if (is Implement item) {
                fixtures.remove(item.id);
                ostream("Equipment: ``item.kind``");
            } else {
                throw IllegalArgumentException("Unexpected FortressMember type");
            }
        } else {
            MutableList<Pair<Point, IFixture>> values =
                    ArrayList<Pair<Point, IFixture>> { *CeylonCollection(fixtures.values())
                        .sort(pairComparator.compare) };
            HeadedMap<Implement, Point>&MutableMap<Implement, Point> equipment =
                    HeadedMapImpl<Implement, Point>("<li>Equipment:",
                        comparing(byIncreasing(Implement.kind),
                            byIncreasing(Implement.id)));
            MutableMap<String, HeadedMap<ResourcePile, Point>&
                        MutableMap<ResourcePile, Point>> resources =
                    HashMap<String, HeadedMap<ResourcePile, Point>
                        &MutableMap<ResourcePile, Point>>();
            for (pair in values) {
                if (is ResourcePile resource = pair.second()) {
                    HeadedMap<ResourcePile, Point>&MutableMap<ResourcePile, Point> pileMap;
                    if (exists temp = resources.get(resource.kind)) {
                        pileMap = temp;
                    } else {
                        pileMap = HeadedMapImpl<ResourcePile, Point>(
                            "<li>``resource.kind``:",
                            comparing(byIncreasing(ResourcePile.kind),
                                byIncreasing(ResourcePile.contents),
                            // TODO: do full comparison of Quantities, as in Java version
                                byDecreasing((ResourcePile pile) => pile.quantity.units),
                                byIncreasing(ResourcePile.created),
                                byIncreasing(ResourcePile.id)));
                        resources.put(resource.kind, pileMap);
                    }
                    pileMap.put(resource, pair.first());
                    fixtures.remove(resource.id);
                } else if (is Implement implement = pair.second()) {
                    equipment.put(implement, pair.first());
                    fixtures.remove(implement.id);
                }
            }
            if (!equipment.empty || !resources.empty) {
                ostream("""<h4>Resources and Equipment</h4>
                           <ul>
                           """);
                writeMap(ostream, equipment,
                    (Implement key->Point val, formatter) =>
                    produce(fixtures, map, currentPlayer, formatter,
                        [key, val]));
                if (!resources.empty) {
                    ostream("""<li>Resources:<ul>
                                """);
                    for (kind->mapping in resources) {
                        writeMap(ostream, mapping,
                            (ResourcePile key->Point val, formatter) =>
                            produce(fixtures, map, currentPlayer, formatter, [key,
                                val]));
                        ostream("""</li>
                               """);
                    }
                    ostream("""</ul>
                               </li>
                               """);
                }
                ostream("""</ul>
                       """);
            }
        }
    }
    "Produces a sub-report on a resource or piece of equipment, or on all fortress
     members. All fixtures referred to in this report are removed from the collection.
     This method should probably never actually be called and do anything without an
     [[entry]], since nearly all resources will be in fortresses and should be reported
     as such, but we'll handle this properly anyway."
    shared actual IReportNode produceRIR(
            PatientMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map,
            Player currentPlayer, [FortressMember, Point]? entry) {
        if (exists entry) {
            FortressMember item = entry.first;
            Point loc = entry.rest.first;
            if (is IUnit item) {
                return UnitReportGenerator(pairComparator)
                    .produceRIR(fixtures, map, currentPlayer, [item, loc]);
            } else if (is ResourcePile item) {
                fixtures.remove(item.id);
                String age;
                if (item.created < 0) {
                    age = "";
                } else {
                    age = " from turn ``item.created``";
                }
                if (item.quantity.units.empty) {
                    return SimpleReportNode(
                        "A pile of ``item.quantity.number`` ``item.contents`` (``item
                            .kind``)``age``");
                } else {
                    return SimpleReportNode(
                        "A pile of ``item.quantity`` of ``item.contents`` (``item
                            .kind``)``age``");
                }
            } else if (is Implement item) {
                fixtures.remove(item.id);
                return SimpleReportNode("Equipment: ``item.kind``");
            } else {
                throw IllegalArgumentException("Unexpected FortressMember type");
            }
        } else {
            MutableList<Pair<Point, IFixture>> values =
                    ArrayList<Pair<Point, IFixture>> { *CeylonCollection(fixtures.values())
                        .sort(pairComparator.compare) };
            MutableMap<String, IReportNode> resourceKinds = HashMap<String, IReportNode>();
            IReportNode equipment = ListReportNode("Equipment:");
            for (pair in values) {
                if (is ResourcePile resource = pair.second()) {
                    String kind = resource.kind;
                    IReportNode node;
                    if (exists temp = resourceKinds.get(kind)) {
                        node = temp;
                    } else {
                        node = ListReportNode("``kind``:");
                        resourceKinds.put(kind, node);
                    }
                    node.add(produceRIR(fixtures, map, currentPlayer, [resource,
                        pair.first()]));
                } else if (is Implement implement = pair.second()) {
                    equipment.add(produceRIR(fixtures, map, currentPlayer, [implement,
                        pair.first()]));
                }
            }
            IReportNode resources = ListReportNode("Resources:");
            for (node in resourceKinds.items) {
                resources.addIfNonEmpty(node);
            }
            IReportNode retval = SectionListReportNode(4, "Resources and Equipment:");
            retval.addIfNonEmpty(resources, equipment);
            if (retval.childCount == 0) {
                return EmptyReportNode.nullNode;
            } else {
                return retval;
            }
        }
    }
}
"A report generator for caves, battlefields, adventure hooks, and portals."
todo("Use union type instead of interface, here and elsewhere")
class ExplorableReportGenerator(PairComparator<Point, IFixture> comp)
        extends AbstractReportGenerator<ExplorableFixture>(comp) {
    "Produces a more verbose sub-report on a cave, battlefield, portal, or adventure
     hook, or the report on all such."
    shared actual void produce(PatientMap<Integer, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player currentPlayer, Anything(String) ostream,
            [ExplorableFixture, Point]? entry) {
        if (exists entry) {
            ExplorableFixture item = entry.first;
            Point loc = entry.rest.first;
            if (is Cave item) {
                fixtures.remove(item.id);
                ostream("Caves beneath ``loc````distCalculator.distanceString(loc)``");
            } else if (is Battlefield item) {
                fixtures.remove(item.id);
                ostream("Signs of a long-ago battle on ``loc````distCalculator
                    .distanceString(loc)``");
            } else if (is AdventureFixture item) {
                fixtures.remove(item.id);
                ostream("``item.briefDescription`` at ``loc``: ``item
                    .fullDescription`` ``distCalculator.distanceString(loc)``");
                if (!item.owner.independent) {
                    String player;
                    if (item.owner == currentPlayer) {
                        player = "you";
                    } else {
                        player = "another player";
                    }
                    ostream(" (already investigated by ``player``)");
                }
            } else if (is Portal item) {
                fixtures.remove(item.id);
                ostream("A portal to another world at ``loc`` ``distCalculator
                    .distanceString(loc)``");
            } else {
                throw IllegalArgumentException("Unexpected ExplorableFixture type");
            }
        } else {
            MutableList<Pair<Point, IFixture>> values =
                    ArrayList<Pair<Point, IFixture>> { *CeylonCollection(fixtures.values())
                        .sort(pairComparator.compare) };
            MutableList<Point> portals = PointList("Portals to other worlds: ");
            MutableList<Point> battles = PointList(
                "Signs of long-ago battles on the following tiles:");
            MutableList<Point> caves = PointList("Caves beneath the following tiles: ");
            HeadedMap<AdventureFixture, Point>&MutableMap<AdventureFixture, Point> adventures =
                    HeadedMapImpl<AdventureFixture, Point>(
                        "<h4>Possible Adventures</h4>");
            Map<Type<IFixture>, Anything(Pair<Point, IFixture>)> collectors =
                    HashMap<Type<IFixture>, Anything(Pair<Point, IFixture>)> {
                        entries = { `Portal`->((Pair<Point, IFixture> pair) =>
                        portals.add(pair.first())),
                            `Battlefield`->((Pair<Point, IFixture> pair) =>
                            battles.add(pair.first())),
                            `Cave`->((Pair<Point, IFixture> pair) =>
                            caves.add(pair.first())),
                            `AdventureFixture`->((Pair<Point, IFixture> pair) {
                                assert (is AdventureFixture fixture = pair.second());
                                adventures.put(fixture, pair.first());
                            })};
                    };
            for (pair in values) {
                if (exists collector = collectors.get(type(pair.second()))) {
                    collector(pair);
                    fixtures.remove(pair.second().id);
                }
            }
            if (!caves.empty || !battles.empty || !portals.empty) {
                ostream("<h4>Caves, Battlefields, and Portals</h4>
                         <ul>
                         ``caves````battles````portals``</ul>
                         ");
            }
            writeMap(ostream, adventures,
                (AdventureFixture key->Point val, formatter) => produce(fixtures,
                    map, currentPlayer, formatter, [key, val]));
        }
    }
    "Produces a more verbose sub-report on a cave or battlefield, or the report section on
     all such."
    shared actual IReportNode produceRIR(
            PatientMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map,
            Player currentPlayer, [ExplorableFixture, Point]? entry) {
        if (exists entry) {
            ExplorableFixture item = entry.first;
            Point loc = entry.rest.first;
            if (is Cave item) {
                fixtures.remove(item.id);
                return SimpleReportNode(loc,
                    "Caves beneath ``loc`` ``distCalculator.distanceString(loc)``");
            } else if (is Battlefield item) {
                fixtures.remove(item.id);
                return SimpleReportNode(loc,
                    "Signs of a long-ago battle on ``loc`` ``distCalculator
                        .distanceString(loc)``");
            } else if (is AdventureFixture item) {
                fixtures.remove(item.id);
                if (item.owner.independent) {
                    return SimpleReportNode(loc,
                        "``item.briefDescription`` at ``loc``: ``item
                            .fullDescription`` ``distCalculator.distanceString(loc)``");
                } else if (currentPlayer == item.owner) {
                    return SimpleReportNode(loc,
                        "``item.briefDescription`` at ``loc``: ``item
                            .fullDescription`` ``distCalculator
                            .distanceString(loc)`` (already investigated by you)");
                } else {
                    return SimpleReportNode(loc,
                        "``item.briefDescription`` at ``loc``: ``item
                            .fullDescription`` ``distCalculator
                            .distanceString(loc)`` (already investigated by another player)");
                }
            } else if (is Portal item) {
                fixtures.remove(item.id);
                return SimpleReportNode(loc,
                    "A portal to another world at ``loc`` ``distCalculator
                        .distanceString(loc)``");
            } else {
                throw IllegalArgumentException("Unexpected ExplorableFixture type");
            }
        } else {
            MutableList<Pair<Point, IFixture>> values =
                    ArrayList<Pair<Point, IFixture>> { *CeylonCollection(fixtures.values())
                        .sort(pairComparator.compare) };
            IReportNode portals = ListReportNode("Portals");
            IReportNode battles = ListReportNode("Battlefields");
            IReportNode caves = ListReportNode("Caves");
            IReportNode adventures = SectionListReportNode(4, "Possible Adventures");
            Map<Type<IFixture>, IReportNode> nodes = HashMap<Type<IFixture>, IReportNode> {
                entries = { `Portal`->portals, `Battlefield`->battles, `Cave`->caves,
                    `AdventureFixture`->adventures };
            };
            for (pair in values) {
                if (is ExplorableFixture fixture = pair.second(),
                    exists node = nodes.get(type(fixture))) {
                    node.add(produceRIR(fixtures, map, currentPlayer, [fixture,
                        pair.first()]));
                }
            }
            IReportNode retval = SectionListReportNode(4, "Caves, Battlefields, and Portals");
            retval.addIfNonEmpty(caves, battles, portals);
            if (retval.childCount == 0) {
                if (adventures.childCount == 0) {
                    return EmptyReportNode.nullNode;
                } else {
                    return adventures;
                }
            } else if (adventures.childCount == 0) {
                return retval;
            } else {
                IReportNode real = ComplexReportNode();
                real.add(retval);
                real.add(adventures);
                return real;
            }
        }
    }
}
"A report generator for harvestable fixtures (other than caves and battlefields, which
 aren't really)."
class HarvestableReportGenerator(PairComparator<Point, IFixture> comp)
        extends AbstractReportGenerator<HarvestableFixture>(comp) {
    "Convert a Map from kinds to Points to a HtmlList."
    HeadedList<String>&MutableList<String> mapToList(Map<String, MutableList<Point>> map,
            String heading) {
        return HtmlList(heading, map.items.map(Object.string).sort(increasing));
    }
    """Produce the sub-report(s) dealing with "harvestable" fixture(s). All fixtures referred
       to in this report are to be removed from the collection. Caves and battlefields,
       though HarvestableFixtures, are presumed to have been handled already.""""
    shared actual void produce(PatientMap<Integer, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player currentPlayer, Anything(String) ostream,
            [HarvestableFixture, Point]? entry) {
        if (exists entry) {
            HarvestableFixture item = entry.first;
            Point loc = entry.rest.first;
            // TODO: convert to switch statement?
            if (is CacheFixture item) {
                        ostream("At ``loc``: ``distCalculator
                            .distanceString(loc)``A cache of ``item.kind``, containing ``item
                            .contents``");
                    } else if (is Grove item) {
                        ostream("At ``loc``: ``(item.cultivated) then "cultivated" else
                            "wild"`` ``item.kind`` ``(item.orchard) then "orchard" else
                            "grove"`` ``distCalculator.distanceString(loc)``");
                    } else if (is Meadow item) {
                        ostream("At ``loc``: ``item.status`` ``(item.cultivated) then
                            "cultivated" else "wild or abandoned"`` ``item.kind`` ``(item.field) then
                            "field" else "meadow"`` ``distCalculator.distanceString(loc)``");
                    } else if (is Mine item) {
                        ostream("At ``loc``: ``item`` ``distCalculator.distanceString(loc)``");
                    } else if (is MineralVein item) {
                        ostream("At ``loc``: An ``(item.exposed) then
                            "exposed" else "unexposed"`` vein of ``item.kind`` ``distCalculator
                            .distanceString(loc)``");
                    } else if (is Shrub item) {
                        ostream("At ``loc``: ``item.kind`` ``distCalculator
                            .distanceString(loc)``");
                    } else if (is StoneDeposit item) {
                        ostream("At ``loc``: An exposed ``item.kind`` deposit ``distCalculator
                            .distanceString(loc)``");
                    } else {
                        throw IllegalArgumentException("Unexpected HarvestableFixture type");
                    }
        } else {
            MutableList<Pair<Point, IFixture>> values =
                    ArrayList<Pair<Point, IFixture>> { *CeylonCollection(fixtures.values())
                        .sort(pairComparator.compare) };
            MutableMap<String, MutableList<Point>> stone =
                    HashMap<String, MutableList<Point>>();
            MutableMap<String, MutableList<Point>> shrubs =
                    HashMap<String, MutableList<Point>>();
            MutableMap<String, MutableList<Point>> minerals =
                    HashMap<String, MutableList<Point>>();
            HeadedMap<Mine, Point>&MutableMap<Mine, Point> mines = HeadedMapImpl<Mine, Point>("<h5>Mines</h5>",
                comparing(byIncreasing(Mine.kind),
                    byIncreasing((Mine mine) => mine.status.ordinal()), byIncreasing(Mine.id)));
            HeadedMap<Meadow, Point>&MutableMap<Meadow, Point> meadows = HeadedMapImpl<Meadow, Point>(
                "<h5>Meadows and Fields</h5>", comparing(byIncreasing(Meadow.kind),
                    byIncreasing((Meadow meadow) => meadow.status.ordinal()),
                    byIncreasing(Meadow.id)));
            HeadedMap<Grove, Point>&MutableMap<Grove, Point> groves = HeadedMapImpl<Grove, Point>(
                "<h5>Groves and Orchards</h5>", comparing(byIncreasing(Grove.kind),
                    byIncreasing(Grove.id)));
            HeadedMap<CacheFixture, Point>&MutableMap<CacheFixture, Point> caches = HeadedMapImpl<CacheFixture, Point>(
                "<h5>Caches collected by your explorers and workers:</h5>",
                comparing(byIncreasing(CacheFixture.kind),
                    byIncreasing(CacheFixture.contents), byIncreasing(CacheFixture.id)));
            for (pair in values) {
                Point point = pair.first();
                IFixture item = pair.second();
                // TODO: Use a Map by type (or at least a switch); now we have reified
                // generics we can even handle differently based on whether a List or Map
                // is in the Map!
                if (is CacheFixture item) {
                    caches.put(item, point);
                } else if (is Grove item) {
                    groves.put(item, point);
                } else if (is Meadow item) {
                    meadows.put(item, point);
                } else if (is Mine item) {
                    mines.put(item, point);
                } else if (is MineralVein item) {
                    if (exists coll = minerals.get(item.shortDesc())) {
                        coll.add(point);
                    } else {
                        value coll = pointsListAt(item.shortDesc());
                        minerals.put(item.shortDesc(), coll);
                        coll.add(point);
                    }
                    fixtures.remove(item.id);
                } else if (is Shrub item) {
                    if (exists coll = shrubs.get(item.kind)) {
                        coll.add(point);
                    } else {
                        value coll = pointsListAt(item.kind);
                        shrubs.put(item.kind, coll);
                        coll.add(point);
                    }
                    fixtures.remove(item.id);
                } else if (is StoneDeposit item) {
                    if (exists coll = stone.get(item.kind)) {
                        coll.add(point);
                    } else {
                        value coll = pointsListAt(item.kind);
                        stone.put(item.kind, coll);
                        coll.add(point);
                    }
                    fixtures.remove(item.id);
                }
            }
            // TODO: make sure that mapToList() returns a sorted list
            {HeadedList<String>+} all = {mapToList(minerals, "<h5>Mineral Deposits</h5>"),
                mapToList(stone, "<h5>Exposed Stone Deposits</h5>"),
                mapToList(shrubs, "<h5>Shrubs, Small Trees, etc.</h5>") };
            // TODO: When HeadedMap is a Ceylon interface, use { ... }.every()?
            if (!caches.empty || !groves.empty || !meadows.empty || !mines.empty ||
                    !all.every(HeadedList.empty)) {
                ostream("""<h4>Resource Sources</h4>
                       """);
                for (HeadedMap<out HarvestableFixture, Point> mapping in {caches, groves,
                    meadows, mines}) {
                    // TODO: use writeMap(), as in commented-out code
                    if (!mapping.empty) {
                        ostream("``mapping.header``
                                 <ul>
                                 ");
                        for (key->val in mapping) {
                            ostream("<li>");
                            produce(fixtures, map, currentPlayer, ostream,
                                [key, val]);
                            ostream("""</li>
                                   """);
                        }
                        ostream("""</ul>
                               """);
                    }
                    //                writeMap(ostream, mapping,
                    //                    (JMap.Entry<out HarvestableFixture, Point> entry, formatter) =>
                    //                        produce(fixtures, map, currentPlayer, entry.key, entry.\ivalue,
                    //                            formatter));
                }
                for (list in all) {
                    ostream(list.string);
                }
            }
        }
    }
    """Produce the sub-report(s) dealing with "harvestable" fixture(s). All fixtures
       referred to in this report are to be removed from the collection."""
    shared actual IReportNode produceRIR(
            PatientMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map,
            Player currentPlayer, [HarvestableFixture, Point]? entry) {
        if (exists entry) {
            HarvestableFixture item = entry.first;
            Point loc = entry.rest.first;
            SimpleReportNode retval;
            if (is CacheFixture item) {
                retval = SimpleReportNode(loc, "At ``loc``: ``distCalculator
                    .distanceString(loc)`` A cache of ``item.kind``, containing ``item
                    .contents``");
            } else if (is Grove item) {
                retval = SimpleReportNode(loc, "At ``loc``: A ``(item.cultivated) then
                    "cultivated" else "wild"`` ``item.kind`` ``(item.orchard) then "orchard"
                    else "grove"`` ``distCalculator.distanceString(loc)``");
            } else if (is Meadow item) {
                retval = SimpleReportNode(loc, "At ``loc``: A ``item.status`` ``(item
                    .cultivated) then "cultivated" else "wild or abandoned"`` ``item
                    .kind`` ``(item.field) then "field" else "meadow"`` ``distCalculator
                    .distanceString(loc)``");
            } else if (is Mine item) {
                retval = SimpleReportNode(loc, "At ``loc``: ``item`` ``distCalculator
                    .distanceString(loc)``");
            } else if (is MineralVein item) {
                retval = SimpleReportNode(loc, "At ``loc``: An ``(item.exposed) then "exposed"
                    else "unexposed"`` vein of ``item.kind`` ``distCalculator
                    .distanceString(loc)``");
            } else if (is Shrub item) {
                retval = SimpleReportNode(loc, "At ``loc``: ``item.kind`` ``distCalculator
                    .distanceString(loc)``");
            } else if (is StoneDeposit item) {
                retval = SimpleReportNode(loc, "At ``loc``: An exposed ``item
                    .kind`` deposit ``distCalculator.distanceString(loc)``");
            } else {
                throw IllegalArgumentException("Unexpected HarvestableFixture type");
            }
            fixtures.remove(item.id);
            return retval;
        } else {
            MutableList<Pair<Point, IFixture>> values =
                    ArrayList<Pair<Point, IFixture>> { *CeylonCollection(fixtures.values())
                        .sort(pairComparator.compare) };
            MutableMap<String, IReportNode> stone = HashMap<String, IReportNode>();
            MutableMap<String, IReportNode> shrubs = HashMap<String, IReportNode>();
            MutableMap<String, IReportNode> minerals = HashMap<String, IReportNode>();
            IReportNode mines = SortedSectionListReportNode(5, "Mines");
            IReportNode meadows = SortedSectionListReportNode(5, "Meadows and Fields");
            IReportNode groves = SortedSectionListReportNode(5, "Groves and Orchards");
            IReportNode caches = SortedSectionListReportNode(5,
                "Caches collected by your explorers and workers:");
            for (pair in values) {
                if (is HarvestableFixture item = pair.second()) {
                    Point loc = pair.first();
                    if (is CacheFixture item) {
                        caches.add(produceRIR(fixtures, map, currentPlayer, [item, loc]));
                    } else if (is Grove item) {
                        groves.add(produceRIR(fixtures, map, currentPlayer, [item, loc]));
                    } else if (is Meadow item) {
                        meadows.add(produceRIR(fixtures, map, currentPlayer, [item, loc]));
                    } else if (is Mine item) {
                        mines.add(produceRIR(fixtures, map, currentPlayer, [item, loc]));
                    } else if (is MineralVein item) {
                        IReportNode node;
                        if (exists temp = minerals.get(item.shortDesc())) {
                            node = temp;
                        } else {
                            node = ListReportNode(item.shortDesc());
                            minerals.put(item.shortDesc(), node);
                        }
                        node.add(produceRIR(fixtures, map, currentPlayer, [item, loc]));
                    } else if (is Shrub item) {
                        IReportNode node;
                        if (exists temp = shrubs.get(item.shortDesc())) {
                            node = temp;
                        } else {
                            node = ListReportNode(item.shortDesc());
                            shrubs.put(item.shortDesc(), node);
                        }
                        node.add(produceRIR(fixtures, map, currentPlayer, [item, loc]));
                    } else if (is StoneDeposit item) {
                        IReportNode node;
                        if (exists temp = stone.get(item.kind)) {
                            node = temp;
                        } else {
                            node = ListReportNode(item.kind);
                            stone.put(item.kind, node);
                        }
                        node.add(produceRIR(fixtures, map, currentPlayer, [item, loc]));
                    }
                }
            }
            IReportNode shrubsNode = SortedSectionListReportNode(5,
                "Shrubs, Small Trees, etc.");
            for (node in shrubs.items) {
                shrubsNode.add(node);
            }
            IReportNode mineralsNode = SortedSectionListReportNode(5, "Mineral Deposits");
            for (node in minerals.items) {
                mineralsNode.add(node);
            }
            IReportNode stoneNode = SortedSectionListReportNode(5, "Exposed Stone Deposits");
            for (node in stone.items) {
                stoneNode.add(node);
            }
            SectionReportNode retval = SectionReportNode(4, "Resource Sources");
            retval.addIfNonEmpty(caches, groves, meadows, mines, mineralsNode, stoneNode,
                shrubsNode);
            if (retval.childCount == 0) {
                return EmptyReportNode.nullNode;
            } else {
                return retval;
            }
        }
    }
}
"A report generator for Villages."
class VillageReportGenerator(PairComparator<Point, IFixture> comp)
        extends AbstractReportGenerator<Village>(comp) {
    "Produce the (very brief) report for a particular village (we're probably in the
     middle of a bulleted list, but we don't assume that), or the report on all known
     villages."
    shared actual void produce(PatientMap<Integer, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player currentPlayer, Anything(String) ostream,
            [Village, Point]? entry) {
        if (exists entry) {
            Village item = entry.first;
            Point loc = entry.rest.first;
            fixtures.remove(item.id);
            ostream("At ``loc``: ``item.name``, a(n) ``item.race`` village, ");
            if (item.owner.independent) {
                ostream("independent");
            } else if (item.owner == currentPlayer) {
                ostream("sworn to you");
            } else {
                ostream("sworn to ``item.owner.name``");
            }
            ostream(" ``distCalculator.distanceString(loc)``");
        } else {
            MutableList<Pair<Point, IFixture>> values =
                    ArrayList<Pair<Point, IFixture>> { *CeylonCollection(fixtures.values())
                        .sort(pairComparator.compare) };
            value villageComparator = comparing(byIncreasing(Village.name),
                byIncreasing(Village.race), byIncreasing(Village.id));
            // TODO: sort by distance somehow?
            HeadedMap<Village, Point>&MutableMap<Village, Point> own = HeadedMapImpl<Village, Point>(
                "<h4>Villages pledged to your service:</h4>", villageComparator);
            HeadedMap<Village, Point>&MutableMap<Village, Point> independents = HeadedMapImpl<Village, Point>(
                "<h4>Villages you think are independent:</h4>", villageComparator);
            MutableMap<Player, HeadedMap<Village, Point>
                        &MutableMap<Village, Point>> others =
                    HashMap<Player, HeadedMap<Village, Point>
                        &MutableMap<Village, Point>>();
            for (pair in values) {
                if (is Village village = pair.second()) {
                    if (village.owner == currentPlayer) {
                        own.put(village, pair.first());
                    } else if (village.owner.independent) {
                        independents.put(village, pair.first());
                    } else {
                        HeadedMap<Village, Point>&MutableMap<Village, Point> mapping;
                        if (exists temp = others.get(village.owner)) {
                            mapping = temp;
                        } else {
                            mapping = HeadedMapImpl<Village, Point>(
                                "<h5>Villages sworn to ``village.owner.name``</h5>
                                 <ul>
                                 ", villageComparator);
                            others.put(village.owner, mapping);
                        }
                        mapping.put(village, pair.first());
                    }
                }
            }
            Anything(Village->Point, Anything(String)) writer =
                    (Village key->Point val, Anything(String) formatter) =>
                    produce(fixtures, map, currentPlayer, formatter,
                        [key, val]);
            writeMap(ostream, own, writer);
            writeMap(ostream, independents, writer);
            if (!others.empty) {
                ostream("""<h4>Other villages you know about:</h4>
                       """);
                for (mapping in others.items) {
                    writeMap(ostream, mapping, writer);
                }
            }
        }
    }
    "Produce the (very brief) report for a particular village, or the report on all
     known villages."
    shared actual IReportNode produceRIR(
            PatientMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map,
            Player currentPlayer, [Village, Point]? entry) {
        if (exists entry) {
            Village item = entry.first;
            Point loc = entry.rest.first;
            fixtures.remove(item.id);
            if (item.owner.independent) {
                return SimpleReportNode(loc, "At ``loc``: ``item.name``, a(n) ``item
                    .race`` village, independent ``distCalculator.distanceString(loc)``");
            } else if (item.owner == currentPlayer) {
                return SimpleReportNode(loc, "At ``loc``: ``item.name``, a(n) ``item
                    .race`` village, sworn to you ``distCalculator.distanceString(loc)``");
            } else {
                return SimpleReportNode(loc, "At ``loc``: ``item.name``, a(n) ``item
                    .race`` village, sworn to ``item.owner`` ``distCalculator
                    .distanceString(loc)``");
            }
        } else {
            MutableList<Pair<Point, IFixture>> values =
                    ArrayList<Pair<Point, IFixture>> { *CeylonCollection(fixtures.values())
                        .sort(pairComparator.compare) };
            IReportNode own = SectionListReportNode(5, "Villages pledged to your service:");
            IReportNode independents =
                    SectionListReportNode(5, "Villages you think are independent:");
            MutableMap<Player, IReportNode> othersMap = HashMap<Player, IReportNode>();
            for (pair in values) {
                if (is Village village = pair.second()) {
                    Player owner = village.owner;
                    IReportNode parent;
                    if (owner == currentPlayer) {
                        parent = own;
                    } else if (owner.independent) {
                        parent = independents;
                    } else if (exists temp = othersMap.get(owner)) {
                        parent = temp;
                    } else {
                        parent = SectionListReportNode(6, "Villages sworn to ``owner``");
                        othersMap.put(owner, parent);
                    }
                    parent.add(produceRIR(fixtures, map, currentPlayer, [village,
                        pair.first()]));
                }
            }
            IReportNode others = SectionListReportNode(5, "Other villages you know about:");
            others.addIfNonEmpty(*othersMap.items);
            IReportNode retval = SectionReportNode(4, "Villages:");
            retval.addIfNonEmpty(own, independents, others);
            if (retval.childCount == 0) {
                return EmptyReportNode.nullNode;
            } else {
                return retval;
            }
        }
    }
}
"""A report generator for "immortals"---dragons, fairies, centaurs, and such."""
class ImmortalsReportGenerator(PairComparator<Point, IFixture> comp)
        extends AbstractReportGenerator<Immortal>(comp) {
    "Produce a report on an individual immortal, or on all immortals."
    shared actual void produce(PatientMap<Integer, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player currentPlayer, Anything(String) ostream,
            [Immortal, Point]? entry) {
        if (exists entry) {
            Immortal item = entry.first;
            Point loc = entry.rest.first;
            fixtures.remove(item.id);
            ostream("At ``loc``: A(n) ``item`` ``distCalculator
                .distanceString(loc)``");
        } else {
            MutableList<Pair<Point, IFixture>> values =
                    ArrayList<Pair<Point, IFixture>> { *CeylonCollection(fixtures.values())
                        .sort(pairComparator.compare) };
            MutableMap<Type<IFixture>, Anything(String, Point)> meta =
                    HashMap<Type<IFixture>, Anything(String, Point)>();
            MutableMap<SimpleImmortal.SimpleImmortalKind,
                        HeadedList<Point>&MutableList<Point>> simples =
                    HashMap<SimpleImmortal.SimpleImmortalKind,
                        HeadedList<Point>&MutableList<Point>>();
            for (kind in `SimpleImmortal.SimpleImmortalKind`.caseValues) {
                simples.put(kind, PointList("``kind.plural()`` at: "));
            }
            meta.put(`SimpleImmortal`,(kind, point) {
                if (exists list =
                        simples.get(SimpleImmortal.SimpleImmortalKind.parse(kind))) {
                    list.add(point);
                }
            });
            MutableMap<String, MutableList<Point>> handleComplex(Type<Immortal> type,
                    String plural = "(s)") {
                MutableMap<String, MutableList<Point>> retval =
                        HashMap<String, MutableList<Point>>();
                meta.put(type, (kind, point) {
                    if (exists list = retval.get(kind)) {
                        list.add(point);
                    } else {
                        value list = PointList("``kind````plural`` at ");
                        retval.put(kind, list);
                        list.add(point);
                    }
                });
                return retval;
            }
            MutableMap<String, MutableList<Point>> centaurs = handleComplex(`Centaur`);
            MutableMap<String, MutableList<Point>> giants = handleComplex(`Giant`);
            MutableMap<String, MutableList<Point>> fairies = handleComplex(`Fairy`, "");
            MutableMap<String, MutableList<Point>> dragons = handleComplex(`Dragon`);
            for (pair in values) {
                Point point = pair.first();
                IFixture immortal = pair.second();
                if (exists func = meta.get(type(immortal))) {
                    func(immortal.string, point);
                    fixtures.remove(immortal.id);
                }
            }
            if (!centaurs.empty || !giants.empty, !fairies.empty || !dragons.empty ||
            !simples.empty) {
                ostream("""<h4>Immortals</h4>
                       """);
                for (coll in {centaurs.items, giants.items, fairies.items, dragons.items,
                    simples.items}) {
                    for (inner in coll) {
                        ostream(inner.string);
                    }
                }
            }
        }
    }
    "Produce a report node on an individual immortal, or the intermediate-representation
     report on all immortals."
    shared actual IReportNode produceRIR(
            PatientMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map,
            Player currentPlayer, [Immortal, Point]? entry) {
        if (exists entry) {
            Immortal item = entry.first;
            Point loc = entry.rest.first;
            fixtures.remove(item.id);
            return SimpleReportNode(loc, "At ``loc``: A(n) ``item`` ``distCalculator
                .distanceString(loc)``");
        } else {
            MutableList<Pair<Point, IFixture>> values =
                    ArrayList<Pair<Point, IFixture>> { *CeylonCollection(fixtures.values())
                        .sort(pairComparator.compare) };
            MutableMap<SimpleImmortal.SimpleImmortalKind, IReportNode> simples =
                    HashMap<SimpleImmortal.SimpleImmortalKind, IReportNode>();
            MutableMap<String, IReportNode> centaurs = HashMap<String, IReportNode>();
            MutableMap<String, IReportNode> giants = HashMap<String, IReportNode>();
            MutableMap<String, IReportNode> fairies = HashMap<String, IReportNode>();
            MutableMap<String, IReportNode> dragons = HashMap<String, IReportNode>();
            IReportNode separateByKind(MutableMap<String, IReportNode> mapping, Immortal item) {
                // For the classes we deal with here, we don't want just the kind, we want the
                // full `string`, so we use that instead of specifying HasKind and using
                // `kind`.
                if (exists node = mapping.get(item.string)) {
                    return node;
                } else {
                    IReportNode node = ListReportNode(item.string);
                    mapping.put(item.string, node);
                    return node;
                }
            }
            for (pair in values) {
                Point point = pair.first();
                IFixture immortal = pair.second();
                if (is Dragon immortal) {
                    separateByKind(dragons, immortal)
                        .add(produceRIR(fixtures, map, currentPlayer, [immortal, point]));
                } else if (is Fairy immortal) {
                    separateByKind(fairies, immortal)
                        .add(produceRIR(fixtures, map, currentPlayer, [immortal, point]));
                } else if (is SimpleImmortal immortal) {
                    IReportNode node;
                    if (exists temp = simples.get(immortal.kind())) {
                        node = temp;
                    } else {
                        node = ListReportNode(immortal.kind().plural());
                        simples.put(immortal.kind(), node);
                    }
                    node.add(produceRIR(fixtures, map, currentPlayer, [immortal, point]));
                } else if (is Giant immortal) {
                    separateByKind(giants, immortal)
                        .add(produceRIR(fixtures, map, currentPlayer, [immortal, point]));
                } else if (is Centaur immortal) {
                    separateByKind(centaurs, immortal)
                        .add(produceRIR(fixtures, map, currentPlayer, [immortal, point]));
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
                return EmptyReportNode.nullNode;
            } else {
                return retval;
            }
        }
    }
}