import ceylon.collection {
    ArrayList,
    MutableList
}
import ceylon.math.float {
    ceiling
}
import java.lang {
    IllegalStateException
}
import java.nio.file {
    JPath=Path
}

import lovelace.util.common {
    todo
}
import strategicprimer.drivers.common {
    SimpleMultiMapModel,
    IDriverModel,
    SelectionChangeListener
}
import strategicprimer.model.map {
    Point,
    Player,
    IMutableMapNG,
    IMapNG,
    TileType,
    TileFixture,
    FixtureIterable,
    invalidPoint,
    pointFactory,
    IFixture,
    HasOwner,
    MapDimensions
}
import strategicprimer.model.map.fixtures {
    Ground,
    MineralFixture
}
import strategicprimer.model.map.fixtures.mobile {
    IUnit,
    Animal
}
import strategicprimer.model.map.fixtures.resources {
    Grove,
    Meadow,
    Mine,
    MineralVein
}
import strategicprimer.model.map.fixtures.terrain {
    Forest
}
import strategicprimer.model.map.fixtures.towns {
    Village,
    Fortress
}
import ceylon.random {
    randomize
}
"A model for exploration drivers."
shared class ExplorationModel extends SimpleMultiMapModel satisfies IExplorationModel {
    """A fixture is "diggable" if it is a [[MineralFixture]] or a [[Mine]]."""
    static Boolean isDiggable(TileFixture fixture) => fixture is MineralFixture|Mine;
    """Check whether two fixtures are "equal enough" for the purposes of updating a map
       after digging. This method is needed because equals() in
       [[strategicprimer.model.map.fixtures.resources::StoneDeposit]] and
       [[strategicprimer.model.map.fixtures.resources::MineralVein]] compares DCs."""
    static Boolean areDiggablesEqual(IFixture firstFixture, IFixture secondFixture) =>
            firstFixture == secondFixture || firstFixture.copy(true) == secondFixture.copy(true);
    "If a unit's motion could be observed by someone allied to another (non-independent)
     player (which at present means the unit is moving *to* a tile two or fewer tiles away
     from the watcher), print a message saying so to stdout."
    static void checkAllNearbyWatchers(IMapNG map, IUnit unit, Point dest) {
        MapDimensions dimensions = map.dimensions;
        for (point in surroundingPointIterable(dest, dimensions).distinct) {
            for (fixture in map.getOtherFixtures(point)) {
                if (is HasOwner fixture, !fixture.owner.independent, fixture.owner != unit.owner) {
                    process.writeLine("Motion to ``dest`` could be observed by ``fixture
                        .shortDescription`` at ``point``");
                }
            }
        }
    }
    "Remove a unit from a location, even if it's in a fortress."
    static void removeImpl(IMutableMapNG map, Point point, IUnit unit) {
        variable Boolean outside = false;
        for (fixture in map.getOtherFixtures(point)) {
            if (unit == fixture) {
                outside = true;
                break;
            } else if (is Fortress fixture) {
                for (item in fixture) {
                    if (unit == item) {
                        fixture.removeMember(unit);
                        return;
                    }
                }
            }
        }
        if (outside) {
            map.removeFixture(point, unit);
        }
    }
    "Ensure that a given map has at least terrain information for the specified location."
    static void ensureTerrain(IMapNG mainMap, IMutableMapNG map, Point point) {
        if (map.getBaseTerrain(point) == TileType.notVisible) {
            map.setBaseTerrain(point, mainMap.getBaseTerrain(point));
        }
        if (mainMap.isMountainous(point)) {
            map.setMountainous(point, true);
        }
    }
    "Whether the given fixture is at the given location in the given map."
    static Boolean doesLocationHaveFixture(IMapNG map, Point point, TileFixture fixture) {
        Ground? ground = map.getGround(point);
        Forest? forest = map.getForest(point);
        return {ground, forest, *map.getOtherFixtures(point)}.coalesced.flatMap((element) {
            if (is FixtureIterable<out IFixture> element) {
                return {element, *element};
            } else {
                return {element};
            }
        }).any(fixture.equals);
    }
    """A "plus one" method with a configurable, low "overflow"."""
    static Integer increment(
            "The number to increment"
            Integer number,
            "The maximum number we want to return"
            Integer max) {
        if (number >= max) {
            return 0;
        } else {
            return number + 1;
        }
    }
    """A "minus one" method that "underflows" after 0 to a configurable, low value."""
    static Integer decrement(
            "The number to decrement"
            Integer number,
            """The number to "underflow" to"""
            Integer max) {
        if (number <= 0) {
            return max;
        } else {
            return number - 1;
        }
    }
    MutableList<MovementCostListener> mcListeners = ArrayList<MovementCostListener>();
    MutableList<SelectionChangeListener> scListeners =
            ArrayList<SelectionChangeListener>();
    "The currently selected unit and its location."
    variable [Point, IUnit?] selection = [invalidPoint, null];
    shared new (IMutableMapNG map, JPath? file)
            extends SimpleMultiMapModel(map, file) {}
    shared new copyConstructor(IDriverModel model)
            extends SimpleMultiMapModel.copyConstructor(model) {}
    "All the players shared by all the maps."
    shared actual {Player*} playerChoices {
        variable Set<Player> retval = set { *map.players };
        for (pair in allMaps) {
            Set<Player> temp = set { *pair.first.players };
            retval = retval.intersection(temp);
        }
        return { *retval };
    }
    "Collect all the units in the main map belonging to the specified player."
    shared actual {IUnit*} getUnits(Player player) {
        {Object*} temp = map.locations.flatMap((point) => map.getOtherFixtures(point))
            .flatMap((element) {
                if (is Fortress element) {
                    return element;
                } else {
                    return {element};
                }
            });
        return {
            for (item in temp) if (is IUnit item) if (item.owner == player) item
        };
    }
    "Tell listeners that the selected point changed."
    void fireSelectionChange(Point old, Point newSelection) {
        for (listener in scListeners) {
            listener.selectedPointChanged(old, newSelection);
        }
    }
    "Tell listeners to deduct a cost from their movement-point totals."
    void fireMovementCost(Integer cost) {
        for (listener in mcListeners) {
            listener.deduct(cost);
        }
    }
    "Get the location one tile in the given direction from the given point."
    shared actual Point getDestination(Point point, Direction direction) {
        MapDimensions dims = mapDimensions;
        Integer maxColumn = dims.columns - 1;
        Integer maxRow = dims.rows - 1;
        Integer row = point.row;
        Integer column = point.column;
        switch (direction)
        case (Direction.east) { return pointFactory(row, increment(column, maxColumn)); }
        case (Direction.north) { return pointFactory(decrement(row, maxRow), column); }
        case (Direction.northeast) {
            return pointFactory(decrement(row, maxRow), increment(column, maxColumn));
        }
        case (Direction.northwest) {
            return pointFactory(decrement(row, maxRow), decrement(column, maxColumn));
        }
        case (Direction.south) { return pointFactory(increment(row, maxRow), column); }
        case (Direction.southeast) {
            return pointFactory(increment(row, maxRow), increment(column, maxColumn));
        }
        case (Direction.southwest) {
            return pointFactory(increment(row, maxRow), decrement(column, maxColumn));
        }
        case (Direction.west) { return pointFactory(row, decrement(column, maxColumn)); }
        case (Direction.nowhere) { return point; }
    }
    "Move the currently selected unit from its current location one tile in the specified
     direction. Moves the unit in all maps where the unit *was* in that tile, copying
     terrain information if the tile didn't exist in a subordinate map. If movement in the
     specified direction is impossible, we update all subordinate maps with the terrain
     information showing that, then re-throw the exception; callers should deduct a
     minimal MP cost (though we notify listeners of that cost). We return the cost of the
     move in MP, which we also tell listeners about."
    throws(`class TraversalImpossibleException`,
        "if movement in the specified direction is impossible")
    shared actual Integer move(
            "The direction to move"
            Direction direction,
            "How hastily the explorer is moving"
            Speed speed) {
        [Point, IUnit?] local = selection;
        Point point = local.first;
        IUnit? unit = local.rest.first;
        if (!unit exists) {
            throw IllegalStateException("move() called when no unit selected");
        }
        assert (exists unit);
        Point dest = getDestination(point, direction);
        if (landMovementPossible(map.getBaseTerrain(dest))) {
            Integer base;
            if (dest == point) {
                base = 1;
            } else {
                Ground? ground = map.getGround(dest);
                Forest? forest = map.getForest(dest);
                {TileFixture*} fixtures =
                        {ground, forest, *map.getOtherFixtures(dest)}.coalesced;
                base = movementCost(map.getBaseTerrain(dest),
                    map.getForest(dest) exists, map.isMountainous(dest),
                    riversSpeedTravel(direction, map.getRivers(point),
                        map.getRivers(dest)), fixtures);
            }
            Integer retval = (ceiling(base * speed.mpMultiplier) + 0.1).integer;
            removeImpl(map, point, unit);
            map.addFixture(dest, unit);
            for ([subMap, subFile] in subordinateMaps) {
                if (doesLocationHaveFixture(subMap, point, unit)) {
                    ensureTerrain(map, subMap, dest);
                    removeImpl(subMap, point, unit);
                    subMap.addFixture(dest, unit);
                }
            }
            selection = [dest, unit];
            fireSelectionChange(point, dest);
            fireMovementCost(retval);
            checkAllNearbyWatchers(map, unit, dest);
            return retval;
        } else {
            for (pair in subordinateMaps) {
                ensureTerrain(map, pair.first, dest);
            }
            fireMovementCost(1);
            throw TraversalImpossibleException();
        }
    }
    """Search the main map for the given fixture. Returns the first location found (search
       order is not defined) containing a fixture "equal to" the specified one."""
    shared actual Point find(TileFixture fixture) {
        for (point in map.locations) {
            if (doesLocationHaveFixture(map, point, fixture)) {
                return point;
            }
        } else {
            return invalidPoint;
        }
    }
    "The currently selected unit."
    shared actual IUnit? selectedUnit => selection.rest.first;
    "Select the given unit."
    assign selectedUnit {
        Point oldLoc = selection.first;
        Point loc;
        if (exists selectedUnit) {
            loc = find(selectedUnit);
        } else {
            loc = invalidPoint;
        }
        selection = [loc, selectedUnit];
        fireSelectionChange(oldLoc, loc);
    }
    "The location of the currently selected unit."
    shared actual Point selectedUnitLocation => selection.first;
    "Add a selection-change listener."
    shared actual void addSelectionChangeListener(SelectionChangeListener listener) =>
            scListeners.add(listener);
    "Remove a selection-change listener."
    shared actual void removeSelectionChangeListener(SelectionChangeListener listener) =>
            scListeners.remove(listener);
    "Add a movement-cost listener."
    shared actual void addMovementCostListener(MovementCostListener listener) =>
            mcListeners.add(listener);
    "Remove a movement-cost listener."
    shared actual void removeMovementCostListener(MovementCostListener listener) =>
            mcListeners.remove(listener);
    "If there is a currently selected unit, make any independent villages at its location
     change to be owned by the owner of the currently selected unit. This costs MP."
    shared actual void swearVillages() {
        [Point, IUnit?] localSelection = selection;
        Point currentPoint = localSelection.first;
        if (exists unit = localSelection.rest.first) {
            Player owner = unit.owner;
            {Village*} villages = {
                for (pair in allMaps)
                    for (fixture in pair.first.getOtherFixtures(currentPoint))
                            if (is Village fixture, fixture.owner.independent)
                                fixture
            };
            if (!villages.empty) {
                for (village in villages) {
                    village.owner = owner;
                    for (pair in allMaps) {
                        pair.first.addFixture(currentPoint, village);
                    }
                }
                IMapNG mainMap = map;
                {Point*} surroundingPoints =
                        surroundingPointIterable(currentPoint, mapDimensions, 1);
                for (point in surroundingPoints) {
                    for (pair in subordinateMaps) {
                        ensureTerrain(mainMap, pair.first, point);
                        Forest? subForest = pair.first.getForest(point);
                        if (exists forest = map.getForest(point), !subForest exists) {
                            pair.first.setForest(point, forest);
                        }
                    }
                }
                {[Point, TileFixture]*} surroundingFixtures = surroundingPoints
                            .flatMap((point) => mainMap.getOtherFixtures(point)
                                .map((fixture) => [point, fixture]));
                [Point, TileFixture]? vegetation = surroundingFixtures
                    .filter(([loc, fixture]) => fixture is Meadow|Grove).first;
                [Point, TileFixture]? animal = surroundingFixtures
                    .filter(([loc, fixture]) => fixture is Animal).first;
                for (pair in subordinateMaps) {
                    if (exists vegetation) {
                        pair.first.addFixture(vegetation.first, vegetation.rest.first);
                    }
                    if (exists animal) {
                        pair.first.addFixture(animal.first, animal.rest.first);
                    }
                }
            }
            fireMovementCost(5);
        }
    }
    "If there is a currently selected unit, change one [[Ground]],
     [[strategicprimer.model.map.fixtures.resources::StoneDeposit]], or [[MineralVein]] at
     the location of that unit from unexposed to exposed (and discover it). This costs
     MP."
    shared actual void dig() {
        Point currentPoint = selection.first;
        if (currentPoint.valid) {
            IMutableMapNG mainMap = map;
            Ground? ground = mainMap.getGround(currentPoint);
            variable {TileFixture*} diggables =
                    {ground, *mainMap.getOtherFixtures(currentPoint)}.coalesced
                        .filter(isDiggable);
            if (diggables.empty) {
                return;
            }
            variable Integer i = 0;
            variable Boolean first = true;
            while (first || (i < 4 && !diggables.first is Ground)) {
                diggables = randomize(diggables);
                first = false;
                i++;
            }
            assert (exists oldFixture = diggables.first);
            TileFixture newFixture = oldFixture.copy(false);
            if (is Ground newFixture) {
                newFixture.exposed = true;
            } else if (is MineralVein newFixture) {
                newFixture.exposed = true;
            }
            void addToMap(IMutableMapNG map, Boolean condition) {
                Ground? locGround = map.getGround(currentPoint);
                if (is Ground newFixture, exists locGround, exists ground,
                        locGround == ground) {
                    map.setGround(currentPoint, newFixture.copy(condition));
                    return;
                } else if (is Ground newFixture, !locGround exists) {
                    map.setGround(currentPoint, newFixture.copy(condition));
                    return;
                } else if (map.getAllFixtures(currentPoint)
                        .any((fixture) => areDiggablesEqual(fixture, oldFixture))) {
                    map.removeFixture(currentPoint, oldFixture);
                }
                map.addFixture(currentPoint, newFixture.copy(condition));
            }
            Boolean eq(TileFixture? one, TileFixture? two) {
                if (exists one) {
                    if (exists two) {
                        return one == two;
                    } else {
                        return false;
                    }
                } else if (exists two) {
                    return false;
                } else {
                    return true;
                }
            }
            if (eq(ground, oldFixture)) {
                for (pair in allMaps) {
                    addToMap(pair.first, false);
                }
            } else {
                variable Boolean subsequent = false;
                for (pair in allMaps) {
                    addToMap(pair.first, subsequent);
                    subsequent = true;
                }
            }
            fireMovementCost(4);
        }
    }
}