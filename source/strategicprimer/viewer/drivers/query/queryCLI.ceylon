import strategicprimer.model.map {
	Player,
	TileType,
	IMapNG,
	Point,
	HasOwner,
	MapDimensions,
	IFixture,
	HasPopulation,
	TileFixture
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
	SimpleCLIDriver,
	DriverUsage,
	IDriverModel,
	IMultiMapModel,
	ISPDriver
}
import strategicprimer.model.map.fixtures {
	Ground,
	Quantity
}
import strategicprimer.drivers.common.cli {
	ICLIHelper
}
import lovelace.util.common {
	todo,
	matchingValue,
	defer,
	simpleMap
}
import strategicprimer.model.map.fixtures.terrain {
	Forest
}
import ceylon.numeric.float {
	ceiling,
	sqrt,
	round=halfEven
}
import strategicprimer.model.map.fixtures.mobile {
	IWorker,
	Animal,
	AnimalTracks
}
import strategicprimer.drivers.exploration.common {
	surroundingPointIterable,
	pathfinder
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
import strategicprimer.model.map.fixtures.resources {
	Grove,
	Shrub,
	Meadow
}
"A logger."
Logger log = logger(`module strategicprimer.viewer`);
"A driver for 'querying' the driver model about various things."
service(`interface ISPDriver`)
// FIXME: Write GUI equivalent of query CLI
shared class QueryCLI() satisfies SimpleCLIDriver {
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
	void countWorkers(ICLIHelper cli, IMapNG map, Player* players) {
		Player[] playerList = players.sequence();
		value choice = cli.chooseFromList(playerList,
			"Players in the map:", "Map contains no players",
			"Owner of workers to count: ", true);
		if (exists player = choice.item) {
			Integer count = countWorkersInIterable(player,
				map.fixtureEntries.map(Entry.item));
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
	void printDistance(IMapNG map, ICLIHelper cli) {
		Point start = cli.inputPoint("Starting point:\t");
		Point end = cli.inputPoint("Destination:\t");
		if (cli.inputBoolean("Compute ground travel distance?")) {
			cli.println("Distance (on the ground, in MP cost):\t``pathfinder.
				getTravelDistance(map, start, end).first``");
		} else {
			cli.println("Distance (as the crow flies, in tiles):\t``Float
				.format(distance(start, end, map.dimensions), 0, 0)``");
		}
	}
	"If the given driver model *has* subordinate maps, add a copy of the given fixture to
	 them at the given location iff no fixture with the same ID is already there."
	void addToSubMaps(IDriverModel model, Point point, TileFixture fixture, Boolean zero) {
		if (is IMultiMapModel model) {
			for (map->file in model.subordinateMaps) {
				if (!map.fixtures.get(point).any(matchingValue(fixture.id, TileFixture.id))) {
					map.addFixture(point, fixture.copy(zero));
				}
			}
		}
	}
	"Reduce the population of a group of plants, animals, etc., and copy the reduced form
	 into all subordinate maps."
	void reducePopulation(IDriverModel model, ICLIHelper cli, Point point,
			HasPopulation<out TileFixture>&TileFixture fixture, String plural, Boolean zero) {
		Integer count = Integer.smallest(cli.inputNumber("How many ``plural`` to remove: "),
			fixture.population);
		if (count > 0) {
			model.map.removeFixture(point, fixture);
			Integer remaining = fixture.population - count;
			if (remaining > 0) {
				value addend = fixture.reduced(remaining);
				model.map.addFixture(point, addend);
				if (is IMultiMapModel model) {
					for (map->file in model.subordinateMaps) {
						if (exists found = map.fixtures.get(point)
								.find(shuffle(curry(fixture.isSubset))(noop))) {
							map.removeFixture(point, found);
						}
						map.addFixture(point, addend.copy(zero));
					}
				}
			} else if (is IMultiMapModel model) {
				for (map->file in model.subordinateMaps) {
					if (exists found = map.fixtures.get(point)
							.find(shuffle(curry(fixture.isSubset))(noop))) {
						map.removeFixture(point, found);
					}
				}
			}
		} else {
			addToSubMaps(model, point, fixture, zero);
		}
	}
	void huntGeneral(
			"The map model."
			IDriverModel model,
			"How much time is left in the day."
			variable Integer time,
			"How much time is deducted when nothing is found."
			Integer noResultCost,
			"""What verb to use to ask how long an encounter took: "gather", "fight",
			   "process", e.g."""
			String verb,
			"The CLI helper."
			ICLIHelper cli,
			"The source of encounters."
			variable {<Point->Animal|AnimalTracks|HuntingModel.NothingFound>*} encounters) {
		while (time > 0, exists loc->encounter = encounters.first) {
			encounters = encounters.rest;
			if (is HuntingModel.NothingFound encounter) {
				cli.println("Found nothing for the next ``noResultCost`` minutes.");
				time -= noResultCost;
			} else if (is AnimalTracks encounter) { // TODO: Is this case actually possible?
				addToSubMaps(model, loc, encounter, true);
				cli.println("Found only tracks or traces from ``
					encounter.kind`` for the next ``noResultCost`` minutes.");
				time -= noResultCost;
			} else if (cli.inputBooleanInSeries("Found ``(encounter.population > 1) then
					"a group of perhaps ``encounter.population`` ``encounter.kind``" else encounter.kind
						``. Should they ``verb``?", encounter.kind)) {
				Integer cost = cli.inputNumber("Time to ``verb``: ");
				time -= cost;
				if (cli.inputBooleanInSeries("Handle processing now?")) {
					// TODO: somehow handle processing-in-parallel case
					for (i in 0:(cli.inputNumber("How many animals?"))) {
						Integer mass = cli.inputNumber("Weight of this animal's meat in pounds: ");
						Integer hands = cli.inputNumber("# of workers processing this carcass: ");
						time -= round(HuntingModel.processingTime(mass) / hands).integer;
					}
				}
				if (cli.inputBooleanInSeries("Reduce animal group population of ``
						encounter.population``?")) {
					reducePopulation(model, cli, loc, encounter, "animals", true);
				} else {
					addToSubMaps(model, loc, encounter, true);
				}
				cli.println("``time`` minutes remaining.");
			} else {
				addToSubMaps(model, loc, encounter, true);
				time -= noResultCost;
			}
		}
	}
	"""Run hunting---that is, produce a list of "encounters"."""
	todo("Distinguish hunting from fishing in no-result time cost?")
	void hunt(IDriverModel model, HuntingModel huntModel, Point point, ICLIHelper cli,
		"How long to spend hunting."
		Integer time) =>
			huntGeneral(model, time, 60 / hourlyEncounters,
				"fight and process", cli, huntModel.hunt(point));
	"""Run fishing---that is, produce a list of "encounters"."""
	void fish(IDriverModel model, HuntingModel huntModel, Point point, ICLIHelper cli,
		"How long to spend hunting."
		Integer time) =>
			huntGeneral(model, time, 60 / hourlyEncounters,
				"try to catch and process", cli, huntModel.fish(point));
	"""Run food-gathering---that is, produce a list of "encounters"."""
	void gather(IDriverModel model, HuntingModel huntModel, Point point, ICLIHelper cli,
			variable Integer time) {
		variable {<Point->Grove|Shrub|Meadow|HuntingModel.NothingFound>*} encounters =
				huntModel.gather(point);
		Integer noResultCost = 60 / hourlyEncounters;
		while (time > 0, exists loc->encounter = encounters.first) {
			encounters = encounters.rest;
			if (is HuntingModel.NothingFound encounter) {
				cli.println("Found nothing for the next ``noResultCost`` minutes.");
				time -= noResultCost;
				continue;
			} else if (cli.inputBooleanInSeries("Found ``encounter.shortDescription````
					if (is Meadow encounter) then " (``encounter.status``)" else ""``. Should they gather?",
					encounter.kind)) {
				Integer cost = cli.inputNumber("Time to gather: ");
				time -= cost;
				// TODO: Once model supports remaining-quantity-in-fields data, offer to reduce it here
				if (is Shrub encounter, encounter.population > 0,
						cli.inputBooleanInSeries("Reduce shrub population here?")) {
					reducePopulation(model, cli, loc, encounter, "plants", true);
					cli.println("``time`` minutes remaining.");
					continue;
				}
				cli.println("``time`` minutes remaining.");
			} else {
				time -= noResultCost;
			}
			addToSubMaps(model, loc, encounter, true);
		}
	}
	"""Handle herding mammals. Returns how many hours each herder spends "herding." """
	Integer herdMammals(ICLIHelper cli, MammalModel animal, Integer count,
		Integer flockPerHerder) {
		cli.println("Taking the day's two milkings together, tending the animals takes ``
			flockPerHerder * animal.dailyTimePerHead`` minutes, or ``flockPerHerder *
				(animal.dailyTimePerHead - 10)`` with expert herders, plus ``
				animal.dailyTimeFloor`` minutes to gather them");
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
	void herd(ICLIHelper cli, IDriverModel model, HuntingModel huntModel) {
		HerdModel herdModel;
		if (cli.inputBooleanInSeries("Are these small animals, like sheep?\t")) {
			herdModel = MammalModel.smallMammals;
		} else if (cli.inputBooleanInSeries("Are these dairy cattle?\t")) {
			herdModel = MammalModel.dairyCattle;
		} else if (cli.inputBooleanInSeries("Are these chickens?\t")) {
			herdModel = PoultryModel.chickens;
		} else if (cli.inputBooleanInSeries("Are these turkeys?\t")) {
			herdModel = PoultryModel.turkeys;
		} else if (cli.inputBooleanInSeries("Are these pigeons?\t")) {
			herdModel = PoultryModel.pigeons;
		} else {
			herdModel = MammalModel.largeMammals;
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
				gather(model, huntModel, cli.inputPoint("Gathering location? "), cli,
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
			ground.map(Object.string).each(cli.println);
		}
		if (nonempty forests) {
			cli.println("Kind(s) of forests on the tile:");
			forests.map(Object.string).each(cli.println);
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
	"Print a list of active towns within the given distance of the given base that produce
	 any resources, and what resources they produce."
	void suggestTrade(IMapNG map, Point base, Integer distance, ICLIHelper cli) {
		value comparator = DistanceComparator(base, map.dimensions);
		for (location in surroundingPointIterable(base, map.dimensions, distance).distinct
				.sort(comparator.compare)) {
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
		cli.println("help, ?: Print this list of commands.");
		cli.println(
			"fortress: Show what a player automatically knows about a fortress's tile.");
		cli.println("hunt, fish: Generate encounters with animals.`");
		cli.println(
			"gather: Generate encounters with fields, meadows, groves, orchards, or shrubs.");
		cli.println(
			"herd: Determine the output from and time required for maintaining a herd.");
		cli.println(
			"trap: Switch to a trap-modeling program to run trapping or fish-trapping.");
		cli.println("distance: Report the distance between two points.");
		cli.println("count: Count how many workers belong to a player.");
		cli.println("unexplored: Find the nearest unexplored tile not behind water.");
		cli.println("trade: Suggest possible trading partners.");
		cli.println("quit: Exit the program.");
		cli.println("Any string that is the beginning of only one command is also accepted for that command.");
	}
	void findUnexploredCommand(IMapNG map, ICLIHelper cli) {
		Point base = cli.inputPoint("Starting point? ");
		if (exists unexplored = findUnexplored(map, base)) {
			Float distanceTo = distance(base, unexplored, map.dimensions);
			cli.println("Nearest unexplored tile is ``unexplored``, ``Float
				.format(distanceTo, 0, 1, '.', ',')`` tiles away");
		} else {
			cli.println("No unexplored tiles found.");
		}
	}
	void tradeCommand(IMapNG map, ICLIHelper cli) =>
			suggestTrade(map, cli.inputPoint("Base location? "),
				cli.inputNumber("Within how many tiles? "), cli);
	"Handle a series of user commands."
	void handleCommands(SPOptions options, IDriverModel model, HuntingModel huntModel,
			ICLIHelper cli) {
		Map<String, Anything(ICLIHelper)> commands = simpleMap(
			"?"->replUsage,
			"help"->replUsage,
			"fortress"->( // TODO: refactor lambdas including inputPoint() to make defer()ing them easier
					(ICLIHelper clh) => fortressInfo(model.map, clh.inputPoint("Location of fortress? "),
						clh)),
			"hunt"->((ICLIHelper clh)=>hunt(model, huntModel, clh.inputPoint("Location to hunt? "),
				clh, hunterHours * 60)),
			"fish"->((ICLIHelper clh)=>fish(model, huntModel, clh.inputPoint("Location to fish? "),
				clh, hunterHours * 60)),
			"gather"->((ICLIHelper clh)=>gather(model, huntModel,
				clh.inputPoint("Location to gather? "), clh, hunterHours * 60)),
			"herd"->shuffle(curry(herd))(model, huntModel),
			"trap"->uncurry(shuffle(curry(shuffle(compose(TrappingCLI.startDriverOnModel,
				TrappingCLI))))(options, model)),
			"distance"->curry(printDistance)(model.map),
			"count"->shuffle(curry(countWorkers))(model.map, *model.map.players),
			"unexplored"->curry(findUnexploredCommand)(model.map),
			"trade"->curry(tradeCommand)(model.map)
		);
		while (true) {
			String command = cli.inputString("Command:").lowercased;
			if ("quit".startsWith(command)) {
				break;
			}
			{<String->Anything(ICLIHelper)>*} matches =
					commands.filterKeys(shuffle(String.startsWith)(command));
			if (matches.size == 1) {
				assert (exists first = matches.first);
				first.item(cli);
			} else if (matches.empty) {
				cli.println("Unknown command.");
				replUsage(cli);
			} else {
				cli.println("That command was ambiguous between the following: ");
				assert (exists first = matches.first);
				cli.print(first.key);
				for (key->val in matches.rest) {
					cli.print(", ``key``");
				}
				cli.println("");
				replUsage(cli);
			}
		}
	}
	shared actual IDriverUsage usage = DriverUsage(false, ["-q", "--query"], ParamCount.one,
		"Answer questions about a map.",
		"Look a tiles on a map. Or run hunting, gathering, or fishing.", true, false);
	"Accept and respond to commands."
	shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
		IDriverModel model) {
		HuntingModel huntModel = HuntingModel(model.map);
		try {
			handleCommands(options, model, huntModel, cli);
		} catch (IOException except) {
			log.error("I/O error", except);
		}
	}
	"As we're a CLI driver, we can't show a file-chooser dialog."
	shared actual {JPath*} askUserForFiles() => [];
}
