package drivers.gui.common;

import org.jetbrains.annotations.Nullable;

import lovelace.util.MalformedXMLException;
import lovelace.util.MissingFileException;
import java.io.IOException;
import common.xmlio.SPFormatException;
import lovelace.util.ThrowingConsumer;
import common.map.IMapNG;
import java.util.function.Consumer;
import java.awt.Dimension;
import java.nio.file.Paths;
import java.nio.file.Path;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.TransferHandler;
import javax.swing.SwingUtilities;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import drivers.common.ISPDriver;
import drivers.common.ModelDriver;
import drivers.common.MapChangeListener;

/**
 * An intermediate subclass of {@link JFrame} to take care of some common setup
 * things that can't be done in an interface.
 */
public class SPFrame extends JFrame implements ISPWindow {
	@FunctionalInterface
	public static interface IDroppedFileHandler {
		void accept(Path file) throws SPFormatException, IOException, MissingFileException,
			MalformedXMLException;
	}

	private final String windowTitle;
	private final ISPDriver driver;
	private final boolean supportsDroppedFiles;
	private final IDroppedFileHandler droppedFileHandler;
	private final String windowName;

	@Override
	public String getWindowName() {
		return windowName;
	}

	/**
	 * @param supportsDroppedFiles Whether this app supports having files dropped on it.
	 * @param windowName The name of the window, for use in customizing the About dialog
	 */
	public SPFrame(final String windowTitle, final ISPDriver driver, @Nullable final Dimension minSize,
	               final boolean supportsDroppedFiles, final IDroppedFileHandler droppedFileHandler,
	               final String windowName) {
		super(windowTitle);
		this.windowTitle = windowTitle;
		this.driver = driver;
		this.supportsDroppedFiles = supportsDroppedFiles;
		this.droppedFileHandler = droppedFileHandler;
		this.windowName = windowName;
		setTitle(refreshTitle());

		if (driver instanceof ModelDriver) {
			ModelDriver md = (ModelDriver) driver;
			md.getModel().addMapChangeListener(new MapChangeListener() {
				private void impl() {
					Path filename = md.getModel().getMap().getFilename();
					getRootPane().putClientProperty("Window.documentFile", filename);
					setTitle(refreshTitle());
				}

				@Override
				public void mapChanged() {
					SwingUtilities.invokeLater(this::impl);
				}

				@Override
				public void mapMetadataChanged() {
					SwingUtilities.invokeLater(this::impl);
				}
			});
		}
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (minSize != null) {
			setMinimumSize(minSize);
		}
		setTransferHandler(new FileDropHandler(this));
	}
	
	public boolean supportsDroppedFiles() {
		return supportsDroppedFiles;
	}

	public SPFrame(final String windowTitle, final ISPDriver driver, @Nullable final Dimension minSize,
	               final boolean supportsDroppedFiles, final IDroppedFileHandler droppedFileHandler) {
		this(windowTitle, driver, minSize, supportsDroppedFiles, droppedFileHandler, windowTitle);
	}

	public SPFrame(final String windowTitle, final ISPDriver driver, @Nullable final Dimension minSize,
	               final boolean supportsDroppedFiles) {
		this(windowTitle, driver, minSize, supportsDroppedFiles, p -> {});
	}

	public SPFrame(final String windowTitle, final ISPDriver driver, @Nullable final Dimension minSize) {
		this(windowTitle, driver, minSize, false);
	}

	public SPFrame(final String windowTitle, final ISPDriver driver) {
		this(windowTitle, driver, null);
	}

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(SPFrame.class.getName());

	private String refreshTitle() {
		if (driver instanceof ModelDriver &&
				((ModelDriver) driver).getModel().getMap().getFilename() != null) {
			IMapNG map = ((ModelDriver) driver).getModel().getMap();
			return String.format("%s%s | %s", map.isModified() ? "*" : "",
				map.getFilename(), windowTitle);
		} else {
			return windowTitle;
		}
	}

	/**
	 * Handle a dropped file.
	 */
	public void acceptDroppedFile(final Path file) throws SPFormatException, IOException,
			MissingFileException, MalformedXMLException { // TODO: Wrap MalformedXMLException (or whatever it wraps) in SPFormatException (subclass) in MapIOHelper or elsewhere, to reduce exception-declaration surface
		droppedFileHandler.accept(file);
	}

	public void showWindow() {
		setVisible(true);
	}
}
