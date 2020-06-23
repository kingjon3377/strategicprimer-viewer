import strategicprimer.drivers.common {
    CLIDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper,
    AppletChooser
}
import ceylon.collection {
    MutableList,
    ArrayList,
    MutableMap,
    naturalOrderTreeMap
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit,
    ProxyUnit,
    ProxyFor
}
import strategicprimer.drivers.exploration.common {
    IExplorationModel
}
import strategicprimer.model.common.map.fixtures.towns {
    Fortress
}
import lovelace.util.common {
    matchingValue,
    todo,
    comparingOn
}

import strategicprimer.model.common.map.fixtures {
    Quantity,
    ResourcePile
}
import strategicprimer.model.common.idreg {
    IDRegistrar,
    createIDFactory
}
import strategicprimer.viewer.drivers.advancement {
    LevelGainListener,
    AdvancementCLIHelper
}
import strategicprimer.model.common.map {
    HasOwner,
    Player
}

import strategicprimer.viewer.drivers.turnrunning.applets {
    TurnApplet,
    TurnAppletFactory,
    ConsumptionApplet,
    SpoilageApplet
}

todo("Tests") // This'll have to wait until eclipse/ceylon#6986 is fixed
class TurnRunningCLI(ICLIHelper cli, model) satisfies CLIDriver {
    shared actual IExplorationModel model;
    IDRegistrar idf = createIDFactory(model.allMaps.map(Entry.key));

    Boolean unfinishedResults(Integer turn)(IUnit unit) {
        String results = unit.getResults(turn);
        return results.empty || results.lowercased.containsAny(["fixme", "todo", "xxx"]);
    }

    "If [[the argument|fixture]] is a [[Fortress]], return it; otherwise,
     return a [[Singleton]] of the argument. This allows callers to get a
     flattened stream of units, including those in fortresses."
    {Anything*} flatten(Anything fixture) {
        if (is Fortress fixture) {
            return fixture;
        } else {
            return Singleton(fixture);
        }
    }

    "Flatten and filter the stream to include only units, and only those owned by the
     given player."
    {IUnit*} getUnitsImpl({Anything*} iter, Player player) =>
        iter.flatMap(flatten).narrow<IUnit>()
            .filter(compose(matchingValue(player.playerId, Player.playerId),
            IUnit.owner));

    {IUnit*} getUnits(Player player) {
        value temp = model.allMaps.map(Entry.key)
            .flatMap((indivMap) => getUnitsImpl(indivMap.fixtures.items, player));
        MutableMap<Integer, IUnit&ProxyFor<IUnit>> tempMap =
            naturalOrderTreeMap<Integer, IUnit&ProxyFor<IUnit>>([]);
        for (unit in temp) {
            Integer key = unit.id;
            ProxyFor<IUnit> proxy;
            if (exists item = tempMap[key]) {
                proxy = item;
            } else {
                value newProxy = ProxyUnit.fromParallelMaps(key);
                tempMap[key] = newProxy;
                proxy = newProxy;
            }
            proxy.addProxied(unit);
        }
        return tempMap.items.sort(comparingOn(IUnit.name,
            byIncreasing(String.lowercased)));
    }

    AdvancementCLIHelper advancementCLI = AdvancementCLIHelper(cli);
    AppletChooser<TurnApplet> appletChooser =
        AppletChooser<TurnApplet>(cli, *`module strategicprimer.viewer`
            .findServiceProviders(`TurnAppletFactory`).map((factory) => factory.create(model, cli, idf)));

    ConsumptionApplet consumptionApplet = ConsumptionApplet(model, cli, idf);

    shared [ResourcePile*] getFoodFor(Player player, Integer turn) { // TODO: Move into the model?
        return model.map.locations.flatMap(model.map.fixtures.get).narrow<Fortress|IUnit>()
            .filter(matchingValue(player, HasOwner.owner)).flatMap(identity).narrow<ResourcePile>()
            .filter(matchingValue("food", ResourcePile.kind)).filter(matchingValue("pounds",
                compose(Quantity.units, ResourcePile.quantity))).filter((r) => r.created <= turn).sequence();
    }

    SpoilageApplet spoilageApplet = SpoilageApplet(model, cli, idf);

    String createResults(IUnit unit, Integer turn) {
        if (is ProxyFor<out IUnit> unit) {
            model.selectedUnit = unit.proxied.first;
        } else {
            model.selectedUnit = unit;
        }
        cli.print("Orders for unit ", unit.name, " (", unit.kind);
        cli.print(") for turn ", turn.string, ": ");
        cli.println(unit.getLatestOrders(turn));
        StringBuilder buffer = StringBuilder();
        while (true) {
            switch (command = appletChooser.chooseApplet())
            case (null|true) { continue; }
            case (false) { return ""; }
            case (is TurnApplet) {
                if (!"other" in command.commands) {
                    if (exists results = command.run()) {
                        buffer.append(results);
                    } else {
                        return "";
                    }
                }
                break;
            }
        }
        String prompt;
        if (buffer.empty) {
            prompt = "Results: ";
        } else {
            prompt = "Additional Results: ";
        }
        if (exists addendum = cli.inputMultilineString(prompt)) {
            buffer.append(addendum);
        } else {
            return "";
        }
        if (exists runAdvancement =
                    cli.inputBooleanInSeries("Run advancement for this unit now?"),
                runAdvancement, exists expertMentoring = cli.inputBooleanInSeries(
                    "Account for expert mentoring?")) {
            buffer.appendNewline();
            buffer.appendNewline();
            object levelListener satisfies LevelGainListener {
                shared actual void level(String workerName, String jobName,
                        String skillName, Integer gains, Integer currentLevel) {
                    buffer.append(workerName);
                    buffer.append(" showed improvement in the skill of ");
                    buffer.append(skillName);
                    if (gains > 1) {
                        buffer.append(" ``gains`` skill ranks)");
                    }
                    buffer.append(". ");
                }
            }
            advancementCLI.addLevelGainListener(levelListener);
            advancementCLI.advanceWorkersInUnit(unit, expertMentoring);
            advancementCLI.removeLevelGainListener(levelListener);
        }
        if (exists runFoodConsumptionAnswer = cli.inputBooleanInSeries(
                "Run food consumption for this unit now?"), runFoodConsumptionAnswer) {
            consumptionApplet.turn = turn;
            consumptionApplet.unit = unit;
            if (exists consumptionResults = consumptionApplet.run()) {
                if (!consumptionResults.empty) {
                    buffer.appendNewline();
                    buffer.appendNewline();
                    buffer.append(consumptionResults);
                    buffer.appendNewline();
                }
            } else {
                return "";
            }
        }
        if (exists runFoodSpoilageAnswer = cli.inputBooleanInSeries(
                "Run food spoilage and report it under this unit's results?"),
                runFoodSpoilageAnswer) {
            spoilageApplet.owner = unit.owner;
            spoilageApplet.turn = turn;
            if (exists foodSpoilageResult = spoilageApplet.run()) {
                buffer.appendNewline();
                buffer.appendNewline();
                buffer.append(foodSpoilageResult);
                buffer.appendNewline();
            }
        }
        return buffer.string.trimmed;
    }

    shared actual void startDriver() {
        Integer currentTurn = model.map.currentTurn;
        if (exists player = cli.chooseFromList(model.playerChoices.sequence(),
                "Players in the maps:", "No players found", "Player to run:",
                false).item) {
            MutableList<IUnit> units = ArrayList {
                elements = getUnits(player).filter(unfinishedResults(currentTurn));
            };
            while (true) {
                value index->unit = cli.chooseFromList(units,
                    "Units belonging to ``player``:",
                    "Player has no units without apparently-final results",
                    "Unit to run:", false);
                if (exists unit) {
                    String results = createResults(unit, currentTurn);
                    unit.setResults(currentTurn, results);
                    if (!unfinishedResults(currentTurn)(unit)) {
                        units.delete(index);
                    }
                } else {
                    break;
                }
                if (units.empty) {
                    break;
                }
            }
        }
    }
}
