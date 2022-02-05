package utility.subset;

import java.nio.file.NoSuchFileException;
import java.io.FileNotFoundException;
import lovelace.util.MissingFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.io.IOException;

import javax.swing.SwingUtilities;

import lovelace.util.MalformedXMLException;

import common.xmlio.SPFormatException;

import drivers.common.DriverFailedException;
import drivers.common.SPOptions;
import drivers.common.IncorrectUsageException;
import drivers.common.UtilityGUI;

import drivers.common.cli.ICLIHelper;

import drivers.gui.common.WindowCloseListener;
import drivers.gui.common.UtilityMenuHandler;
import drivers.gui.common.SPMenu;

/**
 * A driver to check whether player maps are subsets of the main map and display the results graphically.
 *
 * TODO: Unify with {@link SubsetCLI}, like the map-checker GUI
 */
public class SubsetGUI implements UtilityGUI {
	public SubsetGUI(final ICLIHelper cli, final SPOptions options) {
		this.cli = cli;
		this.options = options;
	}

	private final ICLIHelper cli;
	private final SPOptions options;

	@Override
	public SPOptions getOptions() {
		return options;
	}

	private SubsetFrame frame;

	private boolean initialized = false;

	private static <T> void noop(final T t) { }

	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length == 0) {
			throw new IncorrectUsageException(SubsetGUIFactory.USAGE);
		}
		if (!initialized) { // FIXME: Move this initialization into the constructor, since Java is less strict about 'this' usage in initializers.
			initialized = true;
			frame = new SubsetFrame(this);
			frame.setJMenuBar(SPMenu.forWindowContaining(frame.getContentPane(),
				SPMenu.createFileMenu(new UtilityMenuHandler(this, frame)::handleEvent, this),
				SPMenu.disabledMenu(SPMenu.createMapMenu(SubsetGUI::noop, this)),
				SPMenu.disabledMenu(SPMenu.createViewMenu(SubsetGUI::noop, this))));
			frame.addWindowListener(new WindowCloseListener(ignored -> frame.dispose()));
		}
		SwingUtilities.invokeLater(frame::showWindow);
		String first = args[0];
		try { // Errors are reported via the GUI in loadMain(), then rethrown.
			frame.loadMain(Paths.get(first));
		} catch (MissingFileException|NoSuchFileException|FileNotFoundException except) {
			throw new DriverFailedException(except, String.format("File %s not found", first));
		} catch (final IOException except) {
			throw new DriverFailedException(except, "I/O error loading main map " + first);
		} catch (final MalformedXMLException except) {
			throw new DriverFailedException(except, "Malformed XML in main map " + first);
		} catch (final SPFormatException except) {
			throw new DriverFailedException(except, "Invalid SP XML in main  map " + first);
		}
		Stream.of(args).skip(1).map(Paths::get).forEach(frame::testFile);
	}

	@Override
	public void open(final Path path) {
		frame.testFile(path);
	}
}
