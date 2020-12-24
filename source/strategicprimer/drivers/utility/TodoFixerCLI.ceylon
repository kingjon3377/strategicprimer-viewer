import lovelace.util.common {
    todo,
    matchingValue,
    narrowedStream,
    entryMap
}

import strategicprimer.model.common.map.fixtures.towns {
    Village,
    ITownFixture,
    CommunityStats
}

import ceylon.random {
    Random,
    DefaultRandom
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
    CLIDriver,
    emptyOptions,
    SPOptions
}

import strategicprimer.model.common.map {
    TileType,
    Point,
    IMapNG
}

import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}

import strategicprimer.model.common.map.fixtures {
    ResourcePile
}

import strategicprimer.drivers.exploration.old {
    ExplorationRunner
}

import ceylon.file {
    parsePath,
    Directory
}

"""A hackish driver to fix TODOs (missing content) in the map, namely units with "TODO"
   for their "kind" and aquatic villages with non-aquatic races."""
todo("Write tests of this functionality") // This'll have to wait until eclipse/ceylon#6986 is fixed
// FIXME: Move mutation operations into a driver model
shared class TodoFixerCLI(ICLIHelper cli, model) satisfies CLIDriver {
    shared actual UtilityDriverModel model;
    shared actual SPOptions options = emptyOptions;
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
    SimpleTerrain getTerrain(IMapNG map, Point location) {
        switch (map.baseTerrain[location])
        case (TileType.jungle|TileType.swamp) {
            return SimpleTerrain.forested;
        }
        case (TileType.desert|TileType.tundra|null) {
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

    variable ExplorationRunner? _runner = null;
    ExplorationRunner runner {
        if (exists retval = _runner) {
            return retval;
        } else {
            value retval = ExplorationRunner();
            _runner = retval;
            "TODO fixer requires a tables directory"
            assert (is Directory directory = parsePath("tables").resource);
            retval.loadAllTables(directory);
            return retval;
        }
    }

    String simpleTerrain(IMapNG map, Point loc) {
        if (exists terrain = map.baseTerrain[loc], terrain == TileType.ocean) {
            return "ocean";
        } else if (map.mountainous.get(loc)) { // TODO: syntax sugar
            return "mountain";
        } else if (map.fixtures.get(loc).narrow<Forest>().empty) { // TODO: syntax sugar
            return "plains";
        } else {
            return "forest";
        }
    }

    Boolean productionContainsHash([Point, CommunityStats] pair) =>
                pair.rest.first.yearlyProduction.map(ResourcePile.contents).any(shuffle(String.contains)('#'));

    Boolean anyEmptySkills([Point, CommunityStats] pair) =>
                pair.rest.first.highestSkillLevels.map(Entry.key).any(String.empty);

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
                    .filter(productionContainsHash);

        if (!brokenTownContents.empty) {
            value eRunner = runner;
            for ([loc, population] in brokenTownContents) {
                value production = population.yearlyProduction;
                for (resource in production.filter(compose(shuffle(String.contains)('#'),
                        ResourcePile.contents)).sequence()) {
                    assert (exists table = resource.contents
                        .split('#'.equals, true, true).sequence()[1]);
                    value replacement = ResourcePile(resource.id, resource.kind,
                        eRunner.recursiveConsultTable(table, loc, map.baseTerrain[loc],
                            //map.mountainous[loc],  map.fixtures[loc], // TODO: syntax sugar once compiler bug fixed
                            map.mountainous.get(loc),  map.fixtures.get(loc),
                            map.dimensions), resource.quantity);
                    production.remove(resource);
                    production.add(replacement);
                }
            }
        }

        {[Point, CommunityStats]*} brokenExpertise =
            narrowedStream<Point, ITownFixture>(map.fixtures)
                .map(entryMap(identity<Point>, ITownFixture.population))
                .map(Entry.pair).narrow<[Point, CommunityStats]>()
                .filter(anyEmptySkills);
        if (!brokenExpertise.empty) {
            value eRunner = runner;
            for ([loc, population] in brokenExpertise) {
                assert (exists level = population.highestSkillLevels[""]);
                population.setSkillLevel("", 0);
                variable String newSkill = eRunner.recursiveConsultTable(
                    simpleTerrain(map, loc) + "_skills", loc, map.baseTerrain[loc],
                    //map.mountainous[loc],  map.fixtures[loc], map.dimensions);// TODO: syntax sugar once compiler bug fixed
                    map.mountainous.get(loc),  map.fixtures.get(loc), map.dimensions);
                if (exists existingLevel = population.highestSkillLevels[newSkill],
                        existingLevel >= level) {
                    newSkill = eRunner.recursiveConsultTable("regional_specialty", loc,
                        //map.baseTerrain[loc], map.mountainous[loc],  map.fixtures[loc]);// TODO: syntax sugar once compiler bug fixed
                        map.baseTerrain[loc], map.mountainous.get(loc),  map.fixtures.get(loc),
                        map.dimensions);
                }
                if (exists existingLevel = population.highestSkillLevels[newSkill],
                        existingLevel >= level) {
                    continue;
                }
                population.setSkillLevel(newSkill, level);
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
        totalCount = map.fixtures.items.narrow<Unit>()
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

    shared actual void startDriver() {
        if (!model.subordinateMaps.empty) {
            for (map in model.allMaps) {
                fixAllUnits(map);
                fixAllVillages(map);
                model.setMapModified(map, true);
            }
            for (location in model.map.locations) {
                model.copyRiversAt(location);
            }
        } else {
            fixAllUnits(model.map);
            fixAllVillages(model.map);
            model.mapModified = true;
        }
    }
}
