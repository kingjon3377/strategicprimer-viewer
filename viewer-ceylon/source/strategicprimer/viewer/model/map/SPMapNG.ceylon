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
    todo
}

import model.map {
    Point,
    Player,
    HasOwner,
    MapDimensions,
    River,
    MutablePlayer
}

import strategicprimer.viewer.model.map {
    IMutablePlayerCollection,
    TileFixture,
    IMutableMapNG,
    IMapNG
}
import strategicprimer.viewer.model.map.fixtures {
    Ground,
    SubsettableFixture
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    IUnit
}
import strategicprimer.viewer.model.map.fixtures.terrain {
    Forest
}

"A logger."
Logger log = logger(`module strategicprimer.viewer`);
"A class to represent a game-world map and its contents."
shared class SPMapNG satisfies IMutableMapNG {
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
    todo("Use a multimap once we add a library dependency providing such a class",
        "Use [[MutableSet]] and ported [[ArraySet]] instead of lists?")
    MutableMap<Point, MutableList<TileFixture>> fixtures =
            HashMap<Point, MutableList<TileFixture>>();
    MapDimensions mapDimensions;
    """The ground under various locations. If there's more than one at a point, others go
        in the "other fixtures" collection."""
    MutableMap<Point, Ground> ground = HashMap<Point, Ground>();
    "The rivers in the map."
    MutableMap<Point, {River*}> rivers = HashMap<Point, {River*}>();
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
    shared actual TileType getBaseTerrain(Point location) =>
            terrain.get(location) else TileType.notVisible;
    "Whether the given location is mountainous."
    shared actual Boolean isMountainous(Point location) => mountains.contains(location);
    "The rivers, if any, at the given location."
    shared actual {River*} getRivers(Point location) =>
            {*(rivers.get(location) else {})};
    "The primary forest, if any, at the given location."
    shared actual Forest? getForest(Point location) => forests.get(location);
    "The base ground, if any known, at the given location."
    shared actual Ground? getGround(Point location) => ground.get(location);
    "Any fixtures other than rivers, primary forest, and primary ground at the given
     location."
    shared actual {TileFixture*} getOtherFixtures(Point location) =>
            {*(fixtures.get(location) else {})};
    "The current player."
    shared actual Player currentPlayer => playerCollection.currentPlayer;
    assign currentPlayer {
        Player oldCurrent = playerCollection.currentPlayer;
        if (is MutablePlayer oldCurrent) {
            oldCurrent.setCurrent(false);
        } else {
            log.warn("Previous current player wasn't mutable");
        }
        Player newCurrent = playerCollection.getPlayer(currentPlayer.playerId);
        if (is MutablePlayer newCurrent) {
            newCurrent.setCurrent(true);
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
        {River*} existing = rivers.get(location) else {};
        rivers.put(location, set {*existing}.union(set {*addedRivers}));
    }
    "Remove rivers from the given location."
    shared actual void removeRivers(Point location, River* removedRivers) {
        if (exists existing = rivers.get(location)) {
            rivers.put(location, set {*existing}
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
            ground.put(location, newGround);
        } else {
            ground.remove(location);
        }
    }
    """Add a fixture at a location, and return whether the "all fixtures at this point"
       set has an additional member as a result of this."""
    shared actual Boolean addFixture(Point location, TileFixture fixture) {
        if ({getForest(location), getGround(location)}.coalesced
                .any((existing) => existing.equalsIgnoringID(fixture))) {
            return false;
        }
        MutableList<TileFixture> local;
        if (exists temp = fixtures.get(location)) {
            local = temp;
        } else {
            local = ArrayList<TileFixture>();
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
        if (is IMapNG obj) {
            if (dimensions == obj.dimensions, players.containsEvery(obj.players),
                    obj.players.containsEvery(players), currentTurn == obj.currentTurn,
                    currentPlayer == obj.currentPlayer) {
                for (point in locations) {
                    {TileFixture*} ourFixtures = getAllFixtures(point);
                    {TileFixture*} theirFixtures = obj.getAllFixtures(point);
                    if (getBaseTerrain(point) !=obj.getBaseTerrain(point) ||
                    isMountainous(point) !=obj.isMountainous(point) ||
                    set { *getRivers(point) } !=set { *obj.getRivers(point) } ||
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
            if (isLocationEmpty(location)) {
                continue;
            }
            builder.append("At ``location``");
            if (getBaseTerrain(location) != TileType.notVisible) {
                builder.append("terrain: ``getBaseTerrain(location)``, ");
            }
            if (isMountainous(location)) {
                builder.append("mountains, ");
            }
            if (exists ground = getGround(location)) {
                builder.append("ground: ``ground``");
            }
            if (exists forest = getForest(location)) {
                builder.append("forest: ``forest``");
            }
            if (!getRivers(location).empty) {
                builder.append("rivers:");
                for (river in getRivers(location)) {
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
    shared actual Boolean isSubset(IMapNG obj, Anything(String) report) {
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
                if (!{getBaseTerrain(point), TileType.notVisible}
                        .contains(obj.getBaseTerrain(point))) {
                    if (TileType.notVisible == getBaseTerrain(point)) {
                        localReport("Has terrain information we don't");
                    } else {
                        localReport("Base terrain differs");
                    }
                    retval = false; // return false;
                    continue;
                }
                if (obj.isMountainous(point), !isMountainous(point)) {
                    localReport("Has mountains we don't");
                    retval = false; // return false;
                }
                if (exists forest = obj.getForest(point)) {
                    // There are *far* too many false positives if we don't check the
                    // "other fixtures," because of the way we represent this in the XML.
                    // If we ever start a new campaign with a different data
                    // representation---perhaps a database---we should remove this check.
                    if (!getAllFixtures(point).contains(forest)) {
                        localReport("Has forest we don't");
                        retval = false; // return false;
                    }
                }
                if (exists ground = obj.getGround(point)) {
                    if (exists ourGround = getGround(point)) {
                        // There are *far* too many false positives if we don't check the
                        // "other fixtures," because of the way we represent this in the
                        // XML. If we ever start a new campaign with a different data
                        // representation---perhaps a database---we should remove this
                        // check. Except for the 'exposed' (kind == kind) bit.
                        if (ground != ourGround, !getAllFixtures(point).narrow<Ground>()
                                .any((item) => item.kind == ground.kind)) {
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
                // TODO: Use getAllFixtures instead of adding ground and forest later
                for (fixture in getOtherFixtures(point)) {
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
                if (exists ground = getGround(point)) {
                    ourFixtures.add(ground);
                }
                if (exists forest = getForest(point)) {
                    ourFixtures.add(forest);
                }
                {TileFixture*} theirFixtures = obj.getOtherFixtures(point);
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
                if (!set { *obj.getRivers(point) }
                        .complement(set { *getRivers(point) }).empty) {
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
    todo("Should we really satisfy Comparable?")
    shared actual Comparison compare(IMapNG other) {
        if (equals(other)) {
            return equal;
        } else {
            return hash <=> other.hash;
        }
    }
    "Clone a map, possibly for a specific player, who shouldn't see other players'
     details."
    shared actual IMapNG copy(Boolean zero, Player? player) {
        IMutableMapNG retval = SPMapNG(dimensions, playerCollection.copy(),
            currentTurn);
        for (point in locations) {
            retval.setBaseTerrain(point, getBaseTerrain(point));
            if (exists grd = getGround(point)) {
                retval.setGround(point, grd.copy(false));
            } else {
                retval.setGround(point, null);
            }
            if (exists forest = getForest(point)) {
                retval.setForest(point, forest.copy(false));
            } else {
                retval.setForest(point, null);
            }
            retval.setMountainous(point, isMountainous(point));
            retval.addRivers(point, *getRivers(point));
            // TODO: what other fixtures should we zero, or skip?
            for (fixture in getOtherFixtures(point)) {
                retval.addFixture(point,
                    fixture.copy(zero && shouldZero(fixture, player)));
            }
        }
        return retval;
    }
}