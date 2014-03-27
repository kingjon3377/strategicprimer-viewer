package controller.map.misc;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import model.map.MapView;
import model.map.SPMap;
import model.misc.IDriverModel;
import model.viewer.ViewerModel;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;
import util.TypesafeLogger;
import util.Warning;
import view.map.main.ViewerFrame;
import view.util.ErrorShower;
import controller.map.formatexceptions.SPFormatException;

/**
 * An ActionListener to dispatch file I/O.
 *
 * @author Jonathan Lovelace
 *
 */
public class IOHandler implements ActionListener {
	/**
	 * Error message fragment when file not found.
	 */
	protected static final String NOT_FOUND_ERROR = " not found";
	/**
	 * Error message when the map contains invalid data.
	 */
	protected static final String INV_DATA_ERROR = "Map contained invalid data";
	/**
	 * An error message refactored from at least four uses.
	 */
	protected static final String XML_ERROR_STRING = "Error reading XML file";
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(IOHandler.class);
	/**
	 * File chooser.
	 */
	protected final JFileChooser chooser;

	/**
	 * The map model, which needs to be told about newly loaded maps and holds
	 * maps to be saved.
	 */
	private final IDriverModel model;

	/**
	 * Handle the "load" menu item.
	 *
	 * @param source the source of the event. May be null, since JFileChooser
	 *        doesn't seem to care
	 */
	private void handleLoadMenu(@Nullable final Component source) {
		if (chooser.showOpenDialog(source) == JFileChooser.APPROVE_OPTION) {
			final String filename =
					NullCleaner.assertNotNull(chooser.getSelectedFile()
							.getPath());
			// ESCA-JAVA0166:
			try {
				model.setMap(readMap(filename, Warning.INSTANCE), filename);
			} catch (IOException | SPFormatException | XMLStreamException e) {
				handleError(e, filename, source);
			}
		}
	}

	/**
	 * Handle menu selections.
	 *
	 * @param event the event to handle
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent event) {
		if (event != null) { // it wouldn't be @Nullable except that the JDK
								// isn't annotated
			final Component source = eventSource(event.getSource());
			if ("Load".equals(event.getActionCommand())) {
				handleLoadMenu(source);
			} else if ("Save".equals(event.getActionCommand())) {
				saveMap(source);
			} else if ("Save As".equals(event.getActionCommand())) {
				saveMapAs(model.getMap(), source);
			} else if ("New".equals(event.getActionCommand())) {
				startNewViewerWindow();
			}
		}
	}
	/**
	 * @param obj an object
	 * @return it if it's a component, or null
	 */
	@Nullable
	private static Component eventSource(@Nullable final Object obj) {
		if (obj instanceof Component) {
			return (Component) obj; // NOPMD
		} else {
			return null;
		}
	}
	/**
	 * Start a new viewer window with a blank map of the same size as the
	 * model's current map.
	 */
	private void startNewViewerWindow() {
		SwingUtilities.invokeLater(new WindowThread(new ViewerFrame(
				new ViewerModel(new MapView(
						new SPMap(model.getMapDimensions()), 0, model.getMap()
								.getCurrentTurn()), ""), this)));
	}

	/**
	 * Constructor.
	 *
	 * @param map the map model
	 * @param fchooser the file chooser
	 */
	public IOHandler(final IDriverModel map, final JFileChooser fchooser) {
		model = map;
		chooser = fchooser;
	}

	/**
	 * Display an appropriate error message.
	 *
	 * @param except an Exception
	 * @param filename the file we were trying to process
	 * @param source the component to use as the parent of the error dialog. May
	 *        be null.
	 */
	protected static void handleError(final Exception except,
			final String filename, @Nullable final Component source) {
		// ESCA-JAVA0177:
		String msg;
		if (except instanceof XMLStreamException) {
			msg = XML_ERROR_STRING + ' ' + filename;
		} else if (except instanceof FileNotFoundException) {
			msg = "File " + filename + NOT_FOUND_ERROR;
		} else if (except instanceof IOException) {
			msg = "I/O error reading file " + filename;
		} else if (except instanceof SPFormatException) {
			msg = INV_DATA_ERROR + " in file " + filename;
		} else {
			throw new IllegalStateException("Unknown exception type", except);
		}
		LOGGER.log(Level.SEVERE, msg, except);
		ErrorShower.showErrorDialog(source, msg);
	}

	/**
	 * Save a map to the filename it was loaded from.
	 *
	 * @param source the source of the event that triggered this. May be null if
	 *        it wasn't a Component.
	 */
	private void saveMap(@Nullable final Component source) {
		try {
			new MapReaderAdapter()
					.write(model.getMapFilename(), model.getMap());
		} catch (final IOException e) {
			ErrorShower.showErrorDialog(source, "I/O error writing to file "
					+ model.getMapFilename());
			LOGGER.log(Level.SEVERE, "I/O error writing XML", e);
		}
	}

	/**
	 * Save a map.
	 *
	 * @param map the map to save.
	 * @param source the source of the event. May be null if the source wasn't a
	 *        component.
	 */
	private void saveMapAs(final IMap map, @Nullable final Component source) {
		if (chooser.showSaveDialog(source) == JFileChooser.APPROVE_OPTION) {
			final String filename =
					NullCleaner.assertNotNull(chooser.getSelectedFile()
							.getPath());
			try {
				new MapReaderAdapter().write(filename, map);
			} catch (final IOException e) {
				ErrorShower.showErrorDialog(source,
						"I/O error writing to file "
								+ filename);
				LOGGER.log(Level.SEVERE, "I/O error writing XML", e);
			}
		}
	}

	/**
	 * @param filename a file to load a map from
	 * @param warner the Warning instance to use for warnings.
	 * @return the map in that file
	 * @throws IOException on other I/O error
	 * @throws XMLStreamException if the XML isn't well-formed
	 * @throws SPFormatException if the file contains invalid data
	 */
	protected static MapView readMap(final String filename, final Warning warner)
			throws IOException, XMLStreamException, SPFormatException {
		return new MapReaderAdapter().readMap(filename, warner);
	}

	/**
	 *
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "IOHandler";
	}
}
