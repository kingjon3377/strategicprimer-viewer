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
import org.eclipse.jdt.annotation.Nullable;

/**
 * A driver to check whether player maps are subsets of the main map and display the results graphically.
 *
 * TODO: Unify with {@link SubsetCLI}, like the map-checker GUI
 */
public class SubsetGUI implements UtilityGUI {
	public SubsetGUI(final SPOptions options) {
		this.options = options;
		frame = new SubsetFrame(this);
		frame.setJMenuBar(SPMenu.forWindowContaining(frame.getContentPane(),
				SPMenu.createFileMenu(new UtilityMenuHandler(this, frame)::handleEvent, this),
				SPMenu.disabledMenu(SPMenu.createMapMenu(SubsetGUI::noop, this)),
				SPMenu.disabledMenu(SPMenu.createViewMenu(SubsetGUI::noop, this))));
		frame.addWindowListener(new WindowCloseListener(ignored -> frame.dispose()));
	}

	private final SPOptions options;

	@Override
	public SPOptions getOptions() {
		return options;
	}

	private final SubsetFrame frame;

	private static <T> void noop(final T t) { }

	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length == 0) {
			throw new IncorrectUsageException(SubsetGUIFactory.USAGE);
		}
		SwingUtilities.invokeLater(frame::showWindow);
		final String first = args[0];
		// Errors are reported via the GUI in loadMain(), then rethrown.
		frame.loadMain(Paths.get(first));
		Stream.of(args).skip(1).map(Paths::get).forEach(frame::testFile);
	}

	@Override
	public void open(final Path path) {
		if (frame != null) {
			frame.testFile(path);
		}
	}
}
