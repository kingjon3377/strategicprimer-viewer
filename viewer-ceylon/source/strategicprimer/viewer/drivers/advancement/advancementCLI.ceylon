import strategicprimer.viewer.model {
    IDriverModel
}
import model.map {
    Player
}
import strategicprimer.viewer.drivers.worker_mgmt {
    WorkerModel,
    IWorkerModel
}
import java.io {
    IOException
}
import strategicprimer.viewer.drivers {
    DriverFailedException,
    ICLIHelper,
    DriverUsage,
    ParamCount,
    SimpleCLIDriver,
    IDriverUsage,
    SPOptions
}
import ceylon.logging {
    logger,
    Logger
}
"A logger."
Logger log = logger(`module strategicprimer.viewer`);
"The worker-advancement CLI driver."
shared object advancementCLI satisfies SimpleCLIDriver {
    "Let the user choose a player to run worker advancement for."
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        IWorkerModel workerModel;
        if (is IWorkerModel model) {
            workerModel = model;
        } else {
            workerModel = WorkerModel.copyConstructor(model);
        }
        Player[] playerList = [*workerModel.players];
        try {
            cli.loopOnList(playerList,
                        (clh) => clh.chooseFromList(playerList, "Available players:",
                    "No players found.", "Chosen player: ", false),
                "Select another player? ",
                        (Player player, clh) => advanceWorkers(workerModel, player, clh));
        } catch (IOException except) {
            throw DriverFailedException(except, "I/O error interacting with user");
        }
    }
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        shortOption = "-a";
        longOption = "--adv";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "View a player's workers and manage their advancement";
        longDescription = """View a player's units, the workers in those units, each
                             worker's Jobs, and his or her level in each Skill in each Job.""";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
}
