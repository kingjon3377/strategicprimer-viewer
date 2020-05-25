import strategicprimer.drivers.common {
    CLIDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper,
    Applet,
    AppletChooser
}
import ceylon.collection {
    MutableList,
    ArrayList,
    HashMap,
    MutableMap,
    naturalOrderTreeMap
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit,
    Animal,
    IWorker,
    animalPlurals,
    AnimalTracks,
    ProxyUnit,
    ProxyFor
}
import strategicprimer.drivers.exploration.common {
    IExplorationModel,
    HuntingModel
}
import strategicprimer.viewer.drivers.exploration {
    ExplorationCLIHelper
}
import strategicprimer.model.common.map.fixtures.towns {
    Fortress
}
import lovelace.util.common {
    matchingValue,
    todo,
    comparingOn
}
import strategicprimer.drivers.query {
    HerdModel,
    MammalModel,
    PoultryModel,
    SmallAnimalModel
}
import ceylon.language.meta.model {
    ValueConstructor
}
import com.vasileff.ceylon.structures {
    HashMultimap,
    MutableMultimap
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    IJob
}
import strategicprimer.model.common.map.fixtures {
    Quantity,
    ResourcePile,
    FortressMember
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
    Point,
    TileFixture,
    HasPopulation,
    HasOwner,
    Player
}
import strategicprimer.model.common.map.fixtures.resources {
    Grove,
    Shrub,
    Meadow
}
import ceylon.numeric.float {
    round=halfEven
}
import strategicprimer.viewer.drivers.resourceadding {
    ResourceAddingCLIHelper
}

import ceylon.decimal {
    Decimal,
    decimalNumber
}

import lovelace.util.jvm {
    decimalize
}

class TurnApplet(shared actual String() invoke, shared actual String description,
    shared actual String+ commands) satisfies Applet {}

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
            .flatMap((indivMap) =>
        getUnitsImpl(indivMap.fixtures.items, player));
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

    Fortress? containingFortress(IUnit unit) =>
        model.map.fixtures.get(model.find(unit)).narrow<Fortress>()
            .find(matchingValue(unit.owner, Fortress.owner));

    // TODO: If class converted from initializer to constructor, make this static
    String inHours(Integer minutes) {
        if (minutes < 60) {
            return "``minutes`` minutes";
        } else {
            return "``minutes / 60`` hours, ``minutes % 60`` minutes";
        }
    }

    MutableMap<String, HerdModel> herdModels = HashMap<String, HerdModel>();
    HerdModel? chooseHerdModel(String animal) => cli.chooseFromList(
        `MammalModel`.getValueConstructors().chain(`PoultryModel`.getValueConstructors())
            .chain(`SmallAnimalModel`.getValueConstructors())
            .narrow<ValueConstructor<HerdModel>>().map((model) => model.get()).sequence(),
        "What kind of animal(s) is/are ``animal``?", "No animal kinds found",
        "Kind of animal:", false).item;

    String herd() {
        assert (exists unit = model.selectedUnit);
        StringBuilder buffer = StringBuilder();
        Fortress? home = containingFortress(unit);
        for (kind in unit.narrow<Animal>().map(Animal.kind).distinct
                .filter(not(herdModels.keys.contains))) {
            if (exists herdModel = chooseHerdModel(kind)) {
                herdModels[kind] = herdModel;
            } else {
                cli.println("Aborting ...");
                return "";
            }
        }
        MutableMultimap<HerdModel, Animal> modelMap = HashMultimap<HerdModel, Animal>();
        for (group in unit.narrow<Animal>()
                .filter(or(matchingValue("tame", Animal.status),
                    matchingValue("domesticated", Animal.status)))) {
            assert (exists herdModel = herdModels[group.kind]);
            modelMap.put(herdModel, group);
        }
        Integer workerCount = unit.narrow<IWorker>().size;
        Boolean experts = unit.narrow<IWorker>().map(shuffle(IWorker.getJob)("herder"))
            .map(IJob.level).map((-5).plus).any(Integer.positive);
        variable Integer minutesSpent = 0;
        void addToOrders(String string) {
            cli.print(string);
            buffer.append(string);
        }
        void addLineToOrders(String string) {
            cli.println(string);
            buffer.append(string);
            buffer.appendNewline();
        }
        for (herdModel->animals in modelMap.asMap) {
            if (!buffer.empty) {
                buffer.appendNewline();
                buffer.appendNewline();
            }
            assert (exists combinedAnimal = animals.reduce(uncurry(Animal.combined)));
            Integer flockPerHerder =
                (combinedAnimal.population + workerCount - 1) / workerCount;
            Quantity production = herdModel.scaledProduction(combinedAnimal.population);
            Float pounds = herdModel.scaledPoundsProduction(combinedAnimal.population);
            String resourceProduced;
            switch (herdModel)
            case (is PoultryModel) {
                resourceProduced = combinedAnimal.kind + " eggs";
                Boolean? cleaningDay = cli.inputBoolean("Is this the one turn in every ``
                        herdModel.extraChoresInterval + 1`` to clean up after birds?");
                addToOrders("Gathering ``combinedAnimal`` eggs took the ``workerCount``");
                addLineToOrders(" workers ``herdModel.dailyTime(flockPerHerder)`` min.");
                minutesSpent += herdModel.dailyTimePerHead * flockPerHerder;
                if (exists cleaningDay, cleaningDay) {
                    addToOrders("Cleaning up after them takes ");
                    addToOrders(Float.format(
                        herdModel.dailyExtraTime(flockPerHerder) / 60.0, 0, 1));
                    addLineToOrders(" hours.");
                    minutesSpent += herdModel.extraTimePerHead * flockPerHerder;
                } else if (is Null cleaningDay) {
                    return "";
                }
            }
            case (is MammalModel) {
                resourceProduced = "milk";
                addToOrders("Between two milkings, tending the ");
                addToOrders(animalPlurals.get(combinedAnimal.kind));
                Integer baseCost;
                if (experts) {
                    baseCost = flockPerHerder * (herdModel.dailyTimePerHead - 10);
                } else {
                    baseCost = flockPerHerder * herdModel.dailyTimePerHead;
                }
                addToOrders(" took ``baseCost`` min, plus ");
                addToOrders(herdModel.dailyTimeFloor.string);
                addLineToOrders(" min to gather them.");
                minutesSpent += baseCost;
                minutesSpent += herdModel.dailyTimeFloor;
            }
            case (is SmallAnimalModel) {
                addToOrders("Tending the ");
                addToOrders(animalPlurals.get(combinedAnimal.kind));
                Integer baseCost;
                if (experts) {
                    baseCost = ((flockPerHerder * herdModel.dailyTimePerHead +
                        herdModel.dailyTimeFloor) * 0.9).integer;
                } else {
                    baseCost = flockPerHerder * herdModel.dailyTimePerHead +
                        herdModel.dailyTimeFloor;
                }
                minutesSpent += baseCost;
                addLineToOrders(" took the ``workerCount`` workers ``baseCost`` min.");
                switch (cli.inputBoolean("Is this the one turn in every ``
                    herdModel.extraChoresInterval + 1`` to clean up after the animals?"))
                case (true) {
                    addToOrders("Cleaning up after them took ");
                    addToOrders((herdModel.extraTimePerHead * flockPerHerder).string);
                    addLineToOrders(" minutes.");
                    minutesSpent += herdModel.extraTimePerHead * flockPerHerder;
                }
                case (false) {}
                case (null) { return ""; }
                continue;
            }
            addToOrders("This produced ");
            addToOrders(Float.format(production.floatNumber, 0, 1));
            addToOrders(" ``production.units``, ");
            addToOrders(Float.format(pounds, 0, 1));
            addLineToOrders(" lbs, of ``resourceProduced``.");
            if (exists home) {
                ResourcePile createdResource = ResourcePile(idf.createID(), "food", resourceProduced,
                    production);
                createdResource.created = model.map.currentTurn;
                home.addMember(createdResource);
            }
        }
        addToOrders("In all, tending the animals took ``inHours(minutesSpent)``.");
        return buffer.string.trimmed;
    }

    ExplorationCLIHelper explorationCLI = ExplorationCLIHelper(model, cli);
    model.addMovementCostListener(explorationCLI);

    String explore() {
        StringBuilder buffer = StringBuilder();
        model.addSelectionChangeListener(explorationCLI);
        assert (exists mover = model.selectedUnit);
        // Ask the user about total MP
        model.selectedUnit = mover;
        while (explorationCLI.movement > 0) {
            explorationCLI.moveOneStep();
            if (exists addendum = cli.inputMultilineString("Add to results:")) {
                buffer.append(addendum);
            } else {
                return "";
            }
        }
        // We don't want to be asked about MP for any other applets
        model.removeSelectionChangeListener(explorationCLI);
        return buffer.string;
    }

    HuntingModel huntingModel = HuntingModel(model.map);

    Integer encountersPerHour = 4;

    Integer noResultCost = 60 / encountersPerHour;

    "If argument is a meadow, its status in the format used below; otherwise the empty
     string."
    // TODO: If this class switches from initializer to constructor, make static
    String meadowStatus(Anything argument) {
        if (is Meadow argument) {
            return " (``argument.status``)";
        } else {
            return "";
        }
    }

    "Add a copy of the given fixture to all submaps at the given location iff no fixture
     with the same ID is already there."
    void addToSubMaps(Point point, TileFixture fixture, Boolean zero) {
        for (map->[file, _] in model.subordinateMaps) {
            if (!map.fixtures.get(point).map(TileFixture.id).any(fixture.id.equals)) {
                map.addFixture(point, fixture.copy(zero));
            }
        }
    }

    "Reduce the population of a group of plants, animals, etc., and copy the reduced form
     into all subordinate maps."
    void reducePopulation(Point point, HasPopulation<out TileFixture>&TileFixture fixture,
        String plural, Boolean zero) {
        Integer count = Integer.smallest(cli.inputNumber(
            "How many ``plural`` to remove: ") else 0, fixture.population);
        if (count > 0) {
            model.map.removeFixture(point, fixture);
            Integer remaining = fixture.population - count;
            if (remaining > 0) {
                value addend = fixture.reduced(remaining);
                model.map.addFixture(point, addend);
                for (map->[file , _]in model.subordinateMaps) {
                    if (exists found = map.fixtures.get(point)
                            .find(shuffle(curry(fixture.isSubset))(noop))) {
                        map.removeFixture(point, found);
                    }
                    map.addFixture(point, addend.copy(zero));
                }
            } else {
                for (map->[file, _] in model.subordinateMaps) {
                    if (exists found = map.fixtures.get(point)
                            .find(shuffle(curry(fixture.isSubset))(noop))) {
                        map.removeFixture(point, found);
                    }
                }
            }
        } else {
            addToSubMaps(point, fixture, zero);
        }
    }

    ResourceAddingCLIHelper resourceAddingHelper = ResourceAddingCLIHelper(cli, idf);

    void addResourceToMaps(FortressMember resource, Player owner, String fortName = "HQ") {
        for (map in model.allMaps.map(Entry.key)) {
            // TODO: Make a way to add to units
            Fortress hq;
            variable Fortress? fort = null;
            for (fortress in map.fixtures.items.narrow<Fortress>()
                    .filter(matchingValue(owner, Fortress.owner))) {
                if (fortress.name == fortName) {
                    hq = fortress;
                    break;
                } else if (!fort exists) {
                    fort = fortress;
                }
            } else {
                if (exists fortress = fort) {
                    hq = fortress;
                } else {
                    continue;
                }
            }
            hq.addMember(resource);
        }
    }

    Point? confirmPoint(String prompt) {
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

    String gather() {
        StringBuilder buffer = StringBuilder();
        if (exists center = confirmPoint("Location to search around: "),
                exists startingTime = cli.inputNumber("Minutes to spend gathering: ")) {
            variable Integer time = startingTime;
            variable {<Point->Grove|Shrub|Meadow|HuntingModel.NothingFound>*} encounters =
                huntingModel.gather(center);
            while (time > 0, exists loc->find = encounters.first) {
                encounters = encounters.rest;
                if (is HuntingModel.NothingFound find) {
                    cli.println("Found nothing for the next ``noResultCost`` minutes.");
                    time -= noResultCost;
                } else {
                    switch (cli.inputBooleanInSeries(
                        "Gather from ``find.shortDescription````meadowStatus(find)``?",
                        find.kind))
                    case (true) {
                        if (exists unit = model.selectedUnit) {
                            cli.println(
                                "Enter details of harvest (any empty string aborts):");
                            while (exists resource =
                                    resourceAddingHelper.enterResource()) {
                                addResourceToMaps(resource, unit.owner);
                            }
                        }
                        Integer cost = cli.inputNumber("Time to gather: ")
                            else runtime.maxArraySize;
                        time -= cost;
                        // TODO: Once model supports remaining-quantity-in-fields data, offer to reduce it here
                        if (is Shrub find, find.population>0) {
                            switch (cli.inputBooleanInSeries(
                                "Reduce shrub population here?"))
                            case (true) {
                                reducePopulation(loc, find, "plants", true);
                                cli.print(inHours(time));
                                cli.println("remaining.");
                                continue;
                            }
                            case (false) {}
                            case (null) { return ""; }
                        }
                        cli.print(inHours(time));
                        cli.println(" remaining.");
                    }
                    case (false) { time -= noResultCost; }
                    case (null) { return ""; }
                    addToSubMaps(loc, find, true);
                }
                if (exists addendum = cli.inputMultilineString(
                        "Add to results about that:")) {
                    buffer.append(addendum);
                } else {
                    return "";
                }
            }
        }
        return buffer.string.trimmed;
    }

    "A description of what could be a single animal or a population of animals."
    // TODO: If this class converts from initializer to constructor, make static
    String populationDescription(Animal animal) {
        if (animal.population > 1) {
            return "a group of perhaps ``animal.population
//              `` ``animalPlurals[animal.kind]``"; // TODO: syntax sugar
            `` ``animalPlurals.get(animal.kind)``";
        } else {
            return animal.kind;
        }
    }

    // TODO: Distinguish hunting from fishing in no-result time cost (encounters / hour)?
    String huntGeneral(String command, String verb,
            {<Point->Animal|AnimalTracks|HuntingModel.NothingFound>*}(Point) encounterSrc)
            () {
        StringBuilder buffer = StringBuilder();
        if (exists center = confirmPoint("Location to search around: "),
                exists startingTime = cli
                    .inputNumber("Minutes to spend ``command``ing: ")) {
            variable Integer time = startingTime;
            variable {<Point->Animal|AnimalTracks|HuntingModel.NothingFound>*} encounters
                = encounterSrc(center);
            while (time > 0, exists loc->find = encounters.first) {
                encounters = encounters.rest;
                if (is HuntingModel.NothingFound find) {
                    cli.println("Found nothing for the next ``noResultCost`` minutes.");
                    time -= noResultCost;
                } else if (is AnimalTracks find) {
                    addToSubMaps(loc, find, true);
                    cli.println("Found only tracks or traces from ``
                        find.kind`` for the next ``noResultCost`` minutes.");
                    time -= noResultCost;
                } else {
                    Boolean? fight = cli.inputBooleanInSeries("Found ``
                            populationDescription(find)``. Should they ``verb``?",
                        find.kind);
                    if (is Null fight) {
                        return "";
                    } else if (fight) {
                        Integer cost = cli.inputNumber("Time to ``verb``: ")
                            else runtime.maxArraySize;
                        time -= cost;
                        Boolean? processNow =
                            cli.inputBooleanInSeries("Handle processing now?");
                        if (is Null processNow) {
                            return "";
                        } else if (processNow) {
                            // TODO: somehow handle processing-in-parallel case
                            for (i in 0:(cli.inputNumber("How many animals?") else 0)) {
                                Integer mass = cli.inputNumber(
                                            "Weight of this animal's meat in pounds: ")
                                        else runtime.maxArraySize;
                                Integer hands = cli.inputNumber(
                                    "# of workers processing this carcass: ")else 1;
                                time -= round(HuntingModel.processingTime(mass) / hands)
                                    .integer;
                            }
                        }
                        switch (cli.inputBooleanInSeries(
                            "Reduce animal group population of ``find.population``?"))
                        case (true) { reducePopulation(loc, find, "animals", true); }
                        case (false) { addToSubMaps(loc, find, true); }
                        case (null) { return ""; }
                        cli.print(inHours(time));
                        cli.println(" remaining.");
                        if (exists unit = model.selectedUnit) {
                            cli.println(
                                "Enter resources produced (any empty string aborts):");
                            while (exists resource =
                                resourceAddingHelper.enterResource()) {
                                addResourceToMaps(resource, unit.owner);
                            }
                        }
                    } else {
                        addToSubMaps(loc, find, true);
                        time -= noResultCost;
                    }
                }
                if (exists addendum = cli.inputMultilineString(
                        "Add to results about that:")) {
                    buffer.append(addendum);
                } else {
                    return "";
                }
            }
        }
        return buffer.string.trimmed;
    }

    // TODO: If converted from initializer to constructor, make static
    TrapperCommand[] trapperCommands = sort(`TrapperCommand`.caseValues);

    String trap() {
        StringBuilder buffer = StringBuilder();
        if (exists fishing = cli.inputBooleanInSeries(
                    "Is this a fisherman trapping fish rather than a trapper?"),
                exists center = confirmPoint("Location to search around: "),
                exists startingTime = cli
                    .inputNumber("Minutes to spend working: ")) {
            variable {<Point->Animal|AnimalTracks|HuntingModel.NothingFound>*} encounters;
            String prompt;
            if (fishing) {
                encounters = huntingModel.fish(center);
                prompt = "What should the fisherman do next?";
            } else {
                encounters = huntingModel.hunt(center);
                prompt = "What should the trapper do next?";
            }
            variable Integer time = startingTime;
            while (time > 0, exists command = cli.chooseFromList(trapperCommands, prompt,
                        "Oops! No commands", "Next action: ", false).item,
                    command != TrapperCommand.quit) {
                switch (command)
                case (TrapperCommand.check) {
                    value find = encounters.first;
                    if (!find exists) {
                        cli.println("Ran out of results!");
                        break;
                    }
                    assert (exists loc->item = find);
                    if (is HuntingModel.NothingFound item) {
                        cli.println("Nothing in the trap");
                        if (fishing) {
                            time -= 5;
                        } else {
                            time -= 10;
                        }
                    } else if (is AnimalTracks item) {
                        cli.println("Found evidence of ``item.kind`` escaping");
                        addToSubMaps(center, item, true);
                        if (fishing) {
                            time -= 5;
                        } else {
                            time -= 10;
                        }
                    } else {
                        cli.println(
                            "Found either ``item.kind`` or evidence of it escaping.");
                        if (exists num = cli.inputNumber(
                                "How long to check and deal with the animal? ")) {
                            time -= num;
                        } else {
                            return "";
                        }
                        switch (cli.inputBooleanInSeries("Handle processing now?"))
                        case (true) {
                            if (exists mass = cli.inputNumber(
                                        "Weight of meat in pounds: "),
                                    exists hands = cli.inputNumber(
                                        "# of workers processing this carcass: ")) {
                                time -= round(HuntingModel.processingTime(mass) / hands)
                                    .integer;
                            } else {
                                return "";
                            }
                        }
                        case (false) { }
                        case (null) { return ""; }
                        switch (cli.inputBooleanInSeries(
                            "Reduce animal group population of ``item.population``?"))
                        case (true) {
                            if (exists delenda = cli.inputNumber("Animals to remove: ")) {
                                Integer count = Integer.smallest(delenda,
                                    item.population);
                                if (count > 0) {
                                    for (map in model.allMaps.map(Entry.key)) {
                                        if (exists population = map.fixtures.get(loc)
                                                .narrow<Animal>().find(matchingValue(item.id,
                                                Animal.id)), population.population > 0) {
                                            map.removeFixture(loc, population);
                                            Integer remaining =
                                                population.population - count;
                                            if (remaining > 0) {
                                                map.addFixture(loc,
                                                    population.reduced(remaining));
                                            }
                                        }
                                    }
                                    if (model.map.fixtures.get(loc).narrow<Animal>()
                                            .any(matchingValue(item.id, Animal.id))) {
                                        addToSubMaps(center, AnimalTracks(item.kind),
                                            false);
                                    }
                                }
                            }
                        } case (false) {
                            addToSubMaps(center, AnimalTracks(item.kind), false);
                        }
                        case (null) {
                            return "";
                        }
                        if (exists unit = model.selectedUnit) {
                            cli.println(
                                "Enter resources produced (any empty string aborts):");
                            while (exists resource =
                                resourceAddingHelper.enterResource()) {
                                addResourceToMaps(resource, unit.owner);
                            }
                        }
                    }
                }
                case (TrapperCommand.easyReset) {
                    if (fishing) {
                        time -= 20;
                    } else {
                        time -= 5;
                    }
                }
                case (TrapperCommand.move) { time -= 2; }
                case (TrapperCommand.quit) { time = 0; }
                case (TrapperCommand.setTrap) {
                    if (fishing) {
                        time -= 30;
                    } else {
                        time -= 45;
                    }
                }
                cli.print(inHours(time));
                cli.println(" remaining.");
                if (exists addendum = cli.inputMultilineString(
                    "Add to results about that:")) {
                    buffer.append(addendum);
                } else {
                    return "";
                }
            }
        }
        return buffer.string;
    }

    [ResourcePile*] getFoodFor(Player player, Integer turn) { // TODO: Move into the model?
        return model.map.locations.flatMap(model.map.fixtures.get).narrow<Fortress|IUnit>()
            .filter(matchingValue(player, HasOwner.owner)).flatMap(identity).narrow<ResourcePile>()
            .filter(matchingValue("food", ResourcePile.kind)).filter(matchingValue("pounds",
                compose(Quantity.units, ResourcePile.quantity))).filter((r) => r.created <= turn).sequence();
    }

    Type? chooseFromList<Type>(Type[]|List<Type> items, String description, String none, String prompt,
            Boolean auto, String(Type) converter = Object.string) given Type satisfies Object {
        value entry = cli.chooseStringFromList(items.map(converter).sequence(), description, none, prompt, auto);
        if (entry.item exists) {
            return items[entry.key];
        } else {
            return null;
        }
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

    String supplierNoop() => "";

    AdvancementCLIHelper advancementCLI = AdvancementCLIHelper(cli);
    AppletChooser<TurnApplet> appletChooser =
        AppletChooser(cli,
            TurnApplet(explore, "move a unit", "move"),
            TurnApplet(herd, "milk or gather eggs from animals", "herd"),
            TurnApplet(gather, "gather vegetation from surrounding area", "gather"),
            TurnApplet(huntGeneral("hunt", "fight and process", huntingModel.hunt),
                "search for wild animals", "hunt"),
            TurnApplet(huntGeneral("fish", "try to catch and process", huntingModel.fish),
                "search for aquatic animals", "fish"),
            TurnApplet(trap, "check traps for animals or fish they may have caught",
                "trap"),
            TurnApplet(supplierNoop, "something no applet supports", "other"));

    void runFoodConsumption(IUnit unit, Integer turn) {
        Integer workers = unit.narrow<IWorker>().size;
        variable Decimal remainingConsumption = decimalNumber(4 * workers);
        Decimal zero = decimalNumber(0);
        while (remainingConsumption > zero) { // TODO: extract loop body as a function?
            cli.print(remainingConsumption.string); // TODO: limit precision in printing here and elsewhere
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
                        buffer.append(spoilage.string);
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
                    buffer.append(command.invoke());
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
