import strategicprimer.drivers.worker.common {
    IWorkerModel
}
import strategicprimer.model.map {
    Player
}
import strategicprimer.drivers.common {
    ICLIHelper
}
import strategicprimer.model.map.fixtures.mobile {
    IUnit
}
"Let the user add experience to a player's workers."
void advanceWorkers(IWorkerModel model, Player player, ICLIHelper cli) {
    IUnit[] units = [*model.getUnits(player).filter((unit) => !unit.empty)];
    cli.loopOnList(units, (clh) => clh.chooseFromList(units,
        "``player.name``'s units:", "No unadvanced units remain.",
        "Chosen unit: ", false).key,
        "Choose another unit? ", advanceWorkersInUnit);
}
