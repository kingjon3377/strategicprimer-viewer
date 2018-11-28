import lovelace.util.common {
    todo,
    matchingValue,
    narrowedStream,
    entryMap,
    PathWrapper
}
import strategicprimer.model.common.map.fixtures.towns {
    Village,
    ITownFixture,
    CommunityStats
}
import ceylon.random {
    Random,
    DefaultRandom,
    randomize
}
import strategicprimer.model.common.map.fixtures.mobile {
    Unit
}
import ceylon.collection {
    ArrayList,
    MutableMap,
    MutableList,
    HashMap
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.common {
    SPOptions,
    DriverUsage,
    IMultiMapModel,
    ParamCount,
    IDriverUsage,
    IDriverModel,
    CLIDriver,
    DriverFactory,
    ModelDriverFactory,
    ModelDriver,
    SimpleMultiMapModel
}
import strategicprimer.model.common.map {
    TileType,
    Point,
    IMapNG,
    IMutableMapNG
}
import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}
import strategicprimer.model.common.map.fixtures {
    ResourcePile
}
import strategicprimer.drivers.exploration.old {
    ExplorationRunner,
    loadAllTables
}
import ceylon.file {
    parsePath,
    Directory
}
import strategicprimer.drivers.exploration.common {
    surroundingPointIterable
}
import strategicprimer.model.common.idreg {
    IDRegistrar,
    createIDFactory
}

"""A simplified model of terrain, dividing tiles into "ocean", "forested", and
   "unforested"."""
class SimpleTerrain of unforested | forested | ocean {
    "Plains, desert, and mountains"
    shared new unforested { }
    "Temperate forest, boreal forest, and steppe"
    shared new forested { }
    "Ocean."
    shared new ocean { }
}

"""A factory for the hackish driver to fix missing content in the map, namely units with
   "TODO" for their "kind" and aquatic villages with non-aquatic races."""
service(`interface DriverFactory`)
shared class TodoFixerFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage(false, ["--fix-todos"],
        ParamCount.atLeastOne, "Fix TODOs in maps",
        "Fix TODOs in unit kinds and aquatic villages with non-aquatic races", false,
        false);

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) => TodoFixerCLI(cli, model);

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            SimpleMultiMapModel(map, path);
}

"""A hackish driver to fix TODOs (missing content) in the map, namely units with "TODO"
   for their "kind" and aquatic villages with non-aquatic races."""
todo("Write tests of this functionality") // This'll have to wait until eclipse/ceylon#6986 is fixed
shared class TodoFixerCLI(ICLIHelper cli, model) satisfies CLIDriver {
    shared actual IDriverModel model;
    "A list of unit kinds (jobs) for plains etc."
    MutableList<String> plainsList = ArrayList<String>();

    "A list of unit kinds (jobs) for forest and jungle."
    MutableList<String> forestList = ArrayList<String>();

    "A list of unit kinds (jobs) for ocean."
    MutableList<String> oceanList = ArrayList<String>();

    "A map from village IDs to races."
    MutableMap<Integer, String> raceMap = HashMap<Integer, String>();

    "A list of aqautic races."
    MutableList<String> raceList = ArrayList<String>();

    "How many units we've fixed."
    variable Integer count = -1;

    "The number of units needing to be fixed."
    variable Integer totalCount = -1;

    "Get the simplified-terrain-model instance covering the map's terrain at the given
     location."
    // We don't just use TileType because we need mountains and forests in ver-2 maps.
    suppressWarnings("deprecation")
    SimpleTerrain getTerrain(IMapNG map, Point location) {
        switch (map.baseTerrain[location])
        case (TileType.jungle|TileType.borealForest|TileType.temperateForest|
                TileType.swamp) {
            return SimpleTerrain.forested;
        }
        case (TileType.desert|TileType.mountain|TileType.tundra|null) {
            return SimpleTerrain.unforested; }
        case (TileType.ocean) { return SimpleTerrain.ocean; }
        case (TileType.plains|TileType.steppe) {
//            if (map.mountainous[location]) { // TODO: syntax sugar once compiler bug fixed
            if (map.mountainous.get(location)) {
                return SimpleTerrain.unforested;
            } else if (map.fixtures[location]?.narrow<Forest>()?.first exists) {
                return SimpleTerrain.forested;
            } else {
                return SimpleTerrain.unforested;
            }
        }
    }

    "Search for and fix aquatic villages with non-aquatic races."
    void fixAllVillages(IMapNG map) {
        {Village*} villages = map.locations
            .filter(matchingValue(TileType.ocean, map.baseTerrain.get))
            .flatMap(map.fixtures.get).narrow<Village>()
            .filter(compose(landRaces.contains, Village.race));
        if (!villages.empty) {
            if (raceList.empty) {
                while (true) {
                    if (exists race = cli.inputString("Next aquatic race: ")) {
                        if (race.empty) {
                            break;
                        }
                        raceList.add(race);
                    } else {
                        return;
                    }
                }
            }
            for (village in villages) {
                if (exists race = raceMap[village.id]) {
                    village.race = race;
                } else {
                    Random rng = DefaultRandom(village.id);
                    assert (exists race = rng.nextElement(raceList));
                    village.race = race;
                    raceMap[village.id] = race;
                }
            }
        }

        {[Point, CommunityStats]*} brokenTownContents =
                narrowedStream<Point, ITownFixture>(map.fixtures)
                    .map(entryMap(identity<Point>, ITownFixture.population))
                    .map(Entry.pair)
                    .narrow<[Point, CommunityStats]>()
                    .filter(([loc, pop]) => pop.yearlyProduction.map(
                        ResourcePile.contents).any(shuffle(String.contains)('#')));

        if (!brokenTownContents.empty) {
            value runner = ExplorationRunner();
            "TODO fixer requires a tables directory"
            assert (is Directory directory = parsePath("tables").resource);
            loadAllTables(directory, runner);
            for ([loc, population] in brokenTownContents) {
                value production = population.yearlyProduction;
                for (resource in production.filter(compose(shuffle(String.contains)('#'),
                        ResourcePile.contents)).sequence()) {
                    assert (exists table = resource.contents
                        .split('#'.equals, true, true).sequence()[1]);
                    value replacement = ResourcePile(resource.id, resource.kind,
                        runner.recursiveConsultTable(table, loc, map.baseTerrain[loc],
                            //map.mountainous[loc],  map.fixtures[loc], // TODO: syntax sugar once compiler bug fixed
                            map.mountainous.get(loc),  map.fixtures.get(loc),
                            map.dimensions), resource.quantity);
                    production.remove(resource);
                    production.add(replacement);
                }
            }
        }
    }

    "Fix a stubbed-out kind for a unit."
    void fixUnit(Unit unit, SimpleTerrain terrain) {
        Random rng = DefaultRandom(unit.id);
        count++;
        MutableList<String> jobList;
        String description;
        switch (terrain)
        case (SimpleTerrain.unforested) {
            jobList = plainsList;
            description = "plains, desert, or mountains";
        }
        case (SimpleTerrain.forested) {
            jobList = forestList;
            description = "forest or jungle";
        }
        case (SimpleTerrain.ocean) {
            jobList = oceanList;
            description = "ocean";
        }
        for (job in jobList) {
            if (rng.nextBoolean()) {
                cli.println(
                    "Setting unit with ID #``unit.id`` (``count`` / ``
                        totalCount``) to kind ``job``");
                unit.kind = job;
                return;
            }
        }
        if (exists kind = cli.inputString(
                "What's the next possible kind for ``description``? ")) {
            unit.kind = kind;
            jobList.add(kind);
        }
    }

    "Search for and fix units with kinds missing."
    void fixAllUnits(IMapNG map) {
        totalCount = map.fixtures.map(Entry.item).narrow<Unit>()
            .count(matchingValue("TODO", Unit.kind));
        for (point in map.locations) {
            SimpleTerrain terrain = getTerrain(map, point);
//            for (fixture in map.fixtures[point].narrow<Unit>() // TODO: syntax sugar once compiler bug fixed
            for (fixture in map.fixtures.get(point).narrow<Unit>()
                    .filter(matchingValue("TODO", Unit.kind))) {
                fixUnit(fixture, terrain);
            }
        }
    }

    "Add rivers missing from the subordinate map where it has other terrain information."
    void fixMissingRivers(IMapNG mainMap, IMutableMapNG subordinateMap) {
        for (location in mainMap.locations) {
            if (exists mainTerrain = mainMap.baseTerrain[location],
                    exists subTerrain = subordinateMap.baseTerrain[location],
                    mainTerrain == subTerrain,
                    //!mainMap.rivers[location].empty, subordinateMap.rivers[location].empty) { // TODO: syntax sugar
                    !mainMap.rivers.get(location).empty,
                    subordinateMap.rivers.get(location).empty) {
                //subordinateMap.addRivers(location, *mainMap.rivers[location]); // TODO: syntax sugar
                subordinateMap.addRivers(location, *mainMap.rivers.get(location));
            }
        }
    }

    suppressWarnings("deprecation")
    void fixAdjacentForests(IMutableMapNG map, IDRegistrar idf, Point location, TileType tileType,
            String forest) {
        TileType destination;
        switch (tileType)
        case (TileType.temperateForest) {
            destination = TileType.plains;
        }
        case (TileType.borealForest) {
            destination = TileType.steppe;
        }
        else {
            throw AssertionError(
                "tileType to convert must be temperate or boreal forest");
        }
        if (exists terrain = map.baseTerrain[location], terrain == tileType) {
            map.baseTerrain[location] = destination;
            map.addFixture(location, Forest(forest, false, idf.createID()));
            for (neighbor in surroundingPointIterable(location, map.dimensions, 1)
                .filter(not(location.equals))) {
                fixAdjacentForests(map, idf, neighbor, tileType, forest);
            }
        }
    }
    suppressWarnings("deprecation")
    void fixTerrain(IMutableMapNG map) {
        if (map.dimensions.version < 2) {
            return;
        }
        IDRegistrar idf = createIDFactory(map);
        for (location in randomize(map.locations)) {
            if (exists terrain = map.baseTerrain[location]) {
                if (terrain == TileType.mountain) {
                    map.baseTerrain[location] = TileType.plains;
                    map.mountainous[location] = true;
                } else if (terrain == TileType.temperateForest) {
                    if (exists forest = cli.inputString(
                            "Kind of tree for a temperate forest: ")) {
                        fixAdjacentForests(map, idf, location,
                            TileType.temperateForest, forest);
                    } else {
                        return;
                    }
                } else if (terrain == TileType.borealForest) {
                    if (exists forest = cli.inputString(
                            "Kind of tree for a boreal forest:")) {
                        fixAdjacentForests(map, idf, location, TileType.borealForest,
                            forest);
                    } else {
                        return;
                    }
                }
            }
        }
    }

    shared actual void startDriver() {
        if (is IMultiMapModel model) {
            for (map->[path, _] in model.allMaps) {
                fixAllUnits(map);
                fixAllVillages(map);
                fixTerrain(map);
                model.setModifiedFlag(map, true);
            }
            for (map->[path, _] in model.subordinateMaps) {
                fixMissingRivers(model.map, map);
                model.setModifiedFlag(map, true);
            }
        } else {
            fixAllUnits(model.map);
            fixAllVillages(model.map);
            fixTerrain(model.map);
            model.mapModified = true;
        }
    }
}
