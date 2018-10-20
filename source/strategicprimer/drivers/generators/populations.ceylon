import strategicprimer.drivers.common {
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions,
    IDriverModel,
    CLIDriver,
    DriverFactory,
    ModelDriverFactory,
    ModelDriver,
    SimpleDriverModel
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.common.map {
    Point,
    HasExtent,
    IMutableMapNG
}
import strategicprimer.model.common.map.fixtures.mobile {
    Animal
}
import ceylon.random {
    Random,
    randomize
}
import strategicprimer.model.common.map.fixtures.resources {
    Grove,
    Shrub,
    Meadow
}
import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}
import strategicprimer.drivers.exploration.common {
    surroundingPointIterable
}
import strategicprimer.model.common.map.fixtures.towns {
    ITownFixture,
    TownSize
}
import ceylon.decimal {
    decimalNumber,
    round,
    halfEven,
    implicitlyRounded
}
import lovelace.util.common {
    matchingValue,
    matchingPredicate,
    narrowedStream,
    singletonRandom,
    PathWrapper
}
import lovelace.util.jvm {
    decimalize
}

"A factory for a driver to let the user generate animal and shrub populations, meadow and
 grove sizes, and forest acreages."
service(`interface DriverFactory`)
shared class PopulationGeneratingCLIFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["--populations"];
        paramsWanted = ParamCount.one;
        shortDescription = "Generate animal populations, etc.";
        longDescription = "Generate animal and shrub populations, meadow and grove sizes, and forest acreages.";
        includeInCLIList = true;
        includeInGUIList = false; // TODO: We'd like a GUI equivalent
    };

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) => PopulationGeneratingCLI(cli, model);

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            SimpleDriverModel(map, path);
}

"A driver to let the user generate animal and shrub populations, meadow and grove sizes,
 and forest acreages."
shared class PopulationGeneratingCLI satisfies CLIDriver {
    "Whether the given number is positive."
    static Boolean positiveNumber(Number<out Anything> number) => number.positive;

    "Whether the given number is negative."
    static Boolean negativeNumber(Number<out Anything> number) => number.negative;

    ICLIHelper cli;
    shared actual IDriverModel model;
    shared new(ICLIHelper cli, IDriverModel model) {
        this.cli = cli;
        this.model = model;
    }
    IMutableMapNG map = model.map;

    "Generate [[Animal]] populations."
    void generateAnimalPopulations(Boolean talking, String kind) {
        // We assume there is at most one population of each kind of animal per tile.
        {Point*} locations = randomize(narrowedStream<Point, Animal>(map.fixtures)
            .filter(matchingValue(talking, compose(Animal.talking,
                Entry<Point, Animal>.item)))
            .filter(matchingValue(kind, compose(Animal.kind, Entry<Point, Animal>.item)))
            .filter(not(compose(Integer.positive,
                compose(Animal.population, Entry<Point, Animal>.item))))
            .map(Entry.key).distinct);
        Integer count = locations.size;
        if (count == 0) {
            return;
        }
        String key = (talking) then "talking ``kind``" else kind;
        Integer total = cli.inputNumber(
            "There are ``count`` groups of ``key`` in the world; what should their total population be?") else 0;
        variable Integer remainingTotal = total;
        variable Integer remainingCount = count;
        Random rng = singletonRandom;
        for (location in locations) {
            Integer temp = (remainingCount * 2) + 2;
            if (remainingTotal == temp || remainingTotal < temp) {
                cli.println("With ``remainingCount`` groups left, there is only ``remainingTotal`` left, not enough for 2 or more each");
                cli.println("Adjusting up by ``remainingCount * 3``");
                remainingTotal += remainingCount * 3;
            }
            Integer nextPopulation;
            if (remainingCount == 1) {
                nextPopulation = remainingTotal;
            } else if (remainingCount < 1) {
                cli.println("Ran out of locations while generating ``key``");
                return;
            } else {
                nextPopulation =
                        rng.nextInteger(remainingTotal - (remainingCount * 2) - 2) + 2;
            }
            //if (exists animal = map.fixtures[location].narrow<Animal>() // TODO: syntax sugar
            if (exists animal = map.fixtures.get(location).narrow<Animal>()
                    .filter(matchingValue(talking, Animal.talking))
                    .find(matchingValue(kind, Animal.kind))) {
                Animal replacement = animal.reduced(nextPopulation);
                map.removeFixture(location, animal);
                map.addFixture(location, replacement);
                remainingCount--;
                remainingTotal -= nextPopulation;
            }
        }
    }

    "Generate [[grove and orchard|Grove]] populations."
    void generateGroveCounts(String kind) {
        // We assume there is at most one grove or orchard of each kind per tile.
        {Point*} locations = randomize(narrowedStream<Point, Grove>(map.fixtures)
            .filter(matchingPredicate(matchingValue(kind, Grove.kind),
                Entry<Point, Grove>.item))
            .filter(matchingPredicate(matchingPredicate(Integer.negative,
                Grove.population), Entry<Point, Grove>.item)).map(Entry.key).distinct);
        Integer count = locations.size;
        if (count == 0) {
            return;
        }
        Integer total = cli.inputNumber("There are ``count`` groves or orchards of ``kind
            `` in the world; what should their total population be? ") else 0;
        variable Integer remainingTotal = total;
        variable Integer remainingCount = count;
        Random rng = singletonRandom;
        for (location in locations) {
            if (remainingTotal < remainingCount) {
                cli.println("With ``remainingCount`` groups left, there is only ``remainingTotal`` left");
                return;
            }
            Integer nextPopulation = if (remainingCount == 1) then remainingTotal else
                rng.nextInteger(remainingTotal-remainingCount - 1) + 1;
            //if (exists grove = map.fixtures[location].narrow<Grove>() // TODO: syntax sugar
            if (exists grove = map.fixtures.get(location).narrow<Grove>()
                    .find(matchingValue(kind, Grove.kind))) {
                Grove replacement = Grove(grove.orchard, grove.cultivated, grove.kind,
                    grove.id, nextPopulation);
                map.removeFixture(location, grove);
                map.addFixture(location, replacement);
                remainingCount--;
                remainingTotal -= nextPopulation;
            }
        }
    }

    "Generate [[Shrub]] populations."
    void generateShrubCounts(String kind) {
        // We assume there is at most one population of each kind of shrub per tile.
        {Point*} locations = randomize(narrowedStream<Point, Shrub>(map.fixtures)
            .filter(matchingPredicate(matchingValue(kind, Shrub.kind),
                Entry<Point, Shrub>.item))
            .filter(matchingPredicate(matchingPredicate(Integer.negative,
                Shrub.population), Entry<Point, Shrub>.item)).map(Entry.key).distinct);
        Integer count = locations.size;
        if (count == 0) {
            return;
        }
        Integer total = cli.inputNumber("There are ``count`` populations of ``kind
            `` in the world; what should their total population be? ") else 0;
        variable Integer remainingTotal = total;
        variable Integer remainingCount = count;
        Random rng = singletonRandom;
        for (location in locations) {
            if (remainingTotal < remainingCount) {
                cli.println("With ``remainingCount`` groups left, there is only ``remainingTotal`` left");
                return;
            }
            Integer nextPopulation = if (remainingCount == 1) then remainingTotal else
                rng.nextInteger(remainingTotal-remainingCount - 1) + 1;
            //if (exists grove = map.fixtures[location].narrow<Shrub>() // TODO: syntax sugar
            if (exists shrub = map.fixtures.get(location).narrow<Shrub>()
                    .find(matchingValue(kind, Shrub.kind))) {
                Shrub replacement = Shrub(kind, shrub.id, nextPopulation);
                map.removeFixture(location, shrub);
                map.addFixture(location, replacement);
                remainingCount--;
                remainingTotal -= nextPopulation;
            }
        }
    }

    "Generate [[field and meadow|Meadow]] acreages."
    void generateFieldExtents() {
        {<Point->Meadow>*} entries = randomize(narrowedStream<Point, Meadow>(map.fixtures)
            .filter(matchingPredicate(negativeNumber, compose(Meadow.acres,
                Entry<Point, Meadow>.item))));
        Random rng = singletonRandom;
        for (loc->field in entries) {
            Float acres = rng.nextFloat() * 5.5 + 0.5;
            map.removeFixture(loc, field);
            map.addFixture(loc, Meadow(field.kind, field.field, field.cultivated,
                field.id, field.status, acres));
        }
    }

    "Whether any of the fixtures on the given tile are forests of the given
     kind."
    Boolean hasAdjacentForests(String kind)(Point point) => // TODO: Rename to hasForests()
            map.fixtures.get(point).narrow<Forest>()
                .any(matchingValue(kind, Forest.kind));

    "How many tiles adjacent to the given location have forests of the given
     kind."
    Integer countAdjacentForests(Point center, String kind) =>
            surroundingPointIterable(center, map.dimensions, 1)
                .count(hasAdjacentForests(kind));

    "Generate [[Forest]] acreages."
    void generateForestExtents() {
        {Point*} locations = randomize(narrowedStream<Point, Forest>(map.fixtures)
            .filter(matchingPredicate(not(positiveNumber),
                compose(Forest.acres, Entry<Point, Forest>.item)))
            .map(Entry.key).distinct);
        for (location in locations) {
            assert (exists primaryForest =
                    //map.fixtures[location].narrow<Forest>().first); // TODO: syntax sugar
                    map.fixtures.get(location).narrow<Forest>().first);
            variable Integer reserved = 0;
            if (primaryForest.acres.positive) {
                cli.println("First forest at ``location`` had acreage set already.");
//                reserved += map.fixtures[location].narrow<Forest>().map(Forest.acres) // TODO: syntax sugar
                reserved += map.fixtures.get(location).narrow<Forest>().map(Forest.acres)
                    .filter(positiveNumber).map(decimalize)
                    .fold(decimalNumber(0))(plus).integer;
            }
            //{Forest*} otherForests = map.fixtures[location].narrow<Forest>() // TODO: syntax sugar
            {Forest*} otherForests = map.fixtures.get(location).narrow<Forest>()
                    .select(not(matchingPredicate(positiveNumber, Forest.acres))).rest;
            Integer adjacentCount =
                    countAdjacentForests(location, primaryForest.kind);
            //for (town in map.fixtures[location].narrow<ITownFixture>()) { // TODO: syntax sugar
            for (town in map.fixtures.get(location).narrow<ITownFixture>()) {
                switch (town.townSize)
                case (TownSize.small) {
                    reserved += 15;
                }
                case (TownSize.medium) {
                    reserved += 40;
                }
                case (TownSize.large) {
                    reserved += 80;
                }
            }
            //reserved += map.fixtures[location].narrow<Grove>() // TODO: syntax sugar
            reserved += map.fixtures.get(location).narrow<Grove>()
                    .map(Grove.population).filter(Integer.positive).fold(0)(plus) / 500;
            //reserved += map.fixtures[location].narrow<HasExtent>() // TODO: syntax sugar
            reserved += map.fixtures.get(location).narrow<HasExtent>()
                    .map(HasExtent.acres).filter(positiveNumber).map(decimalize)
                    .fold(decimalNumber(0))(plus).integer;
            if (reserved >= 160) {
                process.writeLine(
                    "The whole tile or more was reserved, despite forests, at ``location``");
                continue;
            }
            if (otherForests.empty) {
                Forest replacement;
                if (adjacentCount > 7) {
                    replacement = Forest(primaryForest.kind, primaryForest.rows,
                        primaryForest.id, 160 - reserved);
                } else if (adjacentCount > 4) {
                    replacement = Forest(primaryForest.kind, primaryForest.rows,
                        primaryForest.id, (160 - reserved) * 4 / 5);
                } else {
                    replacement = Forest(primaryForest.kind, primaryForest.rows,
                        primaryForest.id, (160 - reserved) * 2 / 5);
                }
                map.removeFixture(location, primaryForest);
                map.addFixture(location, replacement);
            } else {
                Integer acreage;
                if (adjacentCount > 4) {
                    acreage = (160 - reserved) * 4 / 5;
                } else {
                    acreage = (160 - reserved) * 2 / 5;
                }
                map.removeFixture(location, primaryForest);
                map.addFixture(location, Forest(primaryForest.kind, primaryForest.rows,
                    primaryForest.id, acreage));
                reserved += acreage;
                for (forest in otherForests) {
                    map.removeFixture(location, forest);
                    map.addFixture(location, Forest(forest.kind, forest.rows, forest.id,
                        // TODO: figure out how to use defer() to avoid a lambda here
                        implicitlyRounded(() => decimalNumber(160 - reserved) /
                            decimalNumber(otherForests.size), round(12, halfEven))));
                }
            }
        }
    }

    shared actual void startDriver() {
        for (kind in model.map.fixtures.map(Entry.item).narrow<Animal>()
                    .filter(not(matchingPredicate(Integer.positive, Animal.population)))
                .map(Animal.kind).distinct) {
            generateAnimalPopulations(true, kind);
            generateAnimalPopulations(false, kind);
        }
        model.map.fixtures.map(Entry.item).narrow<Grove>().map(Grove.kind).distinct
            .each(generateGroveCounts);
        model.map.fixtures.map(Entry.item).narrow<Shrub>().map(Shrub.kind).distinct
            .each(generateShrubCounts);
        generateFieldExtents();
        generateForestExtents();
        model.mapModified = true;
    }
}
