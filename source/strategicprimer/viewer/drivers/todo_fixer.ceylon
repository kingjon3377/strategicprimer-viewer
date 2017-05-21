import lovelace.util.common {
    todo
}
import strategicprimer.model.map.fixtures.towns {
    Village
}
import ceylon.random {
    Random,
    DefaultRandom
}
import strategicprimer.model.map.fixtures.mobile {
    Unit
}
import ceylon.collection {
    ArrayList,
    MutableMap,
    MutableList,
    HashMap
}
import strategicprimer.drivers.common {
    ICLIHelper,
    SPOptions,
    DriverUsage,
    IMultiMapModel,
    SimpleCLIDriver,
    ParamCount,
    IDriverUsage,
    IDriverModel
}
import strategicprimer.model.map {
    TileType,
    IMap,
    Point
}
abstract class SimpleTerrain() of unforested | forested | ocean { }
"Plains, desert, and mountains"
object unforested extends SimpleTerrain() { }
"Temperate forest, boreal forest, and steppe"
object forested extends SimpleTerrain() { }
"Ocean."
object ocean extends SimpleTerrain() { }
"""A hackish driver to fix TODOs (missing content) in the map, namely units with "TODO"
   for their "kind" and aquatic villages with non-aquatic races."""
todo("Write tests of this functionality")
object todoFixerCLI satisfies SimpleCLIDriver {
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
    shared actual IDriverUsage usage = DriverUsage(false, "-o", "--fix-todos",
        ParamCount.atLeastOne, "Fix TODOs in maps",
        "Fix TODOs in unit kinds and aquatic villages with non-aquatic races");
    "Get the simplified-terrain-model instance covering the map's terrain at the given
     location."
    todo("Just use TileType now we have union types available")
    suppressWarnings("deprecation")
    SimpleTerrain getTerrain(IMap map, Point location) {
        switch (map.baseTerrain(location))
        case (TileType.jungle|TileType.borealForest|TileType.temperateForest) {
            return forested;
        }
        case (TileType.desert|TileType.mountain|TileType.tundra|TileType.notVisible) {
            return unforested; }
        case (TileType.ocean) { return ocean; }
        case (TileType.plains|TileType.steppe) {
            if (map.mountainous(location)) {
                return unforested;
            } else if (map.forest(location) exists) {
                return forested;
            } else {
                return unforested;
            }
        }
    }
    "Search for and fix aquatic villages with non-aquatic races."
    void fixAllVillages(IMap map, ICLIHelper cli) {
        Village[] villages = [ for (point in map.locations)
        if (map.baseTerrain(point) == TileType.ocean)
        for (fixture in map.otherFixtures(point))
        if (is Village fixture, landRaces.contains(fixture.race))
        fixture ];
        if (nonempty villages) {
            if (raceList.empty) {
                while (true) {
                    String race = cli.inputString("Next aquatic race: ").trimmed;
                    if (race.empty) {
                        break;
                    }
                    raceList.add(race);
                }
            }
            for (village in villages) {
                if (exists race = raceMap.get(village.id)) {
                    village.race = race;
                } else {
                    Random rng = DefaultRandom(village.id);
                    assert (exists race = rng.nextElement(raceList));
                    village.race = race;
                    raceMap.put(village.id, race);
                }
            }
        }
    }
    "Fix a stubbed-out kind for a unit."
    void fixUnit(Unit unit, SimpleTerrain terrain, ICLIHelper cli) {
        Random rng = DefaultRandom(unit.id);
        count++;
        MutableList<String> jobList;
        String description;
        switch (terrain)
        case (unforested) {
            jobList = plainsList;
            description = "plains, desert, or mountains";
        }
        case (forested) {
            jobList = forestList;
            description = "forest or jungle";
        }
        case (ocean) {
            jobList = oceanList;
            description = "ocean";
        }
        for (job in jobList) {
            if (rng.nextBoolean()) {
                cli.println("Setting unit with ID #``
                unit.id`` (``count`` / 5328) to kind ``job``");
                unit.kind = job;
                return;
            }
        }
        String kind = cli.inputString(
            "What's the next possible kind for ``description``? ");
        unit.kind = kind;
        jobList.add(kind);
    }
    "Search for and fix units with kinds missing."
    void fixAllUnits(IMap map, ICLIHelper cli) {
        for (point in map.locations) {
            SimpleTerrain terrain = getTerrain(map, point);
            for (fixture in map.otherFixtures(point)) {
                if (is Unit fixture, "TODO" == fixture.kind) {
                    fixUnit(fixture, terrain, cli);
                }
            }
        }
    }
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IMultiMapModel model) {
            for (pair in model.allMaps) {
                fixAllUnits(pair.first, cli);
                fixAllVillages(pair.first, cli);
            }
        } else {
            fixAllUnits(model.map, cli);
            fixAllVillages(model.map, cli);
        }
    }
}
