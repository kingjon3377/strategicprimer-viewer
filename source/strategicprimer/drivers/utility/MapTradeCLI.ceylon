import strategicprimer.drivers.common {
    FixtureMatcher,
    CLIDriver,
    SPOptions,
    emptyOptions
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.model.common.map {
    IMapNG,
    TileFixture,
    Player
}

import ceylon.collection {
    ArrayList,
    MutableList
}

import strategicprimer.model.common.map.fixtures.towns {
    AbstractTown,
    IFortress,
    Village
}

import strategicprimer.model.common.map.fixtures {
    TextFixture,
    Ground
}

import strategicprimer.model.common.map.fixtures.resources {
    CacheFixture,
    Meadow,
    Mine,
    Grove,
    Shrub,
    MineralVein,
    StoneDeposit
}

import strategicprimer.model.common.map.fixtures.mobile {
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
    Snowbird,
    Thunderbird,
    Pegasus,
    Unicorn,
    Kraken,
    Animal,
    AnimalTracks
}

import strategicprimer.model.common.map.fixtures.terrain {
    Hill,
    Forest,
    Oasis
}

import strategicprimer.model.common.map.fixtures.explorable {
    Cave,
    Portal,
    AdventureFixture,
    Battlefield
}

import ceylon.language.meta {
    type
}

"An app to copy selected contents from one map to another."
shared class MapTradeCLI satisfies CLIDriver {
    static {FixtureMatcher*} flatten(FixtureMatcher|{FixtureMatcher*} item) {
        if (is {FixtureMatcher*} item) {
            return item;
        } else {
            return Singleton(item);
        }
    }

    static {FixtureMatcher*} initializeMatchers() => [
            FixtureMatcher.complements<IUnit>(not(
                    compose(Player.independent, IUnit.owner)),
                "Units", "Independent Units"),
            FixtureMatcher.trivialMatcher(`IFortress`, "Fortresses"),
            FixtureMatcher.trivialMatcher(`TextFixture`, "Arbitrary-Text Notes"),
            FixtureMatcher.trivialMatcher(`Portal`),
            FixtureMatcher.trivialMatcher(`Oasis`, "Oases"),
            FixtureMatcher.trivialMatcher(`AdventureFixture`, "Adventures"),
            FixtureMatcher.trivialMatcher(`CacheFixture`, "Caches"),
            FixtureMatcher.trivialMatcher(`Forest`),
            FixtureMatcher.trivialMatcher(`AbstractTown`,
                "Cities, Towns, and Fortifications"),
            FixtureMatcher.trivialMatcher(`Village`),
            FixtureMatcher.trivialMatcher(`Animal`),
            FixtureMatcher.trivialMatcher(`AnimalTracks`),
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
            FixtureMatcher.trivialMatcher(`Dragon`),
            FixtureMatcher.trivialMatcher(`Pegasus`, "Pegasi"),
            FixtureMatcher.trivialMatcher(`Snowbird`),
            FixtureMatcher.trivialMatcher(`Thunderbird`),
            FixtureMatcher.trivialMatcher(`Unicorn`),
            FixtureMatcher.trivialMatcher(`Kraken`),
            FixtureMatcher.trivialMatcher(`Cave`),
            FixtureMatcher.trivialMatcher(`Battlefield`),
            FixtureMatcher.complements<Grove>(Grove.orchard, "Orchards", "Groves"),
            FixtureMatcher.trivialMatcher(`Shrub`),
            FixtureMatcher.complements<Meadow>(Meadow.field, "Fields", "Meadows"),
            FixtureMatcher.trivialMatcher(`Hill`),
            FixtureMatcher.complements<Ground>(Ground.exposed, "Ground (exposed)",
                "Ground")
        ].flatMap(flatten);

    ICLIHelper cli;
    shared actual SPOptions options = emptyOptions;
    shared actual MapTradeModel model;
    shared new (ICLIHelper cli, MapTradeModel model) {
        this.cli = cli;
        this.model = model;
    }

    MutableList<FixtureMatcher> matchers =
            ArrayList { elements = initializeMatchers(); };

    void askAbout(FixtureMatcher matcher, String key = "include") {
        assert (exists retval = cli.inputBooleanInSeries(
            "Include \"``matcher.description``\" items?", key));
        matcher.displayed = retval;
    }

    Boolean testFixture(TileFixture fixture) {
        for (matcher in matchers) {
            if (matcher.matches(fixture)) {
                return matcher.displayed;
            }
        }
        FixtureMatcher newMatcher = FixtureMatcher.trivialMatcher(type(fixture),
            fixture.plural);
        askAbout(newMatcher, "new");
        matchers.add(newMatcher);
        return newMatcher.displayed;
    }

    shared actual void startDriver() {
        IMapNG first = model.map;
        assert (exists second = model.subordinateMaps.first);
        Boolean? copyPlayers = cli.inputBoolean("Copy players?");
        if (exists copyPlayers) {
            if (copyPlayers) {
                model.copyPlayers();
            }
        } else {
            return;
        }
        Boolean? copyRivers = cli.inputBoolean("Include rivers?");
        if (is Null copyRivers) {
            return;
        }
        Boolean? copyRoads = cli.inputBoolean("Include roads?");
        if (is Null copyRoads) {
            return;
        }
        matchers.each(askAbout);
        Boolean zeroFixtures;
        if (first.currentPlayer.independent || second.currentPlayer.independent ||
                first.currentPlayer.playerId != second.currentPlayer.playerId) {
            zeroFixtures = true;
        } else {
            zeroFixtures = false;
        }
        Integer totalCount = first.locations.count(not(first.locationEmpty));
        variable Integer count = 1;
        for (location in first.locations.filter(not(first.locationEmpty))) {
            log.debug(
                "Copying contents at ``location``, location ``count``/``totalCount``");
            model.copyBaseTerrainAt(location);
            model.maybeCopyFixturesAt(location, testFixture, zeroFixtures);
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
