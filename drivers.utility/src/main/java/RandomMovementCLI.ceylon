import strategicprimer.drivers.common {
    SPOptions,
    CLIDriver
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.drivers.exploration.common {
    IExplorationModel,
    Speed,
    TraversalImpossibleException
}

import strategicprimer.model.common.map {
    Direction,
    Player
}

import ceylon.random {
    Random,
    DefaultRandom
}

"An app to move independent units around at random."
class RandomMovementCLI(ICLIHelper cli, options, model) satisfies CLIDriver {
    shared actual IExplorationModel model;

    shared actual SPOptions options;

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
