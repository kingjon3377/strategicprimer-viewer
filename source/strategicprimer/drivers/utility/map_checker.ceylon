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
import strategicprimer.model.map {
    Point,
    IFixture,
    IMapNG,
    TileType
}
import strategicprimer.model.map.fixtures.mobile {
    IWorker
}
import strategicprimer.model.map.fixtures.mobile.worker {
    IJob,
    suspiciousSkills,
	ISkill
}
import strategicprimer.model.map.fixtures.resources {
    StoneDeposit,
    StoneKind
}
import strategicprimer.model.map.fixtures.towns {
    Village,
    ITownFixture
}
import strategicprimer.model.xmlio {
    Warning,
    warningLevels,
    mapIOHelper,
    SPFormatException
}
import strategicprimer.model.map.fixtures {
    ResourcePile
}
import strategicprimer.drivers.gui.common {
    SPFrame,
    UtilityMenu
}
import ceylon.logging {
    Logger,
    logger
}

Logger log = logger(`module strategicprimer.drivers.utility`);
// Left outside mapCheckerCLI because it's also used in the todoFixerCLI.
{String+} landRaces = [ "Danan", "dwarf", "elf", "half-elf", "gnome", "human" ];
"A driver to check every map file in a list for errors."
service(`interface ISPDriver`)
shared class MapCheckerCLI() satisfies UtilityDriver {
    shared actual IDriverUsage usage = DriverUsage(false, ["-k", "--check"],
        ParamCount.atLeastOne, "Check map for errors",
        "Check a map file for errors, deprecated syntax, etc.");
    """An interface for checks of a map's *contents* that we don't want the XML-*reading*
       code to do."""
    alias Checker=>Anything(TileType, Point, IFixture, Warning);
    class SPContentWarning(Point context, String message)
            extends Exception("At ``context``: ``message``") { }
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
    {Checker+} extraChecks = [ lateriteChecker, aquaticVillageChecker, suspiciousSkillCheck,
        resourcePlaceholderChecker ];
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
        for (checker in extraChecks) {
            for (location in map.locations) {
                if (exists  terrain = map.baseTerrain[location]) {
                    contentCheck(checker, terrain, location, warner,
//                      *map.fixtures[location]);
                        *map.fixtures.get(location));
                }
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
        "Check a map file for errors, deprecated syntax, etc.");
    shared actual void startDriverOnArguments(ICLIHelper cli, SPOptions options,
            String* args) {
        MapCheckerFrame window = MapCheckerFrame();
        window.jMenuBar = UtilityMenu(window);
        window.setVisible(true);
        for (arg in args.coalesced) {
            window.check(JPaths.get(arg));
        }
    }
}
