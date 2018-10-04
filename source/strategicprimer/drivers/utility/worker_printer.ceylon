import strategicprimer.drivers.common {
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions,
    IDriverModel,
    ReadOnlyDriver,
    DriverFactory,
    ModelDriverFactory,
    ModelDriver
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
    IExplorationModel,
    ExplorationModel
}
import lovelace.util.common {
    matchingPredicate,
    PathWrapper
}
import strategicprimer.model.common.map {
    IMutableMapNG
}

"A factory for the driver to print a mini-report on workers, suitable for inclusion in a
 player's results."
service(`interface DriverFactory`)
shared class WorkerPrinterFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["--print"];
        paramsWanted = ParamCount.one;
        shortDescription = "Print stats of workers";
        longDescription = "Print stats of workers in a unit in a brief list.";
        includeInCLIList = true;
        includeInGUIList = false;
    };
    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        assert (is IExplorationModel model);
        return WorkerPrintCLI(cli, model);
    }

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            ExplorationModel(map, path);
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
            cli.print("- ");
            cli.print(worker.name);
            if (worker.race != "human") {
                cli.print(" (``worker.race``)");
            }
            {IJob*} jobs = worker.filter(matchingPredicate(Integer.positive, IJob.level));
            if (!jobs.empty) {
                cli.print(" (");
                cli.print(", ".join(jobs.map(jobString)));
                cli.print(")");
            }
            if (exists stats = worker.stats) {
                cli.print(" [``", ".join(zipPairs(statLabelArray,
                    stats.array.map(WorkerStats.getModifierString)).map(" ".join))``]");
            }
            cli.println();
        }
    }
    shared actual void startDriver() {
        value playerChoice = cli.chooseFromList(model.playerChoices.sequence(),
            "Players in the map:", "No players", "Player owning the unit:", false);
        if (exists player = playerChoice.item) {
            value unitChoice = cli.chooseFromList(model.getUnits(player).sequence(),
                "Units of that player:", "No units", "Unit to print:", false);
            if (exists unit = unitChoice.item) {
                printWorkers(unit);
            }
        }
    }
}
