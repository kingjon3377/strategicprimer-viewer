package controller.map.drivers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import controller.map.misc.DriverUsage;
import controller.map.misc.DriverUsage.ParamCount;

import util.EqualsAny;
import util.Pair;
import view.util.AppChooserFrame;

/**
 * A driver to start other drivers. At first it just starts one. TODO: make it
 * possible to start multiple specified drivers.
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
	 * A pair of Class objects.
	 */
	private static final class ClassPair extends Pair<Class<? extends ISPDriver>, Class<? extends ISPDriver>> {
		/**
		 * @param first the first class in the pair
		 * @param second the second class in the pair
		 */
		ClassPair(final Class<? extends ISPDriver> first, final Class<? extends ISPDriver> second) {
			super(first, second);
		}
	}
	/**
	 * A map from options to the drivers they represent.
	 */
	private static final Map<String, ClassPair> CACHE = new HashMap<String, ClassPair>();
	/**
	 * @param first the driver to use if --cli
	 * @param second the driveer to use if --gui
	 * @param shrt its short option
	 * @param lng its long option
	 */
	private static void addChoice(final Class<? extends ISPDriver> first,
			final Class<? extends ISPDriver> second,
			final String shrt, final String lng) {
		final ClassPair pair = new ClassPair(first, second);
		CACHE.put(shrt, pair);
		CACHE.put(lng, pair);
	}
	/**
	 * @param driver a driver to add twice.
	 */
	private static void addChoice(final ISPDriver driver) {
		final ClassPair pair = new ClassPair(driver.getClass(), driver.getClass());
		final DriverUsage usage = driver.usage();
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
		final ClassPair pair = new ClassPair(one.getClass(), two.getClass());
		final DriverUsage oneUsage = one.usage();
		final DriverUsage twoUsage = two.usage();
		if (oneUsage.isGraphical() || !twoUsage.isGraphical()) {
			LOGGER.warning("Two-arg addChoice didn't match non-graphical / graphical pair");
		} else if (!oneUsage.getShortOption().equals(twoUsage.getShortOption())
				|| !oneUsage.getLongOption().equals(twoUsage.getLongOption())) {
	LOGGER.warning("Two-arg addChoice called but options of args don't match");
		}
		CACHE.put(oneUsage.getShortOption(), pair);
		CACHE.put(oneUsage.getLongOption(), pair);
	}
	static {
		addChoice(new QueryCLI(), new ViewerStart());
		// FIXME: Write a CLI to _automate_ advancement
		addChoice(new AdvancementStart());
		// FIXME: Write a CLI to print a report of a player's workers
		// FIXME: Write a proper worker-management GUI
		// We leave this as the old-style addChoice because here it's a
		// placeholder for a proper worker GUI
		addChoice(AdvancementStart.class, AdvancementStart.class, "-w", "--worker");
		// FIXME: Write an ExplorationGUI
		// Similarly, we leave this as an old-style AddChoice because
		// ViewerStart here is a placeholder for an ExplorationGUI
		addChoice(ExplorationCLIDriver.class, ViewerStart.class, "-x", "--explore");
		addChoice(new ReaderComparator(), new DrawHelperComparator());
		addChoice(new MapChecker(), new MapCheckerGUI());
		addChoice(new SubsetDriver(), new SubsetGUIDriver());
		addChoice(new EchoDriver());
		// FIXME: Write a GUI for the duplicate feature remover
		addChoice(new DuplicateFixtureRemover());
		addChoice(new AppStarter());
	}
	/**
	 * Start the driver, and then start the specified other driver.
	 * @param args command-line arguments
	 * @throws DriverFailedException on fatal error.
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		final List<String> options = new ArrayList<String>();
		final List<String> others = new ArrayList<String>();
		for (final String arg : args) {
			if (arg.trim().charAt(0) == '-') {
				options.add(arg);
			} else {
				others.add(arg);
			}
		}
		// FIXME: We assume no driver uses options.
		boolean gui = true;
		ClassPair drivers = null;
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
			startChooser(others);
		} else {
			final Class<? extends ISPDriver> driver = gui ? drivers.second() : drivers.first();
			startChosenDriver(driver, others);
		}
	}
	/**
	 * Start the app-chooser window.
	 * @param others the parameters to pass to the chosen driver
	 */
	private static void startChooser(final List<String> others) {
		// TODO: CLI version when --cli
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new AppChooserFrame(others).setVisible(true);
			}
		});
	}
	/**
	 * Start a driver.
	 * @param driver the driver to start
	 * @param params non-option parameters
	 * @throws DriverFailedException on fatal error
	 */
	private static void startChosenDriver(final Class<? extends ISPDriver> driver, // NOPMD
			final List<String> params) throws DriverFailedException {
		try {
			driver.newInstance().startDriver(params.toArray(new String[params.size()]));
		} catch (InstantiationException except) {
			throw new DriverFailedException("Instantiation of proper driver failed", except);
		} catch (IllegalAccessException except) {
			throw new DriverFailedException("Instantiation of proper driver failed", except);
		}
	}
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(AppStarter.class.getName());
	/**
	 * Entry point: start the driver.
	 * @param args command-line arguments
	 */
	public static void main(final String[] args) {
		try {
			new AppStarter().startDriver(args);
		} catch (final DriverFailedException except) {
			LOGGER.log(Level.SEVERE, except.getLocalizedMessage(), except.getCause());
		}
	}
	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE_OBJ;
	}
}
