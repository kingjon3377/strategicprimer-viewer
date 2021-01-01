import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit,
    IWorker
}

import strategicprimer.model.common.map.fixtures {
    IResourcePile
}

import ceylon.decimal {
    decimalNumber,
    Decimal
}
import lovelace.util.jvm {
    decimalize
}

import strategicprimer.viewer.drivers.turnrunning {
    ITurnRunningModel
}

// We *deliberately* do not make a factory with the `service` annotation.
shared class ConsumptionApplet(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf)
        extends AbstractTurnApplet(model, cli, idf) {
    shared variable Integer turn = model.map.currentTurn;
    shared variable IUnit? unit = model.selectedUnit;
    shared actual [String+] commands = ["consumption"];

    shared actual String description = "Determine the food consumed by a unit.";

    String describeFood(IResourcePile food) {
        if (food.created.negative) {
            return "``food.quantity.number`` ``food.quantity.units`` of ``food.contents``";
        } else {
            return "``food.quantity.number`` ``food.quantity.units`` of ``food.contents`` (turn #``food.created``)";
        }
    }

    shared actual String? run() {
        value localUnit = unit;
        if (is Null localUnit) {
            return null;
        }
        Integer workers = localUnit.narrow<IWorker>().size;
        variable Decimal remainingConsumption = decimalNumber(4 * workers);
        Decimal zero = decimalNumber(0);
        while (remainingConsumption > zero) { // TODO: extract loop body as a function?
            cli.print(Float.format(remainingConsumption.float, 0, 1));
            cli.println(" pounds of consumption unaccounted-for");
            value food = chooseFromList(getFoodFor(localUnit.owner, turn), "Food stocks owned by player:",
                "No food stocks found", "Food to consume from:", false, describeFood); // TODO: should only count food *in the same place* (but unit movement away from HQ should ask user how much food to take along, and to choose what food in a similar manner to this)
            if (!food exists) {
                return null;
            }
            assert (exists food);
            if (decimalize(food.quantity.number) <= remainingConsumption) {
                switch (cli.inputBooleanInSeries("Consume all of the ``food.contents``?",
                    "consume-all-of"))
                case (true) {
                    model.removeResource(food, localUnit.owner);
                    remainingConsumption -= decimalize(food.quantity.number);
                    continue;
                }
                case (false) { // TODO: extract this as a function?
                    value amountToConsume = cli.inputDecimal("How many pounds of the ``food.contents`` to consume:");
                    if (exists amountToConsume, amountToConsume >= decimalize(food.quantity.number)) {
                        model.removeResource(food, localUnit.owner);
                        remainingConsumption -= decimalize(food.quantity.number);
                        continue;
                    } else if (exists amountToConsume) {
                        model.reduceResourceBy(food, amountToConsume, localUnit.owner);
                        remainingConsumption -= amountToConsume;
                        continue;
                    } else {
                        return null;
                    }
                }
                case (null) { return null; }
            } // else
            switch (cli.inputBooleanInSeries("Eat all remaining ``remainingConsumption`` from the ``food.contents``?",
                "all-remaining"))
            case (true) {
                model.reduceResourceBy(food, remainingConsumption, localUnit.owner);
                remainingConsumption = decimalize(0);
            }
            case (false) { // TODO: extract this as a function?
                value amountToConsume = cli.inputDecimal("How many pounds of the ``food.contents`` to consume:");
                if (exists amountToConsume, amountToConsume >= remainingConsumption) {
                    model.reduceResourceBy(food, remainingConsumption, localUnit.owner);
                    remainingConsumption = decimalize(0);
                    continue;
                } else if (exists amountToConsume) {
                    model.reduceResourceBy(food, amountToConsume, localUnit.owner);
                    remainingConsumption -= amountToConsume;
                    continue;
                } else {
                    return null;
                }
            }
            case (null) { return null; }
        }
        return ""; // FIXME: Optionally report on what workers ate
    }
}
