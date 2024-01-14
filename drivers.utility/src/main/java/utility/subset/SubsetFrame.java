package utility.subset;

import drivers.common.DriverFailedException;

import javax.xml.stream.XMLStreamException;
import java.io.Serial;
import java.nio.file.NoSuchFileException;
import java.io.FileNotFoundException;
import java.nio.file.Path;

import legacy.map.LegacyPlayerCollection;
import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JScrollPane;

import lovelace.util.StreamingLabel;

import static lovelace.util.StreamingLabel.LabelTextColor;

import legacy.map.MapDimensionsImpl;
import legacy.map.ILegacyMap;
import legacy.map.LegacyMap;
import legacy.xmlio.MapIOHelper;
import common.xmlio.Warning;
import common.xmlio.SPFormatException;
import drivers.gui.common.SPFrame;
import drivers.common.ISPDriver;

/**
 * A window to show the result of running subset tests.
 */
/* package */ final class SubsetFrame extends SPFrame {
    @Serial
    private static final long serialVersionUID = 1L;

    public SubsetFrame(final ISPDriver driver) {
        super("Subset Tester", driver, new Dimension(640, 320), true);
        label = new StreamingLabel();
        setContentPane(new JScrollPane(label));
    }

    private final StreamingLabel label;

    private class HtmlWriter {
        private final String filename;

        public HtmlWriter(final String filename) {
            this.filename = filename;
        }

        private boolean lineStart = true;
        private final Pattern matcher = Pattern.compile(System.lineSeparator());

        public void write(final String string) {
            if (lineStart) {
                label.append("<p style=\"color:black\">");
            }
            label.append(filename);
            label.append(": ");
            label.append(matcher.matcher(string).replaceAll("</p><p style=\"color:black\">"));
            lineStart = false;
        }
    }

    private void printParagraph(final String paragraph) {
        printParagraph(paragraph, LabelTextColor.BLACK);
    }

    private void printParagraph(final String paragraph, final LabelTextColor color) {
        label.append(String.format("<p style=\"color:%s\">%s</p>", color, paragraph));
    }

    private ILegacyMap mainMap = new LegacyMap(new MapDimensionsImpl(0, 0, 2), new LegacyPlayerCollection(), -1);

    public void loadMain(final ILegacyMap arg) {
        mainMap = arg;
        printParagraph("<span style=\"color:green\">OK</span> if strict subset, " +
                "<span style=\"color:yellow\">WARN</span> if apparently not (but " +
                "check by hand), <span style=\"color:red\">FAIL</span> if error " +
                "in reading");
    }

    /**
     * Test a map against the main map, to see if it's a strict subset of it.
     */
    public void testMap(final ILegacyMap map, final @Nullable Path file) {
        final String filename;
        if (Objects.isNull(file)) {
            LovelaceLogger.warning("Given a map with no filename");
            printParagraph("Given a map with no filename", LabelTextColor.YELLOW);
            filename = "an unnamed file";
        } else {
            filename = file.toString();
        }
        printParagraph(String.format("Testing %s ...", filename));
        if (mainMap.isSubset(map, new HtmlWriter(filename)::write)) {
            printParagraph("OK", LabelTextColor.GREEN);
        } else {
            printParagraph("WARN", LabelTextColor.YELLOW);
        }
    }

    public void loadMain(final Path arg) throws DriverFailedException {
        try {
            mainMap = MapIOHelper.readMap(arg, Warning.IGNORE);
        } catch (final NoSuchFileException | FileNotFoundException except) {
            printParagraph(String.format("File %s not found", arg), LabelTextColor.RED);
            throw new DriverFailedException(except, String.format("File %s not found", arg));
        } catch (final XMLStreamException except) {
            printParagraph(String.format(
                            "ERROR: Malformed XML in %s; see following error message for details", arg),
                    LabelTextColor.RED);
            printParagraph(except.getMessage(), LabelTextColor.RED);
            throw new DriverFailedException(except, "Malformed XML in main map " + arg);
        } catch (final SPFormatException except) {
            printParagraph(String.format(
                            "ERROR: SP map format error at line %d in file %s; see following error message for details",
                            except.getLine(), arg),
                    LabelTextColor.RED);
            printParagraph(except.getMessage(), LabelTextColor.RED);
            throw new DriverFailedException(except, "Invalid SP XML in main  map " + arg);
        } catch (final IOException except) {
            printParagraph("ERROR: I/O error reading file " + arg, LabelTextColor.RED);
            throw new DriverFailedException(except, "I/O error loading main map " + arg);
        }
        printParagraph("<span style=\"color:green\">OK</span> if strict subset, " +
                "<span style=\"color:yellow\">WARN</span> if apparently not (but " +
                "check by hand), <span style=\"color:red\">FAIL</span> if error " +
                "in reading");
    }

    /**
     * Read a map from file and test it against the main map to see if it's
     * a strict subset. This method "eats" (but logs) all (anticipated)
     * errors in reading the file.
     */
    public void testFile(final Path path) {
        printParagraph(String.format("Testing %s ...", path));
        final ILegacyMap map;
        try {
            map = MapIOHelper.readMap(path, Warning.IGNORE);
        } catch (final NoSuchFileException | FileNotFoundException except) {
            printParagraph("FAIL: File not found", LabelTextColor.RED);
            LovelaceLogger.error(except, "%s not found", path);
            return;
        } catch (final IOException except) {
            printParagraph("FAIL: I/O error reading file", LabelTextColor.RED);
            LovelaceLogger.error(except, "I/O error reading %s", path);
            return;
        } catch (final XMLStreamException except) {
            printParagraph("FAIL: Malformed XML; see following error message for details",
                    LabelTextColor.RED);
            printParagraph(except.getMessage(), LabelTextColor.RED);
            LovelaceLogger.error(except, "Malformed XML in file %s", path);
            return;
        } catch (final SPFormatException except) {
            printParagraph(String.format(
                            "FAIL: SP map format error at line %d; see following error message for details",
                            except.getLine()),
                    LabelTextColor.RED);
            printParagraph(except.getMessage(), LabelTextColor.RED);
            LovelaceLogger.error(except, "SP map format error reading %s", path);
            return;
        }
        testMap(map, path);
    }

    @Override
    public void acceptDroppedFile(final Path file) {
        testFile(file);
    }
}
