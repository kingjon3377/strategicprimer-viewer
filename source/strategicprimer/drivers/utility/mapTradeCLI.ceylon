import strategicprimer.drivers.common {
	SimpleCLIDriver,
	DriverUsage,
	IDriverUsage,
	ParamCount,
	SPOptions,
	IDriverModel,
	IMultiMapModel,
	FixtureMatcher,
	simpleMatcher,
	ISPDriver
}
import strategicprimer.drivers.common.cli {
	ICLIHelper
}
import strategicprimer.model.map {
	IMapNG,
	TileFixture,
	Player
}
import ceylon.collection {
	ArrayList,
	MutableList
}
import strategicprimer.model.map.fixtures.towns {
	AbstractTown,
	Fortress,
	Village
}
import strategicprimer.model.map.fixtures {
	TextFixture,
	Ground
}
import strategicprimer.model.map.fixtures.resources {
	CacheFixture,
	Meadow,
	Mine,
	Grove,
	Shrub,
	MineralVein,
	StoneDeposit
}
import strategicprimer.model.map.fixtures.mobile {
	Animal,
	Fairy,
	Dragon,
	Centaur,
	IUnit,
	Giant,
	Djinn,
	Phoenix,
	Sphinx,
	Griffin,
	Ogre,
	Minotaur,
	Troll,
	Simurgh,
	AnimalTracks
}
import strategicprimer.model.map.fixtures.terrain {
	Hill,
	Forest,
	Oasis,
	Sandbar
}
import strategicprimer.model.map.fixtures.explorable {
	Cave,
	Portal,
	AdventureFixture,
	Battlefield
}
import ceylon.language.meta.model {
	ClassModel,
	ClassOrInterface
}
import ceylon.language.meta {
	type
}
import lovelace.util.common {
	matchingValue,
	inverse,
	matchingPredicate
}
"An app to copy selected contents from one map to another."
service(`interface ISPDriver`)
shared class MapTradeCLI() satisfies SimpleCLIDriver { // TODO: convert to class-with-constructor and make helpers static
	shared actual IDriverUsage usage = DriverUsage(false, ["--trade"], ParamCount.two,
		"Trade maps", "Copy contents from one map to another.", true, false);
	FixtureMatcher trivialMatcher(ClassOrInterface<TileFixture> type,
			String description = "``type.declaration.name``s") { // TODO: =>
		return FixtureMatcher(type.typeOf, description);
	}
	{FixtureMatcher*} flatten(FixtureMatcher|{FixtureMatcher*} item) {
		if (is {FixtureMatcher*} item) {
			return item;
		} else {
			return Singleton(item);
		}
	}
	{FixtureMatcher*} complements<out T>(Boolean(T) method,
		String firstDescription, String secondDescription)
			given T satisfies TileFixture => [simpleMatcher<T>(method, firstDescription),
			simpleMatcher<T>(inverse(method), secondDescription)];
	{FixtureMatcher*} initializeMatchers() {
		return [
			complements<IUnit>(inverse(matchingPredicate(Player.independent, IUnit.owner)), "Units",
				"Independent Units"),
			trivialMatcher(`Fortress`, "Fortresses"),
			trivialMatcher(`TextFixture`, "Arbitrary-Text Notes"),
			trivialMatcher(`Portal`), trivialMatcher(`Oasis`, "Oases"),
			trivialMatcher(`AdventureFixture`, "Adventures"),
			trivialMatcher(`CacheFixture`, "Caches"), trivialMatcher(`Forest`),
			trivialMatcher(`AbstractTown`, "Cities, Towns, and Fortifications"),
			trivialMatcher(`Village`),
			trivialMatcher(`Animal`), trivialMatcher(`AnimalTracks`),
			trivialMatcher(`Troll`),
			trivialMatcher(`Simurgh`),
			trivialMatcher(`Ogre`),
			trivialMatcher(`Minotaur`),
			trivialMatcher(`Mine`),
			trivialMatcher(`Griffin`),
			trivialMatcher(`Sphinx`, "Sphinxes"),
			trivialMatcher(`Phoenix`, "Phoenixes"),
			trivialMatcher(`Djinn`, "Djinni"),
			trivialMatcher(`Centaur`),
			trivialMatcher(`StoneDeposit`, "Stone Deposits"),
			trivialMatcher(`MineralVein`, "Mineral Veins"),
			trivialMatcher(`Fairy`, "Fairies"), trivialMatcher(`Giant`),
			trivialMatcher(`Dragon`), trivialMatcher(`Cave`), trivialMatcher(`Battlefield`),
			complements<Grove>(Grove.orchard, "Orchards", "Groves"),
			trivialMatcher(`Shrub`), complements<Meadow>(Meadow.field, "Fields", "Meadows"),
			trivialMatcher(`Sandbar`), trivialMatcher(`Hill`),
			complements<Ground>(Ground.exposed, "Ground (exposed)", "Ground")
		].flatMap(flatten);
	}
	shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options, IDriverModel model) {
		IMapNG first = model.map;
		assert (is IMultiMapModel model, exists second = model.subordinateMaps.first?.first);
		if (cli.inputBoolean("Copy players?")) {
			first.players.each(second.addPlayer);
		}
		Boolean copyRivers = cli.inputBoolean("Include rivers?");
		MutableList<FixtureMatcher> matchers = ArrayList { elements = initializeMatchers(); };
		void askAbout(FixtureMatcher matcher, String key = "include") => matcher.displayed =
					cli.inputBooleanInSeries("Include \"``matcher.description``\" items?", key);
		matchers.each(askAbout);
		Boolean testFixture(TileFixture fixture) {
			for (matcher in matchers) {
				if (matcher.matches(fixture)) {
					return matcher.displayed;
				}
			}
			ClassModel<TileFixture> cls = type(fixture);
			FixtureMatcher newMatcher = trivialMatcher(cls, fixture.plural);
			askAbout(newMatcher, "new");
			matchers.add(newMatcher);
			return newMatcher.displayed;
		}
		for (location in first.locations) {
			if (!second.baseTerrain[location] exists, exists terrain = first.baseTerrain[location]) {
				second.baseTerrain[location] = terrain;
			}
			//for (fixture in first.fixtures[location].filter(testFixture)) { // TODO: syntax sugar
			for (fixture in first.fixtures.get(location).filter(testFixture)) {
				//if (!second.fixtures[location].any(matchingValue(fixture.id, TileFixture.id))) {
				if (fixture.id >= 0, !second.fixtures.get(location).any(matchingValue(fixture.id, TileFixture.id))) {
					second.addFixture(location, fixture.copy(true));
				}
			}
			if (copyRivers) {
				//second.addRivers(location, *first.rivers[location]); // TODO: syntax sugar
				second.addRivers(location, *first.rivers.get(location));
			}
		}
	}
}
