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
    IWorker,
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

import ceylon.decimal {
    Decimal,
    decimalNumber
}

import lovelace.util.jvm {
    decimalize
}
import strategicprimer.viewer.drivers.turnrunning.applets {
    TurnApplet,
    TurnAppletFactory
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

    void removeFoodStock(ResourcePile food, Player owner) {
        for (map->_ in model.allMaps) {
            for (container in map.locations.flatMap(map.fixtures.get).narrow<IUnit|Fortress>()
                .filter(matchingValue(owner, HasOwner.owner))) {
                variable Boolean found = false;
                for (item in container.narrow<ResourcePile>()) {
                    if (food.isSubset(item, noop)) { // TODO: is that the right way around?
                        switch (container)
                        case (is IUnit) { container.removeMember(item); }
                        else case (is Fortress) { container.removeMember(item); }
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
        }
    }

    void reduceFoodBy(ResourcePile pile, Decimal amount, Player owner) {
        for (map->_ in model.allMaps) {
            for (container in map.locations.flatMap(map.fixtures.get).narrow<IUnit|Fortress>()
                .filter(matchingValue(owner, HasOwner.owner))) {
                variable Boolean found = false;
                for (item in container.narrow<ResourcePile>()) {
                    if (pile.isSubset(item, noop)) { // TODO: is that the right way around?
                        if (decimalize(item.quantity.number) <= amount) {
                            switch (container)
                            case (is IUnit) { container.removeMember(item); }
                            else case (is Fortress) { container.removeMember(item); }
                        } else {
                            item.quantity = Quantity(decimalize(item.quantity.number) - amount, pile.quantity.units);
                        }
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
        }
    }

    Type? chooseFromList<Type>(Type[]|List<Type> items, String description, String none,
            String prompt, Boolean auto, String(Type) converter = Object.string) given Type satisfies Object {
        value entry = cli.chooseStringFromList(items.map(converter).sequence(), description, none, prompt, auto);
        if (entry.item exists) {
            return items[entry.key];
        } else {
            return null;
        }
    }

    [ResourcePile*] getFoodFor(Player player, Integer turn) { // TODO: Move into the model?
        return model.map.locations.flatMap(model.map.fixtures.get).narrow<Fortress|IUnit>()
            .filter(matchingValue(player, HasOwner.owner)).flatMap(identity).narrow<ResourcePile>()
            .filter(matchingValue("food", ResourcePile.kind)).filter(matchingValue("pounds",
                compose(Quantity.units, ResourcePile.quantity))).filter((r) => r.created <= turn).sequence();
    }

    AdvancementCLIHelper advancementCLI = AdvancementCLIHelper(cli);
    AppletChooser<TurnApplet> appletChooser =
        AppletChooser<TurnApplet>(cli, *`module strategicprimer.viewer`
            .findServiceProviders(`TurnAppletFactory`).map((factory) => factory.create(model, cli, idf)));

    void runFoodConsumption(IUnit unit, Integer turn) {
        Integer workers = unit.narrow<IWorker>().size;
        variable Decimal remainingConsumption = decimalNumber(4 * workers);
        Decimal zero = decimalNumber(0);
        while (remainingConsumption > zero) { // TODO: extract loop body as a function?
            cli.print(Float.format(remainingConsumption.float, 0, 1));
            cli.println(" pounds of consumption unaccounted-for");
            value food = chooseFromList(getFoodFor(unit.owner, turn), "Food stocks owned by player:",
                "No food stocks found", "Food to consume from:", false); // TODO: should only count food *in the same place* (but unit movement away from HQ should ask user how much food to take along, and to choose what food in a similar manner to this)
            if (!food exists) {
                return;
            }
            assert (exists food);
            if (decimalize(food.quantity.number) <= remainingConsumption) {
                switch (cli.inputBooleanInSeries("Consume all of the ``food``?",
                        "consume-all-of"))
                case (true) {
                    removeFoodStock(food, unit.owner);
                    remainingConsumption -= decimalize(food.quantity.number);
                    continue;
                }
                case (false) { // TODO: extract this as a function?
                    value amountToConsume = cli.inputDecimal("How many pounds of the ``food`` to consume:");
                    if (exists amountToConsume, amountToConsume >= decimalize(food.quantity.number)) {
                        removeFoodStock(food, unit.owner);
                        remainingConsumption -= decimalize(food.quantity.number);
                        continue;
                    } else if (exists amountToConsume) {
                        reduceFoodBy(food, amountToConsume, unit.owner);
                        remainingConsumption -= amountToConsume;
                        continue;
                    } else {
                        return;
                    }
                }
                case (null) { return; }
            } // else
            switch (cli.inputBooleanInSeries("Eat all remaining ``remainingConsumption`` from the ``food``?",
                "all-remaining"))
            case (true) {
                reduceFoodBy(food, remainingConsumption, unit.owner);
                remainingConsumption = decimalize(0);
            }
            case (false) { // TODO: extract this as a function?
                value amountToConsume = cli.inputDecimal("How many pounds of the ``food`` to consume:");
                if (exists amountToConsume, amountToConsume >= remainingConsumption) {
                    reduceFoodBy(food, remainingConsumption, unit.owner);
                    remainingConsumption = decimalize(0);
                    continue;
                } else if (exists amountToConsume) {
                    reduceFoodBy(food, amountToConsume, unit.owner);
                    remainingConsumption -= amountToConsume;
                    continue;
                } else {
                    return;
                }
            }
            case (null) { return; }
        }
    }

    String? runFoodSpoilage(Player owner, Integer turn) {
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
                        return null;
                    }
                }
                case (false) { continue; }
                case (null) { return null; }
            } else {
                return null;
            }
        }
        return buffer.string;
    }

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
            runFoodConsumption(unit, turn);
        }
        if (exists runFoodSpoilageAnswer = cli.inputBooleanInSeries(
                "Run food spoilage and report it under this unit's results?"),
                runFoodSpoilageAnswer) {
            if (exists foodSpoilageResult = runFoodSpoilage(unit.owner, turn)) {
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
