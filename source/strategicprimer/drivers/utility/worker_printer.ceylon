import strategicprimer.drivers.common {
	SimpleDriver,
	IDriverUsage,
	DriverUsage,
	ParamCount,
	SPOptions,
	IDriverModel
}
import strategicprimer.model.map.fixtures.mobile {
	IUnit,
	IWorker
}
import strategicprimer.drivers.common.cli {
	ICLIHelper
}
import strategicprimer.model.map.fixtures.mobile.worker {
	IJob,
	WorkerStats
}
import java.nio.file {
	JPath=Path
}
import strategicprimer.drivers.exploration.common {
	IExplorationModel,
	ExplorationModel
}
"A driver to print a mini-report on workers, suitable for inclusion in a player's results."
shared object workerPrintCLI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["--print"];
        paramsWanted = ParamCount.one;
        shortDescription = "Print stats of workers";
        longDescription = "Print stats of workers in a unit in a brief list.";
    };
    String[6] statLabelArray = ["Str", "Dex", "Con", "Int", "Wis", "Cha"];
    void printWorkers(IUnit unit, ICLIHelper cli) {
        for (worker in unit.narrow<IWorker>()) {
            cli.print("- ");
            cli.print(worker.name);
            if (worker.race != "human") {
                cli.print(" (``worker.race``)");
            }
            {IJob*} jobs = worker.filter((job) => job.level > 0);
            if (!jobs.empty) {
                variable Boolean first = true;
                for (job in jobs) {
                    if (first) {
                        cli.print(" (");
                        first = false;
                    } else {
                        cli.print(", ");
                    }
                    cli.print("``job.name`` ``job.level``");
                }
                cli.print(")");
            }
            if (exists stats = worker.stats) {
                Integer[6] statsArray = stats.array;
                for (i in 0:6) {
                    if (i == 0) {
                        cli.print(" [");
                    } else {
                        cli.print(", ");
                    }
                     cli.print("``statLabelArray[i] else ""`` ``WorkerStats.getModifierString(statsArray[i] else -10)``");
                }
                cli.print("]");
            }
            cli.println();
        }
    }
    shared actual {JPath*} askUserForFiles() => {};
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
        IDriverModel model) {
        if (is IExplorationModel model) {
            value playerChoice = cli.chooseFromList(model.playerChoices.sequence(), "Players in the map:",
                "No players", "Player owning the unit:", false);
            if (exists player = playerChoice.item) {
                value unitChoice = cli.chooseFromList(model.getUnits(player).sequence(), "Units of that player:",
                    "No units", "Unit to print:", false);
                if (exists unit = unitChoice.item) {
                    printWorkers(unit, cli);
                }
            }
        } else {
            startDriverOnModel(cli, options, ExplorationModel.copyConstructor(model));
        }
    }
}
