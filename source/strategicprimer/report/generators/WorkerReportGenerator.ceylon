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
import strategicprimer.model.common.map.fixtures.mobile {
    IWorker
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    WorkerStats {
        modifierString=getModifierString
    },
    ISkill
}

"A report generator for Workers."
class WorkerReportGenerator(Comparison([Point, IFixture], [Point, IFixture]) comp,
            Boolean details, MapDimensions dimensions, Player currentPlayer, Integer currentTurn,
            Point? hq = null)
        extends AbstractReportGenerator<IWorker>(comp, dimensions, hq) {
    value animalReportGenerator = AnimalReportGenerator(comp, dimensions, currentTurn, hq);
    value equipmentReportGenerator = FortressMemberReportGenerator(comp, currentPlayer, dimensions,
            currentTurn, hq);
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
    String skills(ISkill* job) =>
        (job.empty) then "" else "(``", ".join(job.map(skillString))``)";

    "Produce a sub-sub-report on a worker (we assume we're already in the middle of a
     paragraph or bullet point)."
    shared actual void produceSingle(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, IWorker worker, Point loc) {
        ostream("``worker.name``, a ``worker.race``.");
        if (details, exists stats = worker.stats) {
            ostream("
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
        if (details, exists mount = worker.mount) {
            ostream("(S)he is mounted on the following animal:");
            animalReportGenerator.produceSingle(fixtures, map, ostream, mount, loc);
        }
        if (details, !worker.equipment.empty) {
            ostream("""(S)he has the following personal equipment:
                       <ul>
                       """);
            for (item in worker.equipment) {
                ostream("<li>");
                equipmentReportGenerator.produceSingle(fixtures, map, ostream, item, loc);
            }
        }
        if (details, exists note = worker.notes.get(currentPlayer), !note.empty) {
            ostream("<p>``note``</p>
                    ");
        }
    }

    // TODO: move to lovelace.util and use elsewhere
    [Second, First] reversePair<First, Second>([First, Second] pair) =>
            [pair.rest.first, pair.first];

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
            for ([worker, loc] in workers) {
                ostream("<li>");
                produceSingle(fixtures, map, ostream, worker, loc);
                ostream("""</li>
                           """);
            }
            ostream("""</ul>
                       """);
        }
    }
}
