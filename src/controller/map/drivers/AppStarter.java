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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.misc.CLIHelper;
import controller.map.misc.WindowThread;
import util.EqualsAny;
import util.NullCleaner;
import util.Pair;
import util.TypesafeLogger;
import view.util.AppChooserFrame;
import view.util.ErrorShower;

/**
 * A driver to start other drivers. At first it just starts one.
 *
 * TODO: make it possible to start multiple specified drivers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
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
	private static final Map<String, Pair<ISPDriver, ISPDriver>> CACHE =
			new HashMap<>();

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
			LOGGER.warning("Two-arg addChoice expects non-GUI / GUI pair");
		} else if (!oneUsage.getShortOption().equals(twoUsage.getShortOption())
				|| !oneUsage.getLongOption().equals(twoUsage.getLongOption())) {
			LOGGER.warning("In two-arg addChoice, args' options should match");
		}
		final Pair<ISPDriver, ISPDriver> pair = Pair.of(one, two);
		CACHE.put(oneUsage.getShortOption(), pair);
		CACHE.put(oneUsage.getLongOption(), pair);
	}

	static {
		addChoice(new QueryCLI(), new ViewerStart());
		addChoice(new AdvancementCLIDriver(), new AdvancementStart());
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
		// FIXME: Write a GUI for the map-expanding driver
		addChoice(new ExpansionDriver());
		// TODO: Add a GUI equivalent of the MapPopulatorDriver
		addChoice(new MapPopulatorDriver());
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
		// FIXME: To reduce calculated complexity and fix the null-object
		// pattern here, make a driver class for CLI driver choosing, and make a
		// Pair of it and the AppChooserFrame be the default.
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
		final boolean localGui = gui;
		final Logger lgr = LOGGER;
		if (drivers == null) {
			SwingUtilities.invokeLater(() -> {
				try {
					startChooser(localGui, others);
				} catch (DriverFailedException e) {
					final String message = e.getMessage();
					assert message != null;
					lgr.log(Level.SEVERE, message, e.getCause());
					ErrorShower.showErrorDialog(null, message);
				}
			});
		} else if (gui) {
			final ISPDriver driver = drivers.second();
			SwingUtilities.invokeLater(() -> {
				try {
					startChosenDriver(driver, others);
				} catch (DriverFailedException e) {
					final String message = e.getMessage();
					assert message != null;
					lgr.log(Level.SEVERE, message, e.getCause());
					ErrorShower.showErrorDialog(null, message);
				}
			});
		} else {
			startChosenDriver(drivers.first(), others);
		}
	}

	/**
	 * Start the app-chooser window.
	 *
	 * @param gui whether to show the GUI chooser (or a CLI list)
	 * @param others the parameters to pass to the chosen driver
	 * @throws DriverFailedException if the chosen driver fails
	 */
	protected static void startChooser(final boolean gui,
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
				startChosenDriver(NullCleaner.assertNotNull(drivers
						.get(new CLIHelper().chooseFromList(drivers,
								"CLI apps available:",
								"No applications available", "App to start: ",
								true))), others);
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
	protected static void startChosenDriver(final ISPDriver driver, // NOPMD
			final List<String> params) throws DriverFailedException {
		driver.startDriver(NullCleaner.assertNotNullArray(params.toArray(new String[params.size()])));
	}

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(AppStarter.class);

	/**
	 * Entry point: start the driver.
	 *
	 * @param args command-line arguments
	 */
	public static void main(final String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException except) {
			LOGGER.log(Level.SEVERE,
					"Failed to switch to system look-and-feel", except);
		}
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
		return usage().getShortDescription();
	}

	/**
	 * @param nomen ignored
	 */
	@Override
	public void setName(final String nomen) {
		throw new IllegalStateException("Can't rename a driver");
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "AppStarter";
	}
}
