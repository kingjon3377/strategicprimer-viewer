package utility;

import legacy.map.IFixture;
import drivers.common.DriverFailedException;
import drivers.common.IncorrectUsageException;

import java.util.Collections;

import legacy.map.Point;
import drivers.common.FixtureMatcher;
import drivers.common.CLIDriver;
import drivers.common.SPOptions;
import drivers.common.EmptyOptions;

import drivers.common.cli.ICLIHelper;

import legacy.map.ILegacyMap;
import legacy.map.TileFixture;

import java.util.ArrayList;
import java.util.List;

import legacy.map.fixtures.towns.AbstractTown;
import legacy.map.fixtures.towns.IFortress;
import legacy.map.fixtures.towns.Village;

import legacy.map.fixtures.TextFixture;
import legacy.map.fixtures.Ground;

import legacy.map.fixtures.resources.CacheFixture;
import legacy.map.fixtures.resources.Meadow;
import legacy.map.fixtures.resources.Mine;
import legacy.map.fixtures.resources.Grove;
import legacy.map.fixtures.resources.Shrub;
import legacy.map.fixtures.resources.MineralVein;
import legacy.map.fixtures.resources.StoneDeposit;

import legacy.map.fixtures.mobile.Fairy;
import legacy.map.fixtures.mobile.Dragon;
import legacy.map.fixtures.mobile.Centaur;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.Giant;
import legacy.map.fixtures.mobile.Djinn;
import legacy.map.fixtures.mobile.Phoenix;
import legacy.map.fixtures.mobile.Sphinx;
import legacy.map.fixtures.mobile.Griffin;
import legacy.map.fixtures.mobile.Ogre;
import legacy.map.fixtures.mobile.Minotaur;
import legacy.map.fixtures.mobile.Troll;
import legacy.map.fixtures.mobile.Simurgh;
import legacy.map.fixtures.mobile.Snowbird;
import legacy.map.fixtures.mobile.Thunderbird;
import legacy.map.fixtures.mobile.Pegasus;
import legacy.map.fixtures.mobile.Unicorn;
import legacy.map.fixtures.mobile.Kraken;
import legacy.map.fixtures.mobile.Animal;
import legacy.map.fixtures.mobile.AnimalTracks;

import legacy.map.fixtures.terrain.Hill;
import legacy.map.fixtures.terrain.Forest;
import legacy.map.fixtures.terrain.Oasis;

import legacy.map.fixtures.explorable.Cave;
import legacy.map.fixtures.explorable.Portal;
import legacy.map.fixtures.explorable.AdventureFixture;
import legacy.map.fixtures.explorable.Battlefield;
import lovelace.util.LovelaceLogger;

/**
 * An app to copy selected contents from one map to another.
 */
public class MapTradeCLI implements CLIDriver {
    private static List<FixtureMatcher> initializeMatchers() {
        final List<FixtureMatcher> retval = new ArrayList<>();
        FixtureMatcher.complements(IUnit.class, u -> !u.owner().isIndependent(),
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

    public MapTradeCLI(final ICLIHelper cli, final MapTradeModel model) {
        this.cli = cli;
        this.model = model;
    }

    private final List<FixtureMatcher> matchers = new ArrayList<>(initializeMatchers());

    void askAbout(final FixtureMatcher matcher) {
        askAbout(matcher, "include");
    }

    private void askAbout(final FixtureMatcher matcher, final String key) {
        final Boolean retval = cli.inputBooleanInSeries(String.format("Include %s items?",
                matcher.getDescription()), key);
        if (retval == null) {
            throw new IllegalStateException("EOF in fixture-matcher query");
        }
        matcher.setDisplayed(retval);
    }

    private boolean testFixture(final TileFixture fixture) {
        for (final FixtureMatcher matcher : matchers) {
            if (matcher.matches(fixture)) {
                return matcher.isDisplayed();
            }
        }
        final FixtureMatcher newMatcher = FixtureMatcher.trivialMatcher(fixture.getClass(),
                fixture.getPlural());
        askAbout(newMatcher, "new");
        matchers.add(newMatcher);
        return newMatcher.isDisplayed();
    }

    @Override
    public void startDriver() throws DriverFailedException {
        final ILegacyMap first = model.getMap();
        if (!model.getSubordinateMaps().iterator().hasNext()) {
            throw new IncorrectUsageException(MapTradeFactory.USAGE);
        }
        final ILegacyMap second = model.getSubordinateMaps().iterator().next();
        final Boolean copyPlayers = cli.inputBoolean("Copy players?");
        if (copyPlayers == null) {
            return;
        } else if (copyPlayers) {
            model.copyPlayers();
        }
        final Boolean copyRivers = cli.inputBoolean("Include rivers?");
        if (copyRivers == null) {
            return;
        }
        final Boolean copyRoads = cli.inputBoolean("Include roads?");
        if (copyRoads == null) {
            return;
        }
        matchers.forEach(this::askAbout);
        final boolean zeroFixtures =
                first.getCurrentPlayer().isIndependent() || second.getCurrentPlayer().isIndependent() ||
                        first.getCurrentPlayer().getPlayerId() != second.getCurrentPlayer().getPlayerId();
        final long totalCount = first.streamLocations()
                .filter(l -> !first.isLocationEmpty(l)).count();
        int count = 1;
        for (final Point location : first.getLocations()) {
            if (first.isLocationEmpty(location)) {
                continue;
            }
            LovelaceLogger.debug("Copying contents at %s, location %d/%d", location,
                    count, totalCount);
            model.copyBaseTerrainAt(location);
            model.maybeCopyFixturesAt(location, this::testFixture,
                    zeroFixtures ? IFixture.CopyBehavior.ZERO : IFixture.CopyBehavior.KEEP);
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
