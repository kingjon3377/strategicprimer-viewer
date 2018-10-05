import strategicprimer.drivers.common {
    SPOptions,
    IDriverModel,
    IDriverUsage,
    DriverUsage,
    ParamCount,
    CLIDriver,
    DriverFactory,
    ModelDriverFactory,
    ModelDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.exploration.common {
    IExplorationModel,
    ExplorationModel,
    Direction,
    Speed,
    TraversalImpossibleException
}
import strategicprimer.model.common.map {
    Player,
    IMutableMapNG
}
import ceylon.random {
    Random,
    DefaultRandom
}
import lovelace.util.common {
    PathWrapper
}

"A factory for an app to move independent units around at random."
service(`interface DriverFactory`)
shared class RandomMovementFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage(false, ["-v", "--move"],
        ParamCount.one, "Move independent units at random",
        "Move independent units randomly around the map.",
        true, false); // TODO: We'd like a GUI for this, perhaps adding customization or limiting the area or something
    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IExplorationModel model) {
            return RandomMovementCLI(cli, options, model);
        } else {
            return createDriver(cli, options, ExplorationModel.copyConstructor(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            ExplorationModel(map, path);
}

"An app to move independent units around at random."
class RandomMovementCLI(ICLIHelper cli, SPOptions options, model) satisfies CLIDriver {
    shared actual IExplorationModel model;
    shared actual void startDriver() {
        for (unit in model.playerChoices.filter(Player.independent)
                .flatMap(model.getUnits).sequence()) {
            Random rng =
                    DefaultRandom(unit.id.leftLogicalShift(8) + model.map.currentTurn);
            Integer steps = rng.nextInteger(3) + rng.nextInteger(3);
            model.selectedUnit = unit;
            for (i in 0:steps) {
                try {
                    model.move(rng.nextElement(`Direction`.caseValues)
                        else Direction.nowhere, Speed.normal);
                } catch (TraversalImpossibleException except) {
                    continue;
                }
            }
        }
    }
}
