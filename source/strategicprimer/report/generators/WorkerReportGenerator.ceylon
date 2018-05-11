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
    IWorker
}
import strategicprimer.model.map.fixtures.mobile.worker {
    WorkerStats {
        modifierString=getModifierString
    },
    IJob,
    ISkill
}
import strategicprimer.report {
    IReportNode
}
import strategicprimer.report.nodes {
    SimpleReportNode,
    ComplexReportNode,
    ListReportNode,
    SectionListReportNode,
    emptyReportNode
}
"A report generator for Workers."
class WorkerReportGenerator(Comparison([Point, IFixture], [Point, IFixture]) comp,
            Boolean details, MapDimensions dimensions, Point hq = invalidPoint)
        extends AbstractReportGenerator<IWorker>(comp, dimensions, hq) {
    "Produce the sub-sub-report on a worker's stats."
    String statsString(WorkerStats stats) {
        return "He or she has the following stats: ``stats.hitPoints`` / ``stats
	            .maxHitPoints`` Hit Points, Strength ``modifierString(stats.strength)
	        ``, Dexterity ``modifierString(stats.dexterity)``, Constitution ``
	        modifierString(stats.constitution)``, Intelligence ``modifierString(stats
	            .intelligence)``, Wisdom ``modifierString(stats.wisdom)``, Charisma ``
	        modifierString(stats.charisma)``";
    }
    String skillString(ISkill skill) => skill.name + " " + skill.level.string;
    "Produce text describing the given Skills."
    String skills(ISkill* job) {
        if (job.empty) {
            return "";
        } else {
            return "(``", ".join(job.map(skillString))``)";
        }
    }
    "Produce the report-intermediate-representation sub-sub-report on a Job."
    IReportNode produceJobRIR(IJob job, Point loc) {
        return SimpleReportNode("``job.level`` levels in ``job.name`` ``skills(*job)``",
            loc);
    }
    "Produce a sub-sub-report on a worker (we assume we're already in the middle of a
     paragraph or bullet point)."
    shared actual void produceSingle(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, IWorker worker, Point loc) {
        ostream("``worker.name``, a ``worker.race``.");
        if (details, exists stats = worker.stats) {
            ostream(
                "
                 <p>``statsString(stats)``</p>
             ");
        }
        if (details, !worker.empty) {
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
    [Second, First] reversePair<First, Second>([First, Second] pair) => [pair.rest.first, pair.first];
    "Produce a sub-sub-report on all workers. This should never be called, but we'll
     implement it properly anyway."
    shared actual void produce(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
        		IMapNG map, Anything(String) ostream) {
        {[IWorker, Point]*} workers = fixtures.items.narrow<[Point, IWorker]>()
                .sort(pairComparator).map(reversePair);
        if (!workers.empty) {
            ostream("""<h5>Workers</h5>
                       <ul>
                       """);
            for (tuple in workers) {
                ostream("<li>");
                produceSingle(fixtures, map, ostream, *tuple);
                ostream("""</li>
                           """);
            }
            ostream("""</ul>
                       """);
        }
    }
    "Produce a sub-sub-report on a worker (we assume we're already in the middle of a
     paragraph or bullet point)."
    shared actual IReportNode produceRIRSingle(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, IWorker worker, Point loc) {
        if (details) {
            IReportNode retval =
                    ComplexReportNode("``worker.name``, a ``worker.race``", loc);
            if (exists stats = worker.stats) {
                retval.appendNode(SimpleReportNode(statsString(stats)));
            }
            if (!worker.empty) {
                IReportNode jobs = ListReportNode(
                    "Has training or experience in the following Jobs (Skills):",
                    loc);
                for (job in worker) {
                    jobs.appendNode(produceJobRIR(job, loc));
                }
                retval.appendNode(jobs);
            }
            return retval;
        } else {
            return SimpleReportNode("``worker.name``, a ``worker.race``", loc);
        }
    }
    "Produce a sub-sub-report on all workers. This should never be called, but we'll
     implement it properly anyway)."
    shared actual IReportNode produceRIR(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
	        IMapNG map) {
        IReportNode retval = SectionListReportNode(5, "Workers");
        for ([loc, worker] in fixtures.items.narrow<[Point, IWorker]>().sort(pairComparator)) {
            retval.appendNode(produceRIRSingle(fixtures, map, worker, loc));
        }
        if (retval.childCount == 0) {
            return emptyReportNode;
        } else {
            return retval;
        }
    }
}
