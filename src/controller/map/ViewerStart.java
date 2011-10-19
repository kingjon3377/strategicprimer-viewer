package controller.map;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.xml.stream.XMLStreamException;

import model.viewer.MapModel;
import view.map.main.MapFileFilter;
import view.map.main.ViewerFrame;
import view.util.ErrorShower;
import controller.map.simplexml.SPFormatException;
import controller.map.simplexml.SimpleXMLReader;

/**
 * A class to start the viewer, to reduce circular dependencies between packages.
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
	 * The viewer instance.
	 */
	private static ViewerFrame frame;
	/**
	 * Run the app.
	 * 
	 * @param args
	 *            Command-line arguments: args[0] is the map filename, others
	 *            are ignored. TODO: Add option handling.
	 * 
	 */
	public static void main(final String[] args) {
		// ESCA-JAVA0177:
		final String filename; // NOPMD // $codepro.audit.disable localDeclaration
		if (args.length == 0) {
			final JFileChooser chooser = new JFileChooser(".");
			chooser.setFileFilter(new MapFileFilter());
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				filename = chooser.getSelectedFile().getPath();
			} else {
				return;
			}
		} else {
			filename = args[0];
		}
		try {
			final MapModel model = new MapModel(new SimpleXMLReader().readMap(filename));
			frame = new ViewerFrame(model);
			frame.setVisible(true);
			if (args.length > 1) {
				model.setSecondaryMap(new SimpleXMLReader().readMap(args[1]));
			}
		} catch (final XMLStreamException e) {
			LOGGER.log(Level.SEVERE, XML_ERROR_STRING, e);
			ErrorShower.showErrorDialog(null, XML_ERROR_STRING + ' ' + filename);
		} catch (final FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, filename + NOT_FOUND_ERROR, e);
			ErrorShower.showErrorDialog(null, "File " + filename + NOT_FOUND_ERROR);
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, XML_ERROR_STRING, e);
			ErrorShower.showErrorDialog(null, "I/O error reading " + filename);
		} catch (SPFormatException e) {
			LOGGER.log(Level.SEVERE, INV_DATA_ERROR, e);
			ErrorShower.showErrorDialog(null, INV_DATA_ERROR);
		}
	}
	/**
	 * @return the quasi-Singleton objects
	 */
	public static ViewerFrame getFrame() {
		return frame;
	}
}
