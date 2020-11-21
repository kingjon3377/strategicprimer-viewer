import ceylon.logging {
    logger,
    Logger
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.exploration.common {
    IExplorationModel
}
import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.model.common.map {
    Player
}

"A logger."
Logger log = logger(`module strategicprimer.viewer`);

// We *deliberately* do not make a factory with the `service` annotation.
shared class SpoilageApplet(IExplorationModel model, ICLIHelper cli, IDRegistrar idf)
        extends AbstractTurnApplet(model, cli, idf) {
    shared variable Player owner = model.map.currentPlayer;
    shared variable Integer turn = model.map.currentTurn;
    shared actual [String+] commands = ["spoilage"];
    shared actual String description = "Determine what food has spoiled";

    shared actual String? run() {
        StringBuilder buffer = StringBuilder();
        for (food in getFoodFor(owner, turn)) {
            if (turn < 0) { // rations whose spoilage isn't tracked
                continue;
            }
            cli.print("Food is ");
            cli.println(food.string);
            if (exists type = FoodType.askFoodType(cli, food.kind)) {
                switch (type.hasSpoiled(food, turn, cli))
                case (true) {
                    if (exists spoilage = type.amountSpoiling(food.quantity, cli)) {
                        buffer.append(Float.format(spoilage.float, 0, 2));
                        buffer.append(" pounds of ");
                        buffer.append(food.string);
                        buffer.append(" spoiled.\n\n");
                        reduceFoodBy(food, spoilage, owner);
                    } else {
                        log.warn("Non-numeric spoilage amount");
                        return null;
                    }
                }
                case (false) { continue; }
                case (null) {
                    log.warn("EOF on has-this-spoiled");
                    return null;
                }
            } else {
                log.warn("Didn't get a food type");
                return null;
            }
        }
        return buffer.string;
    }
}
