package view.map.misc;

import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import model.map.MapDimensions;
import model.map.MapView;
import model.map.SPMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import util.TypesafeLogger;
import util.Warning;
import view.util.StreamingLabel;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;

/**
 * A window to show the result of running subset tests.
 *
 * @author Jonathan Lovelace
 *
 */
public class SubsetFrame extends JFrame {
	/**
	 * A writer to put each line into an HTML paragraph, coloring them
	 * appropriately.
	 *
	 * @author Jonathan Lovelace
	 *
	 */
	private static final class HTMLWriter extends PrintWriter {
		/**
		 * Whether we're in the middle of a line.
		 */
		private boolean middle = false;

		/**
		 * Constructor.
		 *
		 * @param writer the writer we wrap
		 */
		HTMLWriter(final Writer writer) {
			super(writer);
		}

		/**
		 * Start or continue a line.
		 *
		 * @param str the string to print
		 */
		@Override
		public void print(@Nullable final String str) {
			@NonNull
			final String local = str == null ? "null" : str;
			if (!middle) {
				super.print("<p style=\"color:white\">");
			}
			super.print(local.replaceAll("\n", "</p><p style=\"color:white\">"));
			middle = true;
		}

		/**
		 * Finish a line.
		 *
		 * @param line the end of the line
		 */
		@Override
		public void println(@Nullable final String line) {
			@NonNull
			final String local = line == null ? "null" : line;
			if (!middle) {
				super.print("<p style=\"color:white\">");
			}
			super.print(local.replaceAll("\n", "</p><p style=\"color:white\">"));
			super.println("</p>");
			middle = false;
		}
	}

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(MapCheckerFrame.class);
	/**
	 * The map reader we'll use.
	 */
	private final MapReaderAdapter reader = new MapReaderAdapter();
	/**
	 * The label that's the bulk of the GUI.
	 */
	private final StreamingLabel label = new StreamingLabel();

	/**
	 * Constructor.
	 */
	public SubsetFrame() {
		setMinimumSize(new Dimension(640, 320));
		setContentPane(new JScrollPane(label));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * Enclose a string in HTML paragraph indicators, optionally with a color.
	 * And repaint the label so it shows up. This is "package-private" because,
	 * since the anonymous inner class below needs it, we can't make it private.
	 * If no color is specified, we'll make it white, because the label's
	 * background color is black.
	 *
	 * @param string the string to enclose
	 * @param color the color to make it, or the empty string if none.
	 */
	void printParagraph(final String string, final String color) { // NOPMD: See
																	// above
		final PrintWriter writer = label.getWriter();
		if (color.isEmpty()) {
			writer.print("<p style=\"color:white\">");
		} else {
			writer.print("<p style=\"color:");
			writer.print(color);
			writer.print("\">");
		}
		writer.print(string);
		writer.println("</p>");
		// label.updateText();
		label.repaint();
	}

	/**
	 * The color to use for errors.
	 */
	private static final String ERROR_COLOR = "red";
	/**
	 * The main map.
	 */
	private IMap mainMap = new MapView(new SPMap(new MapDimensions(0, 0, 2)),
			0, 0);

	/**
	 * Load a new map as the main map, which the others should be subsets of.
	 *
	 * @param arg the filename to load it from
	 * @throws SPFormatException on bad SP map format in the file
	 * @throws XMLStreamException on malformed XML
	 * @throws IOException on other I/O error
	 */
	public void loadMain(final String arg) throws SPFormatException,
			XMLStreamException, IOException {
		try {
			mainMap = reader.readMap(arg, new Warning(Warning.Action.Ignore));
		} catch (final FileNotFoundException except) {
			printParagraph("File " + arg + " not found", ERROR_COLOR);
			throw except;
		} catch (final MapVersionException except) {
			printParagraph("ERROR: Map version of main map " + arg
					+ " not acceptable to reader", ERROR_COLOR);
			throw except;
		} catch (final XMLStreamException except) {
			printParagraph("ERROR: Malformed XML in file " + arg
					+ "; see following error message for details", ERROR_COLOR);
			final String message = except.getLocalizedMessage();
			assert message != null;
			printParagraph(message, ERROR_COLOR);
			throw except;
		} catch (final SPFormatException except) {
			printParagraph(
					"ERROR: SP map format error at line " + except.getLine()
							+ " in file " + arg
							+ "; see following error message for details",
					ERROR_COLOR);
			final String message = except.getLocalizedMessage();
			assert message != null;
			printParagraph(message, ERROR_COLOR);
			throw except;
		} catch (final IOException except) {
			printParagraph("ERROR: I/O error reading file " + arg, ERROR_COLOR);
			throw except;
		}
		printParagraph(
				"<span style=\"color:green\">OK</span> if strict subset, "
						+ "<span style=\"color:yellow\">WARN</span> if apparently not (but check by hand), "
						+ "<span style=\"color:red\">FAIL</span> if error in reading",
				"");
	}

	/**
	 * Test a map against the main map, to see if it's a strict subset of it.
	 * This method "eats" (but logs) all (anticipated) errors in reading the
	 * file.
	 *
	 * @param arg the file from which to load the possible subset.
	 */
	public void test(final String arg) { // NOPMD: this isn't a JUnit test ...
		printParagraph("Testing " + arg + " ...", "");
		// ESCA-JAVA0177:
		final IMap map; // NOPMD
		try {
			map = reader.readMap(arg, new Warning(Warning.Action.Ignore));
		} catch (final MapVersionException except) {
			LOGGER.log(Level.SEVERE, "Map version in " + arg
					+ " not acceptable to reader", except);
			printParagraph("ERROR: Map version not acceptable to reader",
					ERROR_COLOR);
			return; // NOPMD
		} catch (final FileNotFoundException except) {
			printParagraph("FAIL: File not found", ERROR_COLOR);
			LOGGER.log(Level.SEVERE, arg + " not found", except);
			return; // NOPMD
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O error reading " + arg, except);
			printParagraph("FAIL: I/O error reading file", ERROR_COLOR);
			return; // NOPMD
		} catch (final XMLStreamException except) {
			LOGGER.log(Level.SEVERE, "Malformed XML in file " + arg, except);
			printParagraph(
					"FAIL: Malformed XML in the file; see following error message for details",
					ERROR_COLOR);
			final String message = except.getLocalizedMessage();
			assert message != null;
			printParagraph(message, ERROR_COLOR);
			return; // NOPMD
		} catch (final SPFormatException except) {
			LOGGER.log(Level.SEVERE, "SP map format eror reading " + arg,
					except);
			printParagraph(
					"FAIL: SP map format error at line " + except.getLine()
							+ "; see following error message for details",
					ERROR_COLOR);
			final String message = except.getLocalizedMessage();
			assert message != null;
			printParagraph(message, ERROR_COLOR);
			return;
		}
		try (final PrintWriter out = new HTMLWriter(label.getWriter())) {
			if (mainMap.isSubset(map, out)) {
				printParagraph("OK", "green");
			} else {
				printParagraph("WARN", "yellow");
			}
		}
	}
}
