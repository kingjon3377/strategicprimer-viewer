import strategicprimer.model.map {
	Player,
	TileType,
	IMapNG,
	Point,
	HasOwner,
	MapDimensions,
	IFixture
}
import ceylon.collection {
	MutableSet,
	LinkedList,
	ArrayList,
	MutableList,
	HashSet,
	Queue
}
import java.io {
	IOException
}
import strategicprimer.drivers.common {
	SPOptions,
	ParamCount,
	IDriverUsage,
	SimpleDriver,
	DriverUsage,
	IDriverModel
}
import strategicprimer.model.map.fixtures {
	Ground,
	Quantity
}
import strategicprimer.drivers.common.cli {
	ICLIHelper
}
import lovelace.util.common {
	todo
}
import strategicprimer.model.map.fixtures.terrain {
	Forest
}
import ceylon.math.float {
	ceiling,
	sqrt
}
import strategicprimer.model.map.fixtures.mobile {
	IWorker
}
import strategicprimer.drivers.exploration.common {
	surroundingPointIterable
}
import strategicprimer.model {
	DistanceComparator
}
import java.nio.file {
	JPath=Path
}
import strategicprimer.viewer.drivers.exploration {
	HuntingModel
}
import ceylon.logging {
	logger,
	Logger
}
import strategicprimer.model.map.fixtures.towns {
	ITownFixture,
	TownStatus,
	Village
}
"A logger."
Logger log = logger(`module strategicprimer.viewer`);
"How many hours we assume a working day is for a hunter or such."
Integer hunterHours = 10;
"How many encounters per hour for a hunter or such."
Integer hourlyEncounters = 4;
"Count the workers in an Iterable belonging to a player."
Integer countWorkersInIterable(Player player, {IFixture*} fixtures) {
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
"Count the workers belonging to a player."
void countWorkers(IMapNG map, ICLIHelper cli, Player* players) {
	Player[] playerList = [*players];
	value choice = cli.chooseFromList(playerList,
		"Players in the map:", "Map contains no players",
		"Owner of workers to count: ", true);
	if (exists player = choice.item) {
		Integer count = sum { 0, for (location in map.locations)
			//                countWorkersInIterable(player, map.fixtures[location]) }; // TODO: syntax sugar once compiler bug fixed
				countWorkersInIterable(player, map.fixtures.get(location)) };
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
void hunt(HuntingModel huntModel, Point point, ICLIHelper cli,
	"How many encounters to show."
	Integer encounters) {
	for (encounter in huntModel.hunt(point, encounters)) {
		cli.println(encounter);
	}
}
"Run fishing---that is, produce a list of possible encounters."
void fish(HuntingModel huntModel, Point point, ICLIHelper cli,
	"How many encounters to show"
	Integer encounters) {
	for (encounter in huntModel.fish(point, encounters)) {
		cli.println(encounter);
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
			(animal.dailyTimePerHead - 10)``, plus ``animal.dailyTimeFloor
	`` to gather them");
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
	cli.println("Terrain is ``map.baseTerrain[location] else "unknown"``");
	//        Ground[] ground = map.fixtures[location].narrow<Ground>().sequence();
	Ground[] ground = map.fixtures.get(location).narrow<Ground>().sequence();
	//        Forest[] forests = map.fixtures[location].narrow<Forest>().sequence();
	Forest[] forests = map.fixtures.get(location).narrow<Forest>().sequence();
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
"Print a list of active towns within the given distance of the given base that produce any resources,
 and what resources they produce."
void suggestTrade(IMapNG map, Point base, Integer distance, ICLIHelper cli) {
	value comparator = DistanceComparator(base, map.dimensions);
	for (location in surroundingPointIterable(base, map.dimensions, distance).distinct.sort(comparator.compare)) {
		for (town in map.fixtures.get(location).narrow<ITownFixture>()) {
		//for (town in map.fixtures[location].narrow<ITownFixture>()) { // TODO: syntax sugar once compiler bug fixed
			if (town.status == TownStatus.active, exists population = town.population,
					!population.yearlyProduction.empty) {
				cli.print("At ``location````comparator.distanceString(location, "base")``: ");
				cli.print("``town.name``, a ``town.townSize`` ");
				if (is Village town, town.race != "human") {
					cli.print("``town.race`` village");
				} else {
					cli.print(town.kind);
				}
				cli.println(". Its yearly production:");
				for (resource in population.yearlyProduction) {
					String unitsString;
					if (resource.quantity.units.empty) {
						unitsString = "";
					} else if ("dozen" == resource.quantity.units) {
						unitsString = "dozen ";
					} else {
						unitsString = "``resource.quantity.units`` of ";
					}
					cli.println("- ``resource.kind``: ``resource.quantity.number`` ``
						unitsString````resource.contents``");
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
"Print a usage message for the REPL"
void replUsage(ICLIHelper cli) {
	cli.println("The following commands are supported:");
	cli.println(
		"Fortress: Show what a player automatically knows about a fortress's tile.");
	Integer encounters = hunterHours * hourlyEncounters;
	cli.println("Hunt/fIsh: Generate up to ``encounters`` encounters with animals.`");
	cli.println("Gather: Generate up to ``
		encounters`` encounters with fields, meadows, groves,");
	cli.println("orchards, or shrubs.");
	cli.println(
		"hErd: Determine the output from and time required for maintaining a herd.");
	cli.println(
		"Trap: Switch to a trap-modeling program to run trapping or fish-trapping.");
	cli.println("Distance: Report the distance between two points.");
	cli.println("Count: Count how many workers belong to a player.");
	cli.println("Unexplored: Find the nearest unexplored tile not behind water.");
	cli.println("tRade: Suggest possible trading partners.");
	cli.println("Quit: Exit the program.");
}
"Handle a user command."
void handleCommand(SPOptions options, IDriverModel model, HuntingModel huntModel,
	ICLIHelper cli, Character input) {
	switch (input)
	case ('?') { replUsage(cli); }
	case('f') { fortressInfo(model.map, cli.inputPoint("Location of fortress? "),
		cli); }
	case ('h') { hunt(huntModel, cli.inputPoint("Location to hunt? "), cli,
		hunterHours * hourlyEncounters); }
	case ('i') { fish(huntModel, cli.inputPoint("Location to fish? "), cli,
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
	case ('r') {
		suggestTrade(model.map, cli.inputPoint("Base location? "),
			cli.inputNumber("Within how many tiles? "), cli);
	}
	else { cli.println("Unknown command."); }
}
"A driver for 'querying' the driver model about various things."
shared object queryCLI satisfies SimpleDriver {
	shared actual IDriverUsage usage = DriverUsage(false, ["-q", "--query"], ParamCount.one,
		"Answer questions about a map.",
		"Look a tiles on a map. Or run hunting, gathering, or fishing.");
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
	"As we're a CLI driver, we can't show a file-chooser dialog."
	shared actual {JPath*} askUserForFiles() => {};
}
