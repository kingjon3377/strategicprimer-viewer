import strategicprimer.drivers.common {
    SimpleCLIDriver,
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions,
    IDriverModel,
    IMultiMapModel
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.map.fixtures.towns {
    ITownFixture,
    TownStatus,
    CommunityStats,
    AbstractTown,
    Village,
    TownSize
}
import strategicprimer.model.map {
    Point,
    IMapNG,
    IFixture,
    TileType
}
import lovelace.util.jvm {
    isNumeric,
    parseInt,
    readFileContents
}
import strategicprimer.model.map.fixtures {
    ResourcePile,
    Quantity
}
import strategicprimer.model.idreg {
    IDRegistrar,
    createIDFactory
}
import ceylon.math.decimal {
    Decimal
}
import strategicprimer.model.map.fixtures.resources {
    HarvestableFixture,
    MineralVein,
    Meadow,
    Mine,
    Grove,
    StoneDeposit,
    CacheFixture,
    Shrub
}
import strategicprimer.drivers.exploration.common {
    surroundingPointIterable
}
import ceylon.random {
    Random,
    DefaultRandom,
    randomize
}
import strategicprimer.drivers.exploration.old {
    ExplorationRunner,
    loadTable
}
import strategicprimer.model.map.fixtures.terrain {
    Forest
}
import ceylon.collection {
	LinkedList,
	Stack
}
"A driver to let the user enter or generate 'stats' for towns."
shared object townGeneratingCLI satisfies SimpleCLIDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        shortOption = "-o";
        longOption = "--town";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Enter or generate stats and contents for towns and villages";
        longDescription = "Enter or generate stats and contents for towns and villages";
    };
    alias ModifiableTown=>AbstractTown|Village;
    ExplorationRunner initializeRunner() {
        ExplorationRunner retval = ExplorationRunner();
        Stack<String> firstTables = LinkedList {
            "mountain_skills", "forest_skills", "plains_skills", "ocean_skills"
        };
        Stack<String> secondTables = LinkedList<String>();
        while (exists table = firstTables.pop()) {
            assert (exists tableContents = readFileContents(
                `module strategicprimer.drivers.generators`, "tables/``table``"));
            value loadedTable = loadTable(tableContents.lines);
            retval.loadTable(table, loadedTable);
            for (reference in loadedTable.allEvents) {
                if (reference.contains('#')) {
                    value temp = reference.split('#'.equals, true, false, 2).rest.first;
                    assert (exists temp);
                    if (!retval.hasTable(temp)) {
                        firstTables.push(temp.trimmed);
                    }
                } else if (!reference.trimmed.empty) {
                    secondTables.push("``reference``_production");
                }
            }
        }
        while (exists table = secondTables.pop()) {
            assert (exists tableContents = readFileContents(
                `module strategicprimer.drivers.generators`, "tables/``table``"));
            value loadedTable = loadTable(tableContents.lines);
            retval.loadTable(table, loadedTable);
            for (reference in loadedTable.allEvents) {
                if (reference.contains('#')) {
                    value temp = reference.split('#'.equals, true, false, 2).rest.first;
                    assert (exists temp);
                    if (!retval.hasTable(temp)) {
                        secondTables.push(temp.trimmed);
                    }
                }
            }
        }
        return retval;
    }
    ExplorationRunner runner = initializeRunner();
    "The (for now active) towns in the given map that don't have 'stats' yet."
    {<Point->ModifiableTown>*} unstattedTowns(IMapNG map) => {
        for (loc in map.locations)
//            for (fixture in map.fixtures[loc]) // TODO: syntax sugar once compiler bug fixed
            for (fixture in map.fixtures.get(loc))
                if (is ModifiableTown fixture, fixture.status == TownStatus.active)
                    loc->fixture };
    void assignStatsToTown(ModifiableTown town, CommunityStats stats) {
        if (is AbstractTown town) {
            town.population = stats;
        } else {
            town.population = stats;
        }
    }
    void assignStatsInMap(IMapNG map, Point location, Integer townId,
            CommunityStats stats) {
//        for (item in map.fixtures[location] // TODO: syntax sugar once compiler bug fixed
        for (item in map.fixtures.get(location)
                .narrow<ModifiableTown>()
                .filter((item) => item.id == townId)) {
            assignStatsToTown(item, stats);
        }
    }
    IFixture? findByID(IMapNG map, Integer id) => map.locations
//        .flatMap((loc) => map.fixtures[loc])
        .flatMap((loc) => map.fixtures.get(loc))
        .find((fix) => fix.id == id);
    Point? findLocById(IMapNG map, Integer id) {
        for (location in map.locations) {
//            for (fixture in map.fixtures[location]) {
            for (fixture in map.fixtures.get(location)) {
                if (fixture.id == id) {
                    return location;
                }
            }
        }
        return null;
    }
    Boolean isClaimedField(IMapNG map, Integer id) => map.locations
//        .flatMap((loc) => map.fixtures[loc]).narrow<ITownFixture>() // TODO: syntax sugar once compiler bug fixed
        .flatMap((loc) => map.fixtures.get(loc)).narrow<ITownFixture>()
        .map(ITownFixture.population).coalesced
        .flatMap(CommunityStats.workedFields).contains(id);
    Boolean isUnclaimedField(IMapNG map, Integer id) =>
            !isClaimedField(map, id) && findByID(map, id) is HarvestableFixture;
    Boolean bothOrNeitherOcean(TileType? one, TileType? two) {
        if (exists one, one == TileType.ocean) {
            if (exists two, two == TileType.ocean) {
                return true;
            } else {
                return false;
            }
        } else if (exists two, two == TileType.ocean) {
            return false;
        } else {
            return true;
        }
    }
    Boolean isReallyClaimable(HarvestableFixture fix) {
        switch (fix)
        case (is MineralVein) {
            return fix.exposed;
        }
        case (is Meadow) {
            return fix.cultivated;
        }
        case (is Grove) {
            return fix.cultivated;
        }
        case (is Mine) {
            return fix.status == TownStatus.active;
        }
        case (is CacheFixture) {
            return false;
        } else {
            assert (is Shrub|StoneDeposit fix);
            return true;
        }
    }
    {HarvestableFixture*} findNearestFields(IMapNG map, Point location) {
        if (exists base = map.baseTerrain[location]) {
            return surroundingPointIterable(location, map.dimensions, 10).distinct
//            .filter((point) => bothOrNeitherOcean(base, map.baseTerrain[point])) // TODO: syntax sugar once compiler bug fixed
                .filter((point) => bothOrNeitherOcean(base, map.baseTerrain.get(point)))
//            .flatMap((point) => map.fixtures[point]).narrow<HarvestableFixture>()
                .flatMap((point) => map.fixtures.get(point)).narrow<HarvestableFixture>()
                .filter(isReallyClaimable);
        } else {
            return {};
        }
    }
    CommunityStats enterStats(ICLIHelper cli, IDRegistrar idf, IMapNG map, Point location, ModifiableTown town) {
        CommunityStats retval = CommunityStats(cli.inputNumber("Population: "));
        cli.println("Now enter Skill levels, the highest in the community for each Job.");
        cli.println("(Empty to end.)");
        while (true) {
            String job = cli.inputString("Job: ").trimmed;
            if (job.empty) {
                break;
            }
            Integer level = cli.inputNumber("Level: ");
            retval.setSkillLevel(job, level);
        }
        cli.println("Now enter ID numbers of worked fields (empty to skip).");
        variable {HarvestableFixture*} nearestFields = findNearestFields(map, location);
        while (true) {
            String input = cli.inputString("Field ID #: ").trimmed;
            Integer field;
            if (input.empty) {
                break;
            } else if (isNumeric(input), exists temp = parseInt(input)) {
                field = temp;
            } else if ("nearest" == input.lowercased,
                    exists nearest = nearestFields.first) {
                nearestFields = nearestFields.rest;
                cli.println("Nearest harvestable fixture is as follows:");
                cli.println(nearest.shortDescription);
                field = nearest.id;
            } else {
                cli.println("Invalid input");
                continue;
            }
            if (isClaimedField(map, field)) {
                cli.println("That field is already worked by another town");
            } else if (exists fieldLoc = findLocById(map, field)) {
                if (!bothOrNeitherOcean(map.baseTerrain[location],
                        map.baseTerrain[fieldLoc])) {
                    if (exists terrain = map.baseTerrain[location],
                            terrain == TileType.ocean) {
                        cli.println(
                            "That would be a land resource worked by an aquatic town.");
                    } else {
                        cli.println(
                            "That would be an aquatic resource worked by a town on land.");
                    }
                    if (!cli.inputBooleanInSeries("Are you sure? ", "aquatic")) {
                        continue;
                    }
                }
                if (isUnclaimedField(map, field)) {
                    retval.addWorkedField(field);
                } else {
                    cli.println("That is not the ID of a resource a town can work.");
                }
            } else {
                cli.println("That is not the ID of a resource in the map.");
            }
        }
        cli.println("Now add resources produced each year. (Empty to end.)");
        while (true) {
            String kind = cli.inputString("General kind of resource: ").trimmed;
            if (kind.empty) {
                break;
            }
            String contents = cli.inputString("Specific kind of resource: ").trimmed;
            Decimal quantity = cli.inputDecimal("Quantity of the resource produced: ");
            String units = cli.inputString("Units of that quantity: ").trimmed;
            ResourcePile pile = ResourcePile(idf.createID(), kind, contents,
                Quantity(quantity, units));
            retval.yearlyProduction.add(pile);
        }
        cli.println("Now add resources consumed each year. (Empty to end.)");
        while (true) {
            String kind = cli.inputString("General kind of resource: ").trimmed;
            if (kind.empty) {
                break;
            }
            String contents = cli.inputString("Specific kind of resource: ").trimmed;
            Decimal quantity = cli.inputDecimal("Quantity of the resource consumed: ");
            String units = cli.inputString("Units of that quantity: ").trimmed;
            ResourcePile pile = ResourcePile(idf.createID(), kind, contents,
                Quantity(quantity, units));
            retval.yearlyConsumption.add(pile);
        }
        return retval;
    }
    String getHarvestableKind(HarvestableFixture fixture) {
        if (is Grove fixture) {
            return (fixture.orchard) then "food" else "wood";
        } else if (is Meadow fixture) {
            return (fixture.field) then "food" else "fodder";
        } else if (is MineralVein fixture) {
            return "mineral";
        } else if (is StoneDeposit fixture) {
            return "stone";
        } else {
            return "unknown";
        }
    }
    String getHarvestedProduct(HarvestableFixture fixture) => fixture.kind;
    CommunityStats generateStats(IDRegistrar idf, Point location, ModifiableTown town, IMapNG map) {
        Random rng = DefaultRandom(town.id);
        Integer roll(Integer die) => rng.nextInteger(die) + 1;

        Integer repeatedlyRoll(Integer count, Integer die, Integer addend = 0) {
            variable Integer sum = addend;
            for (i in 0:count) {
                sum +=roll(die);
            }
            return sum;
        }

        Integer population;
        Integer skillCount;
        Integer() skillLevelSource;
        Integer resourceCount;
        if (is Village town) {
            assert (town.townSize == TownSize.small);
            population = repeatedlyRoll(3, 8, 3);
            skillCount = repeatedlyRoll(2, 4);
            skillLevelSource = () => repeatedlyRoll(4, 3, -3);
            resourceCount = repeatedlyRoll(2, 3);
        } else {
            switch (town.townSize)
            case (TownSize.small) {
                population = repeatedlyRoll(4, 10, 5);
                skillCount = repeatedlyRoll(3, 4);
                skillLevelSource = () => repeatedlyRoll(2, 6);
                resourceCount = repeatedlyRoll(2, 3);
            }
            case (TownSize.medium) {
                population = repeatedlyRoll(20, 20, 50);
                skillCount = repeatedlyRoll(4, 6);
                skillLevelSource = () => repeatedlyRoll(3, 6);
                resourceCount = repeatedlyRoll(2, 6);
            }
            case (TownSize.large) {
                population = repeatedlyRoll(23, 100, 200);
                skillCount = repeatedlyRoll(6, 8);
                skillLevelSource = () => repeatedlyRoll(3, 8);
                resourceCount = repeatedlyRoll(4, 6);
            }
        }
        CommunityStats retval = CommunityStats(population);
        String skillTable;
        if (exists terrain = map.baseTerrain[location]) {
            if (terrain == TileType.ocean) {
                skillTable = "ocean_skills";
//          } else if (map.mountainous[location]) {
            } else if (map.mountainous.get(location)) {
                skillTable = "mountain_skills";
//          } else if (map.fixtures[location]?.narrow<Forest>().first exists) {
            } else if (map.fixtures.get(location).narrow<Forest>().first exists) {
                skillTable = "forest_skills";
            } else {
                skillTable = "plains_skills";
            }
        } else {
            skillTable = "plains_skills";
        }
        for (i in 0:skillCount) {
            String skill = runner.recursiveConsultTable(skillTable, location,
//                map.baseTerrain[location], map.mountainous[location],
                map.baseTerrain.get(location), map.mountainous.get(location),
//                map.fixtures[location], map.dimensions);
                map.fixtures.get(location), map.dimensions);
            Integer level = skillLevelSource();
            if ((retval.highestSkillLevels.get(skill) else 0) < level) {
                retval.setSkillLevel(skill, level);
            }
        }
        {HarvestableFixture*} workedFields = findNearestFields(map, location)
            .take(resourceCount);
        for (field in workedFields) {
            retval.addWorkedField(field.id);
            retval.yearlyProduction.add(ResourcePile(idf.createID(),
                getHarvestableKind(field), getHarvestedProduct(field),
                Quantity(1, "unit")));
        }
        for (skill->level in retval.highestSkillLevels) {
            String tableName = "``skill``_production";
            if (runner.hasTable(tableName)) {
                retval.yearlyProduction.add(ResourcePile(idf.createID(), "unknown", 
                    runner.consultTable(tableName, location, map.baseTerrain.get(location), // TODO: syntax sugar
                        map.mountainous.get(location), map.fixtures.get(location), map.dimensions),
                Quantity(2.power(level - 1), (level == 1) then "unit" else "units")));
            } else {
	            retval.yearlyProduction.add(ResourcePile(idf.createID(), "unknown",
	                "product of ``skill``", Quantity(1, "unit")));
	        }
        }
        // FIXME: Need to generate other consumed resources and goods
        retval.yearlyConsumption.add(ResourcePile(idf.createID(), "food", "various",
            Quantity(4 * 14 * population, "pounds")));
        return retval;
    }
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        IDRegistrar idf;
        if (is IMultiMapModel model) {
            idf = createIDFactory(model.allMaps.map((pair) => pair.first));
        } else {
            idf = createIDFactory(model.map);
        }
        if (cli.inputBoolean("Enter or generate stats for just specific towns? ")) {
            while (true) {
                String input = cli.inputString("ID or name of town to create stats for: ")
                    .trimmed;
                Point? location;
                ModifiableTown? town;
                if (input.empty) {
                    break;
                } else if (isNumeric(input), exists id = parseInt(input)) {
                    value temp = unstattedTowns(model.map).find((loc->town) => town.id == id);
                    location = temp?.key;
                    town = temp?.item;
                } else {
                    value temp = unstattedTowns(model.map).find((loc->town) => town.name == input);
                    location = temp?.key;
                    town = temp?.item;
                }
                if (exists town, exists location) {
                    CommunityStats stats;
                    if (cli.inputBooleanInSeries("Enter stats rather than generating them? ")) {
                        stats = enterStats(cli, idf, model.map, location, town);
                    } else {
                        stats = generateStats(idf, location, town, model.map);
                    }
                    assignStatsToTown(town, stats);
                    if (is IMultiMapModel model) {
                        for ([subMap, file] in model.subordinateMaps) {
                            assignStatsInMap(subMap, location, town.id, stats);
                        }
                    }
                } else {
                    cli.println("No matching town found.");
                }
            }
        } else {
            variable Boolean? always = null;
            for (location->town in randomize(unstattedTowns(model.map))) {
                cli.println("Next town is ``town.shortDescription``, at ``location``. ");
                CommunityStats stats;
	            if (cli.inputBooleanInSeries("Enter stats rather than generating them? ")) {
		            // TODO: uncomment and remove above line once ceylon/ceylon#7165 fixed
//                // We effectively duplicate inputBooleanInSeries() here to allow "quit."
//                Boolean resp;
//                if (exists temp = always) {
//                    cli.print("Enter stats rather than generating them? ");
//                    resp = temp;
//                    cli.println((resp) then "yes" else "no");
//                } else {
//                    while (true) {
//                        String input = cli.inputString(
//                            "Enter stats rather than generating them? ").lowercased;
//                        switch(input)
//                        case ("all"|"ya"|"ta"|"always") {
//                            always = true;
//                            resp = true;
//                            break;
//                        }
//                        case ("none"|"na"|"fa"|"never") {
//                            always = false;
//                            resp = false;
//                            break;
//                        }
//                        case ("yes"|"true"|"y"|"t") {resp = true; break; }
//                        case ("no"|"false"|"n"|"f") { resp = false; break; }
//                        case ("quit"|"q"|"exit") { return; }
//                        else {
//                            cli.print(
//                                """Please enter "yes", "no", "true", or "false", the first
//                                   character of any of those, or "all", "none", "always",
//                                   or "never" to use the same answer for all further
//                                   questions, or "quit" to stop generating towns.""");
//                        }
//                    }
//                }
//                if (resp) {
                    stats = enterStats(cli, idf, model.map, location, town);
                } else {
                    stats = generateStats(idf, location, town, model.map);
                }
                assignStatsToTown(town, stats);
                if (is IMultiMapModel model) {
                    for ([subMap, file] in model.subordinateMaps) {
                        assignStatsInMap(subMap, location, town.id, stats);
                    }
                }
            }
        }
    }
}