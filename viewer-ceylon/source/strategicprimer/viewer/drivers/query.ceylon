import ceylon.collection {
    ArrayList,
    Queue,
    LinkedList,
    MutableSet,
    HashSet,
    MutableList
}
import ceylon.math.float {
    sqrt,
    ceiling
}

import java.io {
    IOException
}
import java.lang {
    JInteger=Integer
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.map.fixtures {
    Ground,
    Quantity
}
import strategicprimer.model.map.fixtures.mobile {
    IWorker
}
import strategicprimer.model.map.fixtures.terrain {
    Forest
}

import strategicprimer.viewer.drivers.exploration {
    HuntingModel,
    surroundingPointIterable
}
import strategicprimer.viewer.model {
    IDriverModel,
    DistanceComparator
}
import strategicprimer.model.map {
    Point,
    Player,
    HasOwner,
    TileType,
    IMapNG,
    TileFixture,
    FixtureIterable,
    HasName,
    MapDimensions
}

"Models of (game statistics for) herding."
interface HerdModel of PoultryModel | MammalModel {
    "How much is produced per head per turn, in some model-specified unit."
    shared formal Quantity productionPerHead;
    "The coefficient to turn production into pounds."
    shared formal Float poundsCoefficient;
    "How much time, per head, in minutes, must be spent to milk, gather eggs, or otherwise
     collect the food produced by the animals. Callers may adjust this downward somewhat
     if the herders in question are experts."
    shared formal Integer dailyTimePerHead;
    """How much time is spent for a flock (per herder) of the given size, possibly
       including any time "floor"."""
    shared formal Integer dailyTime(Integer heads);
    "How much is produced by a flock of the given size."
    shared Quantity scaledProduction(Integer heads) => Quantity(
        productionPerHead.floatNumber * heads, productionPerHead.units);
    "How many pounds are produced by a flock of the given size."
    shared Float scaledPoundsProduction(Integer heads) =>
            productionPerHead.floatNumber * heads * poundsCoefficient;
}
"Models of (game statistics for) herding mammals."
abstract class MammalModel(production, dailyTimePerHead) of dairyCattle | largeMammals |
        smallMammals satisfies HerdModel {
    "The amount produced per head per turn, in gallons"
    Float production;
    "How much time, per head, in minutes, must be spent to milk, or otherwise collect the
     food produced by the animals."
    shared actual Integer dailyTimePerHead;
    "The amount produced per head per turn."
    shared actual Quantity productionPerHead = Quantity(production, "gallons");
    "The number of pounds per gallon."
    shared actual Float poundsCoefficient = 8.6;
    "How much time, in minutes, must be spent on the entire herd or flock each turn,
     regardless of its size, in addition to each herder's time with individual animals."
    shared Integer dailyTimeFloor = 60;
    "How much time, in minutes, herders must spend on a flock with this many animals per
     herder."
    shared actual Integer dailyTime(Integer heads) =>
            heads * dailyTimePerHead + dailyTimeFloor;
    "How much time, in minutes, an expert herder must spend on a flock with this many
     animals per herder."
    shared Integer dailyExpertTime(Integer heads) =>
            heads * (dailyTimePerHead - 10) + dailyTimeFloor;
}
"The model for dairy cattle."
object dairyCattle extends MammalModel(4.0, 40) { }
"The model for other roughly-cattle-sized mammals. (Not for anything as large as
 elephants.)"
object largeMammals extends MammalModel(3.0, 40) { }
"The model for roughly-goat-sized mammals."
object smallMammals extends MammalModel(1.5, 30) { }
"Models of (game statistics for) herding poultry."
abstract class PoultryModel(production, poundsCoefficient, dailyTimePerHead, extraChoresInterval) of chickens | turkeys | pigeons satisfies HerdModel {
    "The number of eggs produced per head per turn."
    Float production;
    "The amount produced per head per turn."
    shared actual Quantity productionPerHead = Quantity(production, "eggs");
    "The coefficient to turn production into pounds."
    shared actual Float poundsCoefficient;
    "How much time, per head, in minutes, must be spent to gather eggs."
    shared actual Integer dailyTimePerHead;
    """How many turns, at most, should elapse between "extra chores" days."""
    shared Integer extraChoresInterval;
    """How much time, in minutes, must be spent per head on "extra chores" days."""
    shared Integer extraTimePerHead = 30;
    "How much time, in minutes, herders must spend on a flock with this many animals per
     herder."
    shared actual Integer dailyTime(Integer heads) => heads * dailyTimePerHead;
    """How much time, in minutes, herders must spend on a flock with this many animals per
       head on "extra chores" days."""
    shared Integer dailyExtraTime(Integer heads) => heads * extraTimePerHead;
}
"The model for chickens."
object chickens extends PoultryModel(0.75, 0.125, 2, 2) { }
"The model for turkeys."
object turkeys extends PoultryModel(0.75, 0.25, 2, 2) { }
"The model for pigeons."
object pigeons extends PoultryModel(0.5, 0.035, 1, 4) { }
"A driver for 'querying' the driver model about various things."
object queryCLI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage(false, "-q", "--query", ParamCount.one,
        "Answer questions about a map.",
        "Look a tiles on a map. Or run hunting, gathering, or fishing.");
    "How many hours we assume a working day is for a hunter or such."
    Integer hunterHours = 10;
    "How many encounters per hour for a hunter or such."
    Integer hourlyEncounters = 4;
    "Count the workers in an Iterable belonging to a player."
    Integer countWorkersInIterable(Player player, {TileFixture*} fixtures) {
        variable Integer retval = 0;
        for (fixture in fixtures) {
            if (is IWorker fixture, is HasOwner fixtures, player == fixtures) {
                retval++;
            } else if (is FixtureIterable<out TileFixture> fixture) {
                retval += countWorkersInIterable(player, fixture);
            }
        }
        return retval;
    }
    "Count the workers belonging to a player."
    void countWorkers(IMapNG map, ICLIHelper cli, Player* players) {
        Player[] playerList = [*players];
        Integer playerNum = cli.chooseFromList(playerList,
            "Players in the map:", "Map contains no players",
            "Owner of workers to count: ", true);
        if (exists player = playerList[playerNum]) {
            Integer count = sum { 0, for (location in map.locations)
                countWorkersInIterable(player, map.getOtherFixtures(location)) };
            cli.println("``player.name`` has ``count`` workers");
        }
    }
    "The distance between two points in a map with the given dimensions."
    Float distance(Point base, Point destination, MapDimensions dimensions) {
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
    "Report the distance between two points."
    todo("Use some sort of pathfinding.")
    void printDistance(MapDimensions dimensions, ICLIHelper cli) {
        Point start = cli.inputPoint("Starting point:\t");
        Point end = cli.inputPoint("Destination:\t");
        cli.println("Distance (as the crow flies, in tiles):\t``Float
            .format(distance(start, end, dimensions), 0, 0)``");
    }
    "Run hunting or fishing---that is, produce a list of possible encounters."
    void hunt(HuntingModel huntModel, Point point,
            "True if this is hunting, false if fishing."
            Boolean land, ICLIHelper cli,
            "How many encounters to show."
            Integer encounters) {
        if (land) {
            for (encounter in huntModel.hunt(point, encounters)) {
                cli.println(encounter);
            }
        } else {
            for (encounter in huntModel.fish(point, encounters)) {
                cli.println(encounter);
            }
        }
    }
    "Run food-gathering---that is, produce a list of possible encounters."
    void gather(HuntingModel huntModel, Point point, ICLIHelper cli, Integer encounters) {
        for (encounter in huntModel.gather(point, encounters)) {
            cli.println(encounter);
        }
    }
    """Handle herding mammals. Returns how many hours each herder spends "herding." """
    Integer herdMammals(ICLIHelper cli, MammalModel animal, Integer count,
            Integer flockPerHerder) {
        cli.println("Taking the day's two milkings together, tending the animals takes ``
            flockPerHerder * animal.dailyTimePerHead`` minutes, or ``flockPerHerder *
            (animal.dailyTimePerHead - 10)``, plus ``animal.dailyTimeFloor`` to gather them");
        Quantity base = animal.scaledProduction(count);
        Float production = base.floatNumber;
        cli.println("This produces ``Float.format(production, 0,
            1)`` ``base.units``, ``Float.format(animal.scaledPoundsProduction(count),
            0, 1)`` lbs, of milk per day.`");
        Integer cost;
        if (cli.inputBooleanInSeries("Are the herders experts? ")) {
            cost = animal.dailyExpertTime(flockPerHerder);
        } else {
            cost = animal.dailyTime(flockPerHerder);
        }
        return (ceiling(cost / 60.0) + 0.1).integer;
    }
    """Handle herding mammals. Returns how many hours each herder spends "herding." """
    Integer herdPoultry(ICLIHelper cli, PoultryModel bird, Integer count,
            Integer flockPerHerder) {
        cli.println("Gathering eggs takes ``bird
            .dailyTime(flockPerHerder)`` minutes; cleaning up after them,");
        cli.println("which should be done at least every ``bird.extraChoresInterval
            + 1`` turns, takes ``Float.format(bird
            .dailyExtraTime(flockPerHerder) / 60.0, 0, 1)`` hours.");
        Quantity base = bird.scaledProduction(count);
        Float production = base.floatNumber;
        cli.println("This produces ``Float.format(production, 0, 0)`` ``
            base.units``, totaling ``Float.format(bird.scaledPoundsProduction(count),
            0, 1)`` lbs.");
        Integer cost;
        if (cli.inputBooleanInSeries("Do they do the cleaning this turn? ")) {
            cost = bird.dailyTimePerHead + bird.extraTimePerHead;
        } else {
            cost = bird.dailyTimePerHead;
        }
        return (ceiling((flockPerHerder * cost) / 60.0) + 0.1).integer;
    }
    "Run herding."
    void herd(ICLIHelper cli, HuntingModel huntModel) {
        HerdModel herdModel;
        if (cli.inputBooleanInSeries("Are these small animals, like sheep?\t")) {
            herdModel = smallMammals;
        } else if (cli.inputBooleanInSeries("Are these dairy cattle?\t")) {
            herdModel = dairyCattle;
        } else if (cli.inputBooleanInSeries("Are these chickens?\t")) {
            herdModel = chickens;
        } else if (cli.inputBooleanInSeries("Are these turkeys?\t")) {
            herdModel = turkeys;
        } else if (cli.inputBooleanInSeries("Are these pigeons?\t")) {
            herdModel = pigeons;
        } else {
            herdModel = largeMammals;
        }
        Integer count = cli.inputNumber("How many animals?\t");
        if (count == 0) {
            cli.println("With no animals, no cost and no gain.");
            return;
        } else if (count < 0) {
            cli.println("Can't have a negative number of animals.");
            return;
        } else {
            Integer herders = cli.inputNumber("How many herders?\t");
            if (herders <= 0) {
                cli.println("Can't herd with no herders.");
                return;
            }
            Integer flockPerHerder = ((count + herders) - 1) / herders;
            Integer hours;
            if (is PoultryModel herdModel) {
                hours = herdPoultry(cli, herdModel, count, flockPerHerder);
            } else {
                hours = herdMammals(cli, herdModel, count, flockPerHerder);
            }
            if (hours < hunterHours,
                cli.inputBooleanInSeries("Spend remaining time as Food Gatherers? ")) {
                gather(huntModel, cli.inputPoint("Gathering location? "), cli,
                    hunterHours - hours);
            }
        }
    }
    "Give the data about a tile that the player is supposed to automatically know if he
     has a fortress on it."
    void fortressInfo(IMapNG map, Point location, ICLIHelper cli) {
        cli.println("Terrain is ``map.getBaseTerrain(location)``");
        {TileFixture*} fixtures = {
            map.getGround(location), map.getForest(location), *map.getOtherFixtures(location)
        }.coalesced;
        Ground[] ground = [ for (fixture in fixtures) if (is Ground fixture) fixture ];
        Forest[] forests = [ for (fixture in fixtures) if (is Forest fixture) fixture ];
        if (nonempty ground) {
            cli.println("Kind(s) of ground (rock) on the tile:");
            for (item in ground) {
                cli.println(item.string);
            }
        }
        if (nonempty forests) {
            cli.println("Kind(s) of forests on the tile:");
            for (forest in forests) {
                cli.println(forest.string);
            }
        }
    }
    "Find the nearest obviously-reachable unexplored location."
    Point? findUnexplored(IMapNG map, Point base) {
        Queue<Point> queue = LinkedList<Point>();
        queue.offer(base);
        MapDimensions dimensions = map.dimensions;
        MutableSet<Point> considered = HashSet<Point>();
        MutableList<Point> retval = ArrayList<Point>();
        while (exists current = queue.accept()) {
            TileType currentTerrain = map.getBaseTerrain(current);
            if (considered.contains(current)) {
                continue;
            } else if (currentTerrain == TileType.notVisible) {
                retval.add(current);
            } else if (currentTerrain != TileType.ocean) {
                Float baseDistance = distance(base, current, dimensions);
                for (neighbor in surroundingPointIterable(current, dimensions, 1)) {
                    if (distance(base, neighbor, dimensions) >= baseDistance) {
                        queue.offer(neighbor);
                    }
                }
            }
            considered.add(current);
        }
        return retval.sort(DistanceComparator(base).compare).first;
    }
    "Print a usage message for the REPL"
    void replUsage(ICLIHelper cli) {
        cli.println("The following commands are supported:");
        cli.println(
            "Fortress: Print what a player automatically knows about his fortress's tile.");
        Integer encounters = hunterHours * hourlyEncounters;
        cli.println("Hunt/fIsh: Generates up to ``encounters`` encounters with animals.`");
        cli.println("Gather: Generates up to ``
            encounters`` encounters with fields, meadows, groves,");
        cli.println("orchards, or shrubs.");
        cli.println(
            "hErd: Determine the output from and time required for maintaining a herd.");
        cli.println(
            "Trap: Switch to the trap-modeling program to run trapping or fish-trapping.");
        cli.println("Distance: Report the distance between two points.");
        cli.println("Count: Count how many workers belong to a player.");
        cli.println("Unexplored: Find the nearest unexplored tile not behind water.");
        cli.println("Quit: Exit the program.");
    }
    "Handle a user command."
    void handleCommand(SPOptions options, IDriverModel model, HuntingModel huntModel,
            ICLIHelper cli, Character input) {
        switch (input)
        case ('?') { replUsage(cli); }
        case('f') { fortressInfo(model.map, cli.inputPoint("Location of fortress? "),
            cli); }
        case ('h') { hunt(huntModel, cli.inputPoint("Location to hunt? "), true, cli,
            hunterHours * hourlyEncounters); }
        case ('i') { hunt(huntModel, cli.inputPoint("Location to fish? "), false, cli,
            hunterHours * hourlyEncounters); }
        case ('g') { gather(huntModel, cli.inputPoint("Location to gather? "), cli,
            hunterHours * hourlyEncounters); }
        case ('e') { herd(cli, huntModel); }
        case ('t') { trappingCLI.startDriverOnModel(cli, options, model); }
        case ('d') { printDistance(model.mapDimensions, cli); }
        case ('c') { countWorkers(model.map, cli, *model.map.players); }
        case ('u') {
            Point base = cli.inputPoint("Starting point? ");
            if (exists unexplored = findUnexplored(model.map, base)) {
                Float distanceTo = distance(base, unexplored, model.map.dimensions);
                cli.println("Nearest unexplored tile is ``unexplored``, ``Float
                    .format(distanceTo, 0, 1, '.', ',')`` tiles away");
            } else {
                cli.println("No unexplored tiles found.");
            }
        }
        else { cli.println("Unknown command."); }
    }
    "Accept and respond to commands."
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        HuntingModel huntModel = HuntingModel(model.map);
        try {
            while (exists firstChar = cli.inputString("Command: ").first,
                    firstChar != 'q') {
                handleCommand(options, model, huntModel, cli, firstChar);
            }
        } catch (IOException except) {
            log.error("I/O error", except);
        }
    }
}
"Possible actions in the trapping CLI; top-level so we can switch on the cases,
 since the other alternative, `static`, isn't possible in an `object` anymore."
abstract class TrapperCommand(name) of setTrap | check | move | easyReset | quit
        satisfies HasName {
    shared actual String name;
}
object setTrap extends TrapperCommand("Set or reset a trap") {}
object check extends TrapperCommand("Check a trap") {}
object move extends TrapperCommand("Move to another trap") {}
object easyReset extends TrapperCommand("Reset a foothold trap, e.g.") {}
object quit extends TrapperCommand("Quit") {}
"A driver to run a player's trapping activity."
todo("Tests")
object trappingCLI satisfies SimpleDriver {
    Integer minutesPerHour = 60;
    TrapperCommand[] commands = [setTrap, check, move, easyReset, quit];
    shared actual IDriverUsage usage = DriverUsage(false, "-r", "--trap", ParamCount.one,
        "Run a player's trapping", "Determine the results a player's trapper finds.");
    String inHours(Integer minutes) {
        if (minutes < minutesPerHour) {
            return "``minutes`` minutes";
        } else {
            return "``minutes / minutesPerHour`` hours, ``
                minutes % minutesPerHour`` minutes";
        }
    }
    "Handle a command. Returns how long it took to execute the command."
    Integer handleCommand(
            "The animals generated from the tile and the surrounding tiles."
            Queue<String> fixtures, ICLIHelper cli,
            "The command to handle"
            TrapperCommand command,
            "If true, we're dealing with *fish* traps, which have different costs"
            Boolean fishing) {
        switch (command)
        case (check){
            String? top = fixtures.accept();
            if (!top exists) {
                cli.println("Ran out of results");
                return JInteger.maxValue;
            }
            assert (exists top);
            if (HuntingModel.noResults == top) {
                cli.println("Nothing in the trap");
                return (fishing) then 5 else 10;
            } else {
                cli.println("Found either ``top`` or evidence of it escaping.");
                return cli.inputNumber("How long to check and deal with the animal? ");
            }
        }
        case (easyReset) { return (fishing) then 20 else 5; }
        case (move) { return 2; }
        case (quit) { return 0; }
        case (setTrap) { return (fishing) then 30 else 45; }
    }
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        Boolean fishing = cli.inputBooleanInSeries(
            "Is this a fisherman trapping fish rather than a trapper? ");
        String name = (fishing) then "fisherman" else "trapper";
        variable Integer minutes = cli
            .inputNumber("How many hours will the ``name`` work? ") * minutesPerHour;
        Point point = cli.inputPoint("Where is the ``name`` working? ");
        HuntingModel huntModel = HuntingModel(model.map);
        Queue<String> fixtures;
        if (fishing) {
            fixtures = LinkedList { *huntModel.fish(point, minutes)};
        } else {
            fixtures = LinkedList { *huntModel.hunt(point, minutes)};
        }
        variable Integer input = -1;
        while (minutes > 0, input < commands.size) {
            if (exists command = commands[input]) {
                minutes -= handleCommand(fixtures, cli, command, fishing);
                cli.println("``inHours(minutes)`` remaining");
                if (command == quit) {
                    break;
                }
            }
            input = cli.chooseFromList(commands,
                "What should the ``name`` do next?", "Oops! No commands",
                "Next action: ", false);
        }
    }
}