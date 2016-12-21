package view.map.misc;

import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;
import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Formatter;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JScrollPane;
import javax.xml.stream.XMLStreamException;
import model.map.IMapNG;
import model.map.MapDimensions;
import model.map.PlayerCollection;
import model.map.SPMapNG;
import org.eclipse.jdt.annotation.Nullable;
import util.LineEnd;
import util.TypesafeLogger;
import util.Warning;
import view.util.SPFrame;
import view.util.StreamingLabel;

/**
 * A window to show the result of running subset tests.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public final class SubsetFrame extends SPFrame {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(MapCheckerFrame.class);
	/**
	 * The color to use for errors.
	 */
	private static final StreamingLabel.LabelTextColor ERROR_COLOR =
			StreamingLabel.LabelTextColor.red;
	/**
	 * The map reader we'll use.
	 */
	private final MapReaderAdapter reader = new MapReaderAdapter();
	/**
	 * The label that's the bulk of the GUI.
	 */
	private final StreamingLabel label = new StreamingLabel();
	/**
	 * The main map.
	 */
	private IMapNG mainMap =
			new SPMapNG(new MapDimensions(0, 0, 2), new PlayerCollection(), -1);

	/**
	 * Constructor.
	 */
	public SubsetFrame() {
		super("Subset Tester", Optional.empty(), new Dimension(640, 320));
		setContentPane(new JScrollPane(label));
	}

	/**
	 * Enclose a string in HTML paragraph indicators, using the default color. And
	 * repaint the label so it shows up. This is "package-private" because, since the
	 * anonymous inner class below needs it, we can't make it private. If no color is
	 * specified, we'll make it white, because the label's background color is black.
	 *
	 * @param paragraph the string to enclose
	 */
	protected void printParagraph(final String paragraph) {
		printParagraph(paragraph, StreamingLabel.LabelTextColor.white);
	}

	/**
	 * Enclose a string in HTML paragraph indicators, optionally with a color. And
	 * repaint the label so it shows up. This is "package-private" because, since the
	 * anonymous inner class below needs it, we can't make it private. If no color is
	 * specified, we'll make it white, because the label's background color is black.
	 *
	 * @param paragraph the string to enclose
	 * @param color     the color to make it, or the empty string if none.
	 */
	protected void printParagraph(final String paragraph,
								  final StreamingLabel.LabelTextColor color) {
		try (final PrintWriter writer = label.getWriter()) {
			// Because StringWriter's close() does nothing, this is safe.
			writer.print("<p style=\"color:");
			writer.print(color);
			writer.print("\">");
			writer.print(paragraph);
			writer.println("</p>");
		}
		// At one point we called updateText on the label.
		label.repaint();
	}

	/**
	 * Load a new map as the main map, which the others should be subsets of.
	 *
	 * @param arg the filename to load it from
	 * @throws SPFormatException  on bad SP map format in the file
	 * @throws XMLStreamException on malformed XML
	 * @throws IOException        on other I/O error
	 */
	public void loadMain(final Path arg)
			throws SPFormatException, XMLStreamException, IOException {
		try {
			mainMap = reader.readMap(arg, Warning.Ignore);
		} catch (final FileNotFoundException | NoSuchFileException except) {
			printParagraph("File " + arg + " not found", ERROR_COLOR);
			throw except;
		} catch (final MapVersionException except) {
			printParagraph("ERROR: Map version of main map " + arg +
								   " not acceptable to reader", ERROR_COLOR);
			throw except;
		} catch (final XMLStreamException except) {
			printParagraph("ERROR: Malformed XML in file " + arg +
								   "; see following error message for details",
					ERROR_COLOR);
			printParagraph(except.getLocalizedMessage(), ERROR_COLOR);
			throw except;
		} catch (final SPFormatException except) {
			printParagraph(
					"ERROR: SP map format error at line " + except.getLine()
							+ " in file " + arg
							+ "; see following error message for details",
					ERROR_COLOR);
			printParagraph(except.getLocalizedMessage(), ERROR_COLOR);
			throw except;
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			printParagraph("ERROR: I/O error reading file " + arg,
					ERROR_COLOR);
			throw except;
		}
		printParagraph(
				"<span style=\"color:green\">OK</span> if strict subset, "
						+ "<span style=\"color:yellow\">WARN</span> if apparently "
						+ "not (but check by hand), "
						+ "<span style=\"color:red\">FAIL</span> if "
						+ "error in reading");
	}

	/**
	 * Load a new, but already-read-from-file, map as the main map, which all the others
	 * should be subsets of.
	 *
	 * @param map the map to load.
	 */
	public void loadMain(final IMapNG map) {
		mainMap = map;
	}

	/**
	 * Test a map against the main map, to see if it's a strict subset of it.
	 *
	 * @param map  the map to test
	 * @param file the file from which it was loaded
	 */
	public void test(final IMapNG map, final Optional<Path> file) {
		final String filename;
		if (file.isPresent()) {
			filename = file.get().toString();
		} else {
			LOGGER.warning("Given a map with no filename");
			printParagraph("Given a map with no filename",
					StreamingLabel.LabelTextColor.yellow);
			filename = "an unnamed file";
		}
		printParagraph("Testing " + filename + " ...");
		try (final Writer out = new HTMLWriter(label.getWriter())) {
			if (mainMap.isSubset(map, new Formatter(out), filename + ':')) {
				printParagraph("OK", StreamingLabel.LabelTextColor.green);
			} else {
				printParagraph("WARN", StreamingLabel.LabelTextColor.yellow);
			}
		} catch (final IOException e) {
			//noinspection HardcodedFileSeparator
			LOGGER.log(Level.SEVERE, "I/O error writing to window", e);
			//noinspection HardcodedFileSeparator
			printParagraph("ERROR: I/O error writing to window", ERROR_COLOR);
		}
	}

	/**
	 * Test a map against the main map, to see if it's a strict subset of it. This method
	 * "eats" (but logs) all (anticipated) errors in reading the file.
	 *
	 * @param arg the file from which to load the possible subset.
	 */
	public void test(final Path arg) {
		printParagraph("Testing " + arg + " ...");
		final IMapNG map;
		try {
			map = reader.readMap(arg, Warning.Ignore);
		} catch (final MapVersionException except) {
			LOGGER.log(Level.SEVERE,
					"Map version in " + arg + " not acceptable to reader",
					except);
			printParagraph("ERROR: Map version not acceptable to reader",
					ERROR_COLOR);
			return;
		} catch (final FileNotFoundException | NoSuchFileException except) {
			printParagraph("FAIL: File not found", ERROR_COLOR);
			LOGGER.log(Level.SEVERE, arg + " not found", except);
			return;
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			LOGGER.log(Level.SEVERE, "I/O error reading " + arg, except);
			//noinspection HardcodedFileSeparator
			printParagraph("FAIL: I/O error reading file", ERROR_COLOR);
			return;
		} catch (final XMLStreamException except) {
			LOGGER.log(Level.SEVERE, "Malformed XML in file " + arg,
					except);
			printParagraph("FAIL: Malformed XML in the file; " +
								   "see following error message for details",
					ERROR_COLOR);
			printParagraph(except.getLocalizedMessage(), ERROR_COLOR);
			return;
		} catch (final SPFormatException except) {
			LOGGER.log(Level.SEVERE,
					"SP map format error reading " + arg, except);
			printParagraph(
					"FAIL: SP map format error at line " + except.getLine()
							+ "; see following error message for details",
					ERROR_COLOR);
			printParagraph(except.getLocalizedMessage(), ERROR_COLOR);
			return;
		}
		try (final Writer out = new HTMLWriter(label.getWriter())) {
			if (mainMap.isSubset(map, new Formatter(out), arg.toString() + ':')) {
				printParagraph("OK", StreamingLabel.LabelTextColor.green);
			} else {
				printParagraph("WARN", StreamingLabel.LabelTextColor.yellow);
			}
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			LOGGER.log(Level.SEVERE, "I/O error writing to label", except);
			printParagraph("ERROR: " + except.getLocalizedMessage(),
					StreamingLabel.LabelTextColor.red);
		}
	}

	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({"unused", "static-method"})
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization.
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings({"unused", "static-method"})
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * @return the title of this app
	 */
	@Override
	public String getWindowName() {
		return "Subset Tester";
	}

	/**
	 * A writer to put each line into an HTML paragraph, coloring them appropriately.
	 *
	 * @author Jonathan Lovelace
	 */
	@SuppressWarnings("resource")
	private static final class HTMLWriter extends FilterWriter {
		/**
		 * Pre-compiled pattern for matching newlines.
		 */
		private static final Pattern NEWLINE = Pattern.compile(LineEnd.LINE_SEP);
		/**
		 * Whether we're at the start of a line.
		 */
		private boolean lineStart = true;

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
		 * @param csq the string to print
		 * @return this
		 * @throws IOException on I/O error
		 */
		@SuppressWarnings({"ReturnOfThis", "StandardVariableNames"})
		@Override
		public Writer append(@Nullable final CharSequence csq) throws IOException {
			final String local = Objects.toString(csq);
			if (lineStart) {
				super.append("<p style=\"color:white\">");
			}
			super.append(
					NEWLINE.matcher(local).replaceAll("</p><p style=\"color:white\">"));
			lineStart = false;
			return this;
		}

		/**
		 * @return a String representation of the object
		 */
		@SuppressWarnings("MethodReturnAlwaysConstant")
		@Override
		public String toString() {
			return "HTMLWriter";
		}
	}
}
