package controller.map.misc;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.xml.stream.XMLStreamException;

import model.map.IMutableMapNG;
import model.map.MapNGAdapter;
import model.map.MapNGReverseAdapter;
import model.misc.IMultiMapModel;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;
import util.Pair;
import util.TypesafeLogger;
import util.Warning;
import view.util.ErrorShower;
import controller.map.formatexceptions.SPFormatException;

/**
 * An extension of the IOHandler class to handle I/O items dealing with
 * subordinate maps.
 *
 * @author Jonathan Lovelace
 *
 */
public class MultiIOHandler extends IOHandler {
	/**
	 * The multi-map model.
	 */
	private final IMultiMapModel model;

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(MultiIOHandler.class);

	/**
	 * Constructor.
	 *
	 * @param map the map model
	 * @param fchooser the file chooser
	 */
	public MultiIOHandler(final IMultiMapModel map, final JFileChooser fchooser) {
		super(map, fchooser);
		model = map;
	}

	/**
	 * Handle menu selections. Anything not handled here is passed to the
	 * superclass's implementation.
	 *
	 * @param event the event to handle
	 */
	@Override
	public final void actionPerformed(@Nullable final ActionEvent event) {
		if (event != null) {
			final Component source = componentOrNull(event.getSource());
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
	 * @param obj an object which may be a Component
	 * @return it if it is, or null
	 */
	@Nullable
	private static Component componentOrNull(@Nullable final Object obj) {
		if (obj instanceof Component) {
			return (Component) obj; // NOPMD
		} else {
			return null;
		}
	}
	/**
	 * Save all maps to the filenames they were loaded from.
	 *
	 * @param source the source of the event that triggered this. May be null if
	 *        it was not a component.
	 */
	private void saveAll(@Nullable final Component source) {
		final MapReaderAdapter adapter = new MapReaderAdapter();
		for (final Pair<IMutableMapNG, File> pair : model.getAllMaps()) {
			try {
				adapter.write(pair.second(), new MapNGReverseAdapter(pair.first()));
			} catch (final IOException e) {
				ErrorShower.showErrorDialog(source,
						"I/O error writing to file " + pair.second());
				LOGGER.log(Level.SEVERE, "I/O error writing XML", e);
			}
		}
	}

	/**
	 * Handle the 'load secondary map' menu item.
	 *
	 * @param source the component to attach the dialog box to. May be null.
	 */
	private void handleSecondaryLoadMenu(@Nullable final Component source) {
		if (chooser.showOpenDialog(source) == JFileChooser.APPROVE_OPTION) {
			final File file = chooser.getSelectedFile();
			if (file == null) {
				return;
			}
			try {
				model.addSubordinateMap(
						new MapNGAdapter(readMap(file, Warning.INSTANCE)), file);
			} catch (final IOException e) {
				handleError(e, NullCleaner.valueOrDefault(file.getPath(),
						"a null path"), source);
			} catch (final SPFormatException e) {
				handleError(e, NullCleaner.valueOrDefault(file.getPath(),
						"a null path"), source);
			} catch (final XMLStreamException e) {
				handleError(e, NullCleaner.valueOrDefault(file.getPath(),
						"a null path"), source);
			}
		}
	}
}
