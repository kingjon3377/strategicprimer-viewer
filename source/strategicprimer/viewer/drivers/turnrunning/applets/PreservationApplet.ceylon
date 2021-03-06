import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.model.common.map.fixtures {
    IResourcePile,
    Quantity
}
import ceylon.collection {
    MutableList,
    ArrayList
}
import ceylon.decimal {
    Decimal
}

import strategicprimer.viewer.drivers.turnrunning {
    ITurnRunningModel
}

import lovelace.util.jvm {
    decimalize
}

service(`interface TurnAppletFactory`)
shared class PreservationAppletFactory() satisfies TurnAppletFactory {
    shared actual TurnApplet create(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) =>
        PreservationApplet(model, cli, idf);
}

class PreservationApplet(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf)
        extends AbstractTurnApplet(model, cli, idf) {
    shared actual [String+] commands = ["preserve"]; // TODO: Or simply "cook"?

    shared actual String description => "Convert food into less-perishable form.";

    String describePile(IResourcePile pile) {
        if (pile.created.negative) {
            return "``pile.quantity`` of ``pile.contents``";
        } else {
            return "``pile.quantity`` of ``pile.contents`` (turn #``pile.created``)";
        }
    }

    shared actual String? run() {
        StringBuilder builder = StringBuilder();
        assert (exists unit = model.selectedUnit);
        MutableList<String> foods = ArrayList<String>();
        while (exists item = chooseFromList(getFoodFor(unit.owner, model.map.currentTurn), "Available food:",
                "No food available", "Choose food to convert:", false, describePile)) {
            String convertedForm;
            if (exists temp = chooseFromList(foods, "Preserved food types:", "", "Type that converts into:", false)) {
                convertedForm = temp;
            } else if (exists temp = cli.inputString("Type of food that converts into:"), !temp.empty) {
                convertedForm = temp;
                foods.add(temp);
            } else {
                return null;
            }
            Integer turn;
            if (exists temp = cli.inputNumber("What turn should spoilage counter start from?"), !temp.negative) {
                turn = temp;
            } else {
                return null;
            }
            Decimal newPounds;
            if (exists temp = cli.inputDecimal("How many pounds of that are produced from this source?"),
                    temp.positive) {
                newPounds = temp;
            } else {
                return null;
            }
            Decimal subtrahend;
            switch (cli.inputBoolean("Use all ``item.quantity``?"))
            case (true) {
                subtrahend = decimalize(item.quantity.number);
            }
            case (false) {
                if (exists temp = cli.inputDecimal("How many ``item.quantity.units`` to use?"), temp.positive) {
                    subtrahend = temp;
                } else {
                    return null;
                }
            }
            case (null) { return null; }
            model.reduceResourceBy(item, subtrahend, unit.owner);
            // TODO: findHQ() should instead take the unit and find the fortress in the same tile, if any
            model.addResource(model.findHQ(unit.owner) else unit, idf.createID(), "food",
                convertedForm, Quantity(newPounds, "pounds"), turn);
            if (exists results = cli.inputMultilineString("Description for results:")) {
                builder.append(results);
            } else {
                return null;
            }
        }
        return builder.string;
    }
}
