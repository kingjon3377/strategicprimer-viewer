import ceylon.collection {
    MutableSet,
    HashSet,
    MutableMap,
    HashMap,
    MutableList,
    ArrayList
}
import ceylon.logging {
    Logger,
    logger
}

import lovelace.util.common {
    todo,
    ArraySet
}

import strategicprimer.model.map {
    HasOwner,
    River,
    IMutablePlayerCollection,
    TileFixture,
    IMutableMap,
    IMap,
    MutablePlayer,
    Player,
    TileType,
    MapDimensions,
    Point
}
import strategicprimer.model.map.fixtures {
    Ground,
    SubsettableFixture
}
import strategicprimer.model.map.fixtures.mobile {
    IUnit
}
import strategicprimer.model.map.fixtures.terrain {
    Forest
}

"A logger."
Logger log = logger(`module strategicprimer.model`);
"A class to represent a game-world map and its contents."
shared class SPMap satisfies IMutableMap {
    "Whether the given fixture should be zeroed out if the map is for the given player."
    static Boolean shouldZero(TileFixture fixture, Player? player) {
        if (exists player, is HasOwner fixture) {
            return player == fixture.owner;
        } else {
            return true;
        }
    }
    "The set of mountainous places."
    MutableSet<Point> mountains = HashSet<Point>();
    "The base terrain at points in the map."
    MutableMap<Point, TileType> terrain = HashMap<Point, TileType>();
    "The players in the map."
    IMutablePlayerCollection playerCollection;
    """The forests in the map. If there's more than one forest on a tile, only one is
       here; the rest go in the "miscellaneous fixtures" pile."""
    MutableMap<Point, Forest> forests = HashMap<Point, Forest>();
    "Fixtures at various points, other than the main ground and forest."
    todo("Use a multimap once we add a library dependency providing such a class")
    MutableMap<Point, MutableSet<TileFixture>> fixtures =
            HashMap<Point, MutableSet<TileFixture>>();
    MapDimensions mapDimensions;
    """The ground under various locations. If there's more than one at a point, others go
        in the "other fixtures" collection."""
    MutableMap<Point, Ground> groundMap = HashMap<Point, Ground>();
    "The rivers in the map."
    MutableMap<Point, {River*}> riversMap = HashMap<Point, {River*}>();
    "The current turn."
    shared actual variable Integer currentTurn;
    shared new (MapDimensions dimensions, IMutablePlayerCollection players,
            Integer turn) {
        mapDimensions = dimensions;
        playerCollection = players;
        currentTurn = turn;
    }
    "The dimensions of the map."
    shared actual MapDimensions dimensions => mapDimensions;
    "A stream of the players known in the map"
    shared actual {Player*} players => {*playerCollection};
    "The locations in the map."
    shared actual {Point*} locations => PointIterator(dimensions, true, true);
    "The base terrain at the given location."
    shared actual TileType baseTerrain(Point location) =>
            terrain.get(location) else TileType.notVisible;
    "Whether the given location is mountainous."
    shared actual Boolean mountainous(Point location) => mountains.contains(location);
    "The rivers, if any, at the given location."
    shared actual {River*} rivers(Point location) =>
            {*(riversMap.get(location) else {})};
    "The primary forest, if any, at the given location."
    shared actual Forest? forest(Point location) => forests.get(location);
    "The base ground, if any known, at the given location."
    shared actual Ground? ground(Point location) => groundMap.get(location);
    "Any fixtures other than rivers, primary forest, and primary ground at the given
     location."
    shared actual {TileFixture*} otherFixtures(Point location) =>
            {*(fixtures.get(location) else {})};
    "The current player."
    shared actual Player currentPlayer => playerCollection.currentPlayer;
    assign currentPlayer {
        Player oldCurrent = playerCollection.currentPlayer;
        if (is MutablePlayer oldCurrent) {
            oldCurrent.current = false;
        } else {
            log.warn("Previous current player wasn't mutable");
        }
        Player newCurrent = playerCollection.getPlayer(currentPlayer.playerId);
        if (is MutablePlayer newCurrent) {
            newCurrent.current = true;
        } else {
            log.warn(
                "Player in collection matching specified 'new' player wasn't mutable");
        }
    }
    "Add a player."
    shared actual void addPlayer(Player player) => playerCollection.add(player);
    "Set the base terrain at a location."
    shared actual void setBaseTerrain(Point location, TileType type) =>
            terrain.put(location, type);
    "Set whether a location is mountainous."
    shared actual void setMountainous(Point location, Boolean mtn) {
        if (mtn) {
            mountains.add(location);
        } else {
            mountains.remove(location);
        }
    }
    "Add rivers at a location."
    shared actual void addRivers(Point location, River* addedRivers) {
        {River*} existing = riversMap.get(location) else {};
        riversMap.put(location, set {*existing}.union(set {*addedRivers}));
    }
    "Remove rivers from the given location."
    shared actual void removeRivers(Point location, River* removedRivers) {
        if (exists existing = riversMap.get(location)) {
            riversMap.put(location, set {*existing}
                .complement(set {*removedRivers}));
        }
    }
    "Set the forest at a location."
    shared actual void setForest(Point location, Forest? forest) {
        if (exists forest) {
            forests.put(location, forest);
        } else {
            forests.remove(location);
        }
    }
    "Set the main ground at a location."
    shared actual void setGround(Point location, Ground? newGround) {
        if (exists newGround) {
            groundMap.put(location, newGround);
        } else {
            groundMap.remove(location);
        }
    }
    """Add a fixture at a location, and return whether the "all fixtures at this point"
       set has an additional member as a result of this."""
    shared actual Boolean addFixture(Point location, TileFixture fixture) {
        if ({ forest(location), ground(location)}.coalesced
                .any((existing) => existing.equalsIgnoringID(fixture))) {
            return false;
        }
        MutableSet<TileFixture> local;
        if (exists temp = fixtures.get(location)) {
            local = temp;
        } else {
            local = ArraySet<TileFixture>();
            fixtures.put(location, local);
        }
        if (fixture.id >= 0,
                exists existing = local.find((item) => item.id == fixture.id)) {
            Boolean subsetCheck(TileFixture one, TileFixture two) {
                if (is SubsettableFixture one, one.isSubset(two, noop)) {
                    return true;
                } else if (is SubsettableFixture two, two.isSubset(one, noop)) {
                    return true;
                } else {
                    return false;
                }
            }
            if (existing == fixture || subsetCheck(existing, fixture)) {
                local.remove(existing);
                local.add(fixture);
                // The return value is primarily used by [[FixtureListModel]], which won't
                // care about differences, but would end up with double entries if we
                // returned true here.
                return false;
            } else {
                local.add(fixture);
                log.warn("Inserted duplicate-ID fixture at ``location``");
                log.debug("Stack trace of this location: ", Exception());
                log.debug("Existing fixture was: ``existing.shortDescription``");
                log.debug("Added: ``fixture.shortDescription``");
                return true;
            }
        } else {
            Integer oldSize = local.size;
            local.add(fixture);
            return oldSize < local.size;
        }
    }
    "Remove a fixture from a location."
    shared actual void removeFixture(Point location, TileFixture fixture) {
        if (exists list = fixtures.get(location)) {
            list.remove(fixture);
        }
    }
    shared actual Integer hash =>
            dimensions.hash + (currentTurn.leftLogicalShift(3)) +
                currentPlayer.hash.leftLogicalShift(5);
    shared actual Boolean equals(Object obj) {
        if (is IMap obj) {
            if (dimensions == obj.dimensions, players.containsEvery(obj.players),
                    obj.players.containsEvery(players), currentTurn == obj.currentTurn,
                    currentPlayer == obj.currentPlayer) {
                for (point in locations) {
                    {TileFixture*} ourFixtures = allFixtures(point);
                    {TileFixture*} theirFixtures = obj.allFixtures(point);
                    if (baseTerrain(point) !=obj.baseTerrain(point) ||
                    mountainous(point) !=obj.mountainous(point) ||
                    set { *rivers(point) } !=set { *obj.rivers(point) } ||
                    !ourFixtures.containsEvery(theirFixtures) ||
                    !theirFixtures.containsEvery(ourFixtures)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    shared actual String string {
        StringBuilder builder = StringBuilder();
        builder.append("SPMapNG:
                        Map version: ``dimensions.version``
                        Rows: ``dimensions.rows``
                        Columns: ``dimensions.columns``
                        Current Turn: ``currentTurn``
                        Players:
                        ");
        for (player in players) {
            if (player.current) {
                builder.append("``player`` (current)");
            } else {
                builder.append(player.string);
            }
            builder.appendNewline();
        }
        builder.appendNewline();
        builder.append("Contents:");
        builder.appendNewline();
        for (location in locations) {
            if (locationEmpty(location)) {
                continue;
            }
            builder.append("At ``location``");
            if (baseTerrain(location) != TileType.notVisible) {
                builder.append("terrain: ``baseTerrain(location)``, ");
            }
            if (mountainous(location)) {
                builder.append("mountains, ");
            }
            if (exists localGround = ground(location)) {
                builder.append("ground: ``localGround``");
            }
            if (exists localForest = forest(location)) {
                builder.append("forest: ``localForest``");
            }
            if (!rivers(location).empty) {
                builder.append("rivers:");
                for (river in rivers(location)) {
                    builder.append(" ``river``");
                }
                builder.append(", ");
            }
            if (exists other = fixtures.get(location)) {
                builder.append("other: ");
                for (fixture in other) {
                    builder.appendNewline();
                    builder.append(fixture.string);
                    // builder.append(" (``type(fixture).declaration.name``)");
                }
            }
            builder.appendNewline();
        }
        return builder.string;
    }
    """Returns true if the other map is a "strict subset" of this one, except for those
       cases we deliberately ignore."""
    shared actual Boolean isSubset(IMap obj, Anything(String) report) {
        if (dimensions == obj.dimensions) {
            // TODO: delegate player-subset testing to PlayerCollection
            // TODO: Or else use standard Ceylon Iterable methods
            variable Boolean retval = true;
            for (player in obj.players) {
                if (!playerCollection.contains(player)) {
                    report("\tExtra player ``player``");
                    retval = false; // return false;
                }
            }
            // Declared here to avoid object allocations in the loop.
            MutableList<TileFixture> ourFixtures = ArrayList<TileFixture>();
            MutableMap<Integer, MutableList<SubsettableFixture>> ourSubsettables =
                    HashMap<Integer, MutableList<SubsettableFixture>>();
            // IUnit is Subsettable<IUnit> and thus incompatible with SubsettableFixture
            MutableMap<Integer, IUnit> ourUnits = HashMap<Integer, IUnit>();
            for (point in locations) {
                void localReport(String string) => report("At ``point``:\t``string``");
                if (!{ baseTerrain(point), TileType.notVisible}
                        .contains(obj.baseTerrain(point))) {
                    if (TileType.notVisible == baseTerrain(point)) {
                        localReport("Has terrain information we don't");
                    } else {
                        localReport("Base terrain differs");
                    }
                    retval = false; // return false;
                    continue;
                }
                if (obj.mountainous(point), !mountainous(point)) {
                    localReport("Has mountains we don't");
                    retval = false; // return false;
                }
                if (exists forest = obj.forest(point)) {
                    // There are *far* too many false positives if we don't check the
                    // "other fixtures," because of the way we represent this in the XML.
                    // If we ever start a new campaign with a different data
                    // representation---perhaps a database---we should remove this check.
                    if (!allFixtures(point).contains(forest)) {
                        localReport("Has forest we don't");
                        retval = false; // return false;
                    }
                }
                if (exists theirGround = obj.ground(point)) {
                    if (exists ourGround = ground(point)) {
                        // There are *far* too many false positives if we don't check the
                        // "other fixtures," because of the way we represent this in the
                        // XML. If we ever start a new campaign with a different data
                        // representation---perhaps a database---we should remove this
                        // check. Except for the 'exposed' (kind == kind) bit.
                        if (theirGround != ourGround, !allFixtures(point).narrow<Ground>()
                                .any((item) => item.kind == theirGround.kind)) {
                            localReport("Has ground we don't");
                            retval = false; // return false;
                        }
                    } else {
                        localReport("Has ground we don't");
                        retval = false;
                    }
                }
                ourFixtures.clear();
                ourSubsettables.clear();
                ourUnits.clear();
                for (fixture in allFixtures(point)) {
                    Integer idNum = fixture.id;
                    if (is IUnit fixture) {
                        ourUnits.put(idNum, fixture);
                    } else if (is SubsettableFixture fixture) {
                        if (exists list = ourSubsettables.get(idNum)) {
                            list.add(fixture);
                        } else {
                            MutableList<SubsettableFixture> list =
                                    ArrayList<SubsettableFixture>();
                            list.add(fixture);
                            ourSubsettables.put(idNum, list);
                        }
                    } else {
                        ourFixtures.add(fixture);
                    }
                }
                {TileFixture*} theirFixtures = obj.otherFixtures(point);
                for (fixture in theirFixtures) {
                    if (ourFixtures.contains(fixture) || shouldSkip(fixture)) {
                        continue;
                    } else if (is IUnit fixture, exists unit = ourUnits.get(fixture.id)) {
                        retval = retval && unit.isSubset(fixture, localReport);
                    } else if (is SubsettableFixture fixture,
                            exists list = ourSubsettables.get(fixture.id)) {
                        variable Integer count = 0;
                        variable Boolean unmatched = true;
                        variable SubsettableFixture? match = null;
                        for (subsettable in list) {
                            count++;
                            match = subsettable;
                            if (subsettable.isSubset(fixture, noop)) {
                                unmatched = false;
                                break;
                            } else {
                                unmatched = true;
                            }
                        }
                        if (count == 0) {
                            localReport("Extra fixture:\t``fixture``");
                            retval = false; // return false;
                            break;
                        } else if (count == 1) {
                            assert (exists temp = match);
                            retval = retval && temp.isSubset(fixture, localReport);
                        } else if (unmatched) {
                            localReport(
                                "Fixture with ID #``fixture.id`` didn't match any of the
                                 subsettable fixtures sharing that ID");
                            retval = false; // return false;
                            break;
                        }
                    } else {
                        localReport("Extra fixture:\t``fixture``");
                        retval = false; // return false;
                        break;
                    }
                }
                if (!set { *obj.rivers(point) }
                        .complement(set { *rivers(point) }).empty) {
                    localReport("Extra river(s)");
                    retval = false; // return false;
                    break;
                }
            }
            return retval;
        } else {
            report("Dimension mismatch");
            return false;
        }
    }
    "Clone a map, possibly for a specific player, who shouldn't see other players'
     details."
    shared actual IMap copy(Boolean zero, Player? player) {
        IMutableMap retval = SPMap(dimensions, playerCollection.copy(),
            currentTurn);
        for (point in locations) {
            retval.setBaseTerrain(point, baseTerrain(point));
            if (exists grd = ground(point)) {
                retval.setGround(point, grd.copy(false));
            } else {
                retval.setGround(point, null);
            }
            if (exists forest = forest(point)) {
                retval.setForest(point, forest.copy(false));
            } else {
                retval.setForest(point, null);
            }
            retval.setMountainous(point, mountainous(point));
            retval.addRivers(point, *rivers(point));
            // TODO: what other fixtures should we zero, or skip?
            for (fixture in otherFixtures(point)) {
                retval.addFixture(point,
                    fixture.copy(zero && shouldZero(fixture, player)));
            }
        }
        return retval;
    }
}