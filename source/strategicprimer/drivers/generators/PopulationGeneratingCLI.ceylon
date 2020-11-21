import strategicprimer.drivers.common {
    CLIDriver,
    emptyOptions,
    SPOptions
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.model.common.map {
    Point,
    HasExtent,
    IMapNG
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
    implicitlyRounded,
    Decimal
}

import lovelace.util.common {
    matchingValue,
    narrowedStream,
    singletonRandom,
    defer
}

import lovelace.util.jvm {
    decimalize
}

"A driver to let the user generate animal and shrub populations, meadow and grove sizes,
 and forest acreages."
shared class PopulationGeneratingCLI satisfies CLIDriver {
    "Whether the given number is positive."
    static Boolean positiveNumber(Number<out Anything> number) => number.positive;

    "Whether the given number is negative."
    static Boolean negativeNumber(Number<out Anything> number) => number.negative;

    ICLIHelper cli;
    shared actual PopulationGeneratingModel model;
    shared actual SPOptions options = emptyOptions;
    shared new(ICLIHelper cli, PopulationGeneratingModel model) {
        this.cli = cli;
        this.model = model;
    }
    IMapNG map = model.map;

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
            if (model.setAnimalPopulation(location, talking, kind, nextPopulation)) {
                remainingCount--;
                remainingTotal -= nextPopulation;
            }
        }
    }

    "Generate [[grove and orchard|Grove]] populations."
    void generateGroveCounts(String kind) {
        // We assume there is at most one grove or orchard of each kind per tile.
        {Point*} locations = randomize(narrowedStream<Point, Grove>(map.fixtures)
            .filter(compose(matchingValue(kind, Grove.kind), Entry<Point, Grove>.item))
            .filter(compose(compose(Integer.negative, Grove.population),
                Entry<Point, Grove>.item)).map(Entry.key).distinct);
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
            if (model.setGrovePopulation(location, kind, nextPopulation)) {
                remainingCount--;
                remainingTotal -= nextPopulation;
            }
        }
    }

    "Generate [[Shrub]] populations."
    void generateShrubCounts(String kind) {
        // We assume there is at most one population of each kind of shrub per tile.
        {Point*} locations = randomize(narrowedStream<Point, Shrub>(map.fixtures)
            .filter(compose(matchingValue(kind, Shrub.kind),
                Entry<Point, Shrub>.item))
            .filter(compose(compose(Integer.negative, Shrub.population),
                Entry<Point, Shrub>.item)).map(Entry.key).distinct);
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
            if (model.setShrubPopulation(location, kind, nextPopulation)) {
                remainingCount--;
                remainingTotal -= nextPopulation;
            }
        }
    }

    "Generate [[field and meadow|Meadow]] acreages."
    void generateFieldExtents() {
        {<Point->Meadow>*} entries = randomize(narrowedStream<Point, Meadow>(map.fixtures)
            .filter(compose(negativeNumber, compose(Meadow.acres,
                Entry<Point, Meadow>.item))));
        Random rng = singletonRandom;
        for (loc->field in entries) {
            Float acres = rng.nextFloat() * 5.5 + 0.5;
            model.setFieldExtent(loc, field, acres);
        }
    }

    "Whether any of the fixtures on the given tile are forests of the given
     kind."
    Boolean hasForests(String kind)(Point point) =>
            map.fixtures.get(point).narrow<Forest>() // TODO: syntax sugar
                .any(matchingValue(kind, Forest.kind));

    "How many tiles adjacent to the given location have forests of the given
     kind."
    Integer countAdjacentForests(Point center, String kind) =>
            surroundingPointIterable(center, map.dimensions, 1)
                .count(hasForests(kind));

    Decimal perForestAcreage(Integer reserved, Integer otherForests) =>
        decimalNumber(160 - reserved) / decimalNumber(otherForests);

    Number<out Anything> acreageExtent(HasExtent<out Anything> item) => item.acres;

    "Generate [[Forest]] acreages."
    void generateForestExtents() {
        {Point*} locations = randomize(narrowedStream<Point, Forest>(map.fixtures)
            .filter(compose(not(positiveNumber),
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
                    .select(not(compose(positiveNumber, Forest.acres))).rest;
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
            reserved += map.fixtures.get(location).narrow<HasExtent<out Anything>>()
                    .map(acreageExtent).filter(positiveNumber).map(decimalize)
                    .fold(decimalNumber(0))(plus).integer;
            if (reserved >= 160) {
                cli.println(
                    "The whole tile or more was reserved, despite forests, at ``location``");
                continue;
            }
            if (otherForests.empty) {
                Integer acreage; // FIXME: Should this be Float instead?
                if (adjacentCount > 7) {
                    acreage = 160 - reserved;
                } else if (adjacentCount > 4) {
                    acreage = (160 - reserved) * 4 / 5;
                } else {
                    acreage = (160 - reserved) * 2 / 5;
                }
                model.setForestExtent(location, primaryForest, acreage);
            } else {
                Integer acreage;
                if (adjacentCount > 4) {
                    acreage = (160 - reserved) * 4 / 5;
                } else {
                    acreage = (160 - reserved) * 2 / 5;
                }
                model.setForestExtent(location, primaryForest, acreage);
                reserved += acreage;
                for (forest in otherForests) {
                    model.setForestExtent(location, forest, implicitlyRounded(
                        defer(perForestAcreage, [reserved, otherForests.size]),
                        round(12, halfEven)));
                }
            }
        }
    }
    shared actual void startDriver() {
        for (kind in model.map.fixtures.items.narrow<Animal>()
                    .filter(not(compose(Integer.positive, Animal.population)))
                .map(Animal.kind).distinct) {
            generateAnimalPopulations(true, kind);
            generateAnimalPopulations(false, kind);
        }
        model.map.fixtures.items.narrow<Grove>().map(Grove.kind).distinct
            .each(generateGroveCounts);
        model.map.fixtures.items.narrow<Shrub>().map(Shrub.kind).distinct
            .each(generateShrubCounts);
        generateFieldExtents();
        generateForestExtents();
        model.mapModified = true;
    }
}
