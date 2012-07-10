package controller.map.drivers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.xml.stream.XMLStreamException;

import model.viewer.MapModel;
import util.Warning;
import view.map.main.MapFileFilter;
import view.map.main.ViewerFrame;
import view.util.ErrorShower;
import controller.map.SPFormatException;
import controller.map.misc.IOHandler;
import controller.map.misc.MapReaderAdapter;

/**
 * A class to start the viewer, to reduce circular dependencies between
 * packages.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class ViewerStart {
	/**
	 * Do not instantiate.
	 */
	private ViewerStart() {
		// Do not use.
	}

	/**
	 * An error message refactored from at least four uses.
	 */
	private static final String XML_ERROR_STRING = "Error reading XML file";
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(ViewerFrame.class
			.getName());
	/**
	 * Error message fragment when file not found.
	 */
	private static final String NOT_FOUND_ERROR = " not found";
	/**
	 * Error message when the map contains invalid data.
	 */
	private static final String INV_DATA_ERROR = "Map contained invalid data";

	/**
	 * Run the app.
	 * 
	 * @param args
	 *            Command-line arguments: args[0] is the map filename, others
	 *            are ignored. TODO: Add option handling.
	 * 
	 */
	public static void main(final String[] args) {
		final JFileChooser chooser = new JFileChooser(".");
		chooser.setFileFilter(new MapFileFilter());
		// ESCA-JAVA0177:
		final String filename; // NOPMD // $codepro.audit.disable
								// localDeclaration
		if (args.length == 0) {
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				filename = chooser.getSelectedFile().getPath();
			} else {
				return;
			}
		} else {
			filename = args[0];
		}
		try {
			final MapModel model = new MapModel(
					new MapReaderAdapter().readMap(filename, new Warning(Warning.Action.Warn)));
			final ViewerFrame frame = new ViewerFrame(model, new IOHandler(
					model, chooser).createMenu());
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
			if (args.length > 1) {
				model.setSecondaryMap(new MapReaderAdapter().readMap(args[1], new Warning(Warning.Action.Warn)));
			}
		} catch (final XMLStreamException e) {
			LOGGER.log(Level.SEVERE, XML_ERROR_STRING, e);
			ErrorShower
					.showErrorDialog(null, XML_ERROR_STRING + ' ' + filename);
		} catch (final FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, filename + NOT_FOUND_ERROR, e);
			ErrorShower.showErrorDialog(null, "File " + filename
					+ NOT_FOUND_ERROR);
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, XML_ERROR_STRING, e);
			ErrorShower.showErrorDialog(null, "I/O error reading " + filename);
		} catch (final SPFormatException e) {
			LOGGER.log(Level.SEVERE, INV_DATA_ERROR, e);
			ErrorShower.showErrorDialog(null, INV_DATA_ERROR);
		}
	}
}
