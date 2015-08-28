package view.map.misc;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.xml.stream.XMLStreamException;

import org.eclipse.jdt.annotation.Nullable;

import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;
import model.map.IMapNG;
import model.map.MapDimensions;
import model.map.PlayerCollection;
import model.map.SPMapNG;
import util.NullCleaner;
import util.TypesafeLogger;
import util.Warning;
import util.Warning.Action;
import view.util.StreamingLabel;

/**
 * A window to show the result of running subset tests.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public class SubsetFrame extends JFrame {
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
	 * The color to use for errors.
	 */
	private static final String ERROR_COLOR = "red";
	/**
	 * The main map.
	 */
	private IMapNG mainMap = new SPMapNG(new MapDimensions(0, 0, 2),
			new PlayerCollection(), -1);

	/**
	 * A writer to put each line into an HTML paragraph, coloring them
	 * appropriately.
	 *
	 * @author Jonathan Lovelace
	 *
	 */
	private static final class HTMLWriter extends FilterWriter {
		/**
		 * Whether we're in the middle of a line.
		 */
		private boolean middle = false;

		/**
		 * Constructor.
		 *
		 * @param writer the writer we wrap
		 */
		protected HTMLWriter(final Writer writer) {
			super(writer);
		}

		/**
		 * Start or continue a line.
		 *
		 * @param str the string to print
		 * @throws IOException on I/O error
		 * @return this
		 */
		@Override
		public Writer append(@Nullable final CharSequence str) throws IOException {
			final String local =
					NullCleaner.valueOrDefault(str, "null").toString();
			assert local != null;
			if (!middle) {
				super.append("<p style=\"color:white\">");
			}
			super.append(local.replaceAll("\n", "</p><p style=\"color:white\">"));
			middle = true;
			return this;
		}

		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "HTMLWriter";
		}
	}

	/**
	 * Constructor.
	 */
	public SubsetFrame() {
		super("Subset Tester");
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
	protected void printParagraph(final String string, final String color) {
		try (final PrintWriter writer = label.getWriter()) {
			// Because StringWriter's close() does nothing, this is safe.
			if (color.isEmpty()) {
				writer.print("<p style=\"color:white\">");
			} else {
				writer.print("<p style=\"color:");
				writer.print(color);
				writer.print("\">");
			}
			writer.print(string);
			writer.println("</p>");
		}
		// label.updateText();
		label.repaint();
	}
	/**
	 * Load a new map as the main map, which the others should be subsets of.
	 *
	 * @param arg the filename to load it from
	 * @throws SPFormatException on bad SP map format in the file
	 * @throws XMLStreamException on malformed XML
	 * @throws IOException on other I/O error
	 */
	public void loadMain(final File arg) throws SPFormatException,
			XMLStreamException, IOException {
		try {
			mainMap = reader.readMap(arg, new Warning(Action.Ignore));
		} catch (final FileNotFoundException except) {
			printParagraph("File " + arg + " not found", ERROR_COLOR);
			throw except;
		} catch (final MapVersionException except) {
			printParagraph("ERROR: Map version of main map " + arg.getPath()
					+ " not acceptable to reader", ERROR_COLOR);
			throw except;
		} catch (final XMLStreamException except) {
			printParagraph("ERROR: Malformed XML in file " + arg.getPath()
					+ "; see following error message for details", ERROR_COLOR);
			printParagraph(
					NullCleaner.assertNotNull(except.getLocalizedMessage()),
					ERROR_COLOR);
			throw except;
		} catch (final SPFormatException except) {
			printParagraph(
					"ERROR: SP map format error at line " + except.getLine()
							+ " in file " + arg.getPath()
							+ "; see following error message for details",
					ERROR_COLOR);
			printParagraph(
					NullCleaner.assertNotNull(except.getLocalizedMessage()),
					ERROR_COLOR);
			throw except;
		} catch (final IOException except) {
			printParagraph("ERROR: I/O error reading file " + arg.getPath(),
					ERROR_COLOR);
			throw except;
		}
		printParagraph(
				"<span style=\"color:green\">OK</span> if strict subset, "
						+ "<span style=\"color:yellow\">WARN</span> if apparently "
						+ "not (but check by hand), "
						+ "<span style=\"color:red\">FAIL</span> if "
						+ "error in reading", "");
	}
	/**
	 * Test a map against the main map, to see if it's a strict subset of it.
	 * This method "eats" (but logs) all (anticipated) errors in reading the
	 * file.
	 *
	 * @param arg the file from which to load the possible subset.
	 */
	public void test(final File arg) { // NOPMD: this isn't a JUnit test ...
		printParagraph("Testing " + arg + " ...", "");
		// ESCA-JAVA0177:
		try {
			final IMapNG map = reader.readMap(arg, new Warning(Action.Ignore));
			try (final Writer out = new HTMLWriter(label.getWriter())) {
				if (mainMap.isSubset(map, out, arg.getName() + ':')) {
					printParagraph("OK", "green");
				} else {
					printParagraph("WARN", "yellow");
				}
			}
		} catch (final MapVersionException except) {
			LOGGER.log(Level.SEVERE, "Map version in " + arg.getPath()
					+ " not acceptable to reader", except);
			printParagraph("ERROR: Map version not acceptable to reader",
					ERROR_COLOR);
			return; // NOPMD
		} catch (final FileNotFoundException except) {
			printParagraph("FAIL: File not found", ERROR_COLOR);
			LOGGER.log(Level.SEVERE, arg.getPath() + " not found", except);
			return; // NOPMD
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O error reading " + arg.getPath(), except);
			printParagraph("FAIL: I/O error reading file", ERROR_COLOR);
			return; // NOPMD
		} catch (final XMLStreamException except) {
			LOGGER.log(Level.SEVERE, "Malformed XML in file " + arg.getPath(),
					except);
			printParagraph("FAIL: Malformed XML in the file; "
					+ "see following error message for details", ERROR_COLOR);
			printParagraph(
					NullCleaner.assertNotNull(except.getLocalizedMessage()),
					ERROR_COLOR);
			return; // NOPMD
		} catch (final SPFormatException except) {
			LOGGER.log(Level.SEVERE,
					"SP map format eror reading " + arg.getPath(), except);
			printParagraph(
					"FAIL: SP map format error at line " + except.getLine()
							+ "; see following error message for details",
					ERROR_COLOR);
			printParagraph(
					NullCleaner.assertNotNull(except.getLocalizedMessage()),
					ERROR_COLOR);
			return;
		}
	}
}
