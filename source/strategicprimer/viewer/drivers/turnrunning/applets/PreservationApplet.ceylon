import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.model.common.map.fixtures {
    ResourcePile,
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

service(`interface TurnAppletFactory`)
shared class PreservationAppletFactory() satisfies TurnAppletFactory {
    shared actual TurnApplet create(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) =>
        PreservationApplet(model, cli, idf);
}

class PreservationApplet(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf)
        extends AbstractTurnApplet(model, cli, idf) {
    shared actual [String+] commands = ["preserve"]; // TODO: Or simply "cook"?

    shared actual String description => "Convert food into less-perishable form.";

    String describePile(ResourcePile pile) {
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
            ResourcePile converted = ResourcePile(idf.createID(), "food", convertedForm, Quantity(newPounds, "pounds"));
            converted.created = turn;
            switch (cli.inputBoolean("Use all ``item.quantity``?"))
            case (true) {
                super.removeFoodStock(item, unit.owner);
            }
            case (false) {
                if (exists subtrahend = cli.inputDecimal("How many ``item.quantity.units`` to use?"), subtrahend.positive) {
                    model.reduceResourceBy(item, subtrahend, unit.owner);
                } else {
                    return null;
                }
            }
            case (null) { return null; }
            super.addResourceToMaps(converted, unit.owner);
            if (exists results = cli.inputMultilineString("Description for results:")) {
                builder.append(results);
            } else {
                return null;
            }
        }
        return builder.string;
    }
}
