import ceylon.regex {
    regex,
    Regex
}

import java.awt {
    Dimension
}
import java.io {
    FileNotFoundException,
    IOException,
    JWriter=Writer
}
import java.lang {
    CharArray,
    CharSequence,
    overloaded
}
import java.nio.file {
    NoSuchFileException
}
import java.util {
    Formatter
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

import strategicprimer.model.common.map {
    MapDimensionsImpl,
    PlayerCollection,
    IMapNG,
    SPMapNG
}
import strategicprimer.model.impl.xmlio {
    mapIOHelper
}
import strategicprimer.model.common.xmlio {
    warningLevels,
    SPFormatException
}
import strategicprimer.drivers.gui.common {
    UtilityMenu,
    SPFrame,
    WindowCloseListener
}
import lovelace.util.common {
    silentListener,
    PathWrapper
}
import strategicprimer.drivers.common {
    ISPDriver
}

"A window to show the result of running subset tests."
class SubsetFrame(ISPDriver driver) extends SPFrame("Subset Tester", driver,
        Dimension(640, 320), true) {
    StreamingLabel label = StreamingLabel();
    object htmlWriter extends JWriter() {
        variable Boolean lineStart = true;
        Regex matcher = regex(operatingSystem.newline, true);
        shared actual overloaded JWriter append(CharSequence csq) {
            String local = csq.string;
            if (lineStart) {
                super.append("<p style=\"color:white\">");
            }
            super.append(matcher.replace(local, "</p><p style=\"color:white\">"));
            lineStart = false;
            return this;
        }
        shared actual overloaded JWriter append(CharSequence csq, Integer start,
                Integer end) => append(csq.subSequence(start, end));
        shared actual overloaded JWriter append(Character c) => append(c.string);
        shared actual void close() {}
        shared actual void flush() {}
        shared actual void write(CharArray cbuf, Integer off, Integer len) {
            variable Integer i = 0;
            while (i < cbuf.size) {
                if (i >= off, (i - off) < len) {
                    append(cbuf[i]);
                }
            }
        }

    }
    contentPane = JScrollPane(label);
    void printParagraph(String paragraph,
                LabelTextColor color = LabelTextColor.white) =>
            label.append("<p style=\"color:``color``\">``paragraph``</p>");
    variable IMapNG mainMap = SPMapNG(MapDimensionsImpl(0, 0, 2), PlayerCollection(), -1);
    shared void loadMain(IMapNG|PathWrapper arg) {
        if (is PathWrapper path = arg) {
            try {
                mainMap = mapIOHelper.readMap(path, warningLevels.ignore);
            } catch (FileNotFoundException|NoSuchFileException except) {
                printParagraph("File ``path`` not found", LabelTextColor.red);
                throw except;
            } catch (XMLStreamException except) {
                printParagraph("ERROR: Malformed XML in ``path
                ``; see following error message for details",
                    LabelTextColor.red);
                printParagraph(except.message, LabelTextColor.red);
                throw except;
            } catch (SPFormatException except) {
                printParagraph("ERROR: SP map format error at line ``except.line
                        `` in file ``path``; see following error message for details",
                    LabelTextColor.red);
                printParagraph(except.message, LabelTextColor.red);
                throw except;
            } catch (IOException except) {
                printParagraph("ERROR: I/O error reading file ``path``",
                    LabelTextColor.red);
                throw except;
            }
        } else {
            assert (is IMapNG arg);
            mainMap = arg;
        }
        printParagraph("""<span style="color:green">OK</span> if strict subset,
                          <span style="color:yellow">WARN</span> if apparently not (but
                          check by hand), <span style="color:red">FAIL</span> if error in
                          reading""");
    }
    "Test a map against the main map, to see if it's a strict subset of it."
    shared void testMap(IMapNG map, PathWrapper? file) {
        String filename;
        if (exists file) {
            filename = file.string;
        } else {
            log.warn("Given a map with no filename");
            printParagraph("Given a map with no filename",
                LabelTextColor.yellow);
            filename = "an unnamed file";
        }
        printParagraph("Testing ``filename`` ...");
        try (formatter = Formatter(htmlWriter)) {
            if (mainMap.isSubset(map,
                        // can't use curry() directly: Formatter.format() is overloaded
                        (String string) => formatter.format("%s: %s", filename,
                            string))) {
                printParagraph("OK", LabelTextColor.green);
            } else {
                printParagraph("WARN", LabelTextColor.yellow);
            }
        }
    }
    """Read a map from file and test it against the main map to see if it's a strict
       subset. This method "eats" (but logs) all (anticipated) errors in reading the
       file."""
    shared void testFile(PathWrapper path) {
        printParagraph("Testing ``path`` ...");
        IMapNG map;
        try {
            map = mapIOHelper.readMap(path, warningLevels.ignore);
        } catch (FileNotFoundException|NoSuchFileException except) {
            printParagraph("FAIL: File not found", LabelTextColor.red);
            log.error("``path`` not found", except);
            return;
        } catch (IOException except) {
            printParagraph("FAIL: I/O error reading file",
                LabelTextColor.red);
            log.error("I/O error reading ``path``", except);
            return;
        } catch (XMLStreamException except) {
            printParagraph(
                "FAIL: Malformed XML; see following error message for details",
                LabelTextColor.red);
            printParagraph(except.message, LabelTextColor.red);
            log.error("Malformed XML in file ``path``", except);
            return;
        } catch (SPFormatException except) {
            printParagraph("FAIL: SP map format error at line ``
            except.line``; see following error message for details",
                LabelTextColor.red);
            printParagraph(except.message, LabelTextColor.red);
            log.error("SP map format error reading``path``", except);
            return;
        }
        testMap(map, path);
    }
    shared actual void acceptDroppedFile(PathWrapper file) => testFile(file);
}
"Factory method to add the menu bar to a [[SubsetFrame]]." // FIXME: Merge into SubsetGUI
SubsetFrame subsetFrame(ISPDriver driver) {
    SubsetFrame retval = SubsetFrame(driver);
    retval.jMenuBar = UtilityMenu(retval);
    retval.addWindowListener(WindowCloseListener(silentListener(retval.dispose)));
    return retval;
}
