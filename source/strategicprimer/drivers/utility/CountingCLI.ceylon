import strategicprimer.drivers.common {
    IDriverModel,
    ReadOnlyDriver
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.model.common.map {
    IMapNG,
    TileType,
    River,
    TileFixture,
    Player
}

import lovelace.util.common {
    Accumulator,
    EnumCounter,
    matchingValue
}

import ceylon.decimal {
    Decimal,
    decimalNumber
}

import strategicprimer.model.common.map.fixtures.terrain {
    Forest,
    Hill,
    Oasis
}

import lovelace.util.jvm {
    decimalize
}

import strategicprimer.model.common.map.fixtures {
    Ground
}

import strategicprimer.model.common.map.fixtures.resources {
    StoneDeposit,
    MineralVein,
    Mine,
    CacheFixture,
    Meadow,
    Grove,
    Shrub
}

import strategicprimer.model.common.map.fixtures.explorable {
    AdventureFixture,
    Portal,
    Battlefield,
    Cave
}

import strategicprimer.model.common.map.fixtures.towns {
    Fortress,
    AbstractTown,
    TownStatus,
    Village
}

import strategicprimer.model.common.map.fixtures.mobile {
    IUnit,
    IWorker,
    Immortal,
    Animal,
    AnimalTracks
}

"An app to report statistics on the contents of the map."
class CountingCLI(ICLIHelper cli, model) satisfies ReadOnlyDriver {
    shared actual IDriverModel model;

    Boolean anyIs<Type>({Anything*} stream) => !stream.narrow<Type>().empty;

    String parameterizedCountSpaceKey<Key, Count>(Key->Count entry)
        given Key satisfies Object given Count satisfies Number<Count> =>
            "- ``entry.item`` ``entry.key``";

    void printSummary<Base, Key, Count>(MappedCounter<Base, Key, Count> counter,
            String(Count)|String total,
            String(Key->Count) each = parameterizedCountSpaceKey<Key, Count>)
            given Base satisfies Object given Key satisfies Object
            given Count satisfies Number<Count> {
        if (counter.total.positive) {
            if (is String total) {
                cli.println(total);
            } else {
                cli.println(total(counter.total));
            }
            cli.println();
            counter.map(each).each(cli.println);
            cli.println();
        }
    }

    MappedCounter<Type, String, Integer> simpleCounter<Type>(String(Type) keyExtractor)
            given Type satisfies Object =>
            MappedCounter<Type, String, Integer>(keyExtractor,
                compose(Iterable<Type>.size, Singleton<Type>), Accumulator<Integer>, 0);

    void countSimply<Type>({Anything*} stream, String title, String(Type) extractor)
            given Type satisfies Object {
        MappedCounter<Type, String, Integer> counter = simpleCounter<Type>(extractor);
        stream.narrow<Type>().each(counter.add);
        printSummary(counter, title);
    }

    Boolean exclude<Type>(Anything obj) => !obj is Type;

    String reportForestTotal(Decimal total) => "There are ``total`` acres of forest, including:";

    Boolean hasLake(Iterable<River> iter) => iter.any(River.lake.equals);

    Iterable<River> withNonLake(Iterable<River> iter) => iter.filter(not(River.lake.equals));

    String countOfKind(String-><Decimal|Integer> entry) => "- ``entry.item`` of ``entry.key``";

    String countTilesWithKind(String->Integer entry) => "- ``entry.item`` tiles with ``entry.key``";

    String kindColonCount(String->Integer entry) => "- ``entry.key``: ``entry.item``";

    String countSpaceKind(String->Integer entry) => "  - ``entry.item`` ``entry.key``";

    String townSummary(AbstractTown t) => "``t.status`` ``t.townSize`` ``t.kind``";

    shared actual void startDriver() { // TODO: Reduce duplication
        IMapNG map = model.map;
        cli.println(
            "There are ``map.dimensions.rows * map.dimensions.columns`` tiles in all.");
        EnumCounter<TileType> tileTypeCounts = EnumCounter<TileType>();
        tileTypeCounts.countMany(*map.locations.map(map.baseTerrain.get).coalesced);
        cli.println();
        for (type->count in tileTypeCounts.allCounts.sort(decreasingItem)) {
            cli.println("- ``count`` are ``type``");
        }
        cli.println();
        {TileFixture*} allFixtures = map.locations.flatMap(map.fixtures.get);
        MappedCounter<Forest, String, Decimal> forests = MappedCounter(Forest.kind,
            compose(decimalize, Forest.acres), Accumulator<Decimal>, decimalNumber(0));
        allFixtures.narrow<Forest>().each(forests.add);
        printSummary(forests, reportForestTotal, countOfKind);

        cli.println("Terrain fixtures:");
        cli.println();
        {{TileFixture*}*} separateTiles = map.locations.map(map.fixtures.get);
        cli.println("- ``separateTiles.count(anyIs<Hill>)`` hilly tiles");
        cli.println("- ``map.locations.count(map.mountainous.get)`` mountainous tiles");
	cli.println("- ``separateTiles.count(anyIs<Forest>)`` at least partly forested tiles");
        cli.println("- ``separateTiles.count(anyIs<Oasis>)`` oases");
        {{River*}*} tilesRivers = map.locations.map(map.rivers.get);
        cli.println("- ``tilesRivers.filter(hasLake).count(not(Iterable<River>.empty))`` lakes");
        cli.println("- ``tilesRivers.map(withNonLake)
            .count(not(Iterable<River>.empty))`` tiles with rivers");
        cli.println();

        MappedCounter<Ground, String, Integer> ground = simpleCounter(Ground.kind);
        allFixtures.narrow<Ground>().each(ground.add);
        printSummary(ground, "Ground (bedrock) (counting exposed/not separately):",
                    countTilesWithKind);

        countSimply<StoneDeposit>(allFixtures, "Stone deposits:", StoneDeposit.kind);
        countSimply<MineralVein>(allFixtures, "Mineral veins:", MineralVein.kind);
        countSimply<Mine>(allFixtures, "Mines:", Mine.kind);

        MappedCounter<CacheFixture, String, Integer> caches =
                simpleCounter(CacheFixture.kind);
        allFixtures.narrow<CacheFixture>().each(caches.add);
        printSummary(caches, "Caches:", countOfKind);

        MappedCounter<AdventureFixture, String, Integer> adventures =
                simpleCounter(AdventureFixture.briefDescription);
        allFixtures.narrow<AdventureFixture>().each(adventures.add);
        adventures.addDirectly("Portal to another world",
            separateTiles.count(anyIs<Portal>));
        adventures.addDirectly("Ancient battlefield",
            allFixtures.narrow<Battlefield>().size);
        adventures.addDirectly("Cave system", allFixtures.narrow<Cave>().size);
        printSummary(adventures, "Adventure Hooks and Portals:", kindColonCount);

        cli.println("Active Communities:");
        cli.println();
        cli.println("- ``allFixtures.narrow<Fortress>().size`` fortresses");
        cli.println("- ``allFixtures.narrow<AbstractTown>()
            .filter(matchingValue(TownStatus.active, AbstractTown.status))
            .size`` active towns, cities, or fortifications of any size");

        MappedCounter<Village, String, Integer> villages = simpleCounter(Village.race);
        allFixtures.narrow<Village>().each(villages.add);
        printSummary(villages, "- Villages, grouped by race:", countSpaceKind);

        MappedCounter<AbstractTown, String, Integer> inactiveTowns = simpleCounter(
            townSummary);
        allFixtures.narrow<AbstractTown>().filter(not(matchingValue(TownStatus.active,
            AbstractTown.status))).each(inactiveTowns.add);
        printSummary(inactiveTowns, "Inactive Communities:");

        MappedCounter<IUnit, String, Integer> independentUnits =
                simpleCounter(IUnit.name);
        allFixtures.narrow<IUnit>().filter(compose(Player.independent, IUnit.owner))
            .each(independentUnits.add);
        printSummary(independentUnits, "Independent Units:");

        MappedCounter<IWorker, String, Integer> workers = simpleCounter(IWorker.race);
        allFixtures.narrow<IUnit>().flatMap(identity).narrow<IWorker>().each(workers.add);
        allFixtures.narrow<Fortress>().flatMap(identity).narrow<IUnit>().flatMap(identity)
            .narrow<IWorker>().each(workers.add);
        printSummary(workers, "Worker Races:");

        countSimply<Immortal>(allFixtures, "Immortals:", Immortal.shortDescription);
        countSimply<Meadow>(allFixtures, "Fields and Meadows:", Meadow.kind);
        countSimply<Grove>(allFixtures, "Groves and Orchards:", Grove.kind);
        countSimply<Shrub>(allFixtures, "Shrubs:", Shrub.kind);

        MappedCounter<Animal, String, Integer> animals =
                MappedCounter<Animal, String, Integer>(Animal.kind, Animal.population,
                    Accumulator<Integer>, 0);
        allFixtures.narrow<Animal>().filter(not(Animal.talking)).each(animals.add);
        allFixtures.narrow<IUnit>().flatMap(identity).narrow<Animal>().each(animals.add);
        allFixtures.narrow<Fortress>().flatMap(identity).narrow<IUnit>().flatMap(identity)
            .narrow<Animal>().each(animals.add);
        animals.addDirectly("various talking animals", allFixtures.narrow<Animal>()
            .filter(Animal.talking).size);
        printSummary(animals, "Animals");

        value remaining =
                allFixtures.filter(exclude<Animal|Shrub|Grove|Meadow|Immortal|Fortress
                    |IUnit|AbstractTown|Village|Portal|AdventureFixture|CacheFixture|Mine|
                    MineralVein|StoneDeposit|Ground|Forest|Hill|Oasis|AnimalTracks|Cave|
                    Battlefield>);

        if (!remaining.empty) {
            cli.println();
            cli.println("Remaining fixtures:");
            cli.println();
            for (fixture in remaining) {
                cli.println("- ``fixture.shortDescription``");
            }
        }
    }
}
