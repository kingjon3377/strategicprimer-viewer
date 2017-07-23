import strategicprimer.drivers.worker.common {
    IWorkerModel
}
import strategicprimer.model.map {
    Player
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.map.fixtures.mobile {
    IUnit
}
"Let the user add experience to a player's workers."
void advanceWorkers(IWorkerModel model, Player player, ICLIHelper cli) {
    IUnit[] units = [*model.getUnits(player).filter((unit) => !unit.empty)];
    cli.loopOnList(units, (clh, List<IUnit> list) => clh.chooseFromList(list,
        "``player.name``'s units:", "No unadvanced units remain.",
        "Chosen unit: ", false),
        "Choose another unit? ", advanceWorkersInUnit);
}
