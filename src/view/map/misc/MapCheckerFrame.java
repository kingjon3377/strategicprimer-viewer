package view.map.misc;

import controller.map.drivers.MapChecker;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import util.TypesafeLogger;
import util.Warning;
import view.util.SPFrame;
import view.util.StreamingLabel;
import view.util.StreamingLabel.LabelTextColor;

/**
 * A window to show the results of checking maps for errors.
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
public final class MapCheckerFrame extends SPFrame {
	/**
	 * The color to use for errors.
	 */
	private static final StreamingLabel.LabelTextColor ERROR_COLOR =
			StreamingLabel.LabelTextColor.red;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(MapCheckerFrame.class);
	/**
	 * The object that will do the actual checks.
	 */
	private final MapChecker checker = new MapChecker();
	/**
	 * The label that's the bulk of the GUI.
	 */
	private final StreamingLabel label = new StreamingLabel();

	/**
	 * Constructor.
	 */
	public MapCheckerFrame() {
		super("Strategic Primer Map Checker", Optional.empty(), new Dimension(640, 320));
		Warning.Custom.setCustomPrinter(Warning.wrapHandler(
				str -> printParagraph(str, StreamingLabel.LabelTextColor.yellow)));
		setBackground(Color.black);
		setContentPane(new JScrollPane(label));
		getContentPane().setBackground(Color.black);
	}

	/**
	 * Enclose a string in HTML paragraph indicators, using the default color. And
	 * repaint the label so it shows up. This is "package-private" because, since the
	 * anonymous inner class below needs it, we can't make it private. If no color is
	 * specified, we'll make it white, because the label's background color is black.
	 *
	 * @param paragraph the string to enclose
	 */
	private void printParagraph(final String paragraph) {
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
	private void printParagraph(final String paragraph,
								final StreamingLabel.LabelTextColor color) {
		try (final PrintWriter writer = label.getWriter()) {
			// This is safe because StringWriter.close() does nothing.
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
	 * Check a map.
	 *
	 * @param filename the name of the file to check.
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	public void check(final Path filename) {
		checker.check(filename, text -> {
			if (text.startsWith("No errors")) {
				printParagraph(text, LabelTextColor.green);
			} else {
				printParagraph(text);
			}
		}, text -> printParagraph(text, ERROR_COLOR));
	}

	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * A simple toString().
	 * @return a diagnostic String
	 */
	@Override
	public String toString() {
		return "MapCheckerFrame showing: " + label.getText();
	}

	/**
	 * A trivial toString().
	 * @return the title of this app
	 */
	@Override
	public String getWindowName() {
		return "Map Checker";
	}
}
