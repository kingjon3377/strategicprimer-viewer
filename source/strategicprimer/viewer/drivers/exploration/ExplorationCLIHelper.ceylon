import ceylon.collection {
    ArrayList,
    MutableList,
    Queue
}

import lovelace.util.common {
    matchingValue
}

import strategicprimer.model.common.map {
    IMapNG,
    TileType,
    Point,
    HasOwner,
    Player,
    TileFixture,
    HasPopulation,
    HasExtent,
    Direction,
    River
}

import strategicprimer.model.common.map.fixtures.mobile {
    IUnit,
    Immortal,
    Animal,
    AnimalTracks
}

import strategicprimer.drivers.common.cli {
    ICLIHelper,
    SimpleApplet,
    AppletChooser
}
import strategicprimer.drivers.exploration.common {
    IExplorationModel,
    Speed,
    MovementCostListener,
    TraversalImpossibleException,
    simpleMovementModel,
    pathfinder,
    Pathfinder,
    HuntingModel
}
import strategicprimer.model.common.map.fixtures.towns {
    Village,
    IFortress,
    AbstractTown,
    TownStatus
}
import strategicprimer.drivers.common {
    SelectionChangeListener
}

"The logic split out of [[ExplorationCLI]], some also used in
 [[strategicprimer.viewer.drivers.turnrunning::TurnRunningCLI]]"
shared class ExplorationCLIHelper(IExplorationModel model, ICLIHelper cli)
        satisfies MovementCostListener&SelectionChangeListener {
    HuntingModel huntingModel = HuntingModel(model.map);

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
    void printAndTransferFixture(Point destPoint, TileFixture? fixture, HasOwner mover, Boolean automatic) {
        if (exists fixture) {
            if (automatic) {
                cli.print(fixture.string);
                cli.println(" (automatically)");
            } else {
                cli.println(fixture.string);
            }
            Boolean zero;
            if (is HasOwner fixture,
                    (fixture.owner != mover.owner || fixture is Village)) {
                zero = true;
            } else if (is HasPopulation<out Anything>|HasExtent<out Anything> fixture) {
                zero = true;
            } else {
                zero = false;
            }
            model.copyToSubMaps(destPoint, fixture, zero);
        }
    }

    variable Integer totalMP = 0;
    variable Integer runningTotal = 0;
    shared Integer movement => runningTotal;

    shared actual void deduct(Integer cost) => runningTotal -= cost;

    MutableList<Point>&Queue<Point> proposedPath = ArrayList<Point>();
    variable ExplorationAutomationConfig automationConfig =
        ExplorationAutomationConfig(model.map.currentPlayer);

    String[] commands = ["Set Speed", "SW", "S", "SE", "W", "Linger", "E", "NW", "N", "NE", "Toward Point", "Quit"];

    String usage = ", ".join(commands.indexed.map((index->command) => "``index``: ``command``"));

    "When the selected unit changes, print the unit's details and ask how many MP the unit
     has."
    shared actual void selectedUnitChanged(IUnit? old, IUnit? newSelection) {
        if (exists newSelection) {
            cli.print("Details of the unit (apparently at ");
            cli.print(model.selectedUnitLocation.string);
            cli.println("):");
            cli.println(newSelection.verbose);
            if (exists number = cli.inputNumber("MP the unit has: ")) {
                runningTotal = totalMP = number;
            }
            if (automationConfig.player != newSelection.owner) {
                automationConfig = ExplorationAutomationConfig(newSelection.owner);
            }
        }
    }
    AppletChooser<SimpleApplet<[]>, []> appletChooser = AppletChooser(cli,
        SimpleApplet<[]>(model.swearVillages, "Swear any village here to the player",
            "swear"),
        SimpleApplet<[]>(model.dig, "Dig to expose some ground here", "dig"));
    Pathfinder pather = pathfinder(model.subordinateMaps.first else model.map);
    "If the unit has a proposed path, move one more tile along it; otherwise, ask the user
     for directions once and make that move, then return to the caller."
    // No need to set the 'modified' flag anywhere in this method, as
    // ExplorationModel.move() always sets it.
    shared void moveOneStep() {
        if (exists mover = model.selectedUnit) {
            Point point = model.selectedUnitLocation;
            Direction direction;
            if (exists proposedDestination = proposedPath.accept()) {
                direction = `Direction`.caseValues.find(
                    matchingValue(proposedDestination,
                        curry(model.getDestination)(point))) else Direction.nowhere;
                if (proposedDestination == point) {
                    return;
                } else if (direction == Direction.nowhere) {
                    cli.println("Next step ``
                        proposedDestination`` isn't adjacent to ``point``");
                    return;
                }
                cli.println("``runningTotal``/``totalMP`` MP remaining. Current speed: ``
                    speed.shortName``.");
            } else {
                cli.println("``runningTotal``/``totalMP`` MP remaining. Current speed: ``
                    speed.shortName``.");
                cli.printlnAtInterval(usage);
                Integer directionNum = cli.inputNumber("Direction to move: ") else -1;
                switch (directionNum)
                case (0) { changeSpeed(); return; }
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
                        value [cost, path] = pather.getTravelDistance(point,
                            destination);
                        if (path.empty) {
                            cli.println(
                                "S/he doesn't know how to get there from here.");
                        } else {
                            proposedPath.addAll(path);
                        }
                        return;
                    } else {
                        runningTotal = 0;
                        return;
                    }
                }
                else { runningTotal = 0; return; }
            }

            Point destPoint = model.getDestination(point, direction);
            try {
                model.move(direction, speed);
            } catch (TraversalImpossibleException except) {
                log.debug("Attempted movement to impossible destination");
                cli.println("That direction is impassable; we've made sure all maps show
                             that at a cost of 1 MP");
                return;
            }

            MutableList<TileFixture> constants = ArrayList<TileFixture>();
            IMapNG map = model.map;
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
                tracksAnimal = huntingModel.fish(destPoint).map(Entry.item).first;
            } else {
                tracksAnimal = huntingModel.hunt(destPoint).map(Entry.item).first;
            }

            if (is Animal tracksAnimal) {
                allFixtures.add(AnimalTracks(tracksAnimal.kind));
            } else if (is AnimalTracks tracksAnimal) {
                allFixtures.add(tracksAnimal.copy(false));
            }

            if (Direction.nowhere == direction) {
                while (exists response =
                        cli.inputBooleanInSeries("Take an action here?"), response) {
                    switch (applet = appletChooser.chooseApplet())
                    case (true|null) { continue; }
                    case (false) { break; }
                    case (is SimpleApplet<[]>) { applet.invoke(); }
                }
            }

            String mtn;
            //        if (map.mountainous[destPoint]) { // TODO: syntax sugar once compiler bug fixed
            if (map.mountainous.get(destPoint)) {
                mtn = "mountainous ";
            } else {
                mtn = "";
            }
            model.copyTerrainToSubMaps(destPoint);

            cli.print("The explorer comes to ``destPoint``, a ``mtn````
                map.baseTerrain[destPoint] else "unknown-terrain"`` tile");
            {River*} rivers = map.rivers.get(destPoint);
            Boolean anyRivers;
            if (rivers.any(River.lake.equals)) {
                anyRivers = true;
                if (rivers.any(not(River.lake.equals))) {
                    cli.print(" with a lake and (a) river(s) flowing ");
                } else {
                    cli.print(" with a lake");
                }
            } else if (!rivers.empty) {
                anyRivers = true;
                cli.print(" with (a) river(s) flowing ");
            } else {
                anyRivers = false;
            }
            cli.println(", ".join(rivers));

            if (exists roads = map.roads[destPoint], !roads.empty) {
                if (anyRivers) {
                    cli.print("There are (a) road(s) to the ");
                } else {
                    cli.print(" with (a) road(s) to the ");
                }
                cli.println(", ".join(roads.keys)); // TODO: Report on road quality
            }
            {TileFixture*} noticed = simpleMovementModel.selectNoticed(allFixtures,
                identity<TileFixture>, mover, speed);

            if (!constants.empty || !noticed.empty) {
                cli.println("The following were noticed:");
                for (fixture in constants) {
                    printAndTransferFixture(destPoint, fixture, mover, true);
                }
                for (fixture in noticed) {
                    printAndTransferFixture(destPoint, fixture, mover, false);
                }
            }

            if (!proposedPath.empty, automationConfig.stopAtPoint(cli,
                    model.subordinateMaps.first else model.map, destPoint)) {
                proposedPath.clear();
            }
        } else {
            cli.println("No unit is selected");
        }
    }

    shared actual void selectedPointChanged(Point? previousSelection, Point newSelection) {}
    shared actual void interactionPointChanged() {}
    shared actual void cursorPointChanged(Point? previousCursor, Point newCursor) {}
}

class ExplorationAutomationConfig(shared Player player) {
    class Condition<Type>(configExplanation, stopExplanation, conditions)
            given Type satisfies TileFixture {
        "A description to use in the question asking wheter to stop for this condition."
        shared String configExplanation;
        "A description, or a factory for a description, to use when stopping because of
         this condition."
        shared String|String(Type) stopExplanation;
        "Returns true when a tile fixture matches all of these conditions."
        Boolean(Type)* conditions;
        variable Type? matched = null;
        Boolean allConditions(TileFixture fixture) {
            if (is Type fixture) {
                for (condition in conditions) {
                    if (!condition(fixture)) {
                        return false;
                    }
                }
                matched = fixture;
                return true;
            } else {
                return false;
            }
        }
        "Returns true when the given tile matches this condition."
        shared Boolean matches(IMapNG map, Point point) =>
            map.fixtures.get(point).any(allConditions);
        "The description of the fixture that matched, if any, so caller
         doesn't have to figure out which fixture matched to use [[stopExplanation]]."
        shared String explain() {
            if (exists temp = matched) {
                if (is String stopExplanation) {
                    return stopExplanation;
                } else {
                    return stopExplanation(temp);
                }
            } else {
                return "";
            }
        }
    }
    {Condition<out TileFixture>+} conditions =
        [Condition<IFortress>("at others' fortresses",
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
    Boolean matchesCondition(IMapNG map,
        Point point)(Condition<out TileFixture> condition) =>
            condition.matches(map, point);
    shared Boolean stopAtPoint(ICLIHelper cli, IMapNG map, Point point) {
        {Condition<out TileFixture>*} localEnabledConditions;
        if (exists temp = enabledConditions) {
            localEnabledConditions = temp;
        } else {
            MutableList<Condition<out TileFixture>> temp =
                ArrayList<Condition<out TileFixture>>();
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
                .explain()`` here, so the explorer stops.");
            return true;
        } else {
            return false;
        }
    }
}
