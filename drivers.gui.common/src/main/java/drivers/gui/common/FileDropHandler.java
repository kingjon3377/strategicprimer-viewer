package drivers.gui.common;

import lovelace.util.MalformedXMLException;
import lovelace.util.MissingFileException;
import java.io.IOException;
import common.xmlio.SPFormatException;

import javax.swing.TransferHandler;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * A {@link TransferHandler} to allow SP apps to accept dropped files.
 */
/* package */ class FileDropHandler extends TransferHandler {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(FileDropHandler.class.getName());

	public FileDropHandler(final SPFrame app) {
		this.app = app;
	}

	private final SPFrame app;

	@Override
	public boolean canImport(final TransferSupport support) {
		return support.isDrop() && app.supportsDroppedFiles() &&
			support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
	}

	@Override
	public boolean importData(final TransferSupport support) {
		if (!canImport(support)) {
			return false;
		}
		final List<File> payload;
		try {
			payload = (List<File>) support.getTransferable().getTransferData(
				DataFlavor.javaFileListFlavor);
		} catch (final Exception except) {
			LOGGER.log(Level.WARNING, "Caught an exception trying to unmarshall dropped files",
				except);
			return false;
		}
		for (final File file : payload) {
			try {
				app.acceptDroppedFile(file.toPath());
			} catch (final SPFormatException except) {
				LOGGER.log(Level.WARNING, "SP format error in dropped file", except);
				return false;
			} catch (final MissingFileException except) { // FIXME: Or more-standard equivalents
				LOGGER.log(Level.WARNING, "Dropped file not actually present", except);
				return false;
			} catch (final MalformedXMLException except) {
				LOGGER.log(Level.WARNING, "Malformed XML in dropped file", except);
				return false;
			} catch (final IOException except) {
				LOGGER.log(Level.WARNING, "I/O error reading dropped file", except);
				return false;
			}
		}
		return true;
	}
}
