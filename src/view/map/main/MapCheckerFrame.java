package view.map.main;

import java.awt.Color;
import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.xml.stream.XMLStreamException;

import util.Warning;
import view.util.StreamingLabel;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;

/**
 * A window to show the results of checking maps for errors.
 *
 * @author Jonathan Lovelace
 *
 */
public class MapCheckerFrame extends JFrame {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(MapCheckerFrame.class
			.getName());
	/**
	 * The map reader we'll use.
	 */
	private final transient MapReaderAdapter reader = new MapReaderAdapter();
	/**
	 * The label that's the bulk of the GUI.
	 */
	private final StreamingLabel label = new StreamingLabel();

	/**
	 * Constructor.
	 */
	public MapCheckerFrame() {
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
	void printParagraph(final String string, final String color) { // NOPMD: See above
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
//		label.updateText();
		label.repaint();
	}

	/**
	 * The color to use for errors.
	 */
	private static final String ERROR_COLOR = "red";

	/**
	 * Check a map.
	 *
	 * @param filename the name of the file to check.
	 */
	public void check(final String filename) {
		printParagraph("Starting " + filename, "");
		boolean retval = true;
		try {
			reader.readMap(filename, new Warning() {
				@Override
				public void warn(final Exception warning) {
//					super.warn(warning);
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
			});
		} catch (final MapVersionException except) {
			LOGGER.log(Level.SEVERE, "Map version in " + filename
					+ " not acceptable to reader", except);
			printParagraph("ERROR: Map version not acceptable to reader",
					ERROR_COLOR);
			retval = false;
		} catch (final FileNotFoundException except) {
			printParagraph("ERROR: File not found", ERROR_COLOR);
			LOGGER.log(Level.SEVERE, filename + " not found", except);
			retval = false;
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O error reading " + filename, except);
			printParagraph("ERROR: I/O error reading file", ERROR_COLOR);
			retval = false;
		} catch (final XMLStreamException except) {
			LOGGER.log(Level.SEVERE, "Malformed XML in file " + filename,
					except);
			printParagraph(
					"ERROR: Malformed XML in the file; see following error message for details",
					ERROR_COLOR);
			printParagraph(except.getLocalizedMessage(), ERROR_COLOR);
			retval = false;
		} catch (final SPFormatException except) {
			LOGGER.log(Level.SEVERE, "SP map format eror reading " + filename,
					except);
			printParagraph(
					"ERROR: SP map format error at line " + except.getLine()
							+ "; see following error message for details",
					ERROR_COLOR);
			printParagraph(except.getLocalizedMessage(), ERROR_COLOR);
			retval = false;
		}
		if (retval) {
			printParagraph("No errors in " + filename, "green");
		}
	}
}
