package utility;

import java.util.logging.Logger;
import java.util.Collections;
import common.map.Point;
import java.util.stream.Collectors;
import drivers.common.FixtureMatcher;
import drivers.common.CLIDriver;
import drivers.common.SPOptions;
import drivers.common.EmptyOptions;

import drivers.common.cli.ICLIHelper;

import common.map.IMapNG;
import common.map.TileFixture;
import common.map.Player;

import java.util.ArrayList;
import java.util.List;

import common.map.fixtures.towns.AbstractTown;
import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.Village;

import common.map.fixtures.TextFixture;
import common.map.fixtures.Ground;

import common.map.fixtures.resources.CacheFixture;
import common.map.fixtures.resources.Meadow;
import common.map.fixtures.resources.Mine;
import common.map.fixtures.resources.Grove;
import common.map.fixtures.resources.Shrub;
import common.map.fixtures.resources.MineralVein;
import common.map.fixtures.resources.StoneDeposit;

import common.map.fixtures.mobile.Fairy;
import common.map.fixtures.mobile.Dragon;
import common.map.fixtures.mobile.Centaur;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Giant;
import common.map.fixtures.mobile.Djinn;
import common.map.fixtures.mobile.Phoenix;
import common.map.fixtures.mobile.Sphinx;
import common.map.fixtures.mobile.Griffin;
import common.map.fixtures.mobile.Ogre;
import common.map.fixtures.mobile.Minotaur;
import common.map.fixtures.mobile.Troll;
import common.map.fixtures.mobile.Simurgh;
import common.map.fixtures.mobile.Snowbird;
import common.map.fixtures.mobile.Thunderbird;
import common.map.fixtures.mobile.Pegasus;
import common.map.fixtures.mobile.Unicorn;
import common.map.fixtures.mobile.Kraken;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.AnimalTracks;

import common.map.fixtures.terrain.Hill;
import common.map.fixtures.terrain.Forest;
import common.map.fixtures.terrain.Oasis;

import common.map.fixtures.explorable.Cave;
import common.map.fixtures.explorable.Portal;
import common.map.fixtures.explorable.AdventureFixture;
import common.map.fixtures.explorable.Battlefield;

/**
 * An app to copy selected contents from one map to another.
 */
public class MapTradeCLI implements CLIDriver {
	private static final Logger LOGGER = Logger.getLogger(MapTradeCLI.class.getName());
	private static final List<FixtureMatcher> initializeMatchers() {
		final List<FixtureMatcher> retval = new ArrayList<>();
		FixtureMatcher.complements(IUnit.class, u -> !u.getOwner().isIndependent(),
			"Units", "Independent Units").forEach(retval::add);
		retval.add(FixtureMatcher.trivialMatcher(IFortress.class, "Fortresses"));
		retval.add(FixtureMatcher.trivialMatcher(TextFixture.class, "Arbitrary-Text Notes"));
		retval.add(FixtureMatcher.trivialMatcher(Portal.class));
		retval.add(FixtureMatcher.trivialMatcher(Oasis.class, "Oases"));
		retval.add(FixtureMatcher.trivialMatcher(AdventureFixture.class, "Adventures"));
		retval.add(FixtureMatcher.trivialMatcher(CacheFixture.class, "Caches"));
		retval.add(FixtureMatcher.trivialMatcher(Forest.class));
		retval.add(FixtureMatcher.trivialMatcher(AbstractTown.class,
			"Cities, Towns, and Fortifications"));
		retval.add(FixtureMatcher.trivialMatcher(Village.class));
		retval.add(FixtureMatcher.trivialMatcher(Animal.class));
		retval.add(FixtureMatcher.trivialMatcher(AnimalTracks.class));
		retval.add(FixtureMatcher.trivialMatcher(Troll.class));
		retval.add(FixtureMatcher.trivialMatcher(Simurgh.class));
		retval.add(FixtureMatcher.trivialMatcher(Ogre.class));
		retval.add(FixtureMatcher.trivialMatcher(Minotaur.class));
		retval.add(FixtureMatcher.trivialMatcher(Mine.class));
		retval.add(FixtureMatcher.trivialMatcher(Griffin.class));
		retval.add(FixtureMatcher.trivialMatcher(Sphinx.class, "Sphinxes"));
		retval.add(FixtureMatcher.trivialMatcher(Phoenix.class, "Phoenixes"));
		retval.add(FixtureMatcher.trivialMatcher(Djinn.class, "Djinni"));
		retval.add(FixtureMatcher.trivialMatcher(Centaur.class));
		retval.add(FixtureMatcher.trivialMatcher(StoneDeposit.class, "Stone Deposits"));
		retval.add(FixtureMatcher.trivialMatcher(MineralVein.class, "Mineral Veins"));
		retval.add(FixtureMatcher.trivialMatcher(Fairy.class, "Fairies"));
		retval.add(FixtureMatcher.trivialMatcher(Giant.class));
		retval.add(FixtureMatcher.trivialMatcher(Dragon.class));
		retval.add(FixtureMatcher.trivialMatcher(Pegasus.class, "Pegasi"));
		retval.add(FixtureMatcher.trivialMatcher(Snowbird.class));
		retval.add(FixtureMatcher.trivialMatcher(Thunderbird.class));
		retval.add(FixtureMatcher.trivialMatcher(Unicorn.class));
		retval.add(FixtureMatcher.trivialMatcher(Kraken.class));
		retval.add(FixtureMatcher.trivialMatcher(Cave.class));
		retval.add(FixtureMatcher.trivialMatcher(Battlefield.class));
		FixtureMatcher.complements(Grove.class, Grove::isOrchard, "Orchards", "Groves")
			.forEach(retval::add);
		retval.add(FixtureMatcher.trivialMatcher(Shrub.class));
		FixtureMatcher.complements(Meadow.class, Meadow::isField, "Fields", "Meadows")
			.forEach(retval::add);
		retval.add(FixtureMatcher.trivialMatcher(Hill.class));
		FixtureMatcher.complements(Ground.class, Ground::isExposed, "Ground (exposed)",
			"Ground").forEach(retval::add);
		return Collections.unmodifiableList(retval);
	}

	private final ICLIHelper cli;
	@Override
	public SPOptions getOptions() {
		return EmptyOptions.EMPTY_OPTIONS;
	}
	
	private final MapTradeModel model;

	@Override
	public MapTradeModel getModel() {
		return model;
	}

	public MapTradeCLI(ICLIHelper cli, MapTradeModel model) {
		this.cli = cli;
		this.model = model;
	}

	private final List<FixtureMatcher> matchers = new ArrayList<>(initializeMatchers());

	void askAbout(FixtureMatcher matcher) {
		askAbout(matcher, "include");
	}

	private void askAbout(FixtureMatcher matcher, String key) {
		Boolean retval = cli.inputBooleanInSeries(String.format("Include %s items?",
			matcher.getDescription()), key);
		if (retval == null) {
			throw new IllegalStateException("EOF in fixture-matcher query");
		}
		matcher.setDisplayed(retval);
	}

	private boolean testFixture(TileFixture fixture) {
		for (FixtureMatcher matcher : matchers) {
			if (matcher.matches(fixture)) {
				return matcher.isDisplayed();
			}
		}
		FixtureMatcher newMatcher = FixtureMatcher.trivialMatcher(fixture.getClass(),
			fixture.getPlural());
		askAbout(newMatcher, "new");
		matchers.add(newMatcher);
		return newMatcher.isDisplayed();
	}

	@Override
	public void startDriver() {
		IMapNG first = model.getMap();
		if (!model.getSubordinateMaps().iterator().hasNext()) {
			// TODO: Should be DriverFailedException, right?
			throw new IllegalArgumentException("Must have at least one map to copy to");
		}
		IMapNG second = model.getSubordinateMaps().iterator().next();
		Boolean copyPlayers = cli.inputBoolean("Copy players?");
		if (copyPlayers != null) { // TODO: invert
			if (copyPlayers) {
				model.copyPlayers();
			}
		} else {
			return;
		}
		Boolean copyRivers = cli.inputBoolean("Include rivers?");
		if (copyRivers == null) {
			return;
		}
		Boolean copyRoads = cli.inputBoolean("Include roads?");
		if (copyRoads == null) {
			return;
		}
		matchers.forEach(this::askAbout);
		boolean zeroFixtures;
		if (first.getCurrentPlayer().isIndependent() || second.getCurrentPlayer().isIndependent() ||
				first.getCurrentPlayer().getPlayerId() !=
					second.getCurrentPlayer().getPlayerId()) { // TODO: inline into declaration
			zeroFixtures = true;
		} else {
			zeroFixtures = false;
		}
		final long totalCount = first.streamLocations()
			.filter(l -> !first.isLocationEmpty(l)).count();
		int count = 1;
		for (Point location : first.getLocations()) {
			if (first.isLocationEmpty(location)) {
				continue;
			}
			LOGGER.fine(String.format("Copying contents at %s, location %d/%d", location,
				count, totalCount));
			model.copyBaseTerrainAt(location);
			model.maybeCopyFixturesAt(location, this::testFixture, zeroFixtures);
			if (copyRivers) {
				model.copyRiversAt(location);
			}
			if (copyRoads) {
				model.copyRoadsAt(location);
			}
			count++;
		}
	}
}
