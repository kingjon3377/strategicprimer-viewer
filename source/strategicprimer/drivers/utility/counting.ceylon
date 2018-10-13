import strategicprimer.drivers.common {
    DriverFactory,
    ModelDriverFactory,
    DriverUsage,
    IDriverUsage,
    ParamCount,
    ModelDriver,
    SPOptions,
    IDriverModel,
    SimpleDriverModel,
    ReadOnlyDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.common.map {
    IMutableMapNG,
    IMapNG,
    TileType,
    River,
    TileFixture,
    Player
}
import lovelace.util.common {
    PathWrapper,
    Accumulator,
    EnumCounter,
    comparingOn,
    entryMap,
    IntHolder,
    matchingValue
}
import ceylon.decimal {
    Decimal,
    decimalNumber
}
import ceylon.collection {
    MutableMap,
    HashMap
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
class DecimalHolder(variable Decimal count) satisfies Accumulator<Decimal> {
    shared actual void add(Decimal addend) => count += addend;
    shared actual Decimal sum => count;
}
class MappedCounter<Base, Key, Count>(Key(Base) keyExtractor, Count(Base) countExtractor, // TODO: Is Key ever anything other than String? If not, drop the type parameter
            Accumulator<Count>(Count) factory, Count zero) satisfies {<Key->Count>*}
        given Base satisfies Object given Key satisfies Object
        given Count satisfies Summable<Count>&Comparable<Count> {
    MutableMap<Key, Accumulator<Count>> totals = HashMap<Key, Accumulator<Count>>();
    shared void addDirectly(Key key, Count addend) {
        if (exists count = totals[key]) {
            count.add(addend);
        } else {
            totals[key] = factory(addend);
        }
    }
    shared void add(Base obj) => addDirectly(keyExtractor(obj), countExtractor(obj));
    shared actual Iterator<Key->Count> iterator() =>
            totals.map(entryMap(identity<Key>, Accumulator<Count>.sum))
                .sort(comparingOn(Entry<Key, Count>.item, decreasing<Count>)).iterator();
    shared Count total => totals.items.map(Accumulator<Count>.sum).fold(zero)(plus);
}
"A factory for an app to report statistics on the contents of the map."
service(`interface DriverFactory`)
shared class CountingCLIFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["--count"];
        paramsWanted = ParamCount.one;
        shortDescription = "Calculate statistics of map contents";
        longDescription = "Print statistical report of map contents.";
        includeInCLIList = false;
        includeInGUIList = false;
    };
    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) => CountingCLI(cli, model);
    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            SimpleDriverModel(map, path);
}

"An app to report statistics on the contents of the map."
class CountingCLI(ICLIHelper cli, model) satisfies ReadOnlyDriver {
    shared actual IDriverModel model;
    Boolean anyIs<Type>({Anything*} stream) => !stream.narrow<Type>().empty;
    void printSummary<Base, Key, Count>(MappedCounter<Base, Key, Count> counter,
            String(Count)|String total,
            String(Key->Count) each = (Key key->Count count) => "- ``count`` ``key``")
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
                compose(Iterable<Type>.size, Singleton<Type>), IntHolder, 0);
    void countSimply<Type>({Anything*} stream, String title, String(Type) extractor)
            given Type satisfies Object {
        MappedCounter<Type, String, Integer> counter = simpleCounter<Type>(extractor);
        stream.narrow<Type>().each(counter.add);
        printSummary(counter, title);
    }
    Boolean exclude<Type>(Anything obj) => !obj is Type;
    shared actual void startDriver() { // TODO: Reduce duplication
        IMapNG map = model.map;
        cli.println(
            "There are ``map.dimensions.rows * map.dimensions.columns`` tiles in all.");
        EnumCounter<TileType> tileTypeCounts = EnumCounter<TileType>();
        tileTypeCounts.countMany(*map.locations.map(map.baseTerrain.get).coalesced);
        cli.println();
        for (type->count in tileTypeCounts.allCounts.sort(comparingOn(
                Entry<TileType, Integer>.item, decreasing<Integer>))) {
            cli.println("- ``count`` are ``type``");
        }
        cli.println();
        {TileFixture*} allFixtures = map.locations.flatMap(map.fixtures.get);
        MappedCounter<Forest, String, Decimal> forests = MappedCounter(Forest.kind,
            compose(decimalize, Forest.acres), DecimalHolder, decimalNumber(0));
        allFixtures.narrow<Forest>().each(forests.add);
        printSummary(forests,
                    (Decimal total) => "There are ``total`` acres of forest, including:",
                    (String kind->Decimal acres) => "- ``acres`` of ``kind``");
        cli.println("Terrain fixtures:");
        cli.println();
        {{TileFixture*}*} separateTiles = map.locations.map(map.fixtures.get);
        cli.println("- ``separateTiles.count(anyIs<Hill>)`` hilly tiles");
        cli.println("- ``map.locations.count(map.mountainous.get)`` mountainous tiles");
        cli.println("- ``separateTiles.count(anyIs<Oasis>)`` oases");
        {{River*}*} tilesRivers = map.locations.map(map.rivers.get);
        cli.println("- ``tilesRivers.filter((iter) => iter.any(River.lake.equals))
            .count(not(Iterable<River>.empty))`` lakes");
        cli.println("- ``tilesRivers.map((iter) => iter.filter(not(River.lake.equals)))
            .count(not(Iterable<River>.empty))`` tiles with rivers");
        cli.println();
        MappedCounter<Ground, String, Integer> ground = simpleCounter(Ground.kind);
        allFixtures.narrow<Ground>().each(ground.add);
        printSummary(ground, "Ground (bedrock) (counting exposed/not separately):",
                    (String kind->Integer count) => "- ``count`` tiles with ``kind``");
        countSimply<StoneDeposit>(allFixtures, "Stone deposits:", StoneDeposit.kind);
        countSimply<MineralVein>(allFixtures, "Mineral veins:", MineralVein.kind);
        countSimply<Mine>(allFixtures, "Mines:", Mine.kind);
        MappedCounter<CacheFixture, String, Integer> caches =
                simpleCounter(CacheFixture.kind);
        allFixtures.narrow<CacheFixture>().each(caches.add);
        printSummary(caches, "Caches:",
                    (String kind->Integer count) => "- ``count`` of ``kind``");
        MappedCounter<AdventureFixture, String, Integer> adventures =
                simpleCounter(AdventureFixture.briefDescription);
        allFixtures.narrow<AdventureFixture>()
            .each(adventures.add);
        adventures.addDirectly("Portal to another world",
            separateTiles.count(anyIs<Portal>));
        adventures.addDirectly("Ancient battlefield",
            allFixtures.narrow<Battlefield>().size);
        adventures.addDirectly("Cave system", allFixtures.narrow<Cave>().size);
        printSummary(adventures, "Adventure Hooks and Portals:",
                    (String kind->Integer count) => "- ``kind``: ``count``");
        cli.println("Active Communities:");
        cli.println();
        cli.println("- ``allFixtures.narrow<Fortress>().size`` fortresses");
        cli.println("- ``allFixtures.narrow<AbstractTown>()
            .filter(matchingValue(TownStatus.active, AbstractTown.status))
            .size`` active towns, cities, or fortifications of any size");
        MappedCounter<Village, String, Integer> villages = simpleCounter(Village.race);
        allFixtures.narrow<Village>().each(villages.add);
        printSummary(villages, "- Villages, grouped by race:",
                    (String race->Integer count) => "  - ``count`` ``race``");
        MappedCounter<AbstractTown, String, Integer> inactiveTowns = simpleCounter(
                    (AbstractTown t) => "``t.status`` ``t.townSize`` ``t.kind``");
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
                    IntHolder, 0);
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
                    MineralVein|StoneDeposit|Ground|Forest|Hill|Oasis|AnimalTracks|Cave|Battlefield>);
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