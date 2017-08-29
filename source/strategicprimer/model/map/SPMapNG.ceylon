import strategicprimer.model.map.fixtures.mobile {
    IUnit
}
import lovelace.util.common {
    ArraySet,
    todo,
    anythingEqual,
    NonNullCorrespondence
}
import ceylon.collection {
    MutableSet,
    ArrayList,
    MutableMap,
    MutableList,
    HashSet,
    HashMap
}
import ceylon.logging {
    Logger,
    logger
}
"A logger."
Logger log = logger(`module strategicprimer.model`);
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
    "Fixtures at various points, other than the main ground and forest."
    todo("Use a multimap once we add a library dependency providing such a class")
    MutableMap<Point, MutableSet<TileFixture>> fixturesMap =
            HashMap<Point, MutableSet<TileFixture>>();
    MapDimensions mapDimensions;
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
    "Whether the given point is in the map."
    Boolean contained(Point point) =>
            (0:mapDimensions.rows).contains(point.row) && (0:mapDimensions.columns)
                .contains(point.column);
    "The dimensions of the map."
    shared actual MapDimensions dimensions => mapDimensions;
    "A stream of the players known in the map"
    shared actual {Player*} players => {*playerCollection};
    "The locations in the map."
    shared actual {Point*} locations => PointIterator(dimensions, true, true);
    "The base terrain at the given location."
    shared actual object baseTerrain satisfies Correspondence<Point, TileType>&
            KeyedCorrespondenceMutator<Point, TileType?> {
        shared actual Boolean defines(Point key) => contained(key);
        shared actual TileType? get(Point key) => terrain[key];
        shared actual void put(Point key, TileType? item) {
            if (exists item) {
                terrain[key] = item;
            } else {
                terrain.remove(key);
            }
        }
    }
    "Whether the given location is mountainous."
    shared actual object mountainous satisfies NonNullCorrespondence<Point, Boolean>&
            KeyedCorrespondenceMutator<Point, Boolean> {
        shared actual Boolean defines(Point key) => contained(key);
        shared actual Boolean get(Point key) => mountains.contains(key);
        shared actual void put(Point key, Boolean item) {
            if (item) {
                mountains.add(key);
            } else {
                mountains.remove(key);
            }
        }
    }
    "The rivers, if any, at the given location."
    shared actual object rivers satisfies NonNullCorrespondence<Point, {River*}> {
        shared actual Boolean defines(Point key) => contained(key);
        shared actual {River*} get(Point key) => {*(riversMap[key] else {})};
    }
    "The tile fixtures (other than rivers and mountains) at the given location."
    shared actual object fixtures satisfies NonNullCorrespondence<Point, {TileFixture*}> {
        shared actual Boolean defines(Point key) => contained(key);
        shared actual {TileFixture*} get(Point key) =>
                {*(fixturesMap[key] else {})};
    }
    "The current player."
    shared actual Player currentPlayer => playerCollection.currentPlayer;
    assign currentPlayer => playerCollection.currentPlayer = currentPlayer;
    "Add a player."
    shared actual void addPlayer(Player player) => playerCollection.add(player);
    "Add rivers at a location."
    shared actual void addRivers(Point location, River* addedRivers) {
        {River*} existing = riversMap[location] else {};
        riversMap[location] = set {*existing}.union(set {*addedRivers});
    }
    "Remove rivers from the given location."
    shared actual void removeRivers(Point location, River* removedRivers) {
        if (exists existing = riversMap[location]) {
            riversMap[location] = set {*existing}.complement(set {*removedRivers});
        }
    }
    """Add a fixture at a location, and return whether the "all fixtures at this point"
       set has an additional member as a result of this."""
    shared actual Boolean addFixture(Point location, TileFixture fixture) {
        MutableSet<TileFixture> local;
        if (exists temp = fixturesMap[location]) {
            local = temp;
        } else {
            local = ArraySet<TileFixture>();
            fixturesMap[location] = local;
        }
        if (fixture.id >= 0,
            exists existing = local.find((item) => item.id == fixture.id)) {
            Boolean subsetCheck(TileFixture one, TileFixture two) {
                if (is Subsettable<IFixture> one, one.isSubset(two, noop)) {
                    return true;
                } else if (is Subsettable<IFixture> two, two.isSubset(one, noop)) {
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
        if (exists list = fixturesMap[location]) {
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
                    assert (exists ourFixtures = fixtures[point]);
                    assert (exists theirFixtures = obj.fixtures[point]);
                    if (!anythingEqual(baseTerrain[point], obj.baseTerrain[point]) ||
                            !anythingEqual(mountainous[point], obj.mountainous[point]) ||
                            set { *(rivers[point] else {}) } !=
                                set { *(obj.rivers[point] else {})  } ||
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
            if (exists terrain = baseTerrain[location]) {
                builder.append("terrain: ``terrain``, ");
            }
            if (exists mtn = mountainous[location], mtn) {
                builder.append("mountains, ");
            }
            if (exists rvr = rivers[location], !rvr.empty) {
                builder.append("rivers:");
                for (river in rvr) {
                    builder.append(" ``river``");
                }
                builder.append(", ");
            }
            if (exists other = fixtures[location]) {
                builder.append("fixtures: ");
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
            variable Boolean retval = playerCollection.isSubset(obj.players, report);
            // Declared here to avoid object allocations in the loop.
            MutableList<TileFixture> ourFixtures = ArrayList<TileFixture>();
            MutableMap<Integer, MutableList<Subsettable<IFixture>>> ourSubsettables =
                    HashMap<Integer, MutableList<Subsettable<IFixture>>>();
            // IUnit is Subsettable<IUnit> and thus incompatible with SubsettableFixture
            MutableMap<Integer, IUnit> ourUnits = HashMap<Integer, IUnit>();
            for (point in locations) {
                void localReport(String string) => report("At ``point``:\t``string``");
                if (exists theirTerrain = obj.baseTerrain[point]) {
                    if (exists ourTerrain = baseTerrain[point]) {
                        if (ourTerrain != theirTerrain) {
                            localReport("Base terrain differs");
                            retval = false;
                            continue;
                        }
                    } else {
                        localReport("Has terrain information we don't");
                        retval = false;
                        continue;
                    }
                }
                if (exists theirMountains = obj.mountainous[point], theirMountains,
                        anythingEqual(false, mountainous[point])) {
                    localReport("Has mountains we don't");
                    retval = false; // return false;
                }
                ourFixtures.clear();
                ourSubsettables.clear();
                ourUnits.clear();
                for (fixture in (fixtures[point] else {})) {
                    Integer idNum = fixture.id;
                    if (is IUnit fixture) {
                        ourUnits[idNum] = fixture;
                    } else if (is Subsettable<IFixture> fixture) {
                        if (exists list = ourSubsettables[idNum]) {
                            list.add(fixture);
                        } else {
                            MutableList<Subsettable<IFixture>> list =
                                    ArrayList<Subsettable<IFixture>>();
                            list.add(fixture);
                            ourSubsettables[idNum] = list;
                        }
                    } else {
                        ourFixtures.add(fixture);
                    }
                }
                {TileFixture*} theirFixtures = obj.fixtures[point] else {};
                for (fixture in theirFixtures) {
                    if (ourFixtures.contains(fixture) || shouldSkip(fixture)) {
                        continue;
                    } else if (is IUnit fixture, exists unit = ourUnits[fixture.id]) {
                        retval = retval && unit.isSubset(fixture, localReport);
                    } else if (is Subsettable<IFixture> fixture,
                            exists list = ourSubsettables[fixture.id]) {
                        variable Integer count = 0;
                        variable Boolean unmatched = true;
                        variable Subsettable<IFixture>? match = null;
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
                if (!set { *(obj.rivers[point] else {}) }
                    .complement(set { *(rivers[point] else {}) }).empty) {
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
    shared actual IMapNG copy(Boolean zero, Player? player) {
        IMutableMapNG retval = SPMapNG(dimensions, playerCollection.copy(),
            currentTurn);
        for (point in locations) {
            if (exists terrain = baseTerrain[point]) {
                retval.baseTerrain[point] = terrain;
            }
            retval.mountainous[point] = (mountainous[point] else false);
            retval.addRivers(point, *(rivers[point] else {}));
            // TODO: what other fixtures should we zero, or skip?
            for (fixture in (fixtures[point] else {})) {
                retval.addFixture(point,
                    fixture.copy(zero && shouldZero(fixture, player)));
            }
        }
        return retval;
    }
}