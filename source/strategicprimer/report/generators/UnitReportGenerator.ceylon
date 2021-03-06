import ceylon.collection {
    MutableList,
    ArrayList
}

import lovelace.util.common {
    DelayedRemovalMap
}

import strategicprimer.model.common.map {
    Player,
    IFixture,
    Point,
    MapDimensions,
    IMapNG
}
import strategicprimer.model.common.map.fixtures {
    IResourcePile,
    FortressMember,
    Implement,
    UnitMember
}
import strategicprimer.model.common.map.fixtures.mobile {
    IWorker,
    IUnit,
    Animal,
    AnimalOrTracks
}
import com.vasileff.ceylon.structures {
    MutableMultimap,
    HashMultimap
}

"A report generator for units."
shared class UnitReportGenerator(Comparison([Point, IFixture], [Point, IFixture]) comp,
        Player currentPlayer, MapDimensions dimensions, Integer currentTurn,
        Point? hq = null)
        extends AbstractReportGenerator<IUnit>(comp, dimensions, hq) {
    IReportGenerator<FortressMember> memberReportGenerator =
            FortressMemberReportGenerator(comp, currentPlayer, dimensions, currentTurn,
                hq);
    IReportGenerator</*Animal|AnimalTracks*/AnimalOrTracks> animalReportGenerator =
            AnimalReportGenerator(comp, dimensions, currentTurn, hq);
    IReportGenerator<IWorker> ourWorkerReportGenerator = WorkerReportGenerator(comp,
        true, dimensions, currentPlayer, currentTurn, hq);
    IReportGenerator<IWorker> otherWorkerReportGenerator = WorkerReportGenerator(comp,
        false, dimensions, currentPlayer, currentTurn, hq);

    "Produce the sub-sub-report about a unit's orders and results."
    void produceOrders(IUnit item, Anything(String) formatter) {
        if (!item.allOrders.empty || !item.allResults.empty) {
            formatter("""Orders and Results:<ul>
                         """);
            for (turn in sort(item.allOrders.keys.chain(item.allResults.keys)).distinct) {
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
     paragraph or bullet point)."
    shared actual void produceSingle(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, IUnit item, Point loc) {
        ostream("Unit ``item.name`` (``item.kind``), ");
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
            MutableMultimap<String, IResourcePile> resources =
                    HashMultimap<String, IResourcePile>();
            MutableList<Animal> animals = ArrayList<Animal>();
            MutableList<UnitMember> others = ArrayList<UnitMember>();
            for (member in item) {
                if (is IWorker member) {
                    workers.add(member);
                } else if (is Implement member) {
                    equipment.add(member);
                } else if (is IResourcePile member) {
                    resources.put(member.kind, member);
                } else if (is Animal member) {
                    if (exists existing = animals.findAndRemoveFirst(
                            member.equalExceptPopulation)) {
                        animals.add(member.combined(existing));
                    } else {
                        animals.add(member);
                    }
                } else {
                    others.add(member);
                }
            }
            ostream(""". Members of the unit:
                       <ul>
                       """);
            void produceInner<Member>(String heading, List<Member> collection,
                    Anything(Member) generator) given Member satisfies UnitMember {
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
            // TODO: Make a produceInnerComplex() so we only have to have one lambda wrapping X.produceSingle(), not three
            IReportGenerator<IWorker> workerReportGenerator;
            if (item.owner == currentPlayer) {
                workerReportGenerator = ourWorkerReportGenerator;
            } else {
                workerReportGenerator = otherWorkerReportGenerator;
            }
            produceInner<IWorker>("Workers", workers, (worker) =>
                workerReportGenerator.produceSingle(fixtures, map, ostream, worker, loc));
            produceInner</*Animal|AnimalTracks*/AnimalOrTracks>("Animals", animals,
                        (animal) => animalReportGenerator
                            .produceSingle(fixtures, map, ostream, animal, loc));
            produceInner<Implement>("Equipment", equipment, (member) =>
                memberReportGenerator.produceSingle(fixtures, map, ostream, member, loc));
            if (!resources.empty) {
                ostream("<li>Resources:
                         <ul>
                         ");
                for (kind->list in resources.asMap) {
                    produceInner(kind, list.sequence(), (IResourcePile member) =>
                        memberReportGenerator.produceSingle(fixtures, map, ostream,
                        member, loc));
                }
                ostream("""</ul>
                           </li>
                           """);
            }
            produceInner<UnitMember>("Others", others, compose(ostream, Object.string));
            ostream("""</ul>
                       """);
        }
        produceOrders(item, ostream); // TODO: Use compose(ostream, operatingSystem.newline.plus) (possibly with a shuffle() in there) to allow us to condense produceOrders
        fixtures.remove(item.id);
    }

    "Produce the part of the report on all units not covered as part of fortresses."
    shared actual void produce(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream) {
        MutableHeadedMap<IUnit, Point> foreign =
                HeadedMapImpl<IUnit, Point>("<h5>Foreign Units</h5>");
        MutableHeadedMap<IUnit, Point> ours =
                HeadedMapImpl<IUnit, Point>("<h5>Your units</h5>");
        for ([loc, unit] in fixtures.items.narrow<[Point, IUnit]>()
                .sort(pairComparator)) {
            if (currentPlayer == unit.owner) {
                ours[unit] = loc;
            } else {
                foreign[unit] = loc;
            }
        }
        if (!ours.empty || !foreign.empty) {
            ostream("""<h4>Units in the map</h4>
                       <p>(Any units listed above are not described again.)</p>
                       """);
            void unitFormatter(IUnit->Point entry, Anything(String) formatter) {
                formatter(
                    "At ``entry.item````distanceString(entry.item)``");
                produceSingle(fixtures, map, formatter, entry.key, entry.item);
            }
            writeMap(ostream, ours, unitFormatter);
            writeMap(ostream, foreign, unitFormatter);
        }
    }
}
