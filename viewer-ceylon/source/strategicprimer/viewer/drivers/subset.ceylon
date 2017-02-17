import controller.map.drivers {
    DriverFailedException
}
import controller.map.misc {
    ICLIHelper,
    MapReaderAdapter
}

import java.lang {
    Appendable,
    CharSequence
}
import java.nio.file {
    JPath=Path, JPaths = Paths,
    NoSuchFileException
}
import java.util {
    Formatter, JOptional=Optional
}

import model.misc {
    IDriverModel,
    IMultiMapModel,
    SimpleMultiMapModel
}
import lovelace.util.common {
    todo
}
import view.util {
    ErrorShower,
    SPFrame,
    StreamingLabel
}
import javax.swing {
    SwingUtilities,
    JScrollPane
}
import java.io {
    IOException,
    FileNotFoundException,
    FilterWriter,
    JWriter=Writer
}
import javax.xml.stream {
    XMLStreamException
}
import controller.map.formatexceptions {
    SPFormatException
}
import java.awt {
    Dimension
}
import model.map {
    IMapNG,
    SPMapNG,
    MapDimensionsImpl,
    PlayerCollection
}
import util {
    Warning,
    LineEnd
}
import ceylon.regex {
    Regex,
    regex
}
class AppendableHelper(ICLIHelper wrapped) satisfies Appendable {
    shared actual Appendable append(CharSequence csq) {
        wrapped.print(csq.string);
        return this;
    }
    shared actual Appendable append(CharSequence csq, Integer start, Integer end) =>
            append(csq.subSequence(start, end));
    shared actual Appendable append(Character c) => append(c.string);
}
"A driver to check whether player maps are subsets of the main map."
object subsetCLI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage(false, "-s", "--subset", ParamCount.atLeastTwo,
        "Check players' maps against master",
        "Check that subordinate maps are subsets of the main map, containing nothing that
         it does not contain in the same place.");
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IMultiMapModel model) {
            for (pair in model.subordinateMaps) {
                String filename = pair.second().map(JPath.string)
                    .orElse("map without a filename");
                cli.print("``filename``\t...\t\t");
                if (model.map.isSubset(pair.first(), Formatter(AppendableHelper(cli)),
                        "In ``filename``:")) {
                    cli.println("OK");
                } else {
                    cli.println("WARN");
                }
            }
        } else {
            log.warn("Subset checking does nothing with no subordinate maps");
            startDriverOnModel(cli, options, SimpleMultiMapModel(model));
        }
    }
}
"A window to show the result of running subset tests."
class SubsetFrame() extends SPFrame("Subset Tester", JOptional.empty<JPath>(),
        Dimension(640, 320)) {
    shared actual String windowName = "Subset Tester";
    StreamingLabel label = StreamingLabel();
    object htmlWriter extends FilterWriter(label.writer) {
        variable Boolean lineStart = true;
        Regex matcher = regex(LineEnd.lineSep, true);
        shared actual JWriter append(CharSequence csq) {
            String local = csq.string;
            if (lineStart) {
                super.append("<p style=\"color:white\">");
            }
            super.append(matcher.replace(local, "</p><p style=\"color:white\">"));
            lineStart = false;
            return this;
        }
    }
    contentPane = JScrollPane(label);
    void printParagraph(String paragraph,
            StreamingLabel.LabelTextColor color = StreamingLabel.LabelTextColor.white) {
        // This is safe because StringWriter's close() is a no-op
        try (writer = label.writer) {
            writer.println("<p style=\"color:``color``\">``paragraph``</p>");
        }
        // At one time we called updateText on the label.
        label.repaint();
    }
    variable IMapNG mainMap = SPMapNG(MapDimensionsImpl(0, 0, 2), PlayerCollection(), -1);
    MapReaderAdapter reader = MapReaderAdapter();
    shared void loadMain(IMapNG|JPath arg) {
        if (is JPath path = arg) {
            try {
                mainMap = reader.readMap(path, Warning.ignore);
            } catch (FileNotFoundException|NoSuchFileException except) {
                printParagraph("File ``path`` not found", StreamingLabel.LabelTextColor.red);
                throw except;
            } catch (XMLStreamException except) {
                printParagraph("ERROR: Malformed XML in ``path
                ``; see following error message for details",
                    StreamingLabel.LabelTextColor.red);
                printParagraph(except.message, StreamingLabel.LabelTextColor.red);
                throw except;
            } catch (SPFormatException except) {
                printParagraph("ERROR: SP map format error at line ``except.line`` in file ``
                path``; see following error message for details",
                    StreamingLabel.LabelTextColor.red);
                printParagraph(except.message, StreamingLabel.LabelTextColor.red);
                throw except;
            } catch (IOException except) {
                printParagraph("ERROR: I/O error reading file ``path``",
                    StreamingLabel.LabelTextColor.red);
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
    shared void testMap(IMapNG map, JPath? file) {
        String filename;
        if (exists file) {
            filename = file.string;
        } else {
            log.warn("Given a map with no filename");
            printParagraph("Given a map with no filename",
                StreamingLabel.LabelTextColor.yellow);
            filename = "an unnamed file";
        }
        printParagraph("Testing ``filename`` ...");
        try (formatter = Formatter(htmlWriter)) {
            if (mainMap.isSubset(map, formatter, "``filename``: ")) {
                printParagraph("OK", StreamingLabel.LabelTextColor.green);
            } else {
                printParagraph("WARN", StreamingLabel.LabelTextColor.yellow);
            }
        }
    }
    """Read a map from file and test it against the main map to see if it's a strict subset.
       This method "eats" (but logs) all (anticipated) errors in reading the file."""
    shared void testFile(JPath path) {
        printParagraph("Testing ``path`` ...");
        IMapNG map;
        try {
            map = reader.readMap(path, Warning.ignore);
        } catch (FileNotFoundException|NoSuchFileException except) {
            printParagraph("FAIL: File not found", StreamingLabel.LabelTextColor.red);
            log.error("``path`` not found", except);
            return;
        } catch (IOException except) {
            printParagraph("FAIL: I/O error reading file",
                StreamingLabel.LabelTextColor.red);
            log.error("I/O error reading ``path``", except);
            return;
        } catch (XMLStreamException except) {
            printParagraph(
                "FAIL: Malformed XML in the file; see following error message for details",
                StreamingLabel.LabelTextColor.red);
            printParagraph(except.message, StreamingLabel.LabelTextColor.red);
            log.error("Malformed XML in file ``path``", except);
            return;
        } catch (SPFormatException except) {
            printParagraph("FAIL: SP map format error at line ``
                    except.line``; see following error message for details",
                StreamingLabel.LabelTextColor.red);
            printParagraph(except.message, StreamingLabel.LabelTextColor.red);
            log.error("SP map format error reading``path``", except);
            return;
        }
        testMap(map, path);
    }
}
"A driver to check whether player maps are subsets of the main map and display the
 results graphically."
todo("Unify with subsetCLI")
object subsetGUI satisfies ISPDriver {
    shared actual IDriverUsage usage = DriverUsage(true, "-s", "--subset", ParamCount.atLeastTwo,
        "Check players' maps against master",
        "Check that subordinate maps are subsets of the main map, containing nothing that
         it does not contain in the same place.");
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IMultiMapModel model) {
            SubsetFrame frame = SubsetFrame();
            SwingUtilities.invokeLater(() => frame.setVisible(true));
            frame.loadMain(model.map);
            for (pair in model.subordinateMaps) {
                frame.testMap(pair.first(), pair.second().orElse(null));
            }
        } else {
            ErrorShower.showErrorDialog(null,
                "The subset driver doesn't make sense on a non-multi-map driver");
        }
    }
    shared actual void startDriverOnArguments(ICLIHelper cli, SPOptions options,
            String* args) {
        if (args.size < 2) {
            throw IncorrectUsageException(usage);
        }
        SubsetFrame frame = SubsetFrame();
        SwingUtilities.invokeLater(() => frame.setVisible(true));
        assert (exists first = args.first);
        try {
            frame.loadMain(JPaths.get(first));
        } catch (IOException except) {
            throw DriverFailedException("I/O error loading main map ``first``", except);
        } catch (XMLStreamException except) {
            throw DriverFailedException("Malformed XML in main map ``first``", except);
        } catch (SPFormatException except) {
            throw DriverFailedException("Invalid SP XML in main  map ``first``", except);
        }
        for (arg in args.rest) {
            frame.testFile(JPaths.get(arg));
        }
    }
}