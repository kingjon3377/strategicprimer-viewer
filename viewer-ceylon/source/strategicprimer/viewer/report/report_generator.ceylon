import ceylon.logging {
    logger,
    Logger
}
import util {
    PatientMap,
    Pair,
    PairComparatorImpl,
    PairComparator,
    IntMap
}
import model.map {
    IMapNG,
    Player,
    IFixture,
    Point,
    DistanceComparator,
    TerrainFixture,
    PointFactory,
    TileFixture,
    FixtureIterable
}
import java.util {
    JComparator=Comparator,
    Formatter
}
import controller.map.report {
    FortressMemberReportGenerator,
    FortressReportGenerator,
    UnitReportGenerator,
    TownReportGenerator,
    AnimalReportGenerator,
    ExplorableReportGenerator,
    ImmortalsReportGenerator,
    VillageReportGenerator,
    HarvestableReportGenerator,
    IReportGenerator
}
import model.map.fixtures.towns {
    Fortress
}
import lovelace.util.common {
    todo
}
import lovelace.util.jvm {
    AppendableHelper
}
import java.lang {
    JInteger=Integer, JIterable=Iterable
}
import model.map.fixtures.mobile {
    IUnit
}
import model.report {
    IReportNode,
    RootReportNode
}
import controller.map.misc {
    IDFactoryFiller,
    IDRegistrar
}
import ceylon.interop.java {
    CeylonIterable
}
"A logger."
Logger log = logger(`module strategicprimer.viewer`);
"Find the location of the given player's HQ in the given map."
todo("""Return null instead of an "invalid" Point when not found?""")
Point findHQ(IMapNG map, Player player) {
    variable Point? retval = null;
    for (location in map.locations()) {
        for (fixture in map.getOtherFixtures(location)) {
            if (is Fortress fixture, fixture.owner == player) {
                if ("hq" == fixture.name) {
                    return location;
                } else if (location.valid, !retval exists) {
                    retval = location;
                }
            }
        }
    } else {
        return retval else PointFactory.invalidPoint;
    }
}
"Create a mapping from ID numbers to Pairs of fixtures and their location for all fixtures
 in the map."
PatientMap<JInteger, Pair<Point, IFixture>> getFixtures(IMapNG map) {
    PatientMap<JInteger, Pair<Point, IFixture>> retval = IntMap<Pair<Point, IFixture>>();
    IDRegistrar idf = IDFactoryFiller.createFactory(map);
    JInteger checkID(IFixture fixture) {
        if (fixture.id < 0) {
            return JInteger(idf.createID());
        } else {
            return JInteger(fixture.id);
        }
    }
    void addToMap(Point location, IFixture fixture) {
        if (fixture is TileFixture || fixture.id >= 0) {
            JInteger key = checkID(fixture);
            value val = Pair.\iof(location, fixture);
            if (exists existing = retval.put(key, val)) {
                log.warn("Duplicate key, ``key``, for Pairs ``
                existing`` and ``val``");
            }
        }
        if (is FixtureIterable<out Object> fixture) {
            assert (is JIterable<out IFixture> fixture);
            for (inner in CeylonIterable(fixture)) {
                addToMap(location, inner);
            }
        }
    }
    for (location in map.locations()) {
        for (IFixture fixture in {map.getGround(location), map.getForest(location),
                *map.getOtherFixtures(location)}.coalesced) {
            addToMap(location, fixture);
        }
    }
    return retval;
}
"Produces sub-reports, appending them to the buffer and calling coalesce() on the fixtures
 collection after each."
void createSubReports(StringBuilder builder,
        PatientMap<JInteger, Pair<Point, IFixture>> fixtures, IMapNG map, Player player,
        IReportGenerator<out Object>* generators) {
    try (ostream = Formatter(AppendableHelper(builder.append))) {
        for (generator in generators) {
            generator.produce(fixtures, map, player, ostream);
            fixtures.coalesce();
        }
    }
}
"Create the report for the given player based on the given map."
shared String createReport(IMapNG map, Player player = map.currentPlayer) {
    StringBuilder builder = StringBuilder();
    builder.append("""<html>
                      <head><title>Strategic Primer map summary report</title></head>
                      <body>
                      """);
    // TODO: Use Tuple instead of Pair
    PatientMap<JInteger, Pair<Point, IFixture>> fixtures = getFixtures(map);
    PairComparator<Point, IFixture> comparator = PairComparatorImpl(
        DistanceComparator(findHQ(map, player)), JComparator.comparingInt(IFixture.hash));
    createSubReports(builder, fixtures, map, player, FortressReportGenerator(comparator),
        UnitReportGenerator(comparator), TextReportGenerator(comparator),
        TownReportGenerator(comparator), FortressMemberReportGenerator(comparator),
        ExplorableReportGenerator(comparator), HarvestableReportGenerator(comparator),
        AnimalReportGenerator(comparator), VillageReportGenerator(comparator),
        ImmortalsReportGenerator(comparator));
    builder.append("""</body>
                      </html>
                      """);
    for (pair in fixtures.values()) {
        IFixture fixture = pair.second();
        if (fixture.id < 0) {
            continue;
        } else if (is TerrainFixture fixture) {
            fixtures.remove(fixture.id);
            continue;
        }
        process.writeLine("Unhandled fixture:\t``fixture``");
    }
    return builder.string;
}
"Create a slightly abbreviated report, omitting the player's fortresses and units."
shared String createAbbreviatedReport(IMapNG map, Player player = map.currentPlayer) {
    StringBuilder builder = StringBuilder();
    builder.append(
        """<html>
            <head><title>Strategic Primer map summary abridged report</title></head>
            <body>
            """);
    // TODO: Use Tuple instead of Pair
    PatientMap<JInteger, Pair<Point, IFixture>> fixtures = getFixtures(map);
    PairComparator<Point, IFixture> comparator = PairComparatorImpl(
        DistanceComparator(findHQ(map, player)), JComparator.comparingInt(IFixture.hash));
    for (pair in fixtures.values()) {
        if (is IUnit|Fortress fixture = pair.second(), fixture.owner == player) {
            fixtures.remove(fixture.id);
        }
    }
    fixtures.coalesce();
    createSubReports(builder, fixtures, map, player,
        FortressMemberReportGenerator(comparator), FortressReportGenerator(comparator),
        UnitReportGenerator(comparator), TextReportGenerator(comparator),
        TownReportGenerator(comparator), ExplorableReportGenerator(comparator),
        HarvestableReportGenerator(comparator), AnimalReportGenerator(comparator),
        VillageReportGenerator(comparator), ImmortalsReportGenerator(comparator));
    builder.append("""</body>
                      </html>
                      """);
    for (pair in fixtures.values()) {
        IFixture fixture = pair.second();
        if (fixture.id < 0) {
            continue;
        } else if (is TerrainFixture fixture) {
            fixtures.remove(fixture.id);
            continue;
        }
        process.writeLine("Unhandled fixture:\t``fixture``");
    }
    return builder.string;
}
"Produce sub-reports in report-intermediate-representation, adding them to the root node
 and calling coalesce() on the fixtures collection after each."
void createSubReportsIR(IReportNode root,
        PatientMap<JInteger, Pair<Point, IFixture>> fixtures, IMapNG map, Player player,
        IReportGenerator<out Object>* generators) {
    for (generator in generators) {
        root.add(generator.produceRIR(fixtures, map, player));
        fixtures.coalesce();
    }
}
"Create the report, in report-intermediate-representation, based on the given map."
shared IReportNode createReportIR(IMapNG map, Player player = map.currentPlayer) {
    IReportNode retval = RootReportNode("Strategic Primer map summary report");
    // TODO: Use Tuple instead of Pair
    PatientMap<JInteger, Pair<Point, IFixture>> fixtures = getFixtures(map);
    PairComparator<Point, IFixture> comparator = PairComparatorImpl(
        DistanceComparator(findHQ(map, player)), JComparator.comparingInt(IFixture.hash));
    createSubReportsIR(retval, fixtures, map, player, FortressReportGenerator(comparator),
        UnitReportGenerator(comparator), TextReportGenerator(comparator),
        TownReportGenerator(comparator), ExplorableReportGenerator(comparator),
        HarvestableReportGenerator(comparator), FortressMemberReportGenerator(comparator),
        AnimalReportGenerator(comparator), VillageReportGenerator(comparator),
        ImmortalsReportGenerator(comparator));
    return retval;
}
"Create a slightly abbreviated report, omitting the player's fortresses and units, in
 intermediate representation."
shared IReportNode createAbbreviatedReportIR(IMapNG map,
        Player player = map.currentPlayer) {
    // TODO: Use Tuple instead of Pair
    PatientMap<JInteger, Pair<Point, IFixture>> fixtures = getFixtures(map);
    PairComparator<Point, IFixture> comparator = PairComparatorImpl(
        DistanceComparator(findHQ(map, player)), JComparator.comparingInt(IFixture.hash));
    for (pair in fixtures.values()) {
        if (is IUnit|Fortress fixture = pair.second(), fixture.owner == player) {
            fixtures.remove(fixture.id);
        }
    }
    fixtures.coalesce();
    IReportNode retval = RootReportNode(
        "Strategic Primer map summary abbreviated report");
    createSubReportsIR(retval, fixtures, map, player, FortressMemberReportGenerator(comparator),
        FortressReportGenerator(comparator), UnitReportGenerator(comparator),
        TextReportGenerator(comparator), TownReportGenerator(comparator),
        ExplorableReportGenerator(comparator), HarvestableReportGenerator(comparator),
        AnimalReportGenerator(comparator), VillageReportGenerator(comparator),
        ImmortalsReportGenerator(comparator));
    return retval;
}