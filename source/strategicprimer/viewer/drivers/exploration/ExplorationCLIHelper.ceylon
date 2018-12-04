import ceylon.collection {
    ArrayList,
    MutableList,
    Queue
}

import lovelace.util.common {
    todo,
    matchingValue
}

import strategicprimer.model.common.map {
    IMutableMapNG,
    IMapNG,
    TileType,
    Point,
    HasOwner,
    Player,
    TileFixture,
    HasPopulation,
    HasExtent
}

import strategicprimer.model.common.map.fixtures.mobile {
    IUnit,
    Immortal,
    Animal,
    AnimalTracks
}
import strategicprimer.model.common.map.fixtures.resources {
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
    pathfinder
}
import strategicprimer.model.common.map.fixtures.towns {
    Village,
    Fortress,
    AbstractTown,
    TownStatus
}

"The logic split out of [[ExplorationCLI]]" // TODO: Merge back in
class ExplorationCLIHelper(IExplorationModel model, ICLIHelper cli) {
    HuntingModel huntingModel = HuntingModel(model.map);

    "Have the user choose a player."
    shared Player? choosePlayer() {
        Player[] players = model.playerChoices.sequence();
        return cli.chooseFromList(players,
            "Players shared by all the maps:", "No players shared by all the maps:",
            "Chosen player: ", true).item;
    }

    "Have the user choose a unit belonging to that player."
    shared IUnit? chooseUnit(Player player) {
        IUnit[] units = model.getUnits(player).sequence();
        return cli.chooseFromList(units, "Player's units:",
            "That player has no units in the master map", "Chosen unit: ", true).item;
    }

    "The explorer's current movement speed."
    variable Speed speed = Speed.normal;

    "Let the user change the explorer's speed"
    void changeSpeed() {
        if (exists temp = cli.chooseFromList(sort(`Speed`.caseValues),
                "Possible Speeds:", "No speeds available", "Chosen Speed: ",
                true).item) {
            speed = temp;
        }
    }

    "Copy the given fixture to subordinate maps and print it to the output stream."
    void printAndTransferFixture(Point destPoint, TileFixture? fixture, HasOwner mover) {
        if (exists fixture) {
            cli.println(fixture.string);
            Boolean zero;
            if (is HasOwner fixture, (fixture.owner != mover.owner || fixture is Village)) {
                zero = true;
            } else if (is HasPopulation<out Anything>|HasExtent fixture) {
                zero = true;
            } else {
                zero = false;
            }
            for (map->[file, _] in model.subordinateMaps) {
                map.addFixture(destPoint, fixture.copy(zero));
            }
            if (is CacheFixture fixture) {
                model.map.removeFixture(destPoint, fixture);
            }
        }
    }

    "Ask the user for directions the unit should move until it runs out of MP or the user
      decides to quit."
    todo("Inline back into [[ExplorationCLI]]?")
    // No need to set the 'modified' flag anywhere in this method, as
    // ExplorationModel.move() always sets it.
    shared void moveUntilDone() {
        if (exists mover = model.selectedUnit) {
            cli.println("Details of the unit:");
            cli.println("That unit appears to be at ``model.selectedUnitLocation``");
            cli.println(mover.verbose);
            Integer totalMP = cli.inputNumber("MP the unit has: ") else 0;
            variable Integer movement = totalMP;
            object handleCost satisfies MovementCostListener {
                shared actual void deduct(Integer cost) => movement -= cost;
            }
            model.addMovementCostListener(handleCost);

            MutableList<Point>&Queue<Point> proposedPath = ArrayList<Point>();
            ExplorationAutomationConfig automationConfig =
                ExplorationAutomationConfig(mover.owner);

            while (movement > 0) {
                Point point = model.selectedUnitLocation;
                Direction direction;
                if (exists proposedDestination = proposedPath.accept()) {
                    direction = `Direction`.caseValues.find(
                        matchingValue(proposedDestination,
                            curry(model.getDestination)(point))) else Direction.nowhere;
                    if (proposedDestination == point) {
                        continue;
                    } else if (direction == Direction.nowhere) {
                        cli.println("Next step ``
                            proposedDestination`` isn't adjacent to ``point``");
                        continue;
                    }
                    cli.println("``movement``/``totalMP`` MP remaining. Current speed: ``
                        speed.shortName``.");
                } else {
                    cli.println("``movement``/``totalMP`` MP remaining. Current speed: ``
                        speed.shortName``.");
                    cli.println(
                        """0: Set Speed, 1: SW, 2: S, 3: SE, 4: W, 5: Linger, 6: E, 7: NW, 8: N, 9: NE, 10: Toward Point, 11: Quit""");
                    Integer directionNum = cli.inputNumber("Direction to move: ") else -1;
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
                        if (exists destination =
                                cli.inputPoint("Location to move toward: ")) {
                            value [cost, path] = pathfinder.getTravelDistance(model
                                .subordinateMaps.first?.key else model.map,
                                point, destination);
                            if (path.empty) {
                                cli.println(
                                    "S/he doesn't know how to get there from here.");
                            } else {
                                proposedPath.addAll(path);
                            }
                            continue;
                        } else {
                            return;
                        }
                    }
                    else { movement = 0; continue; }
                }

                Point destPoint = model.getDestination(point, direction);
                try {
                    model.move(direction, speed);
                } catch (TraversalImpossibleException except) {
                    log.debug("Attempted movement to impossible destination");
                    cli.println("That direction is impassable; we've made sure all maps
                                 show that at a cost of 1 MP");
                    continue;
                }

                MutableList<TileFixture> constants = ArrayList<TileFixture>();
                IMutableMapNG map = model.map;
                MutableList<TileFixture> allFixtures = ArrayList<TileFixture>();

                //        for (fixture in map.fixtures[destPoint]) { // TODO: syntax sugar once compiler bug fixed
                for (fixture in map.fixtures.get(destPoint)) {
                    if (simpleMovementModel.shouldAlwaysNotice(mover, fixture)) {
                        constants.add(fixture);
                    } else if (simpleMovementModel.shouldSometimesNotice(mover, speed,
                            fixture)) {
                        allFixtures.add(fixture);
                    }
                }

                Animal|AnimalTracks|HuntingModel.NothingFound tracksAnimal;
                // Since not-visible terrain is impassable, by this point we know the tile
                // is visible.
                assert (exists terrain = model.map.baseTerrain[destPoint]);
                if (TileType.ocean == terrain) {
                    tracksAnimal = huntingModel.fish(destPoint).map(Entry.item).first
                        else HuntingModel.NothingFound.nothingFound;
                } else {
                    tracksAnimal = huntingModel.hunt(destPoint).map(Entry.item).first
                        else HuntingModel.NothingFound.nothingFound;
                }

                if (is Animal tracksAnimal) {
                    allFixtures.add(AnimalTracks(tracksAnimal.kind));
                } else if (is AnimalTracks tracksAnimal) {
                    allFixtures.add(tracksAnimal.copy(false));
                }

                if (Direction.nowhere == direction) {
                    switch (cli.inputBooleanInSeries(
                        "Should any village here swear to the player?  "))
                    case (true) { model.swearVillages(); }
                    case (null) { return; }
                    case (false) {}
                    switch (cli.inputBooleanInSeries("Dig to expose some ground here? "))
                    case (true) { model.dig(); }
                    case (false) {}
                    case (null) { return; }
                }

                String mtn;
                //        if (map.mountainous[destPoint]) { // TODO: syntax sugar once compiler bug fixed
                if (map.mountainous.get(destPoint)) {
                    mtn = "mountainous ";
                    for (subMap->[file, _] in model.subordinateMaps) {
                        subMap.mountainous[destPoint] = true;
                    }
                } else {
                    mtn = "";
                }

                cli.println("The explorer comes to ``destPoint``, a ``mtn````
                    map.baseTerrain[destPoint] else "unknown-terrain"`` tile");
                {TileFixture*} noticed = simpleMovementModel.selectNoticed(allFixtures,
                    identity<TileFixture>, mover, speed);

                if (!constants.empty || !noticed.empty) {
                    if (noticed.empty) {
                        cli.println("The following were automatically noticed:");
                    } else if (noticed.size > 1) {
                        cli.println(
                            "The following were noticed, all but the last ``noticed
                                    .size`` automatically:");
                    } else {
                        cli.println(
                            "The following were noticed, all but the last automatically:");
                    }
                    constants.addAll(noticed);
                    for (fixture in constants) {
                        printAndTransferFixture(destPoint, fixture, mover);
                    }
                }

                if (!proposedPath.empty, automationConfig.stopAtPoint(cli,
                        model.subordinateMaps.first?.key else model.map, destPoint)) {
                    proposedPath.clear();
                }
            }
        } else {
            cli.println("No unit is selected");
        }
    }
}

class ExplorationAutomationConfig(Player player) {
    class Condition<Type>(configExplanation, stopExplanation, conditions)
        given Type satisfies TileFixture {
        "A description to use in the question asking wheter to stop for this condition."
        shared String configExplanation;
        "A description, or a factory for a description, to use when stopping because of
         this condition."
        shared String|String(Type) stopExplanation;
        "Returns true when a tile fixture matches all of these conditions."
        Boolean(Type)* conditions;
        Boolean allConditions(TileFixture fixture) {
            if (is Type fixture) {
                for (condition in conditions) {
                    if (!condition(fixture)) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        }
        "Returns true when the given tile matches this condition."
        shared Boolean matches(IMapNG map, Point point) =>
            map.fixtures.get(point).any(allConditions);
    }
    {Condition<out TileFixture>+} conditions =
        [Condition<Fortress>("at others' fortresses",
                (fixture) => "a fortress belonging to " + fixture.owner.string),
            Condition<AbstractTown>("at active towns",
                    (fixture) => "a ``fixture.townSize`` active ``fixture.kind``",
                matchingValue(TownStatus.active, AbstractTown.status)),
            Condition<AbstractTown>("at inactive towns",
                    (fixture) => "a ``fixture.townSize`` ``fixture.status`` ``fixture.kind``",
                not(matchingValue(TownStatus.active, AbstractTown.status))),
            Condition<Village>("at independent villages", "an independent village",
                compose(Player.independent, Village.owner)),
            Condition<Village>("at other players' villages", "another player's village",
                not(matchingValue(player, Village.owner)),
                not(compose(Player.independent, Village.owner))),
            Condition<Village>("at villages sworn to you", "one of your villages",
                matchingValue(player, Village.owner)),
            Condition<IUnit>("on meeting other players' units",
                    (unit) => "a unit belonging to " + unit.owner.string,
                not(matchingValue(player, IUnit.owner)),
                not(compose(Player.independent, IUnit.owner))),
            Condition<IUnit>("on meeting independent units", "an independent unit",
                compose(Player.independent, IUnit.owner)),
            Condition<Immortal>("on meeting an immortal",
                compose("a(n) ".plus, Immortal.shortDescription))];
    variable {Condition<out TileFixture>*}? enabledConditions = null;
    Boolean matchesCondition(IMapNG map, Point point)(Condition<out TileFixture> condition) =>
        condition.matches(map, point);
    shared Boolean stopAtPoint(ICLIHelper cli, IMapNG map, Point point) {
        {Condition<out TileFixture>*} localEnabledConditions;
        if (exists temp = enabledConditions) {
            localEnabledConditions = temp;
        } else {
            MutableList<Condition<out TileFixture>> temp = ArrayList<Condition<out TileFixture>>();
            for (condition in conditions) {
                switch (cli.inputBooleanInSeries(
                    "Stop for instructions ``condition.configExplanation``?"))
                case (true) { temp.add(condition); }
                case (false) {}
                case (null) { return true; }
            }
            localEnabledConditions = temp.sequence();
            enabledConditions = localEnabledConditions;
        }
        if (exists matchingCondition = localEnabledConditions
                .find(matchesCondition(map, point))) {
            cli.println("There is ``matchingCondition
                .stopExplanation`` here, so the explorer stops.");
            return true;
        } else {
            return false;
        }
    }
}
