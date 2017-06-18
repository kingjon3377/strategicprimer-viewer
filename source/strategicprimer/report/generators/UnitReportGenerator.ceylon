import ceylon.collection {
    MutableList,
    MutableMap,
    ArrayList,
    HashMap
}

import lovelace.util.common {
    DelayedRemovalMap
}

import strategicprimer.model.map {
    Point,
    Player,
    IFixture,
    IMapNG,
    invalidPoint,
    MapDimensions
}
import strategicprimer.model.map.fixtures {
    ResourcePile,
    FortressMember,
    Implement,
    UnitMember
}
import strategicprimer.model.map.fixtures.mobile {
    Animal,
    IWorker,
    IUnit
}
import strategicprimer.report {
    IReportNode
}
import strategicprimer.report.nodes {
    ListReportNode,
    SimpleReportNode,
    SectionListReportNode,
    emptyReportNode,
    SectionReportNode
}
"A report generator for units."
shared class UnitReportGenerator(Comparison([Point, IFixture], [Point, IFixture]) comp,
        Player currentPlayer, MapDimensions dimensions, Integer currentTurn,
        Point hq = invalidPoint)
        extends AbstractReportGenerator<IUnit>(comp, dimensions, hq) {
    IReportGenerator<FortressMember> memberReportGenerator =
            FortressMemberReportGenerator(comp, currentPlayer, dimensions, currentTurn,
                hq);
    IReportGenerator<Animal> animalReportGenerator =
            AnimalReportGenerator(comp, dimensions, currentTurn, hq);
    IReportGenerator<IWorker> ourWorkerReportGenerator = WorkerReportGenerator(comp,
        true, dimensions, hq);
    IReportGenerator<IWorker> otherWorkerReportGenerator = WorkerReportGenerator(comp,
        false, dimensions, hq);
    "Produce the sub-sub-report about a unit's orders and results."
    void produceOrders(IUnit item, Anything(String) formatter) {
        if (!item.allOrders.empty || !item.allResults.empty) {
            formatter("""Orders and Results:<ul>
                         """);
            for (turn in sort(set { *item.allOrders.keys }
                    .union(set { *item.allResults.keys }))) {
                formatter("<li>Turn ``turn``:<ul>
                           ");
                String orders = item.getOrders(turn);
                if (!orders.empty) {
                    formatter("<li>Orders: ``orders``</li>
                               ");
                }
                String results = item.getResults(turn);
                if (!results.empty) {
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
    shared actual void produce(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, [IUnit, Point]? entry) {
        if (exists entry) {
            IUnit item = entry.first;
            Point loc = entry.rest.first;
            ostream("Unit of type ``item.kind``, named ``item.name``, ");
            if (item.owner.independent) {
                ostream("independent");
            } else if (item.owner == currentPlayer) {
                ostream("owned by you");
            } else {
                ostream("owned by ``item.owner``");
            }
            if (!item.empty) {
                MutableList<IWorker> workers = ArrayList<IWorker>();
                MutableList<Implement> equipment = ArrayList<Implement>();
                MutableMap<String, MutableList<ResourcePile>> resources =
                        HashMap<String, MutableList<ResourcePile>>();
                // TODO: condense like animals somehow ("2 tame donkeys, 3 wild sheep")
                MutableList<Animal> animals = ArrayList<Animal>();
                MutableList<UnitMember> others = ArrayList<UnitMember>();
                for (member in item) {
                    if (is IWorker member) {
                        workers.add(member);
                    } else if (is Implement member) {
                        equipment.add(member);
                    } else if (is ResourcePile member) {
                        MutableList<ResourcePile> list;
                        if (exists temp = resources.get(member.kind)) {
                            list = temp;
                        } else {
                            list = ArrayList<ResourcePile>();
                            resources.put(member.kind, list);
                        }
                        list.add(member);
                    } else if (is Animal member) {
                        animals.add(member);
                    } else {
                        others.add(member);
                    }
                }
                ostream(""". Members of the unit:
                           <ul>
                           """);
                void produceInner<T>(String heading, List<T> collection,
                        Anything(T) generator) given T satisfies UnitMember {
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
                IReportGenerator<IWorker> workerReportGenerator;
                if (item.owner == currentPlayer) {
                    workerReportGenerator = ourWorkerReportGenerator;
                } else {
                    workerReportGenerator = otherWorkerReportGenerator;
                }
                produceInner<IWorker>("Workers", workers, (worker) =>
                workerReportGenerator.produce(fixtures, map, ostream, [worker, loc]));
                produceInner<Animal>("Animals", animals, (animal) => animalReportGenerator
                    .produce(fixtures, map, ostream, [animal, loc]));
                produceInner<Implement>("Equipment", equipment, (member) =>
                memberReportGenerator.produce(fixtures, map, ostream, [member, loc]));
                if (!resources.empty) {
                    ostream("<li>Resources:
                             <ul>
                             ");
                    for (kind->list in resources) {
                        produceInner(kind, list, (ResourcePile member) =>
                            memberReportGenerator.produce(fixtures, map, ostream,
                                [member, loc]));
                    }
                    ostream("""</ul>
                               </li>
                               """);
                }
                produceInner<UnitMember>("Others", others, (obj) => ostream(obj.string));
                ostream("""</ul>
                           """);
            }
            produceOrders(item, ostream);
            fixtures.remove(item.id);
        } else {
            MutableList<[Point, IFixture]> values =
                    ArrayList<[Point, IFixture]> { *fixtures.items
                        .sort(pairComparator) };
            HeadedMap<IUnit, Point>&MutableMap<IUnit, Point> foreign =
                    HeadedMapImpl<IUnit, Point>("<h5>Foreign Units</h5>");
            HeadedMap<IUnit, Point>&MutableMap<IUnit, Point> ours =
                    HeadedMapImpl<IUnit, Point>("<h5>Your units</h5>");
            for ([loc, item] in values) {
                if (is IUnit unit = item) {
                    if (currentPlayer == unit.owner) {
                        ours[unit] = loc;
                    } else {
                        foreign[unit] = loc;
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
                            produce(fixtures, map, formatter, [key, val]);
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
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, [IUnit, Point]? entry) {
        if (exists entry) {
            IUnit item = entry.first;
            Point loc = entry.rest.first;
            String base;
            if (item.owner.independent) {
                base = "Unit of type ``item.kind``, named ``item
                    .name``, independent.";
            } else if (item.owner == currentPlayer) {
                base = "Unit of type ``item.kind``, named ``item.name``, owned by you.";
            } else {
                base = "Unit of type ``item.kind``, named ``item.name``, owned by ``
                    item.owner``.";
            }
            fixtures.remove(item.id);
            ListReportNode workers = ListReportNode("Workers:");
            ListReportNode animals = ListReportNode("Animals:");
            ListReportNode equipment = ListReportNode("Equipment:");
            ListReportNode resources = ListReportNode("Resources:");
            ListReportNode others = ListReportNode("Others:");
            MutableMap<String, IReportNode> resourcesMap = HashMap<String, IReportNode>();
            IReportNode retval = ListReportNode("``base`` Members of the unit:", loc);
            IReportGenerator<IWorker> workerReportGenerator;
            if (item.owner == currentPlayer) {
                workerReportGenerator = ourWorkerReportGenerator;
            } else {
                workerReportGenerator = otherWorkerReportGenerator;
            }
            for (member in item) {
                if (is IWorker member) {
                    workers.add(workerReportGenerator.produceRIR(fixtures, map, [member,
                        loc]));
                } else if (is Animal member) {
                    animals.add(animalReportGenerator
                        .produceRIR(fixtures, map, [member, loc]));
                } else if (is Implement member) {
                    equipment.add(memberReportGenerator.produceRIR(fixtures, map, [member,
                        loc]));
                } else if (is ResourcePile member) {
                    IReportNode resourceNode;
                    if (exists temp = resourcesMap.get(member.kind)) {
                        resourceNode = temp;
                    } else {
                        resourceNode = ListReportNode("``member.kind``:");
                        resourcesMap.put(member.kind, resourceNode);
                        resources.add(resourceNode);
                    }
                    resourceNode.appendNode(memberReportGenerator.produceRIR(fixtures, map,
                        [member, loc]));
                } else {
                    others.add(SimpleReportNode(member.string, loc));
                }
                fixtures.remove(member.id);
            }
            retval.addIfNonEmpty(workers, animals, equipment, resources, others);
            ListReportNode ordersNode = ListReportNode("Orders and Results:");
            for (turn in sort(set { *item.allOrders.keys}
                    .union(set {*item.allResults.keys}))) {
                ListReportNode current = ListReportNode("Turn ``turn``:");
                String orders = item.getOrders(turn);
                if (!orders.empty) {
                    current.add(SimpleReportNode("Orders: ``orders``"));
                }
                String results = item.getResults(turn);
                if (!results.empty) {
                    current.add(SimpleReportNode("Results: ``results``"));
                }
                ordersNode.addIfNonEmpty(current);
            }
            retval.addIfNonEmpty(ordersNode);
            if (retval.childCount == 0) {
                return SimpleReportNode(base, loc);
            } else {
                return retval;
            }
        } else {
            MutableList<[Point, IFixture]> values =
                    ArrayList<[Point, IFixture]> { *fixtures.items
                        .sort(pairComparator) };
            IReportNode theirs = SectionListReportNode(5, "Foreign Units");
            IReportNode ours = SectionListReportNode(5, "Your Units");
            for ([loc, item] in values) {
                if (is IUnit unit = item) {
                    IReportNode unitNode = produceRIR(fixtures, map, [unit,
                        loc]);
                    unitNode.text = "At ``loc``: ``unitNode.text`` ``distCalculator
                        .distanceString(loc)``";
                    if (currentPlayer == unit.owner) {
                        ours.appendNode(unitNode);
                    } else {
                        theirs.appendNode(unitNode);
                    }
                }
            }
            IReportNode textNode = SimpleReportNode(
                "(Any units reported above are not described again.)");
            if (ours.childCount == 0) {
                if (theirs.childCount == 0) {
                    return emptyReportNode;
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
                retval.appendNode(textNode);
                retval.appendNode(ours);
                retval.appendNode(theirs);
                return retval;
            }
        }
    }
}
