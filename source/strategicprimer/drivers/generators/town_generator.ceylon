import strategicprimer.drivers.common {
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions,
    IDriverModel,
    IMultiMapModel,
    CLIDriver,
    DriverFactory,
    ModelDriverFactory,
    ModelDriver,
    SimpleMultiMapModel
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.common.map.fixtures.towns {
    ITownFixture,
    TownStatus,
    CommunityStats,
    AbstractTown,
    Village,
    TownSize
}
import strategicprimer.model.common.map {
    IFixture,
    TileFixture,
    Point,
    HasName,
    TileType,
    IMapNG,
    IMutableMapNG
}
import strategicprimer.model.common.map.fixtures {
    ResourcePile,
    Quantity
}
import strategicprimer.model.common.idreg {
    IDRegistrar,
    createIDFactory
}
import ceylon.decimal {
    Decimal
}
import strategicprimer.model.common.map.fixtures.resources {
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
import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}
import ceylon.collection {
    LinkedList,
    Stack,
    HashMap,
    MutableMap,
    MutableList,
    ArrayList
}
import lovelace.util.common {
    readFileContents,
    isNumeric,
    parseInt,
    matchingValue,
    anythingEqual,
    defer,
    narrowedStream,
    PathWrapper
}

"A command-line app to generate population details for villages."
class TownGenerator(ICLIHelper cli) {
    "Fortresses' [[population]] field cannot be set."
    alias ModifiableTown=>AbstractTown|Village;

    "Load consumption possibilities from file."
    Map<String, {[Quantity, String, String]*}> initConsumption() {
        MutableMap<String, {[Quantity, String, String]*}> retval =
                HashMap<String, {[Quantity, String, String]*}>();
        for (terrain in ["mountain", "forest", "plains", "ocean"]) {
            String file = "``terrain``_consumption";
            assert (exists tableContents =
                    readFileContents(`module strategicprimer.drivers.generators`,
                        "tables/``file``"));
            MutableList<[Quantity, String, String]> inner =
                    ArrayList<[Quantity, String, String]>();
            for (line in tableContents.lines.filter(not(String.empty))) {
                value split = line.split('\t'.equals, true, false);
                value quantity = Integer.parse(split.first);
                if (is Integer quantity) {
                    assert (exists units = split.rest.first, // TODO: can we use destructuring?
                        exists kind = split.rest.rest.first,
                        exists resource = split.rest.rest.rest.first);
                    inner.add([Quantity(quantity, units), kind, resource]);
                } else {
                    throw quantity;
                }
            }
            retval.put(terrain, inner.sequence());
        }
        return map(retval);
    }

    "Load production possibilities from file."
    ExplorationRunner initProduction() {
        ExplorationRunner retval = ExplorationRunner();
        Stack<String> firstTables = LinkedList {
            "mountain_skills", "forest_skills", "plains_skills", "ocean_skills"
        };
        Stack<String> secondTables = LinkedList<String>();
        while (exists table = firstTables.pop()) {
            assert (exists tableContents = readFileContents(
                `module strategicprimer.drivers.generators`, "tables/``table``"));
            value loadedTable = loadTable(tableContents.lines, "tables/``table``");
            retval.loadTable(table, loadedTable);
            for (reference in loadedTable.allEvents) {
                if (reference.contains('#')) {
                    assert (exists temp = reference.split('#'.equals, true, false, 2)
                        .rest.first);
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
            value loadedTable = loadTable(tableContents.lines, "tables/``table``");
            retval.loadTable(table, loadedTable);
            for (reference in loadedTable.allEvents) {
                if (reference.contains('#')) {
                    assert (exists temp = reference.split('#'.equals, true, false, 2)
                        .rest.first);
                    if (!retval.hasTable(temp)) {
                        secondTables.push(temp.trimmed);
                    }
                }
            }
        }
        return retval;
    }

    Map<String,{[Quantity, String, String]*}> consumption = initConsumption(); // TODO: inline that?
    ExplorationRunner runner = initProduction(); // TODO: pull its contents up?

    "The (for now active) towns in the given map that don't have 'stats' yet."
    {<Point->ModifiableTown>*} unstattedTowns(IMapNG map) =>
            narrowedStream<Point, ModifiableTown>(map.fixtures)
                .filter(compose(matchingValue(TownStatus.active, ITownFixture.status),
                    Entry<Point, ITownFixture>.item)).sequence();

    "Assign the given [[population details object|stats]] to the given town."
    void assignStatsToTown(ModifiableTown town, CommunityStats stats) {
        if (is AbstractTown town) {
            town.population = stats;
        } else {
            town.population = stats;
        }
    }

    "Assign the given [[population details object|stats]] to the town
     identified by the given [[ID number|townId]] at the given [[location]] in
     the given [[map]]."
    void assignStatsInMap(IMapNG map, Point location, Integer townId,
            CommunityStats stats) {
//        for (item in map.fixtures[location] // TODO: syntax sugar once compiler bug fixed
        for (item in map.fixtures.get(location)
            .narrow<ModifiableTown>().filter(matchingValue(townId,
            ModifiableTown.id))) {
            assignStatsToTown(item, stats);
        }
    }

    "Get the fixture in the given [[map]] identified by the given [[ID
     number|id]]."
    IFixture? findByID(IMapNG map, Integer id) => map.fixtures
        .map(Entry.item).find(matchingValue(id, IFixture.id));

    "Find the location in the given [[map]] of the fixture identified by the
     given [[ID number|id]]."
    Point? findLocById(IMapNG map, Integer id) =>
            map.fixtures.find(compose(compose(id.equals, IFixture.id),
                Entry<Point, TileFixture>.item))?.key;

    "Whether, in the given [[map]], any town claims a resource identified by
     the given [[ID number|id]]."
    Boolean isClaimedField(IMapNG map, Integer id) => map.fixtures
        .map(Entry.item).narrow<ITownFixture>()
        .map(ITownFixture.population).coalesced
        .flatMap(CommunityStats.workedFields).contains(id);

    "Whether, in the given [[map]], the given [[ID number|id]] refers to [[a
     resource that can be worked|HarvestableFixture]] that [[is presently
     unclaimed|isClaimedField]]."
    Boolean isUnclaimedField(IMapNG map, Integer id) =>
            !isClaimedField(map, id) && findByID(map, id) is HarvestableFixture;

    "If both arguments exist and are ocean, return true; if one is ocean and
     the other is not, return false; otherwise, return true."
    Boolean bothOrNeitherOcean(TileType? one, TileType? two) {
        if (exists one, one == TileType.ocean) {
            return anythingEqual(two, TileType.ocean);
        } else if (exists two, two == TileType.ocean) {
            return false;
        } else {
            return true;
        }
    }

    "Whether the given [[fixture|fix]] is actually claimable: an unexposed
     mineral vein, an uncultivated field or meadow, an uncultivated grove or
     orchard, an abandoned mine, or a cache is not claimable."
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

    "Find the nearest claimable resources to the given location."
    {HarvestableFixture*} findNearestFields(IMapNG map, Point location) {
        if (exists base = map.baseTerrain[location]) {
            return surroundingPointIterable(location, map.dimensions, 10).distinct
                .filter(compose(curry(bothOrNeitherOcean)(base), map.baseTerrain.get))
                .flatMap(map.fixtures.get).narrow<HarvestableFixture>()
                .filter(isReallyClaimable);
        } else {
            return [];
        }
    }

    "Have the user enter expertise levels and claimed resources for a town."
    CommunityStats enterStats(ICLIHelper cli, IDRegistrar idf, IMapNG map, Point location,
            ModifiableTown town) {
        CommunityStats retval = CommunityStats(cli.inputNumber("Population: ") else 0);
        cli.println("Now enter Skill levels, the highest in the community for each Job.");
        cli.println("(Empty to end.)");
        while (true) {
            if (exists job = cli.inputString("Job: "), !job.empty, // TODO: Make this the loop condition instead of breaking on falsehood
                    exists level = cli.inputNumber("Level: ")) {
                retval.setSkillLevel(job, level);
            } else {
                break;
            }
        }

        cli.println("Now enter ID numbers of worked fields (empty to skip).");
        variable {HarvestableFixture*} nearestFields = findNearestFields(map, location);
        while (true) {
            assert (exists input = cli.inputString("Field ID #: "));
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
                            "That would be an ocean resource worked by a town on land.");
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
            assert (exists kind = cli.inputString("General kind of resource: ")); // TODO: Move conditions up into loop condition
            if (kind.empty) {
                break;
            }
            assert (exists contents = cli.inputString("Specific kind of resource: "));
            Decimal quantity;
            if (exists temp = cli.inputDecimal("Quantity of the resource produced: ")) {
                quantity = temp;
            } else {
                break;
            }
            assert (exists units = cli.inputString("Units of that quantity: "));
            ResourcePile pile = ResourcePile(idf.createID(), kind, contents,
                Quantity(quantity, units));
            retval.yearlyProduction.add(pile);
        }

        cli.println("Now add resources consumed each year. (Empty to end.)");
        while (exists kind = cli.inputString("General kind of resource: "), !kind.empty,
                exists contents = cli.inputString("Specific kind of resource: "),
                exists quantity = cli.inputDecimal("Quantity of the resource consumed: "),
                exists units = cli.inputString("Units of that quantity: ")) {
            ResourcePile pile = ResourcePile(idf.createID(), kind, contents, // TODO: inline
                Quantity(quantity, units));
            retval.yearlyConsumption.add(pile);
        }

        return retval;
    }

    "What general kind of thing the given harvestable fixture will produce each year."
    String getHarvestableKind(HarvestableFixture fixture) {
        switch (fixture)
        case (is Grove) {
            return (fixture.orchard) then "food" else "wood";
        }
        case (is Meadow) {
            return (fixture.field) then "food" else "fodder";
        }
        case (is MineralVein) {
            return "mineral";
        }
        case (is StoneDeposit) {
            return "stone";
        }
        else {
            return "unknown";
        }
    }

    "What specific resource the given harvestable fixture will produce."
    String getHarvestedProduct(HarvestableFixture fixture) => fixture.kind;

    "Generate expertise and production and consumption data for the given town."
    CommunityStats generateStats(IDRegistrar idf, Point location, ModifiableTown town,
            IMapNG map) {
        "To ensure consistency between runs of this algorithm, seed the random number generator with the town's ID."
        Random rng = DefaultRandom(town.id);
        "A die roll using our pre-seeded RNG."
        Integer roll(Integer die) => rng.nextInteger(die) + 1;

        "Repeatedly roll our pre-seeded RNG-die, optionally adding a constant value."
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
            skillLevelSource = defer(repeatedlyRoll, [4, 3, -3]);
            resourceCount = repeatedlyRoll(2, 3);
        } else {
            switch (town.townSize)
            case (TownSize.small) {
                population = repeatedlyRoll(4, 10, 5);
                skillCount = repeatedlyRoll(3, 4);
                skillLevelSource = defer(repeatedlyRoll, [2, 6]);
                resourceCount = repeatedlyRoll(2, 3);
            }
            case (TownSize.medium) {
                population = repeatedlyRoll(20, 20, 50);
                skillCount = repeatedlyRoll(4, 6);
                skillLevelSource = defer(repeatedlyRoll, [3, 6]);
                resourceCount = repeatedlyRoll(2, 6);
            }
            case (TownSize.large) {
                population = repeatedlyRoll(23, 100, 200);
                skillCount = repeatedlyRoll(6, 8);
                skillLevelSource = defer(repeatedlyRoll, [3, 8]);
                resourceCount = repeatedlyRoll(4, 6);
            }
        }

        CommunityStats retval = CommunityStats(population);
        String skillTable;
        String consumptionTableName;
        if (exists terrain = map.baseTerrain[location]) {
            if (terrain == TileType.ocean) {
                skillTable = "ocean_skills";
                consumptionTableName = "ocean";
//          } else if (map.mountainous[location]) {
            } else if (map.mountainous.get(location)) {
                skillTable = "mountain_skills";
                consumptionTableName = "mountain";
//          } else if (map.fixtures[location]?.narrow<Forest>().first exists) {
            } else if (map.fixtures.get(location).narrow<Forest>().first exists) {
                skillTable = "forest_skills";
                consumptionTableName = "forest";
            } else {
                skillTable = "plains_skills";
                consumptionTableName = "plains";
            }
        } else {
            skillTable = "plains_skills";
            consumptionTableName = "plains";
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
                    runner.consultTable(tableName, location,
                        map.baseTerrain.get(location), // TODO: syntax sugar
                        map.mountainous.get(location), map.fixtures.get(location),
                        map.dimensions),
                    Quantity(2.power(level - 1), (level == 1) then "unit" else "units")));
            } else {
                retval.yearlyProduction.add(ResourcePile(idf.createID(), "unknown",
                    "product of ``skill``", Quantity(1, "unit")));
            }
        }

        assert (exists consumptionTable = consumption[consumptionTableName]);
        for ([quantity, kind, resource] in consumptionTable) {
            retval.yearlyConsumption.add(ResourcePile(idf.createID(), kind, resource,
                quantity));
        }

        retval.yearlyConsumption.add(ResourcePile(idf.createID(), "food", "various",
            Quantity(4 * 14 * population, "pounds")));
        return retval;
    }

    """If the given [[string]] is "quit", return null; otherwise return false."""
    Boolean? nullIfQuit(String string) {
        if ("quit" == string) {
            return null;
        } else {
            return false;
        }
    }

    "Allow the user to create population details for specific towns."
    shared void generateSpecificTowns(IDRegistrar idf, IDriverModel model) {
        while (exists input = cli.inputString("ID or name of town to create stats for: "),
                !input.empty) {
            Point? location;
            ModifiableTown? town;
            if (isNumeric(input), exists id = parseInt(input)) {
                value temp = unstattedTowns(model.map)
                    .find(compose(matchingValue(id, IFixture.id),
                        Entry<Point, ITownFixture>.item));
                location = temp?.key;
                town = temp?.item;
            } else {
                value temp = unstattedTowns(model.map)
                    .find(compose(matchingValue(input, HasName.name),
                        Entry<Point, ITownFixture>.item));
                location = temp?.key;
                town = temp?.item;
            }
            if (exists town, exists location) {
                CommunityStats stats;
                if (cli.inputBooleanInSeries(
                    "Enter stats rather than generating them? ")) {
                    stats = enterStats(cli, idf, model.map, location, town);
                } else {
                    stats = generateStats(idf, location, town, model.map);
                }
                assignStatsToTown(town, stats);
                model.mapModified = true;
                if (is IMultiMapModel model) {
                    for (subMap->[file, _] in model.subordinateMaps) {
                        assignStatsInMap(subMap, location, town.id, stats);
                        model.setModifiedFlag(subMap, true);
                    }
                }
            } else {
                cli.println("No matching town found.");
            }
        }
    }

    "Help the user generate population details for all the towns in the map
     that don't have such details already."
    shared void generateAllTowns(IDRegistrar idf, IDriverModel model) {
        for (location->town in randomize(unstattedTowns(model.map))) {
            cli.println("Next town is ``town.shortDescription``, at ``location``. ");
            CommunityStats stats;
            Boolean? resp = cli.inputBooleanInSeries<Null>(
                "Enter stats rather than generating them?", "enter stats",
                nullIfQuit);
            if (exists resp) {
                if (resp) {
                    stats = enterStats(cli, idf, model.map, location, town);
                } else {
                    stats = generateStats(idf, location, town, model.map);
                }
                model.mapModified = true;
            } else {
                break;
            }
            assignStatsToTown(town, stats);
            if (is IMultiMapModel model) {
                for (subMap->[file, _] in model.subordinateMaps) {
                    assignStatsInMap(subMap, location, town.id, stats);
                    model.setModifiedFlag(subMap, true);
                }
            }
        }
    }
}

"A factory for a driver to let the user enter or generate 'stats' for towns."
service(`interface DriverFactory`)
shared class TownGeneratingCLIFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["--town"];
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Enter or generate stats and contents for towns and villages";
        longDescription = "Enter or generate stats and contents for towns and villages";
        includeInCLIList = true;
        includeInGUIList = false;
    };

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) => TownGeneratingCLI(cli, model);

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            SimpleMultiMapModel(map, path);
}

"A driver to let the user enter or generate 'stats' for towns."
// TODO: Write GUI to allow user to generate or enter town contents
shared class TownGeneratingCLI(ICLIHelper cli, model) satisfies CLIDriver {
    shared actual IDriverModel model;

    shared actual void startDriver() {
        TownGenerator generator = TownGenerator(cli); // TODO: Consider combining that with this class again.
        IDRegistrar idf;
        if (is IMultiMapModel model) {
            idf = createIDFactory(model.allMaps.map(Entry.key));
        } else {
            idf = createIDFactory(model.map);
        }
        if (exists specific =
                cli.inputBoolean("Enter or generate stats for just specific towns? ")) {
            if (specific) {
                generator.generateSpecificTowns(idf, model);
            } else {
                generator.generateAllTowns(idf, model);
            }
        }
    }
}
