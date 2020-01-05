import strategicprimer.drivers.common {
    ReadOnlyDriver
}

import strategicprimer.model.common.map.fixtures.mobile {
    IUnit,
    IWorker
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.model.common.map.fixtures.mobile.worker {
    IJob,
    WorkerStats
}

import strategicprimer.drivers.exploration.common {
    IExplorationModel
}

"A driver to print a mini-report on workers, suitable for inclusion in a player's
 results."
class WorkerPrintCLI satisfies ReadOnlyDriver {
    static String[6] statLabelArray = ["Str", "Dex", "Con", "Int", "Wis", "Cha"];
    static String jobString(IJob job) => job.name + " " + job.level.string;

    ICLIHelper cli;
    shared actual IExplorationModel model;
    shared new (ICLIHelper cli, IExplorationModel model) {
        this.cli = cli;
        this.model = model;
    }

    void printWorkers(IUnit unit) {
        for (worker in unit.narrow<IWorker>()) {
            cli.print("- ", worker.name);
            if (worker.race != "human") {
                cli.print(" (", worker.race, ")");
            }

            {IJob*} jobs = worker.filter(compose(Integer.positive, IJob.level));
            if (!jobs.empty) {
                cli.print(" (", ", ".join(jobs.map(jobString)), ")");
            }

            if (exists stats = worker.stats) {
                cli.print(" [", ", ".join(zipPairs(statLabelArray,
                    stats.array.map(WorkerStats.getModifierString)).map(" ".join)), "]");
            }
            cli.println();
        }
    }

    shared actual void startDriver() {
        if (exists player = cli.chooseFromList(model.playerChoices.sequence(),
                "Players in the map:", "No players", "Player owning the unit:",
                false).item, exists unit = cli.chooseFromList(model.getUnits(player)
                    .sequence(), "Units of that player:", "No units", "Unit to print:",
                    false).item) {
            printWorkers(unit);
        }
    }
}
