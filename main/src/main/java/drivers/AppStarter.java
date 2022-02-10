package drivers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

import java.awt.GraphicsEnvironment;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.swing.SwingUtilities;

import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.DriverFailedException;
import drivers.common.SPOptionsImpl;
import drivers.common.DriverFactory;

import drivers.common.cli.ICLIHelper;

import org.jetbrains.annotations.Nullable;

/* package */ class AppStarter {
	private static final Logger LOGGER = Logger.getLogger(AppStarter.class.getName());
	private final Map<String, Iterable<DriverFactory>> driverCache =
			new AppChooserState().createCache(); // TODO: Can we, and should we, inline that into here?

	private static boolean includeInCLIList(final DriverFactory driver) {
		return driver.getUsage().includeInList(false);
	}

	public void startDriverOnArguments(final ICLIHelper cli, final SPOptions options, final String... args) throws DriverFailedException {
		LOGGER.finer("Inside AppStarter#startDriver()");
		boolean gui = !GraphicsEnvironment.isHeadless();
		final SPOptionsImpl currentOptions = new SPOptionsImpl(StreamSupport.stream(options.spliterator(), false).toArray((Map.Entry[]::new)));
		if (!currentOptions.hasOption("--gui")) {
			currentOptions.addOption("--gui", Boolean.toString(gui));
		}
		final List<String> others = new ArrayList<>();

		// TODO: Try to make an instance method
		final BiConsumer<DriverFactory, SPOptions> startChosenDriver = (driver, currentOptionsTyped) -> {
			if (driver.getUsage().isGraphical()) {
				SwingUtilities.invokeLater(() -> new DriverWrapper(driver).startCatchingErrors(cli,
						currentOptionsTyped, others.stream().skip(1).toArray(String[]::new)));
			} else {
				new DriverWrapper(driver).startCatchingErrors(cli, currentOptionsTyped,
						others.stream().skip(1).toArray(String[]::new));
			}
		};

		for (final String arg : args) {
			if (arg == null) {
				continue;
			} else if ("-g".equals(arg) || "--gui".equals(arg)) {
				LOGGER.finer("User specified either -g or --gui");
				currentOptions.addOption("--gui");
				gui = true;
			} else if ("-c".equals(arg) || "--cli".equals(arg)) {
				LOGGER.finer("User specified either -c or --cli");
				currentOptions.addOption("--gui", "false");
				gui = false;
			} else if (arg.startsWith("--gui=")) {
				final String tempString = arg.substring(6);
				LOGGER.finer("User specified --gui=" + tempString);
				if ("true".equalsIgnoreCase(tempString)) {
					gui = true;
				} else if ("false".equalsIgnoreCase(tempString)) {
					gui = false;
				} else {
					throw new DriverFailedException(new IllegalArgumentException("--gui=nonBoolean"));
				}
				currentOptions.addOption("--gui", tempString);
			} else if (arg.startsWith("-") && arg.contains("=")) {
				final String[] broken = arg.split("=");
				final String param = broken[0];
				final String rest = Stream.of(broken).skip(1).collect(Collectors.joining("="));
				currentOptions.addOption(param, rest);
				LOGGER.finer(String.format("User specified %s=%s", param, rest));
			} else if (arg.startsWith("-")) {
				LOGGER.finer("User specified non-app-choosing option ``arg``");
				currentOptions.addOption(arg);
			} else {
				LOGGER.finer("User specified non-option argument ``arg``");
				others.add(arg);
			}
		}

		LOGGER.finer("Reached the end of arguments");
		// TODO: Use appletChooser so we can support prefixes
		final @Nullable DriverFactory currentDriver;
		final @Nullable String command = others.stream().findFirst().orElse(null);
		final List<DriverFactory> drivers = Optional.ofNullable(command).map(driverCache::get)
				// TODO: Drop StreamSupport use if driverCache is changed to specify List.
				.map(l -> StreamSupport.stream(l.spliterator(), false).collect(Collectors.toList()))
				.orElse(Collections.emptyList());
		if (command != null && !drivers.isEmpty()) {
			final DriverFactory first = drivers.stream().findFirst().orElse(null);
			LOGGER.finer("Found a driver or drivers");
			if (drivers.size() == 1) {
				LOGGER.finer("Only one driver registered for that command");
				currentDriver = first;
			} else {
				final boolean localGui = gui;
				LOGGER.finer("Multiple drivers registered; filtering by interface");
				currentDriver = drivers.stream()
						.filter(d -> d.getUsage().isGraphical() == localGui).findAny().orElse(null);
			}
		} else {
			LOGGER.finer("No matching driver found");
			currentDriver = null;
		}
		if (currentOptions.hasOption("--help")) {
			if (currentDriver == null) {
				LOGGER.finer("No driver selected, so giving choices.");
				System.out.println("Strategic Primer assistive programs suite");
				System.out.println("No app specified; use one of the following invocations:");
				System.out.println();
				final AppChooserState acs = new AppChooserState();
				for (final DriverFactory driver : driverCache.values().stream()
						.flatMap(l -> StreamSupport.stream(l.spliterator(), false)).distinct()
						.collect(Collectors.toList())) {
					// TODO: in Java 11+ use String.lines()
					final String[] lines = acs.usageMessage(driver.getUsage(), "true".equals(
							options.getArgument("--verbose"))).split(System.lineSeparator());
					final String invocationExample = lines[0].replace("Usage: ", "");
					final String description = lines.length > 1 ? lines[1].replace(".", "") : "An unknown app";
					System.out.printf("%s: %s", description, invocationExample);
				}
			} else {
				final IDriverUsage currentUsage = currentDriver.getUsage();
				LOGGER.finer("Giving usage information for selected driver");
				// TODO: Can we and should we move the usageMessage() method into this class?
				System.out.println(new AppChooserState().usageMessage(currentUsage,
						"true".equals(options.getArgument("--verbose"))));
			}
		} else if (currentDriver != null) {
			LOGGER.finer("Starting chosen app.");
			startChosenDriver.accept(currentDriver, currentOptions.copy());
		} else {
			LOGGER.finer("Starting app-chooser.");
			final SPOptions currentOptionsTyped = currentOptions.copy();
			if (gui) {
//				try {
				SwingUtilities.invokeLater(
						() -> new AppChooserGUI(cli, currentOptionsTyped)
								.startDriver(others.toArray(new String[0])));
//				} catch (DriverFailedException except) {
//					LOGGER.log(Level.SEVERE, except.getMessage(), except);
//					SwingUtilities.invokeLater(() -> showErrorDialog(null,
//						"Strategic Primer Assistive Programs", except.getMessage()));
//				}
			} else {
				final DriverFactory chosenDriver = cli.chooseFromList(driverCache.values().stream()
								.flatMap(i -> StreamSupport.stream(i.spliterator(), false))
								.filter(AppStarter::includeInCLIList).collect(Collectors.toList()),
						"CLI apps available:", "No applications available", "App to start: ",
						true).getValue1();
				if (chosenDriver != null) {
					new DriverWrapper(chosenDriver).startCatchingErrors(cli, options,
							others.toArray(new String[0]));
				}
			}
		}
	}
}
