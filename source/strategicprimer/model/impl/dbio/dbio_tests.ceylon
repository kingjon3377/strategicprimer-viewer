import ceylon.dbc {
    Sql,
    newConnectionFromDataSource
}
import ceylon.decimal {
    decimalNumber
}
import ceylon.test {
    assertEquals,
    assertNotEquals,
    parameters,
    test,
    assertFalse,
    assertTrue,
    assumeFalse
}

import org.sqlite {
    SQLiteDataSource
}

import strategicprimer.model.common.map {
    IMapNG,
    IMutableMapNG,
    SPMapNG,
    MapDimensions,
    MapDimensionsImpl,
    PlayerImpl,
    HasOwner,
    Player,
    TileFixture,
    Point,
    PlayerCollection,
    Direction,
    TileType
}
import strategicprimer.model.common.map.fixtures {
    ResourcePile,
    Quantity,
    Implement,
    Ground,
    TextFixture
}
import strategicprimer.model.common.map.fixtures.explorable {
    AdventureFixture,
    Portal,
    Cave,
    Battlefield
}
import strategicprimer.model.common.map.fixtures.mobile {
    Unit,
    Sphinx,
    Immortal,
    Djinn,
    Griffin,
    Minotaur,
    Ogre,
    Phoenix,
    Simurgh,
    Troll,
    Centaur,
    Dragon,
    Fairy,
    Worker,
    Giant,
    AnimalImpl,
    AnimalTracks,
    Snowbird,
    Thunderbird,
    Pegasus,
    Unicorn,
    IWorker,
    IUnit,
    Kraken
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    raceFactory,
    Job,
    Skill
}
import strategicprimer.model.common.map.fixtures.resources {
    CacheFixture,
    Meadow,
    FieldStatus,
    Grove,
    Mine,
    MineralVein,
    StoneDeposit,
    StoneKind,
    Shrub
}
import strategicprimer.model.common.map.fixtures.terrain {
    Forest,
    Hill,
    Oasis
}
import strategicprimer.model.common.map.fixtures.towns {
    TownStatus,
    Fortification,
    TownSize,
    CommunityStats,
    City,
    Town,
    Fortress,
    Village
}
import strategicprimer.model.common.xmlio {
    warningLevels
}
import lovelace.util.common {
    enumeratedParameter,
    randomlyGenerated,
    todo,
    fewParameters
}

// Unfortunately, encapsulating anything referred to by parameters()
// results in a compile error about it being a "metamodel reference to local declaration"
{Immortal(Integer)*} simpleImmortalConstructors =
        [`Sphinx`, `Djinn`, `Griffin`, `Minotaur`, `Ogre`, `Phoenix`, `Simurgh`, `Troll`,
         `Snowbird`, `Thunderbird`, `Pegasus`, `Unicorn`, `Kraken`];

{Immortal(String, Integer)*} kindedImmortalConstructors = [`Centaur`, `Dragon`, `Fairy`,
    `Giant`];

{TileFixture(Integer)*} simpleTerrainConstructors = [`Hill`, `Oasis`];

{String*} races = raceFactory.races.distinct;

"Tests of the SQLite database I/O functionality."
todo("All tests should be more robust, as if developed test-first")
object dbio_tests {
    SQLiteDataSource source = SQLiteDataSource();
    source.url = "jdbc:sqlite:file::memory:";

    IMapNG assertDatabaseSerialization(IMapNG map) {
        Sql db = Sql(newConnectionFromDataSource(source));
        spDatabaseWriter.writeToDatabase(db, map);
        IMapNG deserialized = spDatabaseReader.readMapFromDatabase(db, warningLevels.die);
        assertEquals(deserialized, map, "Deserialized form is the same as original");
        return deserialized;
    }

    FixtureType assertFixtureSerialization<FixtureType>(FixtureType fixture) given FixtureType satisfies TileFixture {
        MapDimensions dimensions = MapDimensionsImpl(2, 2, 2);
        IMutableMapNG firstMap = SPMapNG(dimensions, PlayerCollection(), -1);
        firstMap.addFixture(Point(0, 0), fixture);
        IMutableMapNG secondMap = SPMapNG(dimensions, PlayerCollection(), -1);
        secondMap.addFixture(Point(1, 1), fixture);
        if (is HasOwner fixture) {
            firstMap.addPlayer(fixture.owner);
            secondMap.addPlayer(fixture.owner);
        }
        IMapNG deserializedFirst = assertDatabaseSerialization(firstMap);
        IMapNG deserializedSecond = assertDatabaseSerialization(secondMap);
        assertNotEquals(deserializedFirst, deserializedSecond);
        assert (is FixtureType retval = deserializedFirst.fixtures.get(Point(0, 0)).first);
        return retval;
    }

    test
    shared void testAdventureSerialization(randomlyGenerated(3) Integer id) =>
            assertFixtureSerialization(AdventureFixture(PlayerImpl(1, "independent"),
                "hook brief", "hook full", id));

    test
    shared void testPortalSerialization(randomlyGenerated(2) Integer id,
        randomlyGenerated(2) Integer row, randomlyGenerated(2) Integer column) =>
            assertFixtureSerialization(Portal("portal dest", Point(row, column), id));

    test
    shared void testAnimalSerialization(randomlyGenerated(2) Integer id,
        enumeratedParameter(`class Boolean`) Boolean talking) =>
            assertFixtureSerialization(AnimalImpl("animal kind", talking, "status", id,
                -1, 1));

    test
    shared void testTracksSerialization() =>
            assertFixtureSerialization(AnimalTracks("kind"));

    test
    shared void testCacheSerialization(
            randomlyGenerated(3) Integer id) =>
            assertFixtureSerialization(CacheFixture("kind", "contents", id));

    test
    shared void testCaveSerialization(randomlyGenerated(2) Integer id,
        randomlyGenerated(2) Integer dc) =>
            assertFixtureSerialization(Cave(dc, id));

    test
    shared void testBattlefieldSerialization(
            randomlyGenerated(2) Integer id,
        randomlyGenerated(2) Integer dc) =>
            assertFixtureSerialization(Battlefield(dc, id));

    test
    shared void testFortificationSerialization(
            randomlyGenerated(2) Integer id,
            enumeratedParameter(`class TownStatus`) TownStatus status,
            enumeratedParameter(`class TownSize`) TownSize size,
            randomlyGenerated(1) Integer dc) {
        // TODO: We want more of the state to be random
        value town = Fortification(status, size, dc, "name", id, PlayerImpl(0, ""));
        CommunityStats stats = CommunityStats(5);
        stats.addWorkedField(8);
        stats.addWorkedField(13);
        stats.setSkillLevel("skillOne", 2);
        stats.setSkillLevel("skillTwo", 5);
        stats.yearlyProduction.add(ResourcePile(1, "first", "first detail",
            Quantity(5, "pounds")));
        stats.yearlyProduction.add(ResourcePile(2, "second", "second detail",
            Quantity(decimalNumber(1) / decimalNumber(2), "quarts")));
        stats.yearlyConsumption.add(ResourcePile(3, "third", "third detail",
            Quantity(8, "pecks")));
        stats.yearlyConsumption.add(ResourcePile(4, "fourth", "fourth detail",
            Quantity(decimalNumber(5) / decimalNumber(4), "square feet")));
        town.population = stats;
        assertFixtureSerialization(town);
    }

    test
    shared void testCitySerialization(randomlyGenerated(1) Integer id,
            enumeratedParameter(`class TownStatus`) TownStatus status,
            enumeratedParameter(`class TownSize`) TownSize size,
            randomlyGenerated(1) Integer dc) {
        // TODO: We want more of the state to be random
        value town = City(status, size, dc, "name", id, PlayerImpl(0, ""));
        CommunityStats stats = CommunityStats(5);
        stats.addWorkedField(8);
        stats.addWorkedField(13);
        stats.setSkillLevel("skillOne", 2);
        stats.setSkillLevel("skillTwo", 5);
        stats.yearlyProduction.add(ResourcePile(1, "first", "first detail",
            Quantity(5, "pounds")));
        stats.yearlyProduction.add(ResourcePile(2, "second", "second detail",
            Quantity(decimalNumber(1) / decimalNumber(2), "quarts")));
        stats.yearlyConsumption.add(ResourcePile(3, "third", "third detail",
            Quantity(8, "pecks")));
        stats.yearlyConsumption.add(ResourcePile(4, "fourth", "fourth detail",
            Quantity(decimalNumber(5) / decimalNumber(4), "square feet")));
        town.population = stats;
        assertFixtureSerialization(town);
    }

    test
    shared void testTownSerialization(randomlyGenerated(1) Integer id,
            enumeratedParameter(`class TownStatus`) TownStatus status,
                enumeratedParameter(`class TownSize`) TownSize size,
            randomlyGenerated(1) Integer dc) {
        // TODO: We want more of the state to be random
        value town = Town(status, size, dc, "name", id, PlayerImpl(0, ""));
        CommunityStats stats = CommunityStats(5);
        stats.addWorkedField(8);
        stats.addWorkedField(13);
        stats.setSkillLevel("skillOne", 2);
        stats.setSkillLevel("skillTwo", 5);
        stats.yearlyProduction.add(ResourcePile(1, "first", "first detail",
            Quantity(5, "pounds")));
        stats.yearlyProduction.add(ResourcePile(2, "second", "second detail",
            Quantity(decimalNumber(1) / decimalNumber(2), "quarts")));
        stats.yearlyConsumption.add(ResourcePile(3, "third", "third detail",
            Quantity(8, "pecks")));
        stats.yearlyConsumption.add(ResourcePile(4, "fourth", "fourth detail",
            Quantity(decimalNumber(5) / decimalNumber(4), "square feet")));
        town.population = stats;
        assertFixtureSerialization(town);
    }

    test
    shared void testMeadowSerialization(
            enumeratedParameter(`class Boolean`) Boolean field,
            enumeratedParameter(`class Boolean`) Boolean cultivated,
            randomlyGenerated(1) Integer id,
            enumeratedParameter(`class FieldStatus`) FieldStatus status,
            randomlyGenerated(1) Integer acres) =>
                assertFixtureSerialization(Meadow("kind", field, cultivated, id, status,
                    acres));

    test
    shared void testFractionalMeadowSerialization(
        enumeratedParameter(`class Boolean`) Boolean field,
        enumeratedParameter(`class Boolean`) Boolean cultivated,
        randomlyGenerated(1) Integer id,
        enumeratedParameter(`class FieldStatus`) FieldStatus status,
        randomlyGenerated(1) Integer acres) =>
            assertFixtureSerialization(Meadow("kind", field, cultivated, id, status,
                decimalNumber(acres) + decimalNumber(1) / decimalNumber(2)));

    test
    shared void testForestSerialization(enumeratedParameter(`class Boolean`) Boolean rows,
        randomlyGenerated(1) Integer id, randomlyGenerated(1) Integer acres) =>
            assertFixtureSerialization(Forest("kind", rows, id, acres));

    test
    shared void testFractionalForestSerialization(
        enumeratedParameter(`class Boolean`) Boolean rows,
        randomlyGenerated(1) Integer id,
        randomlyGenerated(1) Integer acres) =>
            assertFixtureSerialization(Forest("kind", rows, id,
                decimalNumber(acres) + decimalNumber(1) / decimalNumber(4)));

    test
    shared void testFortressSerialization(
            randomlyGenerated(2) Integer id,
            enumeratedParameter(`class TownSize`) TownSize size) {
        Player owner = PlayerImpl(1, "owner");
        value fortress = Fortress(owner, "fortress", id, size);
        value unit = Unit(owner, "unitKind", "unitName", id + 2);
        unit.addMember(Worker("workerName", "human", id + 3, Job("jobName", 2)));
        unit.addMember(Sphinx(id + 5));
        unit.addMember(ResourcePile(id + 6, "resource kind", "resource contents",
            Quantity(8, "counts")));
        fortress.addMember(unit);
        fortress.addMember(Implement("equipment", id + 4, 2));
        fortress.addMember(ResourcePile(id + 7, "second resource", "second contents",
            Quantity(decimalNumber(3) / decimalNumber(4), "gallon")));
        assertFixtureSerialization(fortress);
    }

    test
    shared void testGroundSerialization(randomlyGenerated(2) Integer id,
        enumeratedParameter(`class Boolean`) Boolean exposed, fewParameters(`value races`, 2) String kind) =>
            assertFixtureSerialization(Ground(id, kind, exposed));

    test
    shared void testGroveSerialization(
            enumeratedParameter(`class Boolean`) Boolean orchard,
            enumeratedParameter(`class Boolean`) Boolean cultivated,
            randomlyGenerated(2) Integer id,
            randomlyGenerated(1) Integer count,
            fewParameters(`value races`, 1) String kind) =>
                assertFixtureSerialization(Grove(orchard, cultivated, kind, id, count));

    test
    shared void testSimpleImmortalSerialization(
        parameters(`value simpleImmortalConstructors`) Immortal(Integer) constructor,
        randomlyGenerated(2) Integer id) =>
            assertFixtureSerialization(constructor(id));

    test
    shared void testKindedImmortalSerialization(
        parameters(`value kindedImmortalConstructors`)
            Immortal(String, Integer) constructor,
        randomlyGenerated(2) Integer id, fewParameters(`value races`, 1) String kind) =>
            assertFixtureSerialization(constructor(kind, id));

    test
    shared void testMineSerialization(
        enumeratedParameter(`class TownStatus`) TownStatus status,
        randomlyGenerated(2) Integer id, fewParameters(`value races`, 1) String kind) =>
            assertFixtureSerialization(Mine(kind, status, id));

    test
    shared void testMineralSerialization(
        enumeratedParameter(`class Boolean`) Boolean exposed,
        randomlyGenerated(2) Integer dc, randomlyGenerated(2) Integer id,
        fewParameters(`value races`, 1) String kind) =>
            assertFixtureSerialization(MineralVein(kind, exposed, dc, id));

    test
    shared void testStoneSerialization(
            enumeratedParameter(`class StoneKind`) StoneKind kind,
            randomlyGenerated(2) Integer dc, randomlyGenerated(2) Integer id) =>
                assertFixtureSerialization(StoneDeposit(kind, dc, id));

    test
    shared void testShrubSerialization(randomlyGenerated(2) Integer id,
        randomlyGenerated(2) Integer count, fewParameters(`value races`, 1) String kind) =>
            assertFixtureSerialization(Shrub(kind, id, count));

    test
    shared void testSimpleTerrainSerialization(
        parameters(`value simpleTerrainConstructors`) TileFixture(Integer) constructor,
        randomlyGenerated(2) Integer id) =>
            assertFixtureSerialization(constructor(id));

    test
    shared void testTextSerialization(randomlyGenerated(3) Integer turn)
            => assertFixtureSerialization(TextFixture("test text", turn - 1));

    test
    shared void testUnitSerialization(randomlyGenerated(3) Integer id) {
        Player owner = PlayerImpl(1, "owner");
        value unit = Unit(owner, "unitKind", "unitName", id);
        unit.addMember(Worker("worker name", "elf", id + 1, Job("job name", 2,
            Skill("first skill", 1, 2), Skill("second skill", 3, 4)),
            Job("second job", 4)));
        unit.addMember(Centaur("horse", id + 5));
        unit.addMember(AnimalImpl("elephant", false, "domesticated", id + 6, -1, 4));
        assertFixtureSerialization(unit);
    }

    test
    shared void testVillageSerialization(
        enumeratedParameter(`class TownStatus`) TownStatus status,
        randomlyGenerated(1) Integer id,
        fewParameters(`value races`, 3) String race) =>
            assertFixtureSerialization(Village(status, "village name", id,
                PlayerImpl(1, "player name"), race));

    test shared void testNotesSerialization(randomlyGenerated(1) Integer id, randomlyGenerated(1) Integer player,
            fewParameters(`value races`, 2) String note) {
        Worker worker = Worker("test worker", "human", id);
        Player playerObj = PlayerImpl(player, "");
        worker.notes[playerObj] = note;
        Unit unit = Unit(playerObj, "unitKind", "unitName", id + 1);
        unit.addMember(worker);
        IUnit deserializedUnit = assertFixtureSerialization(unit);
        assert(is IWorker member = deserializedUnit.first);
        assertEquals(member.notes.get(playerObj), note, "Note was deserialized");
    }

    test
    shared void testBookmarkSerialization() {
        IMutableMapNG map = SPMapNG(MapDimensionsImpl(1, 1, 2), PlayerCollection(), 1);
        Player player = map.players.getPlayer(1);
        map.currentPlayer = player;
        assertFalse(Point(0, 0) in map.bookmarks, "Map by default doesn't have a bookmark");
        assertEquals(map.allBookmarks.size, 0, "Map by default has no bookmarks");
        map.addBookmark(Point(0, 0));
        value deserialized = assertDatabaseSerialization(map);
        assertFalse(map === deserialized, "Deserialization doesn't just return the input");
        assertTrue(Point(0, 0) in deserialized.bookmarks, "Deserialized map has the bookmark we saved");
    }

    test
    shared void testRoadSerialization(enumeratedParameter(`class Direction`) Direction directionOne,
            randomlyGenerated(1, 8) Integer qualityOne, enumeratedParameter(`class Direction`) Direction directionTwo,
            randomlyGenerated(1, 8) Integer qualityTwo) {
        assumeFalse(directionOne == directionTwo,  "We can't have the same direction twice");
        IMutableMapNG map = SPMapNG(MapDimensionsImpl(1, 1, 2), PlayerCollection(), 1);
        map.baseTerrain[Point(0, 0)] = TileType.plains;
        for (direction->quality in [directionOne->qualityOne, directionTwo->qualityTwo]) {
            if (direction != Direction.nowhere) {
                map.setRoadLevel(Point(0, 0), direction, quality);
            }
        }
        assertDatabaseSerialization(map);
    }
}
