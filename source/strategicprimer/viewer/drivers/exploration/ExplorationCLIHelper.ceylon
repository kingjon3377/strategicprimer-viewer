import ceylon.collection {
    ArrayList,
    MutableList,
	Queue
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.map {
    Player,
    TileType,
    TileFixture,
    HasOwner,
    IMutableMapNG,
    Point,
	IMapNG
}
import strategicprimer.model.map.fixtures.mobile {
    IUnit,
    Animal,
	AnimalImpl
}
import strategicprimer.model.map.fixtures.resources {
    CacheFixture
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.exploration.common {
    IExplorationModel,
    Speed,
    Direction,
    MovementCostListener,
    TraversalImpossibleException,
    simpleMovementModel,
    MovementCostSource,
	pathfinder
}
import strategicprimer.model.map.fixtures.towns {
    Village,
	Fortress,
	AbstractTown,
	TownStatus
}

"The logic split out of [[explorationCLI]]"
class ExplorationCLIHelper(IExplorationModel model, ICLIHelper cli)
        satisfies MovementCostSource {
    HuntingModel huntingModel = HuntingModel(model.map);
    MutableList<Anything(Integer)|MovementCostListener> listeners =
            ArrayList<Anything(Integer)|MovementCostListener>();
    shared actual void addMovementCostListener(MovementCostListener listener) =>
            listeners.add(listener);
    shared actual void removeMovementCostListener(MovementCostListener listener) =>
            listeners.remove(listener);
    void fireMovementCost(Integer cost) {
        for (listener in listeners) {
            if (is MovementCostListener listener) {
                listener.deduct(cost);
            } else {
                listener(cost);
            }
        }
    }
    "Have the user choose a player."
    shared Player? choosePlayer() {
        Player[] players = [*model.playerChoices];
        return cli.chooseFromList(players,
            "Players shared by all the maps:", "No players shared by all the maps:",
            "Chosen player: ", true).item;
    }
    "Have the user choose a unit belonging to that player."
    shared IUnit? chooseUnit(Player player) {
        IUnit[] units = [*model.getUnits(player)];
        return cli.chooseFromList(units, "Player's units:",
            "That player has no units in the master map", "Chosen unit: ", true).item;
    }
    "The explorer's current movement speed."
    variable Speed speed = Speed.normal;
    "Let the user change the explorer's speed"
    void changeSpeed() {
        Speed[] speeds = sort(`Speed`.caseValues);
        Integer->Speed? newSpeed = cli.chooseFromList(speeds,
            "Possible Speeds:", "No speeds available", "Chosen Speed: ", true);
        if (exists temp = newSpeed.item) {
            speed = temp;
        }
    }
    "Copy the given fixture to subordinate maps and print it to the output stream."
    void printAndTransferFixture(Point destPoint, TileFixture? fixture, HasOwner mover) {
        if (exists fixture) {
            cli.println(fixture.string);
            Boolean zero;
            if (is HasOwner fixture, fixture.owner != mover.owner || fixture is Village) {
                zero = true;
            } else {
                zero = false;
            }
            for ([map, file] in model.subordinateMaps) {
                map.addFixture(destPoint, fixture.copy(zero));
            }
            if (is CacheFixture fixture) {
                model.map.removeFixture(destPoint, fixture);
            }
        }
    }
    "Ask the user for directions the unit should move until it runs out of MP or the user
      decides to quit."
    todo("Inline back into [[explorationCLI]]?")
    shared void moveUntilDone() {
        if (exists mover = model.selectedUnit) {
            cli.println("Details of the unit:");
            cli.println(mover.verbose);
            Integer totalMP = cli.inputNumber("MP the unit has: ");
            variable Integer movement = totalMP;
            object handleCost satisfies MovementCostListener {
                shared actual void deduct(Integer cost) => movement -= cost;
            }
            model.addMovementCostListener(handleCost);
//            addMovementCostListener(handleCost);
            listeners.add(handleCost);
            MutableList<Point>&Queue<Point> proposedPath = ArrayList<Point>();
            object automationConfig {
                variable ExplorationAutomationConfig? wrapped = null;
                shared ExplorationAutomationConfig config {
                    if (exists temp = wrapped) {
                        return temp;
                    } else {
                        value retval = ExplorationAutomationConfig(mover.owner,
	                            cli.inputBooleanInSeries("Stop for instructions at others' fortresses?"),
	                            cli.inputBooleanInSeries("Stop for instructions at active towns?"),
	                            cli.inputBooleanInSeries("Stop for instructions at inactive towns?"),
	                            cli.inputBooleanInSeries("Stop for instructions at villages?"),
	                            cli.inputBooleanInSeries("Stop for instructions on meeting other players' units?"),
	                            cli.inputBooleanInSeries("Stop for instructions on meeting independent units?"));
                        wrapped = retval;
                        return retval;
                    }
                }
            }
            while (movement > 0) {
                Point point = model.selectedUnitLocation;
                Direction direction;
                if (exists proposedDestination = proposedPath.accept()) {
                    direction = `Direction`.caseValues.find(
                        (dir) => model.getDestination(point, dir) == proposedDestination) else Direction.nowhere;
                    if (proposedDestination == point) {
                        continue;
                    } else if (direction == Direction.nowhere) {
                        cli.println("Intended next destination ``proposedDestination`` is not adjacent to current location ``point``");
                        continue;
                    }
                    cli.println("``movement`` MP of ``totalMP`` remaining.");
                    cli.println("Current speed: ``speed.name``");
                } else {
	                cli.println("``movement`` MP of ``totalMP`` remaining.");
	                cli.println("Current speed: ``speed.name``");
	                cli.println("""0 = Change Speed, 1 = SW, 2 = S, 3 = SE, 4 = W, 5 = Stay Here,
	                               6 = E, 7 = NW, 8 = N, 9 = NE, 10 = Pathfind Toward Location, 11 = Quit.""");
	                Integer directionNum = cli.inputNumber("Direction to move: ");
	                switch (directionNum)
	                case (0) { changeSpeed(); continue; }
	                case (1) { direction = Direction.southwest; }
	                case (2) { direction = Direction.south; }
	                case (3) { direction = Direction.southeast; }
	                case (4) { direction = Direction.west; }
	                case (5) { direction = Direction.nowhere; }
	                case (6) { direction = Direction.east; }
	                case (7) { direction = Direction.northwest; }
	                case (8) { direction = Direction.north; }
	                case (9) { direction = Direction.northeast; }
	                case (10) {
	                    value [cost, path] = pathfinder.getTravelDistance(model.subordinateMaps.first?.first else model.map,
	                        point, cli.inputPoint("Location to move toward: "));
	                    if (path.empty) {
	                        cli.println("The explorer doesn't know how to get there from here.");
	                    } else {
	                        proposedPath.addAll(path);
	                    }
	                    continue;
	                }
	                else { fireMovementCost(runtime.maxArraySize); continue; }
	            }
	            Point destPoint = model.getDestination(point, direction);
                try {
                    model.move(direction, speed);
                } catch (TraversalImpossibleException except) {
                    log.debug("Attempted movement to impossible destination");
                    cli.println("That direction is impassable; we've made sure all maps show that
                                 at a cost of 1 MP");
                    continue;
                }
                MutableList<TileFixture> constants = ArrayList<TileFixture>();
                IMutableMapNG map = model.map;
                MutableList<TileFixture> allFixtures = ArrayList<TileFixture>();
                //        for (fixture in map.fixtures[destPoint]) { // TODO: syntax sugar once compiler bug fixed
                for (fixture in map.fixtures.get(destPoint)) {
                    if (simpleMovementModel.shouldAlwaysNotice(mover, fixture)) {
                        constants.add(fixture);
                    } else if (simpleMovementModel.shouldSometimesNotice(mover, speed, fixture)) {
                        allFixtures.add(fixture);
                    }
                }
                Animal|HuntingModel.NothingFound tracksAnimal;
                // Since not-visible terrain is impassable, by this point we know the tile is
                // visible.
                assert (exists terrain = model.map.baseTerrain[destPoint]);
                if (TileType.ocean == terrain) {
                    tracksAnimal = huntingModel.fish(destPoint).map(Entry.item).first else HuntingModel.NothingFound.nothingFound;
                } else {
                    tracksAnimal = huntingModel.hunt(destPoint).map(Entry.item).first else HuntingModel.NothingFound.nothingFound;
                }
                if (is Animal tracksAnimal) {
                    allFixtures.add(AnimalImpl(tracksAnimal.kind, true, false, "wild", -1));
                }
                if (Direction.nowhere == direction) {
                    if (cli.inputBooleanInSeries(
                        "Should any village here swear to the player?  ")) {
                        model.swearVillages();
                    }
                    if (cli.inputBooleanInSeries("Dig to expose some ground here? ")) {
                        model.dig();
                    }
                }
                String mtn;
                //        if (map.mountainous[destPoint]) { // TODO: syntax sugar once compiler bug fixed
                if (map.mountainous.get(destPoint)) {
                    mtn = "mountainous ";
                    for (pair in model.subordinateMaps) {
                        pair.first.mountainous[destPoint] = true;
                    }
                } else {
                    mtn = "";
                }
                cli.println("The explorer comes to ``destPoint``, a ``mtn``tile with terrain ``
                    map.baseTerrain[destPoint] else "Unknown"``");
                {TileFixture*} noticed = simpleMovementModel.selectNoticed(allFixtures, identity<TileFixture>,
                    mover, speed);
                if (noticed.empty) {
                    cli.println("The following were automatically noticed:");
                } else if (noticed.size > 1) {
                    cli.println(
                        "The following were noticed, all but the last ``noticed
                                .size`` automatically:");
                } else {
                    cli.println("The following were noticed, all but the last automatically:");
                }
                constants.addAll(noticed);
                for (fixture in constants) {
                    printAndTransferFixture(destPoint, fixture, mover);
                }
                if (!proposedPath.empty, automationConfig.config.stopAtPoint(
	                    model.subordinateMaps.first?.first else model.map, destPoint)) {
                    cli.println("One of your stop-pathfinding conditions triggered, so control is returned to you.");
                    proposedPath.clear();
                }
            }
        } else {
            cli.println("No unit is selected");
        }
    }
}

class ExplorationAutomationConfig(Player player, Boolean stopForForts,
	Boolean stopForActiveTowns, Boolean stopForInactiveTowns, Boolean stopForVillages,
	Boolean stopForPlayerUnits, Boolean stopForIndieUnits) {
	shared Boolean stopAtPoint(IMapNG map, Point point) {
		//for (fixture in map.fixtures[point]) { // TODO: syntax sugar
		for (fixture in map.fixtures.get(point)) {
			if (is Fortress fixture, fixture.owner != player, stopForForts) {
				return true;
			} else if (is AbstractTown fixture) {
				if (fixture.status == TownStatus.active, stopForActiveTowns) {
					return true;
				} else if (fixture.status != TownStatus.active, stopForInactiveTowns) {
					return true;
				}
			} else if (is Village fixture, stopForVillages) {
				return true;
			} else if (is IUnit fixture) {
				if (fixture.owner.independent, stopForIndieUnits) {
					return true;
				} else if (!fixture.owner.independent, fixture.owner != player, stopForPlayerUnits) {
					return true;
				}
			}
		}
		return false;
	}
}