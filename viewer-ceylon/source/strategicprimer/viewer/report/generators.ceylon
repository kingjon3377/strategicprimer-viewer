import util {
    PairComparator,
    PatientMap,
    Pair
}
import model.map {
    Point,
    IFixture,
    IMapNG,
    Player,
    River,
    HasOwner
}
import controller.map.report {
    AbstractReportGenerator,
    IReportGenerator,
    VillageReportGenerator
}
import model.map.fixtures {
    TextFixture,
    ResourcePile,
    Implement,
    FortressMember,
    UnitMember
}
import java.lang {
    JInteger = Integer,
    IllegalStateException,
    IllegalArgumentException
}
import java.util {
    Formatter,
    JCollection=Collection,
    JMap=Map,
    JTreeMap=TreeMap,
    JHashMap=HashMap,
    JComparator=Comparator,
    JSet=Set
}
import ceylon.collection {
    MutableList,
    ArrayList,
    MutableMap,
    HashMap
}
import model.report {
    IReportNode,
    SectionListReportNode,
    SimpleReportNode,
    EmptyReportNode,
    ListReportNode,
    SectionReportNode,
    ComplexReportNode
}
import model.map.fixtures.mobile {
    Animal,
    IUnit,
    IWorker
}
import model.map.fixtures.mobile.worker {
    WorkerStats { modifierString=getModifierString },
    ISkill,
    IJob
}
import ceylon.interop.java {
    CeylonList,
    CeylonCollection,
    CeylonIterable,
    CeylonSet
}
import lovelace.util.jvm {
    ceylonComparator,
    javaComparator
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
import javax.swing.tree {
    MutableTreeNode
}
"A report generator for arbitrary-text notes."
class TextReportGenerator(PairComparator<Point, IFixture> comp)
        extends AbstractReportGenerator<TextFixture>(comp) {
    "Produce the part of the report dealing ith an individual arbitrary-text note. This
     does *not* remove the fixture from the collection, because this method doesn't know
     the synthetic ID # that was assigned to it."
    shared actual void produce(PatientMap<JInteger, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player currentPlayer, TextFixture item, Point loc,
            Formatter ostream) {
        ostream.format("At %s %s", loc.string, distCalculator.distanceString(loc));
        if (item.turn >= 0) {
            ostream.format(": On turn %d", item.turn);
        }
        ostream.format(": %s", item.text);
    }
    "Produce the sub-report dealing with arbitrary-text notes."
    shared actual void produce(PatientMap<JInteger, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player currentPlayer, Formatter ostream) {
        MutableList<[Point, TextFixture]> items = ArrayList<[Point, TextFixture]>();
        for (entry in fixtures.entrySet()) {
            value pair = entry.\ivalue;
            if (is TextFixture fixture = pair.second()) {
                items.add([pair.first(), fixture]);
                fixtures.remove(entry.key);
            }
        }
        items.sort(([Point, TextFixture] x, [Point, TextFixture] y) =>
                x[1].turn <=> y[1].turn);
        if (!items.empty) {
            ostream.format("<h4>Miscellaneous Notes</h4>%n<ul>%n");
            for ([location, item] in items) {
                ostream.format("<li>");
                produce(fixtures, map, currentPlayer, item, location, ostream);
                ostream.format("</li>%n");
            }
            ostream.format("</ul>%n");
        }
    }
    "Produce the part of the report dealing ith an individual arbitrary-text note, in
     report intermediate representation. This does *not* remove the fixture from the
     collection, because this method doesn't know the synthetic ID # that was assigned to
     it."
    shared actual IReportNode produceRIR(
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures, IMapNG map,
            Player currentPlayer, TextFixture item, Point loc) {
        if (item.turn >= 0) {
            return SimpleReportNode(
                "At ``loc`` ``distCalculator.distanceString(loc)`` On turn ``item
                    .turn``: ``item.text``");
        } else {
            return SimpleReportNode(
                "At ``loc`` ``distCalculator.distanceString(loc)``: ``item.text``");
        }
    }
    "Produce the sub-report, in report-intermediate-representation, dealing with
     arbitrary-text notes."
    shared actual IReportNode produceRIR(
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures, IMapNG map,
            Player currentPlayer) {
        IReportNode retval = SectionListReportNode(4, "Miscellaneous Notes");
        for (entry in fixtures.entrySet()) {
            value pair = entry.\ivalue;
            if (is TextFixture fixture = pair.second()) {
                retval.add(produceRIR(fixtures, map, currentPlayer, fixture,
                    pair.first()));
                fixtures.remove(entry.key);
            }
        }
        if (retval.childCount > 0) {
            return retval;
        } else {
            return EmptyReportNode.nullNode;
        }
    }
}
"A report generator for sightings of animals."
class AnimalReportGenerator(PairComparator<Point, IFixture> comp)
        extends AbstractReportGenerator<Animal>(comp) {
    "Produce the sub-report about an individual Animal."
    shared actual void produce(PatientMap<JInteger, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player currentPlayer, Animal item, Point loc,
            Formatter ostream) {
        ostream.format("At ``loc``:");
        if (item.traces) {
            ostream.format(" tracks or traces of");
        } else if (item.talking) {
            ostream.format(" talking");
        }
        ostream.format(" ``item.kind`` ``distCalculator.distanceString(loc)``");
    }
    "Produce the sub-report on sightings of animals."
    shared actual void produce(PatientMap<JInteger,Pair<Point,IFixture>> fixtures,
            IMapNG map, Player currentPlayer, Formatter ostream) {
        MutableList<Pair<Point, IFixture>> values =
                ArrayList<Pair<Point, IFixture>> { *CeylonCollection(fixtures.values()) };
        values.sort(ceylonComparator(pairComparator));
        MutableMap<String, JCollection<Point>> items =
                HashMap<String, JCollection<Point>>();
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
                JCollection<Point> list;
                if (exists temp = items.get(desc)) {
                    list = temp;
                } else {
                    list = pointsListAt(desc);
                    items.put(desc, list);
                }
                list.add(pair.first());
                if (animal.id > 0) {
                    fixtures.remove(JInteger(animal.id));
                } else {
                    for (entry in fixtures.entrySet()) {
                        if (entry.\ivalue == pair) {
                            fixtures.remove(entry.key);
                        }
                    }
                }
            }
        }
        if (!items.empty) {
            ostream.format("""<h4>Animal sightings or encounters</h4>
                              <ul>
                              """);
            for (key->list in items) {
                ostream.format("<li>``key``: ``list.string``</li>%n");
            }
            ostream.format("""</ul>
                              """);
        }
    }
    "Produce the sub-report about an individual Animal."
    shared actual IReportNode produceRIR(
            PatientMap<JInteger,Pair<Point,IFixture>> fixtures, IMapNG map,
            Player currentPlayer, Animal item, Point loc) {
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
    }
    "Produce the sub-report on sightings of animals."
    shared actual IReportNode produceRIR(
            PatientMap<JInteger,Pair<Point,IFixture>> fixtures, IMapNG map,
            Player currentPlayer) {
        MutableList<Pair<Point, IFixture>> values =
                ArrayList<Pair<Point, IFixture>> { *CeylonCollection(fixtures.values()) };
        values.sort(ceylonComparator(pairComparator));
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
                node.add(produceRIR(fixtures, map, currentPlayer, animal, pair.first()));
                if (animal.id > 0) {
                    fixtures.remove(JInteger(animal.id));
                } else {
                    for (entry in fixtures.entrySet()) {
                        if (entry.\ivalue == pair) {
                            fixtures.remove(entry.key);
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
"An implementation of HeadedMap."
todo("Switch to Ceylon collections interfaces")
class HeadedMapImplTemp<K, V>(shared actual String header,
            JComparator<K>? comparator = null)
        satisfies IReportGenerator.HeadedMap<K, V> {
    JMap<K, V> wrapped;
    if (exists comparator) {
        wrapped = JTreeMap<K, V>(comparator);
    } else {
        wrapped = JHashMap<K, V>();
    }
    shared actual Integer size() => wrapped.size();
    shared actual Boolean empty => wrapped.empty;
    shared actual Boolean containsKey(Object key) => wrapped.containsKey(key);
    shared actual Boolean containsValue(Object val) => wrapped.containsValue(val);
    shared actual V? get(Object key) => wrapped.get(key);
    shared actual V? put(K? key, V? val) => wrapped.put(key, val);
    shared actual V? remove(Object key) => wrapped.remove(key);
    shared actual void putAll(JMap<out K, out V> map) => wrapped.putAll(map);
    shared actual void clear() => wrapped.clear();
    shared actual JSet<K> keySet() => wrapped.keySet();
    shared actual JCollection<V> values() => wrapped.values();
    shared actual JSet<JMap.Entry<K, V>> entrySet() => wrapped.entrySet();
    shared actual Integer hash => wrapped.hash;
    shared actual Boolean equals(Object that) {
        if (is JMap<out Anything, out Anything> that) {
            return that.entrySet() == entrySet();
        } else {
            return false;
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
        list.sort(ceylonComparator(pairComparator));
        for (pair in list) {
            if (is ITownFixture item = pair.second(),
                    exists result = mapping.get(item.status)) {
                func(result, pair);
            }
        }
    }
    "Produce a report for a town. Handling of fortresses and villages is delegated to
     their dedicated report-generating classes. We remove the town from the set of
     fixtures."
    shared actual void produce(PatientMap<JInteger,Pair<Point,IFixture>> fixtures,
            IMapNG map, Player currentPlayer, ITownFixture item, Point loc,
            Formatter ostream) {
        if (is Village item) {
            VillageReportGenerator(comp)
                .produce(fixtures, map, currentPlayer, item, loc, ostream);
        } else if (is Fortress item) {
            FortressReportGenerator(comp)
                .produce(fixtures, map, currentPlayer, item, loc, ostream);
        } else if (is AbstractTown item) {
            fixtures.remove(JInteger(item.id));
            ostream.format("At ``loc``: ``item.name``, ");
            if (item.owner.independent) {
                ostream.format(
                    "an independent ``item.size()`` ``item.status()`` ``item.kind()``");
            } else {
                ostream.format(
                    "a ``item.size()`` ``item.status()`` allied with ``playerNameOrYou(
                        item.owner)``");
            }
            ostream.format(" ``distCalculator.distanceString(loc)``");
        } else {
            throw IllegalStateException("Unhandled ITownFixture subclass");
        }
    }
    "Produce the part of the report dealing with towns, sorted in a way I hope is
     helpful. Note that while this class specifies [[ITownFixture]], this method ignores
     [[Fortress]]es and [[Village]]s. All fixures referred to in this report are removed
     from the collection."
    shared actual void produce(PatientMap<JInteger,Pair<Point,IFixture>> fixtures,
            IMapNG map, Player currentPlayer, Formatter ostream) {
        HeadedMap<ITownFixture, Point> abandoned =
                HeadedMapImplTemp<ITownFixture, Point>("<h5>Abandoned Communities</h5>");
        HeadedMap<ITownFixture, Point> active =
                HeadedMapImplTemp<ITownFixture, Point>("<h5>Active Communities</h5>");
        HeadedMap<ITownFixture, Point> burned =
                HeadedMapImplTemp<ITownFixture, Point>("<h5>Burned-Out Communities</h5>");
        HeadedMap<ITownFixture, Point> ruined =
                HeadedMapImplTemp<ITownFixture, Point>("<h5>Ruined Communities</h5>");
        Map<TownStatus, JMap<ITownFixture, Point>> separated =
                HashMap<TownStatus, JMap<ITownFixture, Point>> {
                        *{ TownStatus.abandoned->abandoned, TownStatus.active->active,
                        TownStatus.burned->burned, TownStatus.ruined->ruined }
                };
        // separateByStatus() sorts using pairComparator, which should be by distance
        // from HQ
        separateByStatus(separated, fixtures.values(),
            (JMap<ITownFixture, Point> mapping, pair) {
                assert (is ITownFixture town = pair.second());
                mapping.put(town, pair.first());
        });
        if (separated.items.any((mapping) => !mapping.empty)) {
            ostream.format(
                "<h4>Cities, towns, and/or fortifications you know about:</h4>%n");
            for (mapping in {abandoned, active, burned, ruined}) {
                writeMap(ostream, mapping,
                    (JMap.Entry<ITownFixture, Point> entry, formatter) =>
                        produce(fixtures, map, currentPlayer, entry.key, entry.\ivalue,
                            formatter));
            }
        }
    }
    "Produce a report for a town. Handling of fortresses and villages is delegated to
     their dedicated report-generating classes. We remove the town from the set of
     fixtures."
    shared actual IReportNode produceRIR(
            PatientMap<JInteger,Pair<Point,IFixture>> fixtures, IMapNG map,
            Player currentPlayer, ITownFixture item, Point loc) {
        if (is Village item) {
            return VillageReportGenerator(comp)
                .produceRIR(fixtures, map, currentPlayer, item, loc);
        } else if (is Fortress item) {
            return FortressReportGenerator(comp)
                .produceRIR(fixtures, map, currentPlayer, item, loc);
        } else if (is AbstractTown item) {
            fixtures.remove(JInteger(item.id));
            if (item.owner.independent) {
                return SimpleReportNode(loc,
                    "At ``loc``: ``item.name``, an independent ``item.size()`` ``item
                        .status()`` ``item.kind()`` ``distCalculator.distanceString(loc)``");
            } else {
                return SimpleReportNode(loc,
                    "At ``loc``: ``item.name``, a ``item.size()`` ``item.status()`` ``item
                        .kind()`` allied with ``playerNameOrYou(item.owner)`` ``
                        distCalculator.distanceString(loc)``");
            }
        } else {
            throw IllegalStateException("Unhandled ITownFixture subclass");
        }
    }
    "Produce the part of the report dealing with towns, sorted in a way I hope is
     helpful. Note that while this class specifies [[ITownFixture]], this method ignores
     [[Fortress]]es and [[Village]]s. All fixures referred to in this report are removed
     from the collection."
    shared actual IReportNode produceRIR(
            PatientMap<JInteger,Pair<Point,IFixture>> fixtures, IMapNG map,
            Player currentPlayer) {
        Map<TownStatus, IReportNode> separated = HashMap<TownStatus, IReportNode> {
            *{TownStatus.abandoned -> SectionListReportNode(5, "Abandoned Communities"),
                TownStatus.active->SectionListReportNode(5, "Active Communities"),
                TownStatus.burned->SectionListReportNode(5, "Burned-Out Communities"),
                TownStatus.ruined->SectionListReportNode(5, "Ruined Communities") }
        };
        separateByStatus(separated, fixtures.values(),
            (IReportNode node, pair) {
                assert (is ITownFixture town = pair.second());
                node.add(produceRIR(fixtures, map, currentPlayer, town, pair.first()));
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
"A report generator for fortresses."
class FortressReportGenerator(PairComparator<Point, IFixture> comp)
        extends AbstractReportGenerator<Fortress>(comp) {
    IReportGenerator<IUnit> urg = UnitReportGenerator(comp);
    IReportGenerator<FortressMember> memberReportGenerator =
            FortressMemberReportGenerator(comp);
    String terrain(IMapNG map, Point point,
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures) {
        StringBuilder builder = StringBuilder();
        builder.append("Surrounding terrain: ``map.getBaseTerrain(point).toXML()
            .replace("_", " ")``");
        variable Boolean unforested = true;
        if (exists forest = map.getForest(point)) {
            builder.append(", forested with ``forest.kind``");
            fixtures.remove(JInteger(forest.id));
            unforested = false;
        }
        if (map.isMountainous(point)) {
            builder.append(", mountainous");
        }
        for (fixture in map.getOtherFixtures(point)) {
            if (unforested, is Forest fixture) {
                unforested = false;
                builder.append(", forested with ``fixture.kind``");
                fixtures.remove(JInteger(fixture.id));
            } else if (is Hill fixture) {
                builder.append(", hilly");
                fixtures.remove(JInteger(fixture.id));
            } else if (is Oasis fixture) {
                builder.append(", with a nearby oasis");
                fixtures.remove(JInteger(fixture.id));
            }
        }
        return builder.string;
    }
    "Write HTML representing a collection of rivers."
    void riversToString(Formatter formatter, River* rivers) {
        if (rivers.contains(River.lake)) {
            formatter.format("""<li>There is a nearby lake.</li>
                                """);
        }
        value temp = rivers.filter((river) => river != River.lake);
        if (exists first = temp.first) {
            formatter.format(
                "<li>There is a river on the tile, flowing through the following borders: ");
            formatter.format(first.description);
            for (river in temp.rest) {
                formatter.format(", ``river.description``");
            }
            formatter.format("""</li>
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
    "Produces a sub-report on a fortress. All fixtures referred to in this report are
     removed from the collection."
    shared actual void produce(PatientMap<JInteger, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player currentPlayer, Fortress item, Point loc,
            Formatter ostream) {
        ostream.format("<h5>Fortress ``item.name`` belonging to ``playerNameOrYou(
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
            fixtures.remove(JInteger(item.id));
        }
        if (!units.empty) {
            ostream.format("""Units on the tile:<ul>
                               """);
            for (unit in units) {
                ostream.format("<li>");
                urg.produce(fixtures, map, currentPlayer, unit, loc, ostream);
                ostream.format("""</li>
                                 """);
            }
            ostream.format("""</ul>
                              """);
        }
        if (!equipment.empty) {
            ostream.format("""Equipment:<ul>
                              """);
            for (implement in equipment) {
                ostream.format("<li>");
                memberReportGenerator.produce(fixtures, map, currentPlayer, implement,
                    loc, ostream);
                ostream.format("""</li>
                                 """);
            }
            ostream.format("""</ul>
                              """);
        }
        if (!resources.empty) {
            ostream.format("""Resources:<ul>
                              """);
            for (kind->list in resources) {
                ostream.format("<li>``kind``
                                <ul>
                                ");
                for (pile in list) {
                    ostream.format("<li>");
                    memberReportGenerator.produce(fixtures, map, currentPlayer, pile,
                        loc, ostream);
                    ostream.format("""</li>
                                   """);
                }
                ostream.format("""</ul>
                                  </li>
                                  """);
            }
            ostream.format("""</ul>
                              """);
        }
        if (!contents.empty) {
            ostream.format("""Other fortress contents:<ul>
                              """);
            for (member in contents) {
                ostream.format("<li>");
                memberReportGenerator.produce(fixtures, map, currentPlayer, member,
                    loc, ostream);
                ostream.format("""</li>
                                 """);
            }
            ostream.format("""</ul>
                              """);
        }
        fixtures.remove(JInteger(item.id));
    }
    "Produce the sub-report on fortresses. All fixtures referred to in this report are
     removed from the collection."
    shared actual void produce(PatientMap<JInteger, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player currentPlayer, Formatter ostream) {
        MutableMap<Fortress, Point> ours = HashMap<Fortress, Point>();
        MutableMap<Fortress, Point> others = HashMap<Fortress, Point>();
        MutableList<Pair<Point, IFixture>> values =
                ArrayList<Pair<Point, IFixture>>{*CeylonCollection(fixtures.values())};
        values.sort(ceylonComparator(pairComparator));
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
            ostream.format("""<h4>Your fortresses in the map:</h4>
                              """);
            for (fort->loc in ours) {
                produce(fixtures, map, currentPlayer, fort, loc, ostream);
            }
        }
        if (!others.empty) {
            ostream.format("""<h4>Other fortresses in the map:</h4>
                              """);
            for (fort->loc in others) {
                produce(fixtures, map, currentPlayer, fort, loc, ostream);
            }
        }
    }
    "Produces a sub-report on a fortress. All fixtures referred to in this report are
     removed from the collection."
    shared actual IReportNode produceRIR(
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures, IMapNG map,
            Player currentPlayer, Fortress item, Point loc) {
        IReportNode retval = SectionListReportNode(loc, 5,
            "Fortress ``item.name`` belonging to ``playerNameOrYou(item.owner)``");
        retval.add(SimpleReportNode(loc, "Located at ``loc`` ``distCalculator
            .distanceString(loc)``"));
        // This is a no-op if no rivers, so avoid an if
        riversToNode(loc, retval, *CeylonIterable(map.getRivers(loc)));
        IReportNode units = ListReportNode("Units on the tile:");
        IReportNode resources = ListReportNode(loc, "Resources:");
        MutableMap<String, IReportNode> resourceKinds = HashMap<String, IReportNode>();
        IReportNode equipment = ListReportNode(loc, "Equipment:");
        IReportNode contents = ListReportNode(loc, "Other Contents of Fortress:");
        for (member in item) {
            if (is IUnit member) {
                units.add(urg.produceRIR(fixtures, map, currentPlayer, member, loc));
            } else if (is Implement member) {
                equipment.add(memberReportGenerator.produceRIR(fixtures, map,
                    currentPlayer, member, loc));
            } else if (is ResourcePile member) {
                IReportNode node;
                if (exists temp = resourceKinds.get(member.kind)) {
                    node = temp;
                } else {
                    node = ListReportNode("``member.kind``:");
                    resourceKinds.put(member.kind, node);
                }
                node.add(memberReportGenerator.produceRIR(fixtures, map, currentPlayer,
                    member, loc));
            } else {
                contents.add(memberReportGenerator.produceRIR(fixtures, map,
                    currentPlayer, member, loc));
            }
        }
        for (node in resourceKinds.items) {
            resources.addIfNonEmpty(node);
        }
        retval.addIfNonEmpty(units, resources, equipment, contents);
        fixtures.remove(JInteger(item.id));
        return retval;
    }
    "Produce the sub-report on fortresses. All fixtures referred to in this report are
     removed from the collection."
    shared actual IReportNode produceRIR(
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures, IMapNG map,
            Player currentPlayer) {
        MutableList<Pair<Point, IFixture>> values =
                ArrayList<Pair<Point, IFixture>>{*CeylonCollection(fixtures.values())};
        values.sort(ceylonComparator(pairComparator));
        IReportNode foreign = SectionReportNode(4, "Foreign fortresses in the map:");
        IReportNode ours = SectionReportNode(4, "Your fortresses in the map:");
        for (pair in values) {
            if (is Fortress fort = pair.second) {
                if (currentPlayer == fort.owner) {
                    ours.add(produceRIR(fixtures, map, currentPlayer, fort,
                        pair.first()));
                } else {
                    foreign.add(produceRIR(fixtures, map, currentPlayer, fort,
                        pair.first()));
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
"A report generator for units."
todo("Extract a WorkerReportGenerator class?")
class UnitReportGenerator(PairComparator<Point, IFixture> comp)
        extends AbstractReportGenerator<IUnit>(comp) {
    IReportGenerator<FortressMember> memberReportGenerator =
            FortressMemberReportGenerator(comp);
    IReportGenerator<Animal> animalReportGenerator = AnimalReportGenerator(comp);
    "Write text describing the given Skills to the given Formatter."
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
            Boolean details, Formatter ostream) {
        ostream.format("``worker.name``, a ``worker.race``.");
        if (details, exists stats = worker.stats) {
            ostream.format(
                "
                 <p>``statsString(stats)``</p>
                 ");
        }
        if (details, !CeylonIterable(worker).empty) {
            ostream.format(
                """(S)he has training or experience in the following Jobs (Skills):
                    <ul>
                    """);
            for (job in worker) {
                ostream.format("<li>``job.level`` levels in ``job
                    .name`` ``skills(*job)``</li>
                    ");
            }
            ostream.format("""</ul>
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
    void produceOrders(IUnit item, Formatter formatter) {
        if (!item.allOrders.empty || !item.allResults.empty) {
            formatter.format("""Orders and Results:<ul>
                                """);
            for (turn in CeylonSet(item.allOrders.keySet())
                    .union(CeylonSet(item.allResults.keySet())).map((val) => val.intValue())
                    .sort((x, y) => x <=> y)) {
                formatter.format("<li>Turn ``turn``:<ul>
                                  ");
                if (exists orders = item.getOrders(turn)) {
                    formatter.format("<li>Orders: ``orders``</li>
                                      ");
                }
                if (exists results = item.getResults(turn)) {
                    formatter.format("<li>Results: ``results``</li>
                                      ");
                }
                formatter.format("""</ul>
                                    </li>
                                    """);
            }
            formatter.format("""</ul>
                                """);
        }
    }
    "Produce a sub-sub-report on a unit. We assume we're already in the middle of a
     paragraph or bullet point."
    shared actual void produce(PatientMap<JInteger, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player currentPlayer, IUnit item, Point loc, Formatter ostream) {
        ostream.format("Unit of type ``item.kind``, named ``item.name``, ");
        if (item.owner.independent) {
            ostream.format("independent");
        } else {
            ostream.format("owned by ``playerNameOrYou(item.owner)``");
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
            ostream.format(""". Members of the unit:
                              <ul>
                              """);
            void produceInner<T>(String heading, List<T> collection, Anything(T) generator)
                    given T satisfies UnitMember {
                if (!collection.empty) {
                    ostream.format("<li>``heading``:
                                    <ul>
                                    ");
                    for (member in collection) {
                        ostream.format("<li>");
                        generator(member);
                        ostream.format("""</li>
                                            """);
                        fixtures.remove(JInteger(member.id));
                    }
                    ostream.format("""</ul>
                                      </li>
                                      """);
                }
            }
            produceInner<IWorker>("Workers", workers, (worker) => workerReport(worker,
                item.owner == currentPlayer, ostream));
            produceInner<Animal>("Animals", animals, (animal) => animalReportGenerator
                .produce(fixtures, map, currentPlayer, animal, loc, ostream));
            produceInner<Implement>("Equipment", equipment, (member) => memberReportGenerator
                .produce(fixtures, map, currentPlayer, member, loc, ostream));
            produceInner<ResourcePile>("Resources", resources, (member) => memberReportGenerator
                .produce(fixtures, map, currentPlayer, member, loc, ostream));
            produceInner<UnitMember>("Others", others, (obj) => ostream.format(obj.string));
            ostream.format("""</ul>
                              """);
        }
        produceOrders(item, ostream);
        fixtures.remove(JInteger(item.id));
    }
    "Produce a sub-sub-report on a unit. We assume we're already in the middle of a
     paragraph or bullet point."
    shared actual IReportNode produceRIR(
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures, IMapNG map,
            Player currentPlayer, IUnit item, Point loc) {
        String base;
        if (item.owner.independent) {
            base = "Unit of type ``item.kind``, named ``item
                .name``, independent.";
        } else {
            base = "Unit of type ``item.kind``, named ``item.name``, owned by ``
                playerNameOrYou(item.owner)``.";
        }
        fixtures.remove(JInteger(item.id));
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
                    .produceRIR(fixtures, map, currentPlayer, member, loc));
            } else if (is Implement member) {
                equipment.add(memberReportGenerator.produceRIR(fixtures, map,
                    currentPlayer, member, loc));
            } else if (is ResourcePile member) {
                resources.add(memberReportGenerator.produceRIR(fixtures, map,
                    currentPlayer, member, loc));
            } else {
                others.add(SimpleReportNode(loc, member.string));
            }
            fixtures.remove(JInteger(member.id));
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
    }
    "Produce the part of the report dealing with units. All fixtures referred to in this
     report are removed from the collection."
    shared actual void produce(PatientMap<JInteger, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player currentPlayer, Formatter ostream) {
        MutableList<Pair<Point, IFixture>> values =
                ArrayList { *CeylonIterable(fixtures.values()) };
        values.sort(ceylonComparator(pairComparator));
        HeadedMap<IUnit, Point> foreign =
                HeadedMapImplTemp<IUnit, Point>("<h5>Foreign Units</h5>");
        HeadedMap<IUnit, Point> ours =
                HeadedMapImplTemp<IUnit, Point>("<h5>Your units</h5>");
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
            ostream.format("""<h4>Units in the map</h4>
                              <p>(Any units listed above are not described again.)</p>
                              """);
            Anything(JMap<IUnit, Point>.Entry<IUnit, Point>, Formatter) unitFormatter =
                    (JMap<IUnit, Point>.Entry<IUnit, Point> entry, Formatter formatter) {
                        formatter.format("At ``entry.\ivalue````distCalculator
                            .distanceString(entry.\ivalue)``");
                        produce(fixtures, map, currentPlayer, entry.key, entry.\ivalue,
                            formatter);
            };
            writeMap(ostream, ours, unitFormatter);
            writeMap(ostream, foreign, unitFormatter);
        }
    }
    "Produce the part of the report dealing with units. All fixtures referred to in this
     report are removed from the collection."
    shared actual IReportNode produceRIR(
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures, IMapNG map,
            Player currentPlayer) {
        MutableList<Pair<Point, IFixture>> values =
                ArrayList { *CeylonIterable(fixtures.values()) };
        values.sort(ceylonComparator(pairComparator));
        IReportNode theirs = SectionListReportNode(5, "Foreign Units");
        IReportNode ours = SectionListReportNode(5, "Your Units");
        for (pair in values) {
            if (is IUnit unit = pair.second()) {
                IReportNode unitNode = produceRIR(fixtures, map, currentPlayer, unit,
                    pair.first());
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
"A report generator for equipment and resources."
class FortressMemberReportGenerator(PairComparator<Point, IFixture> comp)
        extends AbstractReportGenerator<FortressMember>(comp) {
    "Produces a sub-report on a resource or piece of equipment."
    shared actual void produce(PatientMap<JInteger, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player  currentPlayer, FortressMember item, Point loc,
            Formatter ostream) {
        if (is IUnit item) {
            UnitReportGenerator(pairComparator).produce(fixtures, map, currentPlayer, item,
                loc, ostream);
        } else if (is ResourcePile item) {
            fixtures.remove(JInteger(item.id));
            if (item.quantity.units.empty) {
                ostream.format(
                    "A pile of ``item.quantity`` ``item.contents`` (``item.kind``)");
            } else {
                ostream.format(
                    "A pile of ``item.quantity`` of ``item.contents`` (``item.kind``)");
            }
            if (item.created >= 0) {
                ostream.format(" from turn ``item.created``");
            }
        } else if (is Implement item) {
            fixtures.remove(JInteger(item.id));
            ostream.format("Equipment: ``item.kind``");
        } else {
            throw IllegalArgumentException("Unexpected FortressMember type");
        }
    }
    "Produce the sub-report on equipment and resources. All fixtures referred to in this
     report are removed from the collection. This method should probably never actually be
     called and do anything, since nearly all resources will be in fortresses and should
     be reported as such, but we'll handle this properly anyway."
    shared actual void produce(PatientMap<JInteger, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player currentPlayer, Formatter ostream) {
        MutableList<Pair<Point, IFixture>> values =
                ArrayList { *CeylonIterable(fixtures.values()) };
        values.sort(ceylonComparator(pairComparator));
        HeadedMap<Implement, Point> equipment =
            HeadedMapImplTemp<Implement, Point>("<li>Equipment:",
                javaComparator(comparing(byIncreasing(Implement.kind),
                    byIncreasing(Implement.id))));
        MutableMap<String, HeadedMap<ResourcePile, Point>> resources =
                HashMap<String, HeadedMap<ResourcePile, Point>>();
        for (pair in values) {
            if (is ResourcePile resource = pair.second()) {
                HeadedMap<ResourcePile, Point> pileMap;
                if (exists temp = resources.get(resource.kind)) {
                    pileMap = temp;
                } else {
                    pileMap = HeadedMapImplTemp<ResourcePile, Point>(
                        "<li>``resource.kind``:",
                        javaComparator(comparing(byIncreasing(ResourcePile.kind),
                            byIncreasing(ResourcePile.contents),
                            // TODO: do full comparison of Quantities, as in Java version
                            byDecreasing((ResourcePile pile) => pile.quantity.units),
                            byIncreasing(ResourcePile.created),
                            byIncreasing(ResourcePile.id))));
                    resources.put(resource.kind, pileMap);
                }
                pileMap.put(resource, pair.first());
                fixtures.remove(JInteger(resource.id));
            } else if (is Implement implement = pair.second()) {
                equipment.put(implement, pair.first());
                fixtures.remove(JInteger(implement.id));
            }
        }
        if (!equipment.empty || !resources.empty) {
            ostream.format("""<h4>Resources and Equipment</h4>
                              <ul>
                              """);
            writeMap(ostream, equipment,
                (JMap.Entry<Implement, Point> entry, formatter) =>
                    produce(fixtures, map, currentPlayer, entry.key, entry.\ivalue,
                        formatter));
            if (!resources.empty) {
                ostream.format("""<li>Resources:<ul>
                                  """");
                for (kind->mapping in resources) {
                    writeMap(ostream, mapping,
                        (JMap.Entry<ResourcePile, Point> entry, formatter) =>
                            produce(fixtures, map, currentPlayer, entry.key,
                                entry.\ivalue, formatter));
                    ostream.format("""</li>
                                      """);
                }
                ostream.format("""</ul>
                                  </li>
                                  """);
            }
            ostream.format("""</ul>
                              """);
        }
    }
    "Produces a sub-report on a resource or piece of equipment."
    shared actual IReportNode produceRIR(
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures, IMapNG map,
            Player currentPlayer, FortressMember item, Point loc) {
        if (is IUnit item) {
            return UnitReportGenerator(pairComparator)
                .produceRIR(fixtures, map, currentPlayer, item, loc);
        } else if (is ResourcePile item) {
            fixtures.remove(JInteger(item.id));
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
            fixtures.remove(JInteger(item.id));
            return SimpleReportNode("Equipment: ``item.kind``");
        } else {
            throw IllegalArgumentException("Unexpected FortressMember type");
        }
    }
    "Produce the sub-report on equipment and resources. All fixtures referred to in this
     report are removed from the collection. This method should probably never actually be
     called and do anything, since nearly all resources will be in fortresses and should
     be reported as such, but we'll handle this properly anyway."
    shared actual IReportNode produceRIR(
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures, IMapNG map,
            Player currentPlayer) {
        MutableList<Pair<Point, IFixture>> values =
                ArrayList { *CeylonIterable(fixtures.values()) };
        values.sort(ceylonComparator(pairComparator));
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
                node.add(produceRIR(fixtures, map, currentPlayer, resource,
                    pair.first()));
            } else if (is Implement implement = pair.second()) {
                equipment.add(produceRIR(fixtures, map, currentPlayer, implement,
                    pair.first()));
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