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
    SPOptions
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
    suspiciousSkills
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
    readMap,
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
"""An interface for checks of a map's *contents* that we don't want the XML-*reading*
   code to do."""
interface Checker {
    shared formal void check(
        "The terrain at the location being checked."
        TileType terrain,
        "The location being checked."
        Point context,
         "The tile fixture being checked."
        IFixture fixture,
        "How warnings should be reported."
        Warning warner);
}
class SPContentWarning(Point context, String message)
        extends Exception("At ``context``: ``message``") { }
object lateriteChecker satisfies Checker {
    shared actual void check(TileType terrain, Point context, IFixture fixture,
            Warning warner) {
        if (is StoneDeposit fixture, StoneKind.laterite ==fixture.stone,
                !TileType.jungle == terrain) {
            warner.handle(SPContentWarning(context, "Laterite stone in non-jungle"));
        }
    }
}
{String+} landRaces = { "Danan", "dwarf", "elf", "half-elf", "gnome", "human" };
object aquaticVillageChecker satisfies Checker {
    shared actual void check(TileType terrain, Point context, IFixture fixture,
            Warning warner) {
        if (is Village fixture, landRaces.contains(fixture.race), 
                TileType.ocean == terrain) {
            warner.handle(SPContentWarning(context,
                "Aquatic village has non-aquatic race"));
        }
    }
}
Boolean suspiciousSkill(IJob job) {
    if (job.size > 1) {
        return false;
    } else {
        return {*job}.any(suspiciousSkills.contains);
    }
}
object suspiciousSkillCheck satisfies Checker {
    shared actual void check(TileType terrain, Point context, IFixture fixture,
            Warning warner) {
        if (is IWorker fixture) {
            if ({*fixture}.any(suspiciousSkill)) {
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
}
{String+} placeholderKinds = { "various", "unknown" };
{String+} placeholderUnits = { "unit", "units" };
object resourcePlaceholderChecker satisfies Checker {
    shared actual void check(TileType terrain, Point context, IFixture fixture,
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
            }
        } else if (is ITownFixture fixture, exists stats = fixture.population) {
            for (resource in stats.yearlyConsumption) {
                check(terrain, context, resource, warner);
            }
            for (resource in stats.yearlyProduction) {
                check(terrain, context, resource, warner);
            }
        }
    }
}
{Checker+} extraChecks = { lateriteChecker, aquaticVillageChecker, suspiciousSkillCheck,
    resourcePlaceholderChecker };
"A driver to check every map file in a list for errors."
shared object mapCheckerCLI satisfies UtilityDriver {
    shared actual IDriverUsage usage = DriverUsage(false, "-k", "--check",
        ParamCount.atLeastOne, "Check map for errors",
        "Check a map file for errors, deprecated syntax, etc.");
    void contentCheck(Checker checker, TileType terrain, Point context, Warning warner,
            IFixture* list) {
        for (fixture in list) {
            if (is {IFixture*} fixture) {
                contentCheck(checker, terrain, context, warner, *fixture);
            }
            checker.check(terrain, context, fixture, warner);
        }
    }
    shared void check(JPath file, Anything(String) outStream, Anything(String) err,
            Warning warner = warningLevels.custom()) {
        outStream("Starting ``file``");
        IMapNG map;
        try {
            map = readMap(file, warner);
        } catch (FileNotFoundException|NoSuchFileException except) {
            err("``file`` not found");
            log.error("``file`` not found", except);
            return;
        } catch (IOException except) {
            err("I/O error reading ``file``");
            log.error("I/O error reading ``file``", except);
            return;
        } catch (XMLStreamException except) {
            err("Malformed XML in ``file``");
            log.error("Malformed XML in ``file``", except);
            return;
        } catch (SPFormatException except) {
            err("SP map format error in ``file``");
            log.error("SP map format error in ``file``", except);
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
        Dimension(640, 320)) {
    shared actual String windowName = "Map Checker";
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
    shared void check(JPath filename) {
        mapCheckerCLI.check(filename, (text) {
            if (text.startsWith("No errors")) {
                printParagraph(text, LabelTextColor.green);
            } else {
                printParagraph(text);
            }
        }, (text) => printParagraph(text, LabelTextColor.red),
            warningLevels.custom(customPrinter));
    }
    shared actual void acceptDroppedFile(JPath file) => check(file);
    shared actual Boolean supportsDroppedFiles = true;
}
"A driver to check every map file in a list for errors and report the results in a
 window."
shared object mapCheckerGUI satisfies UtilityDriver {
    shared actual IDriverUsage usage = DriverUsage(true, "-k", "--check",
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
