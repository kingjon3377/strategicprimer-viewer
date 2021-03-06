import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import lovelace.util.common {
    matchingValue
}
import strategicprimer.model.common.map.fixtures.towns {
    IFortress
}
import strategicprimer.model.common.map {
    HasOwner,
    Point
}
import strategicprimer.viewer.drivers.exploration {
    ExplorationCLIHelper
}
import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import strategicprimer.model.common.map.fixtures {
    IResourcePile
}
import ceylon.collection {
    ArrayList,
    MutableList
}

import strategicprimer.viewer.drivers.turnrunning {
    ITurnRunningModel
}

import lovelace.util.jvm {
    decimalize
}

service(`interface TurnAppletFactory`)
shared class MovementAppletFactory() satisfies TurnAppletFactory {
    shared actual TurnApplet create(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) =>
    MovementApplet(model, cli, idf);
}
class MovementApplet(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf)
        extends AbstractTurnApplet(model, cli, idf) {
    ExplorationCLIHelper explorationCLI = ExplorationCLIHelper(model, cli);
    model.addMovementCostListener(explorationCLI);

    shared actual [String+] commands = ["move"];
    shared actual String description = "move a unit";

    void packFood(IFortress? fortress, IUnit unit) {
        if (is Null fortress) {
            return;
        }
        MutableList<IResourcePile> resources = ArrayList { elements = fortress.narrow<IResourcePile>(); };
        while (exists chosen = chooseFromList(resources, "Resources in ``fortress.name``:", "No resources in fortress.",
                "Resource to take (from):", false)) {
            switch (cli.inputBooleanInSeries("Take it all?"))
            case (true) {
                model.transferResource(chosen, unit, decimalize(chosen.quantity.number), idf.createID);
                resources.remove(chosen);
            }
            case (false) {
                if (exists amount = cli.inputDecimal("Amount to take (in ``chosen.quantity.units``):"),
                        amount.positive) {
                    model.transferResource(chosen, unit, amount, idf.createID);
                    resources.clear();
                    resources.addAll(fortress.narrow<IResourcePile>());
                }
            }
            case (null) {
                return;
            }
        }
    }

    shared actual String? run() {
        StringBuilder buffer = StringBuilder();
        model.addSelectionChangeListener(explorationCLI);
        assert (exists mover = model.selectedUnit);
        // Ask the user about total MP
        model.selectedUnit = mover;
        while (explorationCLI.movement > 0) {
            Point oldPosition = model.selectedUnitLocation;
            explorationCLI.moveOneStep();
            Point newPosition = model.selectedUnitLocation;
            if (model.map.fixtures.get(oldPosition).narrow<IFortress>().any(matchingValue(mover.owner,
                        HasOwner.owner)),
                    !model.map.fixtures.get(newPosition).narrow<IFortress>().any(matchingValue(mover.owner,
                        HasOwner.owner))) {
                switch (pack = cli.inputBooleanInSeries("Leaving a fortress. Take provisions along?"))
                case (true) {
                    packFood(model.map.fixtures.get(oldPosition).narrow<IFortress>().find(
                        matchingValue(mover.owner, HasOwner.owner)), mover);
                }
                case (false) {}
                case (null) { return null; }
            }
            if (exists addendum = cli.inputMultilineString("Add to results:")) {
                buffer.append(addendum);
            } else {
                return null;
            }
        }
        // We don't want to be asked about MP for any other applets
        model.removeSelectionChangeListener(explorationCLI);
        return buffer.string;
    }
}
