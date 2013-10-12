package controller.map.misc;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import model.misc.IMultiMapModel;

import org.eclipse.jdt.annotation.Nullable;

import util.Pair;
import util.Warning;
import view.util.ErrorShower;
import controller.map.formatexceptions.SPFormatException;

/**
 * An extension of the IOHandler class to handle I/O items dealing with subordinate maps.
 * @author Jonathan Lovelace
 *
 */
public class MultiIOHandler extends IOHandler {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(MultiIOHandler.class
			.getName());
	/**
	 * Constructor.
	 * @param map the map model
	 * @param fchooser the file chooser
	 */
	public MultiIOHandler(final IMultiMapModel map, final JFileChooser fchooser) {
		super(map, fchooser);
		model = map;
	}
	/**
	 * The multi-map model.
	 */
	private final IMultiMapModel model;
	/**
	 * Handle menu selections. Anything not handled here is passed to the superclass's implementation.
	 * @param event the event to handle
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent event) {
		if (event != null) {
			final Component source = event.getSource() instanceof Component ? (Component) event
					.getSource() : null; // NOPMD
			if ("Load secondary".equalsIgnoreCase(event.getActionCommand())) {
				handleSecondaryLoadMenu(source);
			} else if ("Save All".equalsIgnoreCase(event.getActionCommand())) {
				saveAll(source);
			} else {
				super.actionPerformed(event);
			}
		}
	}
	/**
	 * Save all maps to the filenames they were loaded from.
	 * @param source the source of the event that triggered this. May be null if it was not a component.
	 */
	private void saveAll(@Nullable final Component source) {
		final MapReaderAdapter adapter = new MapReaderAdapter();
		for (final Pair<IMap, String> pair : model.getAllMaps()) {
			try {
				adapter.write(pair.second(), pair.first());
			} catch (final IOException e) {
				ErrorShower.showErrorDialog(source,
						"I/O error writing to file "
								+ model.getMapFilename());
				LOGGER.log(Level.SEVERE, "I/O error writing XML", e);
			}
		}
	}
	/**
	 * Handle the 'load secondary map' menu item.
	 * @param source the component to attach the dialog box to. May be null.
	 */
	private void handleSecondaryLoadMenu(@Nullable final Component source) {
		if (chooser.showOpenDialog(source) == JFileChooser.APPROVE_OPTION) {
			final String filename = chooser.getSelectedFile().getPath();
			try {
				model.addSubordinateMap(readMap(filename, Warning.INSTANCE), filename);
			} catch (final IOException e) {
				handleError(e, filename, source);
			} catch (final SPFormatException e) {
				handleError(e, filename, source);
			} catch (final XMLStreamException e) {
				handleError(e, filename, source);
			}
		}
	}
}
