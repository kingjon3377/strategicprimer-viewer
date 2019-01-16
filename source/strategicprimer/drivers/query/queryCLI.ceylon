import strategicprimer.model.common.map {
    HasPopulation,
    IFixture,
    Player,
    HasOwner,
    TileFixture,
    TileType,
    MapDimensions,
    Point,
    IMapNG,
    IMutableMapNG
}
import ceylon.collection {
    MutableSet,
    LinkedList,
    ArrayList,
    MutableList,
    HashSet,
    Queue
}
import strategicprimer.drivers.common {
    SPOptions,
    ParamCount,
    IDriverUsage,
    DriverUsage,
    IDriverModel,
    IMultiMapModel,
    CLIDriver,
    ModelDriverFactory,
    DriverFactory,
    ModelDriver,
    SimpleMultiMapModel
}
import strategicprimer.model.common.map.fixtures {
    Ground,
    Quantity
}
import strategicprimer.drivers.common.cli {
    ICLIHelper,
    SimpleApplet,
    AppletChooser
}
import lovelace.util.common {
    todo,
    defer,
    PathWrapper
}
import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}
import ceylon.numeric.float {
    ceiling,
    sqrt,
    round=halfEven
}
import strategicprimer.model.common.map.fixtures.mobile {
    IWorker,
    Animal,
    AnimalTracks,
    animalPlurals
}
import strategicprimer.drivers.exploration.common {
    surroundingPointIterable,
    pathfinder,
    HuntingModel
}
import strategicprimer.model.common {
    DistanceComparator
}
import ceylon.logging {
    logger,
    Logger
}
import strategicprimer.model.common.map.fixtures.towns {
    ITownFixture,
    TownStatus,
    Village
}
import strategicprimer.model.common.map.fixtures.resources {
    Grove,
    Shrub,
    Meadow
}

import ceylon.language.meta.model {
    ValueConstructor
}

"A logger."
Logger log = logger(`module strategicprimer.drivers.query`);

"""A factory for the driver to "query" the driver model about various things."""
service(`interface DriverFactory`)
shared class QueryCLIFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage(false, ["query"],
        ParamCount.atLeastOne, "Answer questions about a map.",
        "Look a tiles on a map. Or run hunting, gathering, or fishing.", true, false);

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) => QueryCLI(cli, model);

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            SimpleMultiMapModel(map, path);

}

"A driver for 'querying' the driver model about various things."
// FIXME: Write GUI equivalent of query CLI
shared class QueryCLI satisfies CLIDriver {
    "How many hours we assume a working day is for a hunter or such."
    static Integer hunterHours = 10;

    "How many encounters per hour for a hunter or such."
    static Integer hourlyEncounters = 4;

    "Count the workers in an Iterable belonging to a player."
    static Integer countWorkersInIterable(Player player, {IFixture*} fixtures) {
        variable Integer retval = 0;
        for (fixture in fixtures) {
            if (is IWorker fixture, is HasOwner fixtures, player == fixtures.owner) {
                retval++;
            } else if (is {IFixture*} fixture) {
                retval += countWorkersInIterable(player, fixture);
            }
        }
        return retval;
    }

    "The distance between two points in a map with the given dimensions."
    todo("Delegate to DistanceComparator instead of duplicating the algorithm here?")
    static Float distance(Point base, Point destination, MapDimensions dimensions) {
        Integer rawXDiff = base.row - destination.row;
        Integer rawYDiff = base.column - destination.column;
        Integer xDiff;
        if (rawXDiff < (dimensions.rows / 2)) {
            xDiff = rawXDiff;
        } else {
            xDiff = dimensions.rows - rawXDiff;
        }
        Integer yDiff;
        if (rawYDiff < (dimensions.columns / 2)) {
            yDiff = rawYDiff;
        } else {
            yDiff = dimensions.columns - rawYDiff;
        }
        return sqrt((xDiff * xDiff + yDiff * yDiff).float);
    }

    "A description of what could be a single animal or a population of animals."
    static String populationDescription(Animal animal) {
        if (animal.population > 1) {
            return "a group of perhaps ``animal.population
//              `` ``animalPlurals[animal.kind]``"; // TODO: syntax sugar
                `` ``animalPlurals.get(animal.kind)``";
        } else {
            return animal.kind;
        }
    }

    "If argument is a meadow, its status in the format used below; otherwise the empty
     string."
    static String meadowStatus(Anything argument) {
        if (is Meadow argument) {
            return " (``argument.status``)";
        } else {
            return "";
        }
    }

    "Print the String representation of a Float to one decimal place."
    static String formatFloat(Float num) => Float.format(num, 0, 1);

    shared actual IDriverModel model;
    ICLIHelper cli;
    IMapNG map;
    HuntingModel huntModel;
    shared new (ICLIHelper cli, IDriverModel model) {
        this.cli = cli;
        this.model = model;
        map = model.map;
        huntModel = HuntingModel(map);
    }

    "Count the workers belonging to a player."
    void countWorkers({Player*} players) {
        Player[] playerList = players.sequence();
        value choice = cli.chooseFromList(playerList,
            "Players in the map:", "Map contains no players",
            "Owner of workers to count: ", true);
        if (exists player = choice.item) {
            Integer count = countWorkersInIterable(player,
                map.fixtures.map(Entry.item));
            cli.println("``player.name`` has ``count`` workers");
        }
    }

    "Report the distance between two points."
    void printDistance() {
        if (exists start = cli.inputPoint("Starting point:\t"),
            exists end = cli.inputPoint("Destination:\t"),
            exists groundTravel =
                cli.inputBoolean("Compute ground travel distance?")) {
            if (groundTravel) {
                cli.print("Distance (on the ground, in MP cost):\t");
                cli.println(pathfinder.getTravelDistance(map, start, end).first.string);
            }
            else {
                cli.print("Distance (as the crow files, in tiles):\t");
                cli.println(Float.format(distance(start, end, map.dimensions), 0, 0));
            }
        }
    }

    "If the given driver model *has* subordinate maps, add a copy of the given fixture to
     them at the given location iff no fixture with the same ID is already there."
    void addToSubMaps(Point point, TileFixture fixture, Boolean zero) {
        if (is IMultiMapModel model) {
            for (map->[file, _] in model.subordinateMaps) {
                if (!map.fixtures.get(point).map(TileFixture.id).any(fixture.id.equals)) {
                    map.addFixture(point, fixture.copy(zero));
                }
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
                if (is IMultiMapModel model) {
                    for (map->[file , _]in model.subordinateMaps) {
                        if (exists found = map.fixtures.get(point)
                                .find(shuffle(curry(fixture.isSubset))(noop))) {
                            map.removeFixture(point, found);
                        }
                        map.addFixture(point, addend.copy(zero));
                    }
                }
            } else if (is IMultiMapModel model) {
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

    void huntGeneral(
            "How much time is left in the day."
            variable Integer time,
            "How much time is deducted when nothing is found."
            Integer noResultCost,
            """What verb to use to ask how long an encounter took: "gather", "fight",
               "process", e.g."""
            String verb,
            "The source of encounters."
            variable {<Point->Animal|AnimalTracks|HuntingModel.NothingFound>*}
            encounters) {
        while (time > 0, exists loc->encounter = encounters.first) {
            encounters = encounters.rest;
            if (is HuntingModel.NothingFound encounter) {
                cli.println("Found nothing for the next ``noResultCost`` minutes.");
                time -= noResultCost;
            } else if (is AnimalTracks encounter) {
                addToSubMaps(loc, encounter, true);
                cli.println("Found only tracks or traces from ``
                    encounter.kind`` for the next ``noResultCost`` minutes.");
                time -= noResultCost;
            } else {
                Boolean? fight = cli.inputBooleanInSeries("Found ``
                        populationDescription(encounter)``. Should they ``verb``?",
                    encounter.kind);
                if (is Null fight) {
                    return;
                } else if (fight) {
                    Integer cost = cli.inputNumber("Time to ``verb``: ")
                    else runtime.maxArraySize;
                    time -= cost;
                    Boolean? processNow =
                        cli.inputBooleanInSeries("Handle processing now?");
                    if (is Null processNow) {
                        return;
                    } else if (processNow) {
                        // TODO: somehow handle processing-in-parallel case
                        for (i in 0:(cli.inputNumber("How many animals?") else 0)) {
                            Integer mass =
                                cli.inputNumber(
                                    "Weight of this animal's meat in pounds: ")
                                else runtime.maxArraySize;
                            Integer hands =
                                cli.inputNumber("# of workers processing this carcass: ")
                                else 1;
                            time -=
                                round(HuntingModel.processingTime(mass) / hands).integer;
                        }
                    }
                    switch (cli.inputBooleanInSeries("Reduce animal group population of ``
                        encounter.population``?"))
                    case (true) { reducePopulation(loc, encounter, "animals", true); }
                    case (false) { addToSubMaps(loc, encounter, true); }
                    case (null) { return; }
                    cli.println("``time`` minutes remaining.");
                } else {
                    addToSubMaps(loc, encounter, true);
                    time -= noResultCost;
                }
            }
        }
    }

    """Run hunting---that is, produce a list of "encounters"."""
    todo("Distinguish hunting from fishing in no-result time cost?")
    void hunt(Point point,
        "How long to spend hunting."
        Integer time) => huntGeneral(time, 60 / hourlyEncounters,
            "fight and process", huntModel.hunt(point));

    """Run fishing---that is, produce a list of "encounters"."""
    void fish(Point point,
        "How long to spend hunting."
        Integer time) => huntGeneral(time, 60 / hourlyEncounters,
            "try to catch and process", huntModel.fish(point));

    """Run food-gathering---that is, produce a list of "encounters"."""
    void gather(Point point, variable Integer time) {
        variable {<Point->Grove|Shrub|Meadow|HuntingModel.NothingFound>*} encounters =
            huntModel.gather(point);
        Integer noResultCost = 60 / hourlyEncounters;
        while (time > 0, exists loc->encounter = encounters.first) {
            encounters = encounters.rest;
            if (is HuntingModel.NothingFound encounter) {
                cli.println("Found nothing for the next ``noResultCost`` minutes.");
                time -= noResultCost;
                continue;
            } else {
                switch (cli.inputBooleanInSeries(
                    "Found ``encounter.shortDescription````
                    meadowStatus(encounter)``. Should they gather?", encounter.kind))
                case (true) {
                    Integer cost = cli.inputNumber("Time to gather: ")
                    else runtime.maxArraySize;
                    time -= cost;
                    // TODO: Once model supports remaining-quantity-in-fields data, offer to reduce it here
                    if (is Shrub encounter, encounter.population>0) {
                        switch (cli.inputBooleanInSeries("Reduce shrub population here?"))
                        case (true) {
                            reducePopulation(loc, encounter, "plants", true);
                            cli.println("``time`` minutes remaining.");
                            continue;
                        }
                        case (false) {}
                        case (null) {
                            return;
                        }
                    }
                    cli.println("``time`` minutes remaining.");
                }
                case (false) { time -= noResultCost; }
                case (null) { return; }
            }
            addToSubMaps(loc, encounter, true);
        }
    }

    """Handle herding mammals. Returns how many hours each herder spends "herding." """
    Integer herdMammals(MammalModel animal, Integer count, Integer flockPerHerder) {
        cli.print("Taking the day's two milkings together, tending the animals takes ");
        cli.print((flockPerHerder * animal.dailyTimePerHead).string);
        cli.println(" minutes, or");
        cli.print((flockPerHerder * (animal.dailyTimePerHead - 10)).string);
        cli.print(" with expert herders, plus ");
        cli.print(animal.dailyTimeFloor.string);
        cli.println(" minutes to gather them. This produces ");
        Quantity base = animal.scaledProduction(count);
        Float production = base.floatNumber;
        cli.print(formatFloat(production));
        cli.print(" ");
        cli.print(base.units);
        cli.print(", ");
        cli.print(formatFloat(animal.scaledPoundsProduction(count)));
        cli.println(" lbs, of milk per day.");
        Integer cost;
        assert (exists experts = cli.inputBooleanInSeries("Are the herders experts? "));
        if (experts) {
            cost = animal.dailyExpertTime(flockPerHerder);
        } else {
            cost = animal.dailyTime(flockPerHerder);
        }
        return (ceiling(cost / 60.0) + 0.1).integer;
    }

    """Handle herding mammals. Returns how many hours each herder spends "herding." """
    Integer herdPoultry(PoultryModel bird, Integer count, Integer flockPerHerder) {
        cli.print("Gathering eggs takes ");
        cli.print(bird.dailyTime(flockPerHerder).string);
        cli.println(" minutes; cleaning up after them,");
        cli.print("which should be done at least every ");
        cli.print((bird.extraChoresInterval + 1).string);
        cli.print(" turns, takes ");
        cli.print(formatFloat(bird.dailyExtraTime(flockPerHerder) / 60.0));
        cli.println(" hours. This produces");
        Quantity base = bird.scaledProduction(count);
        Float production = base.floatNumber;
        cli.print(Float.format(production, 0, 0));
        cli.print(" ");
        cli.print(base.units);
        cli.print(", totaling ");
        cli.print(formatFloat(bird.scaledPoundsProduction(count)));
        cli.println(" lbs.");
        Integer cost;
        assert (exists cleaning =
            cli.inputBooleanInSeries("Do they do the cleaning this turn? "));
        if (cleaning) {
            cost = bird.dailyTimePerHead + bird.extraTimePerHead;
        } else {
            cost = bird.dailyTimePerHead;
        }
        return (ceiling((flockPerHerder * cost) / 60.0) + 0.1).integer;
    }

    "Run herding."
    void herd() {
        assert (exists herdModel = cli.chooseFromList(
            `MammalModel`.getValueConstructors()
                .chain(`PoultryModel`.getValueConstructors())
                .narrow<ValueConstructor<HerdModel>>()
                .map((model) => model.get()).sequence(),
            "What kind of animals are these?", "No animal kinds found",
            "Kind of animal:", false).item);
        Integer count = cli.inputNumber("How many animals?\t") else 0;
        if (count == 0) {
            cli.println("With no animals, no cost and no gain.");
            return;
        } else if (count < 0) {
            cli.println("Can't have a negative number of animals.");
            return;
        } else {
            Integer herders = cli.inputNumber("How many herders?\t") else 0;
            if (herders <= 0) {
                cli.println("Can't herd with no herders.");
                return;
            }
            Integer flockPerHerder = ((count + herders) - 1) / herders;
            Integer hours;
            if (is PoultryModel herdModel) {
                hours = herdPoultry(herdModel, count, flockPerHerder);
            } else {
                hours = herdMammals(herdModel, count, flockPerHerder);
            }
            if (hours < hunterHours,
                    exists gatherNow = cli.inputBooleanInSeries(
                        "Spend remaining time as Food Gatherers? "), gatherNow,
                    exists location = cli.inputPoint("Gathering location? ")) {
                gather(location, hunterHours - hours);
            }
        }
    }

    "Give the data about a tile that the player is supposed to automatically know if he
     has a fortress on it. For the convenience of the sole caller, this method accepts
     [[null]], and does nothing when that is the parameter."
    void fortressInfo(Point? location) {
        if (exists location) {
            cli.println("Terrain is ``map.baseTerrain[location] else "unknown"``");
//          Ground[] ground = map.fixtures[location].narrow<Ground>().sequence();
            Ground[] ground = map.fixtures.get(location).narrow<Ground>().sequence();
//          Forest[] forests = map.fixtures[location].narrow<Forest>().sequence();
            Forest[] forests = map.fixtures.get(location).narrow<Forest>().sequence();
            if (nonempty ground) {
                cli.println("Kind(s) of ground (rock) on the tile:");
                ground.map(Object.string).each(cli.println);
            }
            if (nonempty forests) {
                cli.println("Kind(s) of forests on the tile:");
                forests.map(Object.string).each(cli.println);
            }
        }
    }

    "Find the nearest obviously-reachable unexplored location."
    Point? findUnexplored(Point base) {
        Queue<Point> queue = LinkedList<Point>();
        queue.offer(base);
        MapDimensions dimensions = map.dimensions;
        MutableSet<Point> considered = HashSet<Point>();
        MutableList<Point> retval = ArrayList<Point>();
        while (exists current = queue.accept()) {
            TileType? currentTerrain = map.baseTerrain[current];
            if (considered.contains(current)) {
                continue;
            } else if (exists currentTerrain) {
                if (currentTerrain != TileType.ocean) {
                    Float baseDistance = distance(base, current, dimensions);
                    for (neighbor in surroundingPointIterable(current, dimensions, 1)) {
                        if (distance(base, neighbor, dimensions) >= baseDistance) {
                            queue.offer(neighbor);
                        }
                    }
                }
            } else {
                retval.add(current);
            }
            considered.add(current);
        }
        return retval.sort(DistanceComparator(base, dimensions).compare).first;
    }

    "Print a list of active towns within the given distance of the given base that produce
     any resources, and what resources they produce."
    void suggestTrade(Point base, Integer distance) {
        value comparator = DistanceComparator(base, map.dimensions);
        for (location in surroundingPointIterable(base, map.dimensions, distance).distinct
                .sort(comparator.compare)) { // TODO: can we combine loops?
            for (town in map.fixtures.get(location).narrow<ITownFixture>()) {
                //for (town in map.fixtures[location].narrow<ITownFixture>()) { // TODO: syntax sugar once compiler bug fixed
                if (town.status == TownStatus.active, exists population = town.population,
                        !population.yearlyProduction.empty) {
                    cli.print("At ");
                    cli.print(location.string);
                    cli.print(comparator.distanceString(location, "base"));
                    cli.print(": ");
                    cli.print(town.name);
                    cli.print(", a");
                    cli.print(town.townSize.string);
                    cli.print(" ");
                    if (is Village town, town.race != "human") {
                        cli.print(town.race);
                        cli.print(" village");
                    } else {
                        cli.print(town.kind);
                    }
                    cli.println(". Its yearly production:");
                    for (resource in population.yearlyProduction) {
                        cli.print("- ");
                        cli.print(resource.kind);
                        cli.print(": ");
                        cli.print(resource.quantity.number.string);
                        if (resource.quantity.units.empty) {
                            cli.print(" ");
                        } else if ("dozen" == resource.quantity.units) {
                            cli.print(" dozen ");
                        } else {
                            cli.print(" ");
                            cli.print(resource.quantity.units);
                            cli.print(" of ");
                        }
                        cli.println(resource.contents);
                        if (resource.contents == "milk") {
                            cli.println("- Corresponding livestock");
                        } else if (resource.contents == "eggs") {
                            cli.println("- Corresponding poultry");
                        }
                    }
                }
            }
        }
    }

    void findUnexploredCommand() {
        if (exists base = cli.inputPoint("Starting point? ")) {
            if (exists unexplored = findUnexplored(base)) {
                Float distanceTo = distance(base, unexplored, map.dimensions);
                cli.println("Nearest unexplored tile is ``unexplored``, ``Float
                    .format(distanceTo, 0, 1, '.', ',')`` tiles away");
            } else {
                cli.println("No unexplored tiles found.");
            }
        }
    }

    void tradeCommand() {
        if (exists location = cli.inputPoint("Base location? ")) {
            suggestTrade(location, cli.inputNumber("Within how many tiles? ") else 0);
        }
    }

    Anything() deferAction(Anything(Point, Integer) method, String verb) =>
        // TODO: Replace with method-reference logic using defer()
            () {
            if (exists location = cli.inputPoint("Location to ``verb``? ")) {
                return method (location, hunterHours * 60);
            } else {
                return null;
            }
        };

    AppletChooser<SimpleApplet> appletChooser = AppletChooser(cli,
        SimpleApplet(defer(compose(fortressInfo, cli.inputPoint),
                ["Location of fortress?"]),
            "Show what a player automatically knows about a fortress's tile.",
            "fortress"),
        SimpleApplet(deferAction(hunt, "hunt"), "Generate encounters with land animals",
            "hunt"),
        SimpleApplet(deferAction(fish, "fish"), "Generate encounters with aquatic animals",
            "fish"),
        SimpleApplet(deferAction(gather, "gather"),
            "Generate encounters with fields, meadows, groves, orchards, or shrubs.",
            "gather"),
        SimpleApplet(herd,
            "Determine the output from and time required for maintaining a herd.",
            "herd"),
        SimpleApplet(compose(TrappingCLI.startDriver, TrappingCLI)(cli, model),
            "Switch to a trap-modeling program to run trapping or fish-trapping.",
            "trap"),
        SimpleApplet(printDistance, "Report the distance between two points.", "distance"),
        SimpleApplet(defer(countWorkers, [model.map.players]),
            "Count how many workers belong to a player", "count"),
        SimpleApplet(findUnexploredCommand,
            "Find the nearest unexplored tile not behind water.", "unexplored"),
        SimpleApplet(tradeCommand, "Suggest possible trading partners.", "trade"));

    "Accept and respond to commands."
    shared actual void startDriver() {
        while (true) {
            switch (selection = appletChooser.chooseApplet())
            case (null|true) { continue; }
            case (false) { break; }
            case (is SimpleApplet) {
                selection.invoke();
            }
        }
    }
}
