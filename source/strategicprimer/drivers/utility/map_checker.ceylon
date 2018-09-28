import java.awt {
    Dimension,
    Color
}
import java.io {
    FileNotFoundException,
    IOException
}
import java.nio.file {
    JPaths=Paths,
    JPath=Path,
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
    silentListener
}

import strategicprimer.drivers.common {
    UtilityDriver,
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions,
    ISPDriver
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
    TileType
}
import strategicprimer.model.impl.map {
    IMapNG
}
import strategicprimer.model.common.map.fixtures.mobile {
    IWorker
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
    UtilityMenu,
    WindowCloseListener
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

Logger log = logger(`module strategicprimer.drivers.utility`);
// Left outside mapCheckerCLI because it's also used in the todoFixerCLI.
{String+} landRaces = [ "Danan", "dwarf", "elf", "half-elf", "gnome", "human" ];
"A driver to check every map file in a list for errors."
service(`interface ISPDriver`)
shared class MapCheckerCLI() satisfies UtilityDriver {
    shared actual IDriverUsage usage = DriverUsage(false, ["-k", "--check"],
        ParamCount.atLeastOne, "Check map for errors",
        "Check a map file for errors, deprecated syntax, etc.", true, false);
    """An interface for checks of a map's *contents* that we don't want the XML-*reading*
       code to do."""
    alias Checker=>Anything(TileType, Point, IFixture, Warning);
    class SPContentWarning(Point context, String message)
            extends Exception("At ``context``: ``message``") { }
    class OwnerChecker(IMapNG map) {
        shared void check(TileType terrain, Point context, IFixture fixture,
                Warning warner) {
            if (is HasOwner fixture) {
                if (fixture.owner.name.trimmed.empty) {
                    warner.handle(SPContentWarning(context,
                        "Fixture owned by ``fixture.owner``, who has no name"));
                }
                if (!map.players.map(Player.playerId)
		                .any(fixture.owner.playerId.equals)) {
                    warner.handle(SPContentWarning(context,
                        "Fixture owned by ``fixture.owner``, who is not known by the map"));
                }
            }
        }
    }
    void lateriteChecker(TileType terrain, Point context, IFixture fixture,
            Warning warner) {
        if (is StoneDeposit fixture, StoneKind.laterite == fixture.stone,
                !TileType.jungle == terrain) {
            warner.handle(SPContentWarning(context, "Laterite stone in non-jungle"));
        }
    }
    void aquaticVillageChecker(TileType terrain, Point context, IFixture fixture,
            Warning warner) {
        if (is Village fixture, landRaces.contains(fixture.race),
                TileType.ocean == terrain) {
            warner.handle(SPContentWarning(context,
                "Aquatic village has non-aquatic race"));
        }
    }
    Boolean suspiciousSkill(IJob job) {
        if (job.size > 1) {
            return false;
        } else {
            return job.map(ISkill.name).any(suspiciousSkills.contains);
        }
    }
    void suspiciousSkillCheck(TileType terrain, Point context, IFixture fixture,
            Warning warner) {
        if (is IWorker fixture) {
            if (fixture.any(suspiciousSkill)) {
                warner.handle(SPContentWarning(context,
                    "``fixture.name`` has a Job with one suspiciously-named Skill"));
            }
            for (job in fixture) {
                for (skill in job) {
                    if (skill.name == "miscellaneous", skill.level > 0) {
                        warner.handle(SPContentWarning(context,
                            "``fixture.name`` has a level in 'miscellaneous'"));
                        return;
                    }
                }
            }
        }
    }
    {String+} placeholderKinds = [ "various", "unknown" ];
    {String+} placeholderUnits = [ "unit", "units" ];
    void resourcePlaceholderChecker(TileType terrain, Point context, IFixture fixture,
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
            }
        } else if (is ITownFixture fixture, exists stats = fixture.population) {
            for (resource in stats.yearlyConsumption) {
                resourcePlaceholderChecker(terrain, context, resource, warner);
            }
            for (resource in stats.yearlyProduction) {
                resourcePlaceholderChecker(terrain, context, resource, warner);
            }
        }
    }
    Boolean positiveNumber(Number<out Anything> num) => num.positive;
    void acreageChecker(Point context, Warning warner, {IFixture*} fixtures) {
        variable Float total = 0.0;
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
            }
            if (total > 160.0) {
                warner.handle(SPContentWarning(context,
                    "More explicit acres (``Float.format(total, 0, 1)``) than tile should allow"));
                return;
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
        }
    }
    {Checker+} extraChecks = [ lateriteChecker, aquaticVillageChecker,
        suspiciousSkillCheck, resourcePlaceholderChecker ];
    void contentCheck(Checker checker, TileType terrain, Point context, Warning warner,
            IFixture* list) {
        for (fixture in list) {
            if (is {IFixture*} fixture) {
                contentCheck(checker, terrain, context, warner, *fixture);
            }
            checker(terrain, context, fixture, warner);
        }
    }
    shared void check(JPath file, Anything(String) outStream, Anything(String) err,
            Warning warner = warningLevels.custom()) {
        outStream("Starting ``file``");
        IMapNG map;
        try {
            map = mapIOHelper.readMap(file, warner);
        } catch (FileNotFoundException|NoSuchFileException except) {
            err("``file`` not found");
            log.error("``file`` not found");
            log.debug("Full stack trace of file-not-found:", except);
            return;
        } catch (IOException except) {
            err("I/O error reading ``file``");
            log.error("I/O error reading ``file``: ``except.message``");
        log.debug("Full stack trace of I/O error", except);
            return;
        } catch (XMLStreamException except) {
            err("Malformed XML in ``file``");
            log.error("Malformed XML in ``file``: ``except.message``");
        log.debug("Full stack trace of malformed-XML error", except);
            return;
        } catch (SPFormatException except) {
            err("SP map format error in ``file``");
            log.error("SP map format error in ``file``: ``except.message``");
            log.debug("Full stack trace of SP map format error:", except);
            return;
        }
        for (checker in extraChecks.follow(OwnerChecker(map).check)) {
            for (location in map.locations) {
                if (exists  terrain = map.baseTerrain[location]) {
                    contentCheck(checker, terrain, location, warner,
//                      *map.fixtures[location]);
                        *map.fixtures.get(location));
                }
            }
        }
        for (location in map.locations) {
            if (exists terrain = map.baseTerrain[location]) {
                acreageChecker(location, warner, map.fixtures.get(location));
            }
        }
    }
    shared actual void startDriverOnArguments(ICLIHelper cli, SPOptions options,
            String* args) {
        for (filename in args.coalesced) {
            check(JPaths.get(filename), cli.println, cli.println);
        }
    }
}
"The map-checker GUI window."
class MapCheckerFrame() extends SPFrame("Strategic Primer Map Checker", null,
        Dimension(640, 320), true, noop, "Map Checker") {
    MapCheckerCLI mapCheckerCLI = MapCheckerCLI();
    StreamingLabel label = StreamingLabel();
    void printParagraph(String paragraph,
            LabelTextColor color = LabelTextColor.white) {
        label.append("<p style=\"color:``color``\">``paragraph``</p>");
    }
    void customPrinter(String string) =>
            printParagraph(string, LabelTextColor.yellow);
    setBackground(Color.black);
    contentPane = JScrollPane(label);
    contentPane.background = Color.black;
    void outHandler(String text) {
        if (text.startsWith("No errors")) {
            printParagraph(text, LabelTextColor.green);
        } else {
            printParagraph(text);
        }
    }
    void errHandler(String text) =>
            printParagraph(text, LabelTextColor.red);
    shared void check(JPath filename) {
        mapCheckerCLI.check(filename, outHandler, errHandler,
            warningLevels.custom(customPrinter));
    }
    shared actual void acceptDroppedFile(JPath file) => check(file);
}
"A driver to check every map file in a list for errors and report the results in a
 window."
service(`interface ISPDriver`)
shared class MapCheckerGUI() satisfies UtilityDriver {
    shared actual IDriverUsage usage = DriverUsage(true, ["-k", "--check"],
        ParamCount.atLeastOne, "Check map for errors",
        "Check a map file for errors, deprecated syntax, etc.", false, true);
    shared actual void startDriverOnArguments(ICLIHelper cli, SPOptions options,
            String* args) {
        MapCheckerFrame window = MapCheckerFrame();
        window.jMenuBar = UtilityMenu(window);
        window.addWindowListener(WindowCloseListener(silentListener(window.dispose)));
        window.showWindow();
        for (arg in args.coalesced) {
            // can't condense this using Iterable.each(): JPaths.get() is overloaded
            window.check(JPaths.get(arg));
        }
    }
}
