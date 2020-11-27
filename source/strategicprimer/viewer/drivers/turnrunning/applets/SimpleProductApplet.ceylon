import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.viewer.drivers.resourceadding {
    ResourceAddingCLIHelper
}

import strategicprimer.viewer.drivers.turnrunning {
    ITurnRunningModel
}

service(`interface TurnAppletFactory`)
shared class FarmingAppletFactory() satisfies TurnAppletFactory {
    shared actual TurnApplet create(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) =>
        SimpleProductApplet("farm", "Plant, weed or prune, or harvest a field, meadow, or orchard", model, cli, idf);
}

service(`interface TurnAppletFactory`)
shared class MiningAppletFactory() satisfies TurnAppletFactory {
    shared actual TurnApplet create(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) =>
        SimpleProductApplet("mine", "Extract mineral resources from the ground", model, cli, idf);
}

class SimpleProductApplet(String name, shared actual String description, ITurnRunningModel model, ICLIHelper cli,
        IDRegistrar idf) extends AbstractTurnApplet(model, cli, idf) {
    ResourceAddingCLIHelper raHelper = ResourceAddingCLIHelper(cli, idf);
    shared actual [String+] commands = [name];
    shared actual String? run() {
        StringBuilder builder = StringBuilder();
        variable Boolean another;
        if (is Boolean resp = cli.inputBooleanInSeries("Add resources to the map?", name + "resources")) {
            another = resp;
        } else {
            return null;
        }
        while (another, exists resource = raHelper.enterResource()) {
            super.addResourceToMaps(resource, model.selectedUnit?.owner else model.map.currentPlayer);
            if (is Boolean resp = cli.inputBoolean("Add another resource?")) {
                another = resp;
            } else {
                return null;
            }
        }
        if (exists addendum = cli.inputMultilineString("Desription of results:")) {
            builder.append(addendum);
            return builder.string;
        } else {
            return null;
        }
    }
}
