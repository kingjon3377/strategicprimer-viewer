package drivers.gui.common;

import lovelace.util.MalformedXMLException;
import lovelace.util.MissingFileException;
import java.io.IOException;
import common.xmlio.SPFormatException;
import java.awt.Dimension;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.TransferHandler;
import javax.swing.SwingUtilities;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import drivers.common.ISPDriver;
import drivers.common.ModelDriver;
import drivers.common.MapChangeListener;

/**
 * A {@link TransferHandler} to allow SP apps to accept dropped files.
 */
/* package */ class FileDropHandler extends TransferHandler {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(FileDropHandler.class.getName());

	public FileDropHandler(SPFrame app) {
		this.app = app;
	}

	private SPFrame app;

	@Override
	public boolean canImport(TransferSupport support) {
		return support.isDrop() && app != null && app.supportsDroppedFiles() &&
			support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
	}

	@Override
	public boolean importData(TransferSupport support) {
		if (!canImport(support)) {
			return false;
		}
		List<File> payload;
		try {
			payload = (List<File>) support.getTransferable().getTransferData(
				DataFlavor.javaFileListFlavor);
		} catch (Exception except) {
			LOGGER.log(Level.WARNING, "Caught an exception trying to unmarshall dropped files",
				except);
			return false;
		}
		for (File file : payload) {
			try {
				app.acceptDroppedFile(file.toPath());
			} catch (SPFormatException except) {
				LOGGER.log(Level.WARNING, "SP format error in dropped file", except);
				return false;
			} catch (MissingFileException except) { // FIXME: Or more-standard equivalents
				LOGGER.log(Level.WARNING, "Dropped file not actually present", except);
				return false;
			} catch (MalformedXMLException except) {
				LOGGER.log(Level.WARNING, "Malformed XML in dropped file", except);
				return false;
			} catch (IOException except) {
				LOGGER.log(Level.WARNING, "I/O error reading dropped file", except);
				return false;
			}
		}
		return true;
	}
}
