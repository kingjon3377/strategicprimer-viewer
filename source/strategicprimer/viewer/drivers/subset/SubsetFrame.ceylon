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
    CharSequence
}
import java.nio.file {
    NoSuchFileException,
    JPath=Path
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

import strategicprimer.model.map {
    IMap,
    SPMap,
    MapDimensionsImpl,
    PlayerCollection
}
import strategicprimer.model.xmlio {
    readMap,
    warningLevels,
    SPFormatException
}
import strategicprimer.viewer.drivers {
    SPFrame
}
"A window to show the result of running subset tests."
class SubsetFrame() extends SPFrame("Subset Tester", null, Dimension(640, 320)) {
    shared actual String windowName = "Subset Tester";
    StreamingLabel label = StreamingLabel();
    object htmlWriter extends JWriter() {
        variable Boolean lineStart = true;
        Regex matcher = regex(operatingSystem.newline, true);
        shared actual JWriter append(CharSequence csq) {
            String local = csq.string;
            if (lineStart) {
                super.append("<p style=\"color:white\">");
            }
            super.append(matcher.replace(local, "</p><p style=\"color:white\">"));
            lineStart = false;
            return this;
        }
        shared actual JWriter append(CharSequence csq, Integer start, Integer end) =>
                append(csq.subSequence(start, end));
        shared actual JWriter append(Character c) => append(c.string);
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
            LabelTextColor color = LabelTextColor.white) {
        label.append("<p style=\"color:``color``\">``paragraph``</p>");
    }
    variable IMap mainMap = SPMap(MapDimensionsImpl(0, 0, 2), PlayerCollection(), -1);
    shared void loadMain(IMap|JPath arg) {
        if (is JPath path = arg) {
            try {
                mainMap = readMap(path, warningLevels.ignore);
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
            assert (is IMap arg);
            mainMap = arg;
        }
        printParagraph("""<span style="color:green">OK</span> if strict subset,
                          <span style="color:yellow">WARN</span> if apparently not (but
                          check by hand), <span style="color:red">FAIL</span> if error in
                          reading""");
    }
    "Test a map against the main map, to see if it's a strict subset of it."
    shared void testMap(IMap map, JPath? file) {
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
    shared void testFile(JPath path) {
        printParagraph("Testing ``path`` ...");
        IMap map;
        try {
            map = readMap(path, warningLevels.ignore);
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
    shared actual void acceptDroppedFile(JPath file) => testFile(file);
    shared actual Boolean supportsDroppedFiles = true;
}
