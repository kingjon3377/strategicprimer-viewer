package controller.map.drivers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.EqualsAny;
import util.Pair;

/**
 * A driver to start other drivers. At first it just starts one. TODO: make it
 * possible to start multiple specified drivers.
 *
 * @author Jonathan Lovelace
 *
 */
public class AppStarter implements ISPDriver {
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
	static {
		addChoice(QueryCLI.class, ViewerStart.class, "-m", "--map");
		// FIXME: Write a CLI to *automate* advancement
		addChoice(AdvancementStart.class, AdvancementStart.class, "-a", "--adv");
		// FIXME: Write a CLI to print a report of a player's workers
		// FIXME: Write a proper worker-management GUI
		addChoice(AdvancementStart.class, AdvancementStart.class, "-w", "--worker");
		// FIXME: Write an ExplorationGUI
		addChoice(ExplorationCLI.class, ViewerStart.class, "-x", "--explore");
		addChoice(ReaderComparator.class, DrawHelperComparator.class, "-t", "--test");
		// FIXME: Write a GUI for the map-checker.
		addChoice(MapChecker.class, MapChecker.class, "-k", "--check");
		// FIXME: Write a GUI for the subset-driver
		addChoice(SubsetDriver.class, SubsetDriver.class, "-s", "--subset");
		addChoice(EchoDriver.class, EchoDriver.class, "-e", "--echo");
		// FIXME: Write a GUI for the duplicate feature remover
		addChoice(DuplicateFixtureRemover.class, DuplicateFixtureRemover.class, "-d", "--dupl");
		// FIXME: Write a GUI fo the map-updater
		addChoice(MapUpdater.class, MapUpdater.class, "-u", "--update");
		addChoice(AppStarter.class, AppStarter.class, "-p", "--app-starter");

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
			// FIXME: Implement showing a chooser window
		} else {
			final Class<? extends ISPDriver> driver = gui ? drivers.second() : drivers.first();
			startDriver(driver, others);
		}
	}
	/**
	 * Start a driver.
	 * @param driver the driver to start
	 * @param params non-option parameters
	 * @throws DriverFailedException on fatal error
	 */
	private static void startDriver(final Class<? extends ISPDriver> driver, // NOPMD
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
}
