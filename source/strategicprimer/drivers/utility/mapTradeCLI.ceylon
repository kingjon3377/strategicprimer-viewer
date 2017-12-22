import strategicprimer.drivers.common {
	SimpleCLIDriver,
	DriverUsage,
	IDriverUsage,
	ParamCount,
	SPOptions,
	IDriverModel,
	IMultiMapModel,
	FixtureMatcher,
	simpleMatcher
}
import strategicprimer.drivers.common.cli {
	ICLIHelper
}
import strategicprimer.model.map {
	IMapNG,
	TileFixture
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
	SimpleImmortal,
	Animal,
	SimpleImmortalKind,
	Fairy,
	Dragon,
	Centaur,
	IUnit,
	Giant
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
	ClassModel
}
import ceylon.language.meta {
	type
}
"An app to copy selected contents from one map to another."
shared object mapTradeCLI satisfies SimpleCLIDriver {
	shared actual IDriverUsage usage = DriverUsage(false, ["--trade"], ParamCount.two,
		"Trade maps", "Copy contents from one map to another.");
	FixtureMatcher trivialMatcher(ClassModel<TileFixture> type,
		String description = "``type.declaration.name``s") {
		return FixtureMatcher((TileFixture fixture) => type.typeOf(fixture), description);
	}
	Iterable<FixtureMatcher> initializeMatchers() {
		FixtureMatcher immortalMatcher(SimpleImmortalKind kind) {
			return FixtureMatcher((TileFixture fixture) {
				if (is SimpleImmortal fixture) {
					return fixture.immortalKind == kind;
				} else {
					return false;
				}
			}, kind.plural);
		}
		{FixtureMatcher*} complements<out T>(Boolean(T) method,
				String firstDescription, String secondDescription)
					given T satisfies TileFixture {
			return {simpleMatcher<T>(method, firstDescription),
				simpleMatcher<T>((T fixture) => !method(fixture),
					secondDescription)};
		}
		MutableList<FixtureMatcher> list = ArrayList<FixtureMatcher>();
		// Can't use our preferred initialization form because an Iterable can only be spread
		// as the *last* argument.
		for (arg in {
			complements<IUnit>((unit) => !unit.owner.independent, "Units",
				"Independent Units"),
			trivialMatcher(`Fortress`, "Fortresses"),
			trivialMatcher(`TextFixture`, "Arbitrary-Text Notes"),
			trivialMatcher(`Portal`), trivialMatcher(`Oasis`, "Oases"),
			trivialMatcher(`AdventureFixture`, "Adventures"),
			trivialMatcher(`CacheFixture`, "Caches"), trivialMatcher(`Forest`),
			trivialMatcher(`AbstractTown`, "Cities, Towns, and Fortifications"),
			trivialMatcher(`Village`),
			complements<Animal>((Animal animal) => !animal.traces, "Animals",
				"Animal tracks"),
			immortalMatcher(SimpleImmortalKind.troll),
			immortalMatcher(SimpleImmortalKind.simurgh),
			immortalMatcher(SimpleImmortalKind.ogre),
			immortalMatcher(SimpleImmortalKind.minotaur),
			trivialMatcher(`Mine`),
			immortalMatcher(SimpleImmortalKind.griffin),
			immortalMatcher(SimpleImmortalKind.sphinx),
			immortalMatcher(SimpleImmortalKind.phoenix),
			immortalMatcher(SimpleImmortalKind.djinn),
			trivialMatcher(`Centaur`),
			trivialMatcher(`StoneDeposit`, "Stone Deposits"),
			trivialMatcher(`MineralVein`, "Mineral Veins"),
			trivialMatcher(`Fairy`, "Fairies"), trivialMatcher(`Giant`),
			trivialMatcher(`Dragon`), trivialMatcher(`Cave`), trivialMatcher(`Battlefield`),
			complements<Grove>(Grove.orchard, "Orchards", "Groves"),
			trivialMatcher(`Shrub`), complements<Meadow>(Meadow.field, "Fields", "Meadows"),
			trivialMatcher(`Sandbar`), trivialMatcher(`Hill`),
			complements<Ground>(Ground.exposed, "Ground (exposed)", "Ground")
		}) {
			if (is Iterable<FixtureMatcher> arg) {
				list.addAll(arg);
			} else {
				list.add(arg);
			}
		}
		return list;
	}
	shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options, IDriverModel model) {
		IMapNG first = model.map;
		assert (is IMultiMapModel model, exists second = model.subordinateMaps.first?.first);
		if (cli.inputBoolean("Copy players?")) {
			for (player in first.players) {
				second.addPlayer(player);
			}
		}
		Boolean copyRivers = cli.inputBoolean("Include rivers?");
		MutableList<FixtureMatcher> matchers = ArrayList { *initializeMatchers() };
		void askAbout(FixtureMatcher matcher, String key = "include") => matcher.displayed =
					cli.inputBooleanInSeries("Include \"``matcher.description``\" items?", key);
		for (matcher in matchers) {
			askAbout(matcher);
		}
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
				//if (!second.fixtures[location].any((fix) => fix.id == fixture.id)) {
				if (fixture.id >= 0, !second.fixtures.get(location).any((fix) => fix.id == fixture.id)) {
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
