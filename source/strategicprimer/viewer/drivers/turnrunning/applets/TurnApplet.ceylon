import strategicprimer.drivers.common.cli {
    ICLIHelper,
    Applet
}
import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.model.common.map {
    Point,
    Player,
    TileFixture,
    HasPopulation,
    HasOwner
}
import lovelace.util.common {
    matchingValue
}
import strategicprimer.model.common.map.fixtures.towns {
    IFortress
}
import strategicprimer.model.common.map.fixtures {
    IResourcePile,
    Quantity
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}

import strategicprimer.viewer.drivers.turnrunning {
    ITurnRunningModel
}

shared interface TurnAppletFactory {
    shared formal TurnApplet create(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf);
}

shared interface TurnApplet satisfies Applet<[]> {
    shared actual formal [String+] commands;
    shared actual formal String description;
    shared formal String? run();
    shared actual Anything?() invoke => run;

    shared default String inHours(Integer minutes) {
        if (minutes.negative) {
            return "negative " + inHours(minutes.negated);
        } else if (minutes.zero) {
            return "no time";
        } else if (minutes == 1) {
            return "1 minute";
        } else if (minutes < 60) {
            return minutes.string + " minutes";
        } else if (minutes == 60) {
            return "1 hour";
        } else if (minutes < 120) {
            return "1 hour, " + inHours(minutes.modulo(60));
        } else if (60.divides(minutes)) {
            return (minutes / 60).string + " hours";
        } else {
            return (minutes / 60).string + " hours, " + inHours(minutes.modulo(60));
        }
    }
}

// TODO: Most of these 'default' functions should probably go into a 'TurnRunningModel' interface
shared abstract class AbstractTurnApplet(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) satisfies TurnApplet {
    shared Type? chooseFromList<Type>(Type[]|List<Type> items, String description, String none,
            String prompt, Boolean auto, String(Type) converter = Object.string) given Type satisfies Object {
        value entry = cli.chooseStringFromList(items.map(converter).sequence(), description, none, prompt, auto);
        if (entry.item exists) {
            return items[entry.key];
        } else {
            return null;
        }
    }

    shared Point? confirmPoint(String prompt) {
        if (exists retval = cli.inputPoint(prompt)) {
            Point selectedLocation = model.selectedUnitLocation;
            if (selectedLocation.valid) {
                value confirmation = cli.inputBoolean("``retval`` is ``Float.format(model
                    .mapDimensions.distance(retval, selectedLocation), 0, 1)
                `` away. Is that right?");
                if (exists confirmation, confirmation) {
                    return retval;
                } else {
                    return null;
                }
            } else {
                cli.println("No base location, so can't estimate distance.");
                return retval;
            }
        } else {
            return null;
        }
    }

    shared Integer encountersPerHour = 4; // TODO: These should be configurable, either by callers or the user's SPOptions
    shared Integer noResultCost = 60 / encountersPerHour;

    "Reduce the population of a group of plants, animals, etc., and copy the reduced form
     into all subordinate maps."
    shared void reducePopulation(Point point, HasPopulation<out TileFixture>&TileFixture fixture, String plural,
            Boolean zero) {
        Integer count = Integer.smallest(cli.inputNumber(
            "How many ``plural`` to remove: ") else 0, fixture.population);
        model.reducePopulation(point, fixture, zero, count);
    }

    // FIXME: Should only look at a particular unit's location
    shared [IResourcePile*] getFoodFor(Player player, Integer turn) { // TODO: Move into the model?
        return model.map.locations.flatMap(model.map.fixtures.get).narrow<IFortress|IUnit>()
            .filter(matchingValue(player, HasOwner.owner)).flatMap(identity).narrow<IResourcePile>()
            .filter(matchingValue("food", IResourcePile.kind)).filter(matchingValue("pounds",
                compose(Quantity.units, IResourcePile.quantity))).filter((r) => r.created <= turn).sequence();
    }

}
