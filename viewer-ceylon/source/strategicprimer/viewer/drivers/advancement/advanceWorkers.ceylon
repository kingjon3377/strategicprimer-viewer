import strategicprimer.viewer.drivers.worker_mgmt {
    IWorkerModel
}
import model.map {
    Player
}
import strategicprimer.viewer.drivers {
    ICLIHelper
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    IUnit
}
"Let the user add experience to a player's workers."
void advanceWorkers(IWorkerModel model, Player player, ICLIHelper cli) {
    IUnit[] units = [*model.getUnits(player).filter((unit) => !unit.empty)];
    cli.loopOnList(units, (clh) => clh.chooseFromList(units,
        "``player.name``'s units:", "No unadvanced units remain.",
        "Chosen unit: ", false),
        "Choose another unit? ", advanceWorkersInUnit);
}
