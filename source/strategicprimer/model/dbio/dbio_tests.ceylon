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
	test
}

import lovelace.util.jvm {
	singletonRandom
}

import org.sqlite {
	SQLiteDataSource
}

import strategicprimer.model.map {
	IMapNG,
	TileFixture,
	IMutableMapNG,
	SPMapNG,
	MapDimensions,
	MapDimensionsImpl,
	PlayerCollection,
	pointFactory,
	PlayerImpl,
	HasOwner,
	Player
}
import strategicprimer.model.map.fixtures {
	ResourcePile,
	Quantity,
	Implement,
	Ground,
	TextFixture
}
import strategicprimer.model.map.fixtures.explorable {
	AdventureFixture,
	Portal,
	Cave,
	Battlefield
}
import strategicprimer.model.map.fixtures.mobile {
	AnimalImpl,
	Unit,
	Worker,
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
	Giant
}
import strategicprimer.model.map.fixtures.mobile.worker {
	raceFactory,
	Job,
	Skill
}
import strategicprimer.model.map.fixtures.resources {
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
import strategicprimer.model.map.fixtures.terrain {
	Forest,
	Hill,
	Sandbar,
	Oasis
}
import strategicprimer.model.map.fixtures.towns {
	TownStatus,
	Fortification,
	TownSize,
	CommunityStats,
	City,
	Town,
	Fortress,
	Village
}
import strategicprimer.model.xmlio {
	warningLevels
}
// Unfortunately, encapsulating anything referred to by parameters()
// results in a compile error about it being a "metamodel reference to local declaration"
{Integer*} threeRandomNumbers() => singletonRandom.integers(1200000).take(3);
{Boolean*} booleanCases => `Boolean`.caseValues;
{TownStatus*} townStatuses = `TownStatus`.caseValues;
{TownSize*} townSizes = `TownSize`.caseValues;
{FieldStatus*} fieldStatuses = `FieldStatus`.caseValues;
{Immortal(Integer)*} simpleImmortalConstructors =
		[`Sphinx`, `Djinn`, `Griffin`, `Minotaur`, `Ogre`, `Phoenix`, `Simurgh`, `Troll`];
{Immortal(String, Integer)*} kindedImmortalConstructors = [`Centaur`, `Dragon`, `Fairy`, `Giant`];
{StoneKind*} stoneKinds = `StoneKind`.caseValues;
{TileFixture(Integer)*} simpleTerrainConstructors = [`Hill`, `Sandbar`, `Oasis`];
{String*} races = raceFactory.races.distinct;
object dbio_tests { // TODO: All tests should be more robust, as if developed test-first
	SQLiteDataSource source = SQLiteDataSource();
	source.url = "jdbc:sqlite:file::memory:";
	IMapNG assertDatabaseSerialization(IMapNG map) {
		Sql db = Sql(newConnectionFromDataSource(source));
		spDatabaseWriter.writeToDatabase(db, map);
		IMapNG deserialized = spDatabaseReader.readMapFromDatabase(db, warningLevels.die);
		assertEquals(deserialized, map, "Deserialized form is the same as original");
		return deserialized;
	}
	void assertFixtureSerialization(TileFixture fixture) {
		MapDimensions dimensions = MapDimensionsImpl(2, 2, 2);
		IMutableMapNG firstMap = SPMapNG(dimensions, PlayerCollection(), -1);
		firstMap.addFixture(pointFactory(0, 0), fixture);
		IMutableMapNG secondMap = SPMapNG(dimensions, PlayerCollection(), -1);
		secondMap.addFixture(pointFactory(1, 1), fixture);
		if (is HasOwner fixture) {
			firstMap.addPlayer(fixture.owner);
			secondMap.addPlayer(fixture.owner);
		}
		IMapNG deserializedFirst = assertDatabaseSerialization(firstMap);
		IMapNG deserializedSecond = assertDatabaseSerialization(secondMap);
		assertNotEquals(deserializedFirst, deserializedSecond);
	}
	test
	parameters(`function threeRandomNumbers`)
	shared void testAdventureSerialization(Integer id) =>
			assertFixtureSerialization(AdventureFixture(PlayerImpl(1, "independent"), "hook brief",
				"hook full", id));
	test
	shared void testPortalSerialization(parameters(`function threeRandomNumbers`) Integer id,
		parameters(`function threeRandomNumbers`) Integer row,
		parameters(`function threeRandomNumbers`) Integer column) =>
			assertFixtureSerialization(Portal("portal dest", pointFactory(row, column), id));
	test
	shared void testAnimalSerialization(parameters(`function threeRandomNumbers`) Integer id, // TODO: animals inside units
		parameters(`value booleanCases`) Boolean talking) =>
			assertFixtureSerialization(AnimalImpl("animal kind", false, talking, "status", id, -1, 1));
	test
	shared void testTracksSerialization() =>
			assertFixtureSerialization(AnimalImpl("kind", true, false, "wild", -1, -1, -1));
	test
	shared void testCacheSerialization(parameters(`function threeRandomNumbers`) Integer id) =>
			assertFixtureSerialization(CacheFixture("kind", "contents", id));
	test
	shared void testCaveSerialization(parameters(`function threeRandomNumbers`) Integer id,
		parameters(`function threeRandomNumbers`) Integer dc) => assertFixtureSerialization(Cave(dc, id));
	test
	shared void testBattlefieldSerialization(parameters(`function threeRandomNumbers`) Integer id,
		parameters(`function threeRandomNumbers`) Integer dc) => assertFixtureSerialization(Battlefield(dc, id));
	test
	shared void testFortificationSerialization(parameters(`function threeRandomNumbers`) Integer id,
			parameters(`value townStatuses`) TownStatus status, parameters(`value townSizes`) TownSize size,
			parameters(`function threeRandomNumbers`) Integer dc) { // TODO: We want more of the state to be random
		value town = Fortification(status, size, dc, "name", id, PlayerImpl(0, ""));
		CommunityStats stats = CommunityStats(5);
		stats.addWorkedField(8);
		stats.addWorkedField(13);
		stats.setSkillLevel("skillOne", 2);
		stats.setSkillLevel("skillTwo", 5);
		stats.yearlyProduction.add(ResourcePile(1, "first", "first detail", Quantity(5, "pounds")));
		stats.yearlyProduction.add(ResourcePile(2, "second", "second detail",
			Quantity(decimalNumber(1) / decimalNumber(2), "quarts")));
		stats.yearlyConsumption.add(ResourcePile(3, "third", "third detail", Quantity(8, "pecks")));
		stats.yearlyConsumption.add(ResourcePile(4, "fourth", "fourth detail",
			Quantity(decimalNumber(5) / decimalNumber(4), "square feet")));
		town.population = stats;
		assertFixtureSerialization(town);
	}
	test
	shared void testCitySerialization(parameters(`function threeRandomNumbers`) Integer id,
		parameters(`value townStatuses`) TownStatus status, parameters(`value townSizes`) TownSize size,
		parameters(`function threeRandomNumbers`) Integer dc) { // TODO: We want more of the state to be random
		value town = City(status, size, dc, "name", id, PlayerImpl(0, ""));
		CommunityStats stats = CommunityStats(5);
		stats.addWorkedField(8);
		stats.addWorkedField(13);
		stats.setSkillLevel("skillOne", 2);
		stats.setSkillLevel("skillTwo", 5);
		stats.yearlyProduction.add(ResourcePile(1, "first", "first detail", Quantity(5, "pounds")));
		stats.yearlyProduction.add(ResourcePile(2, "second", "second detail",
			Quantity(decimalNumber(1) / decimalNumber(2), "quarts")));
		stats.yearlyConsumption.add(ResourcePile(3, "third", "third detail", Quantity(8, "pecks")));
		stats.yearlyConsumption.add(ResourcePile(4, "fourth", "fourth detail",
			Quantity(decimalNumber(5) / decimalNumber(4), "square feet")));
		town.population = stats;
		assertFixtureSerialization(town);
	}
	test
	shared void testTownSerialization(parameters(`function threeRandomNumbers`) Integer id,
		parameters(`value townStatuses`) TownStatus status, parameters(`value townSizes`) TownSize size,
		parameters(`function threeRandomNumbers`) Integer dc) { // TODO: We want more of the state to be random
		value town = Town(status, size, dc, "name", id, PlayerImpl(0, ""));
		CommunityStats stats = CommunityStats(5);
		stats.addWorkedField(8);
		stats.addWorkedField(13);
		stats.setSkillLevel("skillOne", 2);
		stats.setSkillLevel("skillTwo", 5);
		stats.yearlyProduction.add(ResourcePile(1, "first", "first detail", Quantity(5, "pounds")));
		stats.yearlyProduction.add(ResourcePile(2, "second", "second detail",
			Quantity(decimalNumber(1) / decimalNumber(2), "quarts")));
		stats.yearlyConsumption.add(ResourcePile(3, "third", "third detail", Quantity(8, "pecks")));
		stats.yearlyConsumption.add(ResourcePile(4, "fourth", "fourth detail",
			Quantity(decimalNumber(5) / decimalNumber(4), "square feet")));
		town.population = stats;
		assertFixtureSerialization(town);
	}
	test
	shared void testMeadowSerialization(parameters(`value booleanCases`) Boolean field,
		parameters(`value booleanCases`) Boolean cultivated,
		parameters(`function threeRandomNumbers`) Integer id,
		parameters(`value fieldStatuses`) FieldStatus status,
		parameters(`function threeRandomNumbers`) Integer acres) =>
			assertFixtureSerialization(Meadow("kind", field, cultivated, id, status, acres));
	test
	shared void testFractionalMeadowSerialization(parameters(`value booleanCases`) Boolean field,
		parameters(`value booleanCases`) Boolean cultivated,
		parameters(`function threeRandomNumbers`) Integer id,
		parameters(`value fieldStatuses`) FieldStatus status,
		parameters(`function threeRandomNumbers`) Integer acres) =>
			assertFixtureSerialization(Meadow("kind", field, cultivated, id, status,
				decimalNumber(acres) + decimalNumber(1) / decimalNumber(2)));
	test
	shared void testForestSerialization(parameters(`value booleanCases`) Boolean rows,
		parameters(`function threeRandomNumbers`) Integer id,
		parameters(`function threeRandomNumbers`) Integer acres) =>
			assertFixtureSerialization(Forest("kind", rows, id, acres));
	test
	shared void testFractionalForestSerialization(parameters(`value booleanCases`) Boolean rows,
		parameters(`function threeRandomNumbers`) Integer id,
		parameters(`function threeRandomNumbers`) Integer acres) =>
			assertFixtureSerialization(Forest("kind", rows, id,
				decimalNumber(acres) + decimalNumber(1) / decimalNumber(4)));
	test
	shared void testFortressSerialization(parameters(`function threeRandomNumbers`) Integer id,
			parameters(`value townSizes`) TownSize size) {
		Player owner = PlayerImpl(1, "owner");
		value fortress = Fortress(owner, "fortress", id, size);
		value unit = Unit(owner, "unitKind", "unitName", id + 2);
		unit.addMember(Worker("workerName", "human", id + 3, Job("jobName", 2)));
		unit.addMember(Sphinx(id + 5));
		unit.addMember(ResourcePile(id + 6, "resource kind", "resource contents", Quantity(8, "counts")));
		fortress.addMember(unit);
		fortress.addMember(Implement("equipment", id + 4, 2));
		fortress.addMember(ResourcePile(id + 7, "second resource", "second contents",
			Quantity(decimalNumber(3) / decimalNumber(4), "gallon")));
		assertFixtureSerialization(fortress);
	}
	test
	shared void testGroundSerialization(parameters(`function threeRandomNumbers`) Integer id,
		parameters(`value booleanCases`) Boolean exposed) =>
			assertFixtureSerialization(Ground(id, "ground kind", exposed));
	test
	shared void testGroveSerialization(parameters(`value booleanCases`) Boolean orchard,
		parameters(`value booleanCases`) Boolean cultivated,
		parameters(`function threeRandomNumbers`) Integer id,
		parameters(`function threeRandomNumbers`) Integer count) =>
			assertFixtureSerialization(Grove(orchard, cultivated, "kind", id, count));
	test
	shared void testSimpleImmortalSerialization(
		parameters(`value simpleImmortalConstructors`) Immortal(Integer) constructor,
		parameters(`function threeRandomNumbers`) Integer id) => assertFixtureSerialization(constructor(id));
	test
	shared void testKindedImmortalSerialization(
		parameters(`value kindedImmortalConstructors`) Immortal(String, Integer) constructor,
		parameters(`function threeRandomNumbers`) Integer id) => assertFixtureSerialization(constructor("kind", id));
	test
	shared void testMineSerialization(parameters(`value townStatuses`) TownStatus status,
		parameters(`function threeRandomNumbers`) Integer id) =>
			assertFixtureSerialization(Mine("mine kind", status, id));
	test
	shared void testMineralSerialization(parameters(`value booleanCases`) Boolean exposed,
		parameters(`function threeRandomNumbers`) Integer dc,
		parameters(`function threeRandomNumbers`) Integer id) =>
			assertFixtureSerialization(MineralVein("mineral kind", exposed, dc, id));
	test
	shared void testStoneSerialization(parameters(`value stoneKinds`) StoneKind kind,
		parameters(`function threeRandomNumbers`) Integer dc,
		parameters(`function threeRandomNumbers`) Integer id) =>
			assertFixtureSerialization(StoneDeposit(kind, dc, id));
	test
	shared void testShrubSerialization(parameters(`function threeRandomNumbers`) Integer id,
		parameters(`function threeRandomNumbers`) Integer count) =>
			assertFixtureSerialization(Shrub("shrub kind", id, count));
	test
	shared void testSimpleTerrainSerialization(
		parameters(`value simpleTerrainConstructors`) TileFixture(Integer) constructor,
		parameters(`function threeRandomNumbers`) Integer id) => assertFixtureSerialization(constructor(id));
	test
	shared void testTextSerialization(parameters(`function threeRandomNumbers`) Integer turn) =>
			assertFixtureSerialization(TextFixture("test text", turn - 1));
	test
	shared void testUnitSerialization(parameters(`function threeRandomNumbers`) Integer id) {
		Player owner = PlayerImpl(1, "owner");
		value unit = Unit(owner, "unitKind", "unitName", id);
		unit.addMember(Worker("worker name", "elf", id + 1, Job("job name", 2,
			Skill("first skill", 1, 2), Skill("second skill", 3, 4)), Job("second job", 4)));
		unit.addMember(Centaur("horse", id + 5));
		assertFixtureSerialization(unit);
	}
	test
	shared void testVillageSerialization(parameters(`value townStatuses`) TownStatus status,
		parameters(`function threeRandomNumbers`) Integer id, parameters(`value races`) String race) =>
			assertFixtureSerialization(Village(status, "village name", id, PlayerImpl(1, "player name"), race));
}