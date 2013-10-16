package controller.map.drivers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import util.EqualsAny;
import util.Pair;
import view.util.AppChooserFrame;
import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.misc.CLIHelper;
import controller.map.misc.WindowThread;

/**
 * A driver to start other drivers. At first it just starts one.
 *
 * TODO: make it possible to start multiple specified drivers.
 *
 * @author Jonathan Lovelace
 *
 */
public class AppStarter implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(true, "-p",
			"--app-starter", ParamCount.Many, "App Chooser",
			"Let the user choose an app to start, or handle options.",
			AppStarter.class);

	/**
	 * A map from options to the drivers they represent.
	 */
	private static final Map<String, Pair<ISPDriver, ISPDriver>> CACHE = new HashMap<>();

	/**
	 * @param driver a driver to add twice.
	 */
	private static void addChoice(final ISPDriver driver) {
		final DriverUsage usage = driver.usage();
		final Pair<ISPDriver, ISPDriver> pair = Pair.of(driver, driver);
		CACHE.put(usage.getShortOption(), pair);
		CACHE.put(usage.getLongOption(), pair);
	}

	/**
	 * If the two drivers don't have the same short and long options, or if both
	 * are or neither is graphical, logs a warning.
	 *
	 * @param one a first driver
	 * @param two a second driver
	 */
	private static void addChoice(final ISPDriver one, final ISPDriver two) {
		final DriverUsage oneUsage = one.usage();
		final DriverUsage twoUsage = two.usage();
		if (oneUsage.isGraphical() || !twoUsage.isGraphical()) {
			LOGGER.warning("Two-arg addChoice didn't match non-graphical / graphical pair");
		} else if (!oneUsage.getShortOption().equals(twoUsage.getShortOption())
				|| !oneUsage.getLongOption().equals(twoUsage.getLongOption())) {
			LOGGER.warning("Two-arg addChoice called but options of args don't match");
		}
		final Pair<ISPDriver, ISPDriver> pair = Pair.of(one, two);
		CACHE.put(oneUsage.getShortOption(), pair);
		CACHE.put(oneUsage.getLongOption(), pair);
	}

	static {
		addChoice(new QueryCLI(), new ViewerStart());
		// FIXME: Write a CLI to _automate_ advancement
		addChoice(new AdvancementStart());
		addChoice(new WorkerReportDriver(), new WorkerStart());
		addChoice(new ExplorationCLIDriver(), new ExplorationGUI());
		addChoice(new ReaderComparator(), new DrawHelperComparator());
		addChoice(new MapChecker(), new MapCheckerGUI());
		addChoice(new SubsetDriver(), new SubsetGUIDriver());
		addChoice(new EchoDriver());
		// FIXME: Write a GUI for the duplicate feature remover
		addChoice(new DuplicateFixtureRemover());
		// FIXME: Write a trapping (and hunting, etc.) GUI.
		addChoice(new TrapModelDriver());
		addChoice(new AppStarter());
		// FIXME: Write a stat-generating/stat-entering GUI.
		addChoice(new StatGeneratingCLIDriver());
	}

	/**
	 * Start the driver, and then start the specified other driver.
	 *
	 * @param args command-line arguments
	 * @throws DriverFailedException on fatal error.
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		final List<String> options = new ArrayList<>();
		final List<String> others = new ArrayList<>();
		for (final String arg : args) {
			if (arg.trim().charAt(0) == '-') {
				options.add(arg);
			} else {
				others.add(arg);
			}
		}
		// FIXME: We assume no driver uses options.
		boolean gui = true;
		Pair<ISPDriver, ISPDriver> drivers = null;
		for (final String option : options) {
			if (EqualsAny.equalsAny(option, "-g", "--gui")) {
				gui = true;
			} else if (EqualsAny.equalsAny(option, "-c", "--cli")) {
				gui = false;
			} else if (CACHE.containsKey(option.toLowerCase(Locale.ENGLISH))) {
				drivers = CACHE.get(option.toLowerCase(Locale.ENGLISH));
			}
		}
		if (drivers == null) {
			startChooser(gui, others);
		} else {
			final ISPDriver driver = gui ? drivers.second() : drivers.first();
			startChosenDriver(driver, others);
		}
	}

	/**
	 * Start the app-chooser window.
	 *
	 * @param gui whether to show the GUI chooser (or a CLI list)
	 * @param others the parameters to pass to the chosen driver
	 * @throws DriverFailedException if the chosen driver fails
	 */
	private static void startChooser(final boolean gui,
			final List<String> others) throws DriverFailedException {
		if (gui) {
			SwingUtilities.invokeLater(new WindowThread(new AppChooserFrame(
					others)));
		} else {
			final List<ISPDriver> drivers = new ArrayList<>();
			for (final Pair<ISPDriver, ISPDriver> pair : CACHE.values()) {
				if (!drivers.contains(pair.first())) {
					drivers.add(pair.first());
				}
			}
			try {
				startChosenDriver(drivers.get(new CLIHelper().chooseFromList(
						drivers, "CLI apps available:",
						"No applications available", "App to start: ", true)),
						others);
			} catch (final IOException except) {
				LOGGER.log(Level.SEVERE,
						"I/O error prompting user for app to start", except);
				return;
			}
		}
	}

	/**
	 * Start a driver.
	 *
	 * @param driver the driver to start
	 * @param params non-option parameters
	 * @throws DriverFailedException on fatal error
	 */
	private static void startChosenDriver(final ISPDriver driver, // NOPMD
			final List<String> params) throws DriverFailedException {
		driver.startDriver(params.toArray(new String[params.size()]));
	}

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(AppStarter.class
			.getName());

	/**
	 * Entry point: start the driver.
	 *
	 * @param args command-line arguments
	 */
	public static void main(final String[] args) {
		try {
			new AppStarter().startDriver(args);
		} catch (final DriverFailedException except) {
			LOGGER.log(Level.SEVERE, except.getLocalizedMessage(),
					except.getCause());
		}
	}

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE_OBJ;
	}

	/**
	 * @return what to call the driver in a CLI list.
	 */
	@Override
	public String getName() {
		return USAGE_OBJ.getShortDescription();
	}

	/**
	 * @param nomen ignored
	 */
	@Override
	public void setName(final String nomen) {
		throw new IllegalStateException("Can't rename a driver");
	}
}
