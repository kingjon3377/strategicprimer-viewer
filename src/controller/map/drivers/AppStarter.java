package controller.map.drivers;

import controller.map.misc.CLIHelper;
import controller.map.misc.ICLIHelper;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import model.misc.IDriverModel;
import util.EqualsAny;
import util.Pair;
import util.TypesafeLogger;
import view.util.AppChooserFrame;
import view.util.DriverQuit;
import view.util.ErrorShower;

import static util.Ternary.ternary;

/**
 * A driver to start other drivers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class AppStarter implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final IDriverUsage USAGE =
			new DriverUsage(true, "-p", "--app-starter", ParamCount.AnyNumber,
								   "App Chooser",
								   "Let the user choose an app to start, or handle " +
										   "options.");

	/**
	 * A map from options to the drivers they represent.
	 */
	private static final Map<String, Pair<ISPDriver, ISPDriver>> CACHE =
			new HashMap<>();
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
												 .getLogger(AppStarter.class);

	static {
		addChoice(new WorkerReportDriver(), new ViewerStart());
		addChoice(new AdvancementCLIDriver(), new AdvancementStart());
		// FIXME: Add strategy-generating CLI app equivalent to worker-management GUI
		addChoice(new WorkerStart());
		addChoice(new ExplorationCLIDriver(), new ExplorationGUI());
		addChoice(new ReaderComparator(), new DrawHelperComparator());
		addChoice(new MapChecker(), new MapCheckerGUI());
		addChoice(new SubsetDriver(), new SubsetGUIDriver());
		// FIXME: Add GUI equivalent of QueryCLI
		addChoice(new QueryCLI());
		addChoice(new EchoDriver());
		// FIXME: Write a GUI for the duplicate feature remover
		addChoice(new DuplicateFixtureRemoverCLI());
		// FIXME: Write a trapping (and hunting, etc.) GUI.
		addChoice(new TrapModelDriver());
		addChoice(new AppStarter());
		// FIXME: Write a stat-generating/stat-entering GUI.
		addChoice(new StatGeneratingCLIDriver());
		// FIXME: Write a GUI for the map-expanding driver
		addChoice(new ExpansionDriver());
		// TODO: Add a GUI equivalent of the MapPopulatorDriver
		addChoice(new MapPopulatorDriver());
		addChoice(new ResourceAddingCLIDriver(), new ResourceAddingGUIDriver());
		// TODO: Add a GUI equivalent of TabularReportDriver
		addChoice(new TabularReportDriver());
		// TODO: Add a GUI to allow the user to visually explore a mine
		addChoice(new MiningCLI());
	}

	/**
	 * Add a driver to our collection that the user is allowed to choose from.
	 * @param driver a driver to add twice.
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private static void addChoice(final ISPDriver driver) {
		final IDriverUsage usage = driver.usage();
		final Pair<ISPDriver, ISPDriver> pair = Pair.of(driver, driver);
		CACHE.put(usage.getShortOption(), pair);
		CACHE.put(usage.getLongOption(), pair);
	}

	/**
	 * If the two drivers don't have the same short and long options, or if both are or
	 * neither is graphical, logs a warning.
	 *
	 * @param cliDriver a first driver
	 * @param guiDriver a second driver
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private static void addChoice(final ISPDriver cliDriver, final ISPDriver guiDriver) {
		final IDriverUsage cliUsage = cliDriver.usage();
		final IDriverUsage guiUsage = guiDriver.usage();
		if (cliUsage.isGraphical() || !guiUsage.isGraphical()) {
			//noinspection HardcodedFileSeparator
			LOGGER.warning("Two-arg addChoice expects non-GUI / GUI pair");
		} else if (!cliUsage.getShortOption().equals(guiUsage.getShortOption())
						   ||
						   !cliUsage.getLongOption().equals(guiUsage.getLongOption())) {
			LOGGER.warning("In two-arg addChoice, args' options should match");
		}
		final Pair<ISPDriver, ISPDriver> pair = Pair.of(cliDriver, guiDriver);
		CACHE.put(cliUsage.getShortOption(), pair);
		CACHE.put(cliUsage.getLongOption(), pair);
	}

	/**
	 * Start the app-chooser window.
	 *
	 * @param gui     whether to show the GUI chooser (or a CLI list)
	 * @param options the option parameters to pass to the chosen driver
	 * @param others  the non-option parameters to pass to the chosen driver
	 * @throws DriverFailedException if the chosen driver fails
	 */
	private static void startChooser(final boolean gui, final SPOptions options,
									 final List<String> others)
			throws DriverFailedException {
		if (gui) {
			SwingUtilities
					.invokeLater(
							() -> new AppChooserFrame(options, others).setVisible(true));
		} else {
			final List<ISPDriver> drivers =
					CACHE.values().stream().map(Pair::first).distinct()
							.collect(Collectors.toList());
			try (final ICLIHelper cli = new CLIHelper()) {
				startChosenDriver(drivers.get(
						cli.chooseFromList(drivers, "CLI apps available:",
								"No applications available", "App to start: ", true)),
						options, others);
			} catch (final IOException except) {
				//noinspection HardcodedFileSeparator
				LOGGER.log(Level.SEVERE,
						"I/O error prompting user for app to start", except);
			}
		}
	}

	/**
	 * Start a driver.
	 *
	 * @param driver  the driver to start
	 * @param options option parameters
	 * @param params  non-option parameters
	 * @throws DriverFailedException on fatal error
	 */
	private static void startChosenDriver(final ISPDriver driver, final SPOptions
																		  options,
										  final List<String> params)
			throws DriverFailedException {
		driver.startDriver(options, params.toArray(new String[params.size()]));
	}

	/**
	 * Start a GUI driver.
	 *
	 * @param driver  the driver to start
	 * @param options option parameters
	 * @param model   the driver model
	 * @throws DriverFailedException on fatal error
	 */
	private static void startChosenDriver(final ISPDriver driver, final SPOptions
																		  options,
										  final IDriverModel model)
			throws DriverFailedException {
		driver.startDriver(new CLIHelper(), options, model);
	}

	/**
	 * Start a GUI driver.
	 *
	 * @param driver  the driver to start
	 * @param options option parameters
	 * @param params  non-option parameters
	 */
	private static void startChosenGUIDriver(final ISPDriver driver,
											 final SPOptions options,
											 final List<String> params) {
		final Logger lgr = LOGGER;
		SwingUtilities.invokeLater(() -> {
			try {
				startChosenDriver(driver, options, params);
			} catch (final DriverFailedException e) {
				final String message = e.getMessage();
				lgr.log(Level.SEVERE, message, e.getCause());
				ErrorShower.showErrorDialog(null, message);
			}
		});
	}

	/**
	 * Entry point: start the driver.
	 *
	 * @param args command-line arguments
	 */
	@SuppressWarnings("AccessOfSystemProperties")
	public static void main(final String... args) {
		System.setProperty("com.apple.mrj.application.apple.menu.about.name",
				"SP Helpers");
		System.setProperty("apple.awt.application.name", "SP Helpers");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final ClassNotFoundException | InstantiationException
							   | IllegalAccessException |
										   UnsupportedLookAndFeelException except) {
			LOGGER.log(Level.SEVERE,
					"Failed to switch to system look-and-feel", except);
		}
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		try {
			new AppStarter().startDriver(new SPOptionsImpl(), args);
		} catch (final IncorrectUsageException except) {
			final IDriverUsage usage = except.getCorrectUsage();
			final String message = getUsageMessage(usage);
			System.err.println(message);
			DriverQuit.quit(1);
		} catch (final DriverFailedException except) {
			LOGGER.log(Level.SEVERE, except.getLocalizedMessage(),
					except.getCause());
			DriverQuit.quit(2);
		}
	}

	/**
	 * Create the usage message to print to stderr on incorrect usage.
	 * @param usage The object indicating proper usage.
	 * @return the usage message
	 */
	private static String getUsageMessage(final IDriverUsage usage) {
		final StringBuilder buff = new StringBuilder();
		try (final Formatter formatter = new Formatter(buff)) {
			formatter.format("Usage: java %s %s %s|%s",
					AppStarter.class.getCanonicalName(),
					ternary(usage.isGraphical(), "[-g|--gui]", "-c|--cli"),
					usage.getShortOption(), usage.getLongOption());
			for (final String option : usage.getSupportedOptions()) {
				formatter.format(" [%s]", option);
			}
			switch (usage.getParamsWanted()) {
			case None:
				break;
			case One:
				formatter.format(" %s", usage.getFirstParamDesc());
				break;
			case AtLeastOne:
				formatter.format(" %s [%s ...]", usage.getFirstParamDesc(),
						usage.getSubsequentParamDesc());
				break;
			case Two:
				formatter.format(" %s %s", usage.getFirstParamDesc(),
						usage.getSubsequentParamDesc());
				break;
			case AtLeastTwo:
				formatter.format(" %s %s [%s ...]", usage.getFirstParamDesc(),
						usage.getSubsequentParamDesc(), usage.getSubsequentParamDesc());
				break;
			case AnyNumber:
				formatter.format(" [%s ...]", usage.getSubsequentParamDesc());
				break;
			}
			formatter.format("%n%s", usage.getShortDescription());
		}
		return buff.toString();
	}

	/**
	 * Since there's no way of choosing which driver programmatically here, we present
	 * our choice to the user.
	 *
	 * @param cli the interface for user I/O
	 * @param options options to pass to the driver
	 * @param model   the driver model
	 * @throws DriverFailedException on driver failure
	 */
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final IDriverModel model)
			throws DriverFailedException {
		if (GraphicsEnvironment.isHeadless()) {
			final List<ISPDriver> drivers =
					CACHE.values().stream().map(Pair::first).distinct()
							.collect(Collectors.toList());
			try {
				startChosenDriver(drivers.get(
						cli.chooseFromList(drivers, "CLI apps available:",
								"No applications available", "App to start: ", true)),
						options, model);
			} catch (final IOException except) {
				//noinspection HardcodedFileSeparator
				LOGGER.log(Level.SEVERE,
						"I/O error prompting user for app to start", except);
				return;
			}
		} else {
			SwingUtilities.invokeLater(
					() -> new AppChooserFrame(model, options).setVisible(true));
		}
	}

	/**
	 * Start the driver, and then start the specified other driver.
	 *
	 * @param cli the interface for user I/O
	 * @param options options to pass to the driver.
	 * @param args    command-line arguments
	 * @throws DriverFailedException on fatal error.
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final String... args)
			throws DriverFailedException {
		boolean gui = !GraphicsEnvironment.isHeadless();
		SPOptionsImpl currentOptions = new SPOptionsImpl(options);
		if (!currentOptions.hasOption("--gui")) {
			currentOptions.setOption("--gui", Boolean.toString(gui));
		}
		final List<String> others = new ArrayList<>();
		// FIXME: To reduce calculated complexity and fix the null-object
		// pattern here, make a driver class for CLI driver choosing, and make a
		// Pair of it and the AppChooserFrame be the default.
		Pair<ISPDriver, ISPDriver> drivers = null;
		for (final String arg : args) {
			if (EqualsAny.equalsAny(arg, "-g", "--gui")) {
				currentOptions.setOption("--gui", "true");
				gui = true;
			} else if (EqualsAny.equalsAny(arg, "-c", "--cli")) {
				currentOptions.setOption("--gui", "false");
				gui = false;
			} else if (arg.startsWith("--gui=")) {
				final String value = arg.substring(6);
				currentOptions.setOption("--gui", value);
				gui = Boolean.parseBoolean(value);
			} else if (arg.startsWith("-") && arg.contains("=")) {
				final String[] broken = arg.split("=", 2);
				currentOptions.setOption(broken[0], broken[1]);
			} else if (CACHE.containsKey(arg.toLowerCase(Locale.ENGLISH))) {
				if (drivers != null) {
					if (gui) {
						startChosenGUIDriver(drivers.second(), currentOptions,
								new ArrayList<>(others));
					} else {
						startChosenDriver(drivers.first(), currentOptions,
								new ArrayList<>(others));
					}
					currentOptions = new SPOptionsImpl(options);
					currentOptions.setOption("--gui", Boolean.toString(gui));
					others.clear();
				}
				drivers = CACHE.get(arg.toLowerCase(Locale.ENGLISH));
			} else if (arg.startsWith("-")) {
				currentOptions.addOption(arg);
			} else {
				others.add(arg);
			}
		}
		final boolean localGui = gui;
		if (drivers == null) {
			// No need to wrap startChooser() with invokeLater(), since it handles it
			// internally.
			try {
				startChooser(localGui, currentOptions, others);
			} catch (final DriverFailedException e) {
				final String message = e.getMessage();
				LOGGER.log(Level.SEVERE, message, e.getCause());
				SwingUtilities
						.invokeLater(() -> ErrorShower.showErrorDialog(null, message));
			}
		} else if (gui) {
			startChosenGUIDriver(drivers.second(), currentOptions, others);
		} else {
			startChosenDriver(drivers.first(), currentOptions, others);
		}
	}

	/**
	 * Returns the usage object.
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public IDriverUsage usage() {
		return USAGE;
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "AppStarter";
	}
}
