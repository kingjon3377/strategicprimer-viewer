import lovelace.util.common {
    todo
}
import strategicprimer.model.map.fixtures.towns {
    Village,
	ITownFixture,
	CommunityStats
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
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.common {
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
    IMapNG,
    Point
}
import strategicprimer.model.map.fixtures.terrain {
    Forest
}
import strategicprimer.model.map.fixtures {
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
import java.lang {
	IllegalStateException
}
class SimpleTerrain of unforested | forested | ocean {
	"Plains, desert, and mountains"
	shared new unforested { }
	"Temperate forest, boreal forest, and steppe"
	shared new forested { }
	"Ocean."
	shared new ocean { }
}
"""A hackish driver to fix TODOs (missing content) in the map, namely units with "TODO"
   for their "kind" and aquatic villages with non-aquatic races."""
todo("Write tests of this functionality") // This'll have to wait until eclipse/ceylon#6986 is fixed
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
    shared actual IDriverUsage usage = DriverUsage(false, ["-o", "--fix-todos"],
        ParamCount.atLeastOne, "Fix TODOs in maps",
        "Fix TODOs in unit kinds and aquatic villages with non-aquatic races");
    "Get the simplified-terrain-model instance covering the map's terrain at the given
     location." // We don't just use TileType because we need mountains and forests in ver-2 maps.
    suppressWarnings("deprecation")
    SimpleTerrain getTerrain(IMapNG map, Point location) {
        switch (map.baseTerrain[location])
        case (TileType.jungle|TileType.borealForest|TileType.temperateForest|TileType.swamp) {
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
    void fixAllVillages(IMapNG map, ICLIHelper cli) {
        {Village*} villages = map.locations
            .filter((loc) => (map.baseTerrain[loc] else TileType.plains) == TileType.ocean)
            .flatMap(map.fixtures.get).narrow<Village>()
            .filter((village) => landRaces.contains(village.race));
        if (!villages.empty) {
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
        {[Point, CommunityStats]*} brokenTownContents = map.locations
        //            .flatMap((loc) => [loc, map.fixtures[loc]]).narrow<ITownFixture>()
                .flatMap((loc) => map.fixtures.get(loc).narrow<ITownFixture>().map((item) => [loc, item]))
                .map(([loc, town]) => [loc, town.population]).narrow<[Point, CommunityStats]>()
                .filter(([loc, pop]) => pop.yearlyProduction.map(ResourcePile.contents)
                    .any((str) => str.contains('#')));
        if (!brokenTownContents.empty) {
            value runner = ExplorationRunner();
            if (is Directory directory = parsePath("tables").resource) {
                loadAllTables(directory, runner);
            } else {
                throw IllegalStateException("TODO fixer requires a tables directory");
            }
	        for ([loc, population] in brokenTownContents) {
	            value production = population.yearlyProduction;
	            for (resource in production.sequence()) {
	                if (resource.contents.contains('#')) {
	                    assert (exists table = resource.contents.split('#'.equals, true, true).sequence()[1]);
	                    value replacement = ResourcePile(resource.id, resource.kind,
	                        runner.recursiveConsultTable(table, loc, map.baseTerrain[loc],
	                            //map.mountainous[loc],  map.fixtures[loc], map.dimensions), resource.quantity);
	                            map.mountainous.get(loc),  map.fixtures.get(loc), map.dimensions), resource.quantity);
	                    production.remove(resource);
	                    production.add(replacement);
	                }
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
                    "Setting unit with ID #``unit.id`` (``count`` / 5328) to kind ``job``");
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
    void fixAllUnits(IMapNG map, ICLIHelper cli) {
        for (point in map.locations) {
            SimpleTerrain terrain = getTerrain(map, point);
//            for (fixture in map.fixtures[point]) { // TODO: syntax sugar once compiler bug fixed
            for (fixture in map.fixtures.get(point)) {
                if (is Unit fixture, "TODO" == fixture.kind) {
                    fixUnit(fixture, terrain, cli);
                }
            }
        }
    }
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IMultiMapModel model) {
            for ([map, path] in model.allMaps) {
                fixAllUnits(map, cli);
                fixAllVillages(map, cli);
            }
        } else {
            fixAllUnits(model.map, cli);
            fixAllVillages(model.map, cli);
        }
    }
}
