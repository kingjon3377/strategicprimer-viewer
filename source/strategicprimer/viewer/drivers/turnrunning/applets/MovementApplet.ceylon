import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.exploration.common {
    IExplorationModel
}
import lovelace.util.common {
    matchingValue
}
import strategicprimer.model.common.map.fixtures.towns {
    Fortress
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
    Quantity,
    ResourcePile
}
import ceylon.collection {
    ArrayList,
    MutableList
}
service(`interface TurnAppletFactory`)
shared class MovementAppletFactory() satisfies TurnAppletFactory {
    shared actual TurnApplet create(IExplorationModel model, ICLIHelper cli, IDRegistrar idf) =>
    MovementApplet(model, cli, idf);
}
class MovementApplet(IExplorationModel model, ICLIHelper cli, IDRegistrar idf)
        extends AbstractTurnApplet(model, cli, idf) {
    ExplorationCLIHelper explorationCLI = ExplorationCLIHelper(model, cli);
    model.addMovementCostListener(explorationCLI);

    shared actual [String+] commands = ["move"];
    shared actual String description = "move a unit";

    void packFood(Fortress? fortress, IUnit unit) {
        if (is Null fortress) {
            return;
        }
        MutableList<ResourcePile> resources = ArrayList { elements = fortress.narrow<ResourcePile>(); };
        while (exists chosen = chooseFromList(resources, "Resources in ``fortress.name``:", "No resources in fortress.",
            "Resource to take (from):", false)) {
            switch (cli.inputBooleanInSeries("Take it all?"))
            case (true) {
                removeFoodStock(chosen, unit.owner);
                unit.addMember(chosen);
                resources.remove(chosen);
            }
            case (false) {
                if (exists amount = cli.inputDecimal("Amount to take (in ``chosen.quantity.units``):"),
                    amount.positive) {
                    reduceFoodBy(chosen, amount, unit.owner);
                    unit.addMember(ResourcePile(idf.createID(), chosen.kind, chosen.contents,
                        Quantity(amount, chosen.quantity.units)));
                    resources.clear();
                    resources.addAll(fortress.narrow<ResourcePile>());
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
            if (model.map.fixtures.get(oldPosition).narrow<Fortress>().any(matchingValue(mover.owner, HasOwner.owner)),
                !model.map.fixtures.get(newPosition).narrow<Fortress>().any(matchingValue(mover.owner,
                    HasOwner.owner))) {
                switch (pack = cli.inputBooleanInSeries("Leaving a fortress. Take provisions along?"))
                case (true) {
                    packFood(model.map.fixtures.get(oldPosition).narrow<Fortress>().find(
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