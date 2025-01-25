package drivers.gui.common;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.nio.file.NoSuchFileException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

import common.xmlio.SPFormatException;

import javax.swing.TransferHandler;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.List;

import lovelace.util.LovelaceLogger;

/**
 * A {@link TransferHandler} to allow SP apps to accept dropped files.
 */
/* package */ final class FileDropHandler extends TransferHandler {
	@Serial
	private static final long serialVersionUID = 1L;

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
			// this is in fact checked in canImport() ... at least that it is a List<?>.
			//noinspection unchecked
			payload = (List<File>) support.getTransferable().getTransferData(
					DataFlavor.javaFileListFlavor);
		} catch (final UnsupportedFlavorException|IOException except) {
			LovelaceLogger.warning(except, "Caught an exception trying to unmarshall dropped files");
			return false;
		}
		for (final File file : payload) {
			try {
				app.acceptDroppedFile(file.toPath());
			} catch (final SPFormatException except) {
				LovelaceLogger.warning(except, "SP format error in dropped file");
				return false;
			} catch (final NoSuchFileException except) {
				LovelaceLogger.warning(except, "Dropped file not actually present");
				return false;
			} catch (final XMLStreamException except) {
				LovelaceLogger.warning(except, "Malformed XML in dropped file");
				return false;
			} catch (final IOException except) {
				LovelaceLogger.warning(except, "I/O error reading dropped file");
				return false;
			}
		}
		return true;
	}

	@Serial
	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		throw new NotSerializableException("drivers.gui.common.FileDropHandler");
	}

	@Serial
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("drivers.gui.common.FileDropHandler");
	}
}
