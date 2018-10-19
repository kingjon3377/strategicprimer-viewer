import java.awt {
    Dimension,
    Color
}
import java.io {
    FileNotFoundException,
    IOException
}
import java.nio.file {
    NoSuchFileException
}

import javax.swing {
    JScrollPane
}
import javax.xml.stream {
    XMLStreamException
}

import lovelace.util.jvm {
    StreamingLabel,
    LabelTextColor
}

import lovelace.util.common {
    matchingPredicate,
    silentListener,
    PathWrapper
}

import strategicprimer.drivers.common {
    UtilityDriver,
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions,
    UtilityDriverFactory,
    DriverFactory,
    ISPDriver,
    UtilityGUI
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.common.map {
    HasExtent,
    IFixture,
    Player,
    HasOwner,
    Point,
    TileType,
    IMapNG
}
import strategicprimer.model.common.map.fixtures.mobile {
    IWorker,
    AnimalTracks
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    IJob,
    suspiciousSkills,
    ISkill
}
import strategicprimer.model.common.map.fixtures.resources {
    StoneDeposit,
    StoneKind,
    Grove
}
import strategicprimer.model.common.map.fixtures.towns {
    Village,
    ITownFixture,
    TownSize
}
import strategicprimer.model.impl.xmlio {
    mapIOHelper
}
import strategicprimer.model.common.xmlio {
    Warning,
    warningLevels,
    SPFormatException
}
import strategicprimer.model.common.map.fixtures {
    ResourcePile
}
import strategicprimer.drivers.gui.common {
    SPFrame,
    WindowCloseListener,
    SPMenu,
    UtilityMenuHandler
}
import ceylon.logging {
    Logger,
    logger
}
import ceylon.decimal {
    Decimal
}
import ceylon.whole {
    Whole
}
import com.pump.window {
    WindowMenu
}
import strategicprimer.model.common.map.fixtures.terrain {
    Hill,
    Oasis
}

Logger log = logger(`module strategicprimer.drivers.utility`);

// Left outside mapCheckerCLI because it's also used in the todoFixerCLI.
{String+} landRaces = [ "Danan", "dwarf", "elf", "half-elf", "gnome", "human" ];

"A factory for a driver to check every map file in a list for errors."
service(`interface DriverFactory`)
shared class MapCheckerCLIFactory() satisfies UtilityDriverFactory {
    shared actual IDriverUsage usage = DriverUsage(false, ["-k", "--check"],
        ParamCount.atLeastOne, "Check map for errors",
        "Check a map file for errors, deprecated syntax, etc.", true, false);

    shared actual UtilityDriver createDriver(ICLIHelper cli, SPOptions options) =>
            MapCheckerCLI(cli.println, cli.println);
}

"A driver to check every map file in a list for errors."
shared class MapCheckerCLI satisfies UtilityDriver {
    """An interface for checks of a map's *contents* that we don't want the XML-*reading*
       code to do. Checkers should return true iff they report at least one warning."""
    static alias Checker=>Boolean(TileType, Point, IFixture, Warning);

    static class SPContentWarning(Point context, String message)
            extends Exception("At ``context``: ``message``") { }

    static class OwnerChecker(IMapNG map) {
        shared Boolean check(TileType terrain, Point context, IFixture fixture,
                Warning warner) {
            variable Boolean retval = false;
            if (is HasOwner fixture) {
                if (fixture.owner.name.trimmed.empty) {
                    warner.handle(SPContentWarning(context,
                        "Fixture owned by ``fixture.owner``, who has no name"));
                    retval = true;
                }
                if (!map.players.map(Player.playerId)
                        .any(fixture.owner.playerId.equals)) {
                    warner.handle(SPContentWarning(context,
                        "Fixture owned by ``fixture.owner``, who is not known by the map"));
                    retval = true;
                }
            }
            return retval;
        }
    }

    static Boolean lateriteChecker(TileType terrain, Point context, IFixture fixture,
            Warning warner) {
        if (is StoneDeposit fixture, StoneKind.laterite == fixture.stone,
                !TileType.jungle == terrain) {
            warner.handle(SPContentWarning(context, "Laterite stone in non-jungle"));
            return true;
        } else {
            return false;
        }
    }

    static Boolean oasisChecker(TileType terrain, Point context, IFixture fixture, Warning warner) {
        if (is Oasis fixture, TileType.desert != terrain) {
            warner.handle(SPContentWarning(context, "Oasis in non-desert"));
            return true;
        } else {
            return false;
        }
    }

    static Boolean animalTracksChecker(TileType terrain, Point context, IFixture fixture,
            Warning warner) {
        if (is AnimalTracks fixture) {
            warner.handle(SPContentWarning(context,
                "Animal tracks in map suspected to be main"));
            return true;
        } else {
            return false;
        }
    }

    static Boolean aquaticVillageChecker(TileType terrain, Point context, IFixture fixture,
            Warning warner) {
        if (is Village fixture, landRaces.contains(fixture.race),
                TileType.ocean == terrain) {
            warner.handle(SPContentWarning(context,
                "Aquatic village has non-aquatic race"));
            return true;
        } else {
            return false;
        }
    }

    static Boolean suspiciousSkill(IJob job) {
        if (job.size > 1) {
            return false;
        } else {
            return job.map(ISkill.name).any(suspiciousSkills.contains);
        }
    }

    static Boolean suspiciousSkillCheck(TileType terrain, Point context, IFixture fixture,
            Warning warner) {
        variable Boolean retval = false;
        if (is IWorker fixture) {
            if (fixture.any(suspiciousSkill)) {
                warner.handle(SPContentWarning(context,
                    "``fixture.name`` has a Job with one suspiciously-named Skill"));
                retval = true;
            }
            for (job in fixture) {
                for (skill in job) {
                    if (skill.name == "miscellaneous", skill.level > 0) {
                        warner.handle(SPContentWarning(context,
                            "``fixture.name`` has a level in 'miscellaneous'"));
                        return true;
                    }
                }
            }
        }
        return retval;
    }

    static {String+} placeholderKinds = [ "various", "unknown" ];
    static {String+} placeholderUnits = [ "unit", "units" ];

    static Boolean resourcePlaceholderChecker(TileType terrain, Point context, IFixture fixture,
            Warning warner) {
        if (is ResourcePile fixture) {
            if (placeholderKinds.contains(fixture.kind)) {
                warner.handle(SPContentWarning(context,
                    "Resource pile, ID #``fixture.id``, has placeholder kind: ``fixture
                            .kind``"));
            } else if (placeholderKinds.contains(fixture.contents)) {
                warner.handle(SPContentWarning(context,
                    "Resource pile, ID #``fixture.id``, has placeholder contents: ``fixture
                            .contents``"));
            } else if (placeholderUnits.contains(fixture.quantity.units)) {
                warner.handle(SPContentWarning(context,
                    "Resource pile, ID #``fixture.id``, has placeholder units"));
            } else if (fixture.contents.contains('#')) {
                warner.handle(SPContentWarning(context, "Resource pile, ID #``fixture
                    .id``, has suspicous contents: ``fixture.contents``"));
            } else {
                return false;
            }
            return true;
        } else if (is ITownFixture fixture, exists stats = fixture.population) {
            variable Boolean retval = false;
            for (resource in stats.yearlyConsumption) {
                retval = resourcePlaceholderChecker(terrain, context, resource, warner) || retval;
            }
            for (resource in stats.yearlyProduction) {
                retval = resourcePlaceholderChecker(terrain, context, resource, warner) || retval;
            }
            return retval;
        } else {
            return false;
        }
    }

    static Boolean positiveNumber(Number<out Anything> num) => num.positive;
    static Boolean acreageChecker(Point context, Warning warner, {IFixture*} fixtures) {
        variable Float total = 0.0;
        variable Boolean retval = false;
        for (fixture in fixtures.narrow<HasExtent>()
                .filter(matchingPredicate(positiveNumber, HasExtent.acres))) {
            switch (acres = fixture.acres)
            case (is Integer) {
                total += acres;
            }
            case (is Whole) {
                total += acres.float;
            }
            case (is Float) {
                total += acres;
            }
            case (is Decimal) {
                total += acres.float;
            }
            else {
                warner.handle(SPContentWarning(context,
                    "Unexpected acreage type in ID #``fixture.id``"));
                retval = true;
            }
            if (total > 160.0) { // TODO: Move out of loop
                warner.handle(SPContentWarning(context,
                    "More explicit acres (``Float.format(total, 0, 1)``) than tile should allow"));
                return true;
            }
        }
        for (fixture in fixtures.narrow<ITownFixture>()) {
            switch (fixture.townSize)
            case (TownSize.small) {
                total += 15;
            }
            case (TownSize.medium) {
                total += 40;
            }
            case (TownSize.large) {
                total += 80;
            }
        }
        total += fixtures.narrow<Grove>().map(Grove.population)
            .filter(Integer.positive).fold(0)(plus) / 500;
        if (total > 160.0) {
            warner.handle(SPContentWarning(context,
                "Counting towns and groves, more acres (``Float.format(total, 0, 1)``) used than tile should allow"));
            return true;
        } else {
            return retval;
        }
    }

    static {Checker+} extraChecks = [ lateriteChecker, aquaticVillageChecker,
        suspiciousSkillCheck, resourcePlaceholderChecker, oasisChecker ];

    static Boolean contentCheck(Checker checker, TileType terrain, Point context, Warning warner,
            IFixture* list) {
        variable Boolean retval = false;
        for (fixture in list) {
            if (is {IFixture*} fixture) {
                retval = contentCheck(checker, terrain, context, warner, *fixture) || retval;
            }
            retval = checker(terrain, context, fixture, warner) || retval;
        }
        return retval;
    }

    Anything(String) stdout;
    Anything(String) stderr;
    shared new (Anything(String) stdout, Anything(String) stderr) {
        this.stdout = stdout;
        this.stderr = stderr;
    }

    shared void check(PathWrapper file, Warning warner = warningLevels.custom()) {
        stdout("Starting ``file``");
        IMapNG map;
        try {
            map = mapIOHelper.readMap(file, warner);
        } catch (FileNotFoundException|NoSuchFileException except) {
            stderr("``file`` not found");
            log.error("``file`` not found");
            log.debug("Full stack trace of file-not-found:", except);
            return;
        } catch (IOException except) {
            stderr("I/O error reading ``file``");
            log.error("I/O error reading ``file``: ``except.message``");
        log.debug("Full stack trace of I/O error", except); // TODO: Indentation here
            return;
        } catch (XMLStreamException except) {
            stderr("Malformed XML in ``file``");
            log.error("Malformed XML in ``file``: ``except.message``");
            log.debug("Full stack trace of malformed-XML error", except);
            return;
        } catch (SPFormatException except) {
            stderr("SP map format error in ``file``");
            log.error("SP map format error in ``file``: ``except.message``");
            log.debug("Full stack trace of SP map format error:", except);
            return;
        }

        variable Boolean result = false;
        for (checker in extraChecks.follow(OwnerChecker(map).check)) {
            for (location in map.locations) {
                if (exists  terrain = map.baseTerrain[location]) {
                    result = contentCheck(checker, terrain, location, warner,
//                      *map.fixtures[location]) || result;
                        *map.fixtures.get(location)) || result;
                }
            }
            log.debug("Finished a check for ``file.filename``");
        }

        for (location in map.locations) {
            if (exists terrain = map.baseTerrain[location]) {
                result = acreageChecker(location, warner, map.fixtures.get(location)) || result;
            }
            if (map.mountainous.get(location), // TODO: syntax sugar
                    !map.fixtures.get(location).narrow<Hill>().empty) { // TODO: syntax sugar
                warner.handle(SPContentWarning(location, "Hill in mountainous tile"));
                result = true;
            }
        }

        if (file.filename.contains("world_turn")) {
            for (location->fixture in map.fixtures) {
                if (exists terrain = map.baseTerrain[location]) {
                    result = animalTracksChecker(terrain, location, fixture, warner) || result;
                }
            }
        }

        log.debug("Finished with ``file.filename``");
        if (result) {
            stdout("... done");
        } else {
            stdout("No errors in ``file.filename``");
        }
    }

    shared actual void startDriver(String* args) {
        for (filename in args.coalesced) {
            check(PathWrapper(filename));
        }
    }
}

"The map-checker GUI window." // TODO: Add an way to "open" files from the menu // TODO: Merge into MapCheckerGUI
class MapCheckerFrame(ISPDriver driver) extends SPFrame("Strategic Primer Map Checker",
        driver, Dimension(640, 320), true, noop, "Map Checker") {
    StreamingLabel label = StreamingLabel();
    void printParagraph(String paragraph,
            LabelTextColor color = LabelTextColor.black) {
        label.append("<p style=\"color:``color``\">``paragraph``</p>");
    }

    void customPrinter(String string) =>
            printParagraph(string, LabelTextColor.yellow);
    setBackground(Color.white);
    contentPane = JScrollPane(label);
    contentPane.background = Color.white;

    void outHandler(String text) {
        if (text.startsWith("No errors")) {
            printParagraph(text, LabelTextColor.green);
        } else {
            printParagraph(text);
        }
    }

    void errHandler(String text) =>
            printParagraph(text, LabelTextColor.red);

    MapCheckerCLI mapCheckerCLI = MapCheckerCLI(outHandler, errHandler);

    shared void check(PathWrapper filename) {
        mapCheckerCLI.check(filename, warningLevels.custom(customPrinter));
    }

    shared actual void acceptDroppedFile(PathWrapper file) => check(file);
}

"A factory for a driver to check every map file in a list for errors and report the
 results in a window."
service(`interface DriverFactory`)
shared class MapCheckerGUIFactory() satisfies UtilityDriverFactory {
    shared actual IDriverUsage usage = DriverUsage(true, ["-k", "--check"],
        ParamCount.anyNumber, "Check map for errors",
        "Check a map file for errors, deprecated syntax, etc.", false, true);

    shared actual UtilityDriver createDriver(ICLIHelper cli, SPOptions options) =>
            MapCheckerGUI();
}

"A driver to check every map file in a list for errors and report the results in a
 window."
shared class MapCheckerGUI() satisfies UtilityGUI {
    late MapCheckerFrame window;
    variable Boolean initialized = false;

    shared actual void startDriver(String* args) {
        if (!initialized) {
            initialized = true;
            window = MapCheckerFrame(this);
            window.jMenuBar = SPMenu(
                SPMenu.createFileMenu(UtilityMenuHandler(this, window).handleEvent, this),
                SPMenu.disabledMenu(SPMenu.createMapMenu(noop, this)),
                SPMenu.disabledMenu(SPMenu.createViewMenu(noop, this)),
                WindowMenu(window));
            window.addWindowListener(WindowCloseListener(silentListener(window.dispose)));
        }
        window.showWindow();
        args.coalesced.map(PathWrapper).each(window.check);
    }

    shared actual void open(PathWrapper path) => window.check(path);

}
