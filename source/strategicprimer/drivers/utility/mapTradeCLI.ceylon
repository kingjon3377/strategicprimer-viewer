import strategicprimer.drivers.common {
	SimpleCLIDriver,
	DriverUsage,
	IDriverUsage,
	ParamCount,
	SPOptions,
	IDriverModel,
	IMultiMapModel,
	FixtureMatcher,
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
	Oasis
}
import strategicprimer.model.map.fixtures.explorable {
	Cave,
	Portal,
	AdventureFixture,
	Battlefield
}
import ceylon.language.meta.model {
	ClassModel
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
shared class MapTradeCLI satisfies SimpleCLIDriver {
	static {FixtureMatcher*} flatten(FixtureMatcher|{FixtureMatcher*} item) {
		if (is {FixtureMatcher*} item) {
			return item;
		} else {
			return Singleton(item);
		}
	}
	static {FixtureMatcher*} initializeMatchers() => [
			FixtureMatcher.complements<IUnit>(inverse(
					matchingPredicate(Player.independent, IUnit.owner)),
				"Units", "Independent Units"),
			FixtureMatcher.trivialMatcher(`Fortress`, "Fortresses"),
			FixtureMatcher.trivialMatcher(`TextFixture`, "Arbitrary-Text Notes"),
			FixtureMatcher.trivialMatcher(`Portal`),
			FixtureMatcher.trivialMatcher(`Oasis`, "Oases"),
			FixtureMatcher.trivialMatcher(`AdventureFixture`, "Adventures"),
			FixtureMatcher.trivialMatcher(`CacheFixture`, "Caches"),
			FixtureMatcher.trivialMatcher(`Forest`),
			FixtureMatcher.trivialMatcher(`AbstractTown`, "Cities, Towns, and Fortifications"),
			FixtureMatcher.trivialMatcher(`Village`),
			FixtureMatcher.trivialMatcher(`Animal`), FixtureMatcher.trivialMatcher(`AnimalTracks`),
			FixtureMatcher.trivialMatcher(`Troll`),
			FixtureMatcher.trivialMatcher(`Simurgh`),
			FixtureMatcher.trivialMatcher(`Ogre`),
			FixtureMatcher.trivialMatcher(`Minotaur`),
			FixtureMatcher.trivialMatcher(`Mine`),
			FixtureMatcher.trivialMatcher(`Griffin`),
			FixtureMatcher.trivialMatcher(`Sphinx`, "Sphinxes"),
			FixtureMatcher.trivialMatcher(`Phoenix`, "Phoenixes"),
			FixtureMatcher.trivialMatcher(`Djinn`, "Djinni"),
			FixtureMatcher.trivialMatcher(`Centaur`),
			FixtureMatcher.trivialMatcher(`StoneDeposit`, "Stone Deposits"),
			FixtureMatcher.trivialMatcher(`MineralVein`, "Mineral Veins"),
			FixtureMatcher.trivialMatcher(`Fairy`, "Fairies"),
			FixtureMatcher.trivialMatcher(`Giant`),
			FixtureMatcher.trivialMatcher(`Dragon`), FixtureMatcher.trivialMatcher(`Cave`),
			FixtureMatcher.trivialMatcher(`Battlefield`),
			FixtureMatcher.complements<Grove>(Grove.orchard, "Orchards", "Groves"),
			FixtureMatcher.trivialMatcher(`Shrub`),
			FixtureMatcher.complements<Meadow>(Meadow.field, "Fields", "Meadows"),
			FixtureMatcher.trivialMatcher(`Hill`),
			FixtureMatcher.complements<Ground>(Ground.exposed, "Ground (exposed)", "Ground")
		].flatMap(flatten);
	shared new () {}
	shared actual IDriverUsage usage = DriverUsage(false, ["--trade"], ParamCount.two,
		"Trade maps", "Copy contents from one map to another.", true, false);
	shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
			IDriverModel model) {
		IMapNG first = model.map;
		assert (is IMultiMapModel model, exists second = model.subordinateMaps.first?.key);
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
			FixtureMatcher newMatcher = FixtureMatcher.trivialMatcher(cls, fixture.plural);
			askAbout(newMatcher, "new");
			matchers.add(newMatcher);
			return newMatcher.displayed;
		}
		Boolean zeroFixtures;
		if (first.currentPlayer.independent || second.currentPlayer.independent ||
				first.currentPlayer.playerId != second.currentPlayer.playerId) {
			zeroFixtures = true;
		} else {
			zeroFixtures = false;
		}
		for (location in first.locations) {
			if (!second.baseTerrain[location] exists, exists terrain =
					first.baseTerrain[location]) {
				second.baseTerrain[location] = terrain;
			}
			//for (fixture in first.fixtures[location].filter(testFixture)) { // TODO: syntax sugar
			for (fixture in first.fixtures.get(location).filter(testFixture)) {
				//if (!second.fixtures[location].any(matchingValue(fixture.id, TileFixture.id))) {
				if (fixture.id >= 0, !second.fixtures.get(location)
						.any(matchingValue(fixture.id, TileFixture.id))) {
					second.addFixture(location, fixture.copy(zeroFixtures));
				}
			}
			if (copyRivers) {
				//second.addRivers(location, *first.rivers[location]); // TODO: syntax sugar
				second.addRivers(location, *first.rivers.get(location));
			}
		}
	}
}
