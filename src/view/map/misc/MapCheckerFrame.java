package view.map.misc;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.xml.stream.XMLStreamException;

import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;
import util.NullCleaner;
import util.TypesafeLogger;
import util.Warning;
import view.util.StreamingLabel;

/**
 * A window to show the results of checking maps for errors.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public class MapCheckerFrame extends JFrame {
	/**
	 * The color to use for errors.
	 */
	private static final String ERROR_COLOR = "red";
	/**
	 * The warning instance to use to print warnings to the frame.
	 */
	private final Warning warner = new Warning() {
		@Override
		public void warn(final Exception warning) {
			// super.warn(warning);
			if (warning instanceof SPFormatException) {
				printParagraph(
						"SP format warning: "
								+ warning.getLocalizedMessage(),
						"yellow");
			} else {
				printParagraph(
						"Warning: " + warning.getLocalizedMessage(),
						"yellow");
			}
		}
	};
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
	public MapCheckerFrame() {
		super("Strategic Primer Map Checker");
		setBackground(Color.black);
		setMinimumSize(new Dimension(640, 320));
		setContentPane(new JScrollPane(label));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		getContentPane().setBackground(Color.black);
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
			// This is safe because StringWriter.close() does nothing.
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
	 * Check a map.
	 *
	 * @param filename the name of the file to check.
	 */
	public void check(final String filename) {
		printParagraph("Starting " + filename, "");
		try {
			reader.readMap(new File(filename), warner);
		} catch (IOException | XMLStreamException | SPFormatException except) {
			printError(except, filename);
			return;
		}
		printParagraph("No errors in " + filename, "green");
	}
	/**
	 * Tell the user about, and log, an exception.
	 * @param except the exception in question
	 * @param filename what file was being read
	 */
	private void printError(final Exception except, final String filename) {
		if (except instanceof MapVersionException) {
			LOGGER.log(Level.SEVERE, "Map version in " + filename
					+ " not acceptable to reader", except);
			printParagraph("ERROR: Map version not acceptable to reader",
					ERROR_COLOR);
		} else if (except instanceof FileNotFoundException) {
			printParagraph("ERROR: File not found", ERROR_COLOR);
			LOGGER.log(Level.SEVERE, filename + " not found", except);
		} else if (except instanceof IOException) {
			printParagraph("ERROR: I/O error reading file", ERROR_COLOR);
			LOGGER.log(Level.SEVERE, "I/O error reading " + filename, except);
		} else if (except instanceof XMLStreamException) {
			printParagraph("ERROR: Malformed XML in the file"
					+ "; see following error message for details", ERROR_COLOR);
			final String message = NullCleaner.valueOrDefault(
					except.getLocalizedMessage(), "(message was null)");
			assert message != null;
			printParagraph(message, ERROR_COLOR);
			LOGGER.log(Level.SEVERE, "Malformed XML in file " + filename,
					except);
		} else if (except instanceof SPFormatException) {
			printParagraph("ERROR: SP map format error at line "
					+ ((SPFormatException) except).getLine()
					+ "; see following error message for details", ERROR_COLOR);
			final String message = NullCleaner.valueOrDefault(
					except.getLocalizedMessage(), "(message was null)");
			printParagraph(message, ERROR_COLOR);
			LOGGER.log(Level.SEVERE, "SP map format eror reading " + filename,
					except);
		} else {
			throw new IllegalStateException("Unhandled exception class");
		}
	}
}
