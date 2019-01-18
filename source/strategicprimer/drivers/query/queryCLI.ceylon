import strategicprimer.model.common.map {
    IFixture,
    Player,
    HasOwner,
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
    CLIDriver,
    ModelDriverFactory,
    DriverFactory,
    ModelDriver,
    SimpleMultiMapModel
}
import strategicprimer.model.common.map.fixtures {
    Ground
}
import strategicprimer.drivers.common.cli {
    ICLIHelper,
    SimpleApplet,
    AppletChooser
}
import lovelace.util.common {
    defer,
    PathWrapper
}
import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}
import strategicprimer.model.common.map.fixtures.mobile {
    IWorker
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
    static Float distance(Point base, Point destination, MapDimensions dimensions) =>
        dimensions.distance(base, destination);

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
        if (exists player = cli.chooseFromList(players.sequence(),
                "Players in the map:", "Map contains no players",
                "Owner of workers to count: ", true).item) {
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
            if (nonempty ground) { // TODO: Inline once syntax sugar in place above
                cli.println("Kind(s) of ground (rock) on the tile:");
                ground.map(Object.string).each(cli.println);
            }
            if (nonempty forests) { // TODO: Inline once syntax sugar in place above
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
        if (exists location = cli.inputPoint("Base location? "),
                exists distance = cli.inputNumber("Within how many tiles? ")) {
            suggestTrade(location, distance);
        }
    }

    AppletChooser<SimpleApplet> appletChooser = AppletChooser(cli,
        SimpleApplet(defer(compose(fortressInfo, cli.inputPoint),
                ["Location of fortress?"]),
            "Show what a player automatically knows about a fortress's tile.",
            "fortress"),
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
