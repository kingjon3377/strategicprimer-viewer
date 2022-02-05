package drivers;

import drivers.common.DriverUsage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;

import java.io.IOException;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JEditorPane;

import static lovelace.util.ShowErrorDialog.showErrorDialog;

import lovelace.util.BorderedPanel;
import lovelace.util.Platform;
import lovelace.util.ListenedButton;

import drivers.common.IDriverModel;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.ParamCount;
import drivers.common.DriverFailedException;
import drivers.common.IncorrectUsageException;
import drivers.common.SPOptionsImpl;
import drivers.common.CLIDriver;
import drivers.common.DriverFactory;
import drivers.common.GUIDriverFactory;
import drivers.common.UtilityDriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.UtilityGUI;

import drivers.common.cli.ICLIHelper;
import drivers.common.cli.CLIHelper;

import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;

import com.pump.window.WindowList;

import drivers.gui.common.SPFrame;
import drivers.gui.common.WindowCloseListener;
import drivers.gui.common.SPMenu;

import common.xmlio.SPFormatException;
import common.xmlio.Warning;

import lovelace.util.MissingFileException;

import java.awt.image.BufferedImage;

import impl.xmlio.MapIOHelper;
import org.jetbrains.annotations.Nullable;

/* package */ class AppStarter {
	private static final Logger LOGGER = Logger.getLogger(AppStarter.class.getName());
	private final Map<String, Iterable<DriverFactory>> driverCache =
			new AppChooserState().createCache(); // TODO: Can we, and should we, inline that into here?

	private static boolean includeInCLIList(DriverFactory driver) {
		return driver.getUsage().includeInList(false);
	}

	public void startDriverOnArguments(ICLIHelper cli, SPOptions options, String... args) throws DriverFailedException {
		LOGGER.finer("Inside AppStarter#startDriver()");
		boolean gui = !GraphicsEnvironment.isHeadless();
		SPOptionsImpl currentOptions = new SPOptionsImpl(StreamSupport.stream(options.spliterator(), false).toArray((Map.Entry[]::new)));
		if (!currentOptions.hasOption("--gui")) {
			currentOptions.addOption("--gui", Boolean.toString(gui));
		}
		final List<String> others = new ArrayList<>();

		// TODO: Try to make an instance method
		BiConsumer<DriverFactory, SPOptions> startChosenDriver = (driver, currentOptionsTyped) -> {
			if (driver.getUsage().isGraphical()) {
				SwingUtilities.invokeLater(() -> new DriverWrapper(driver).startCatchingErrors(cli,
						currentOptionsTyped, others.stream().skip(1).toArray(String[]::new)));
			} else {
				new DriverWrapper(driver).startCatchingErrors(cli, currentOptionsTyped,
						others.stream().skip(1).toArray(String[]::new));
			}
		};

		for (String arg : args) {
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
				String tempString = arg.substring(6);
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
				String[] broken = arg.split("=");
				String param = broken[0];
				String rest = Stream.of(broken).skip(1).collect(Collectors.joining("="));
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
		@Nullable DriverFactory currentDriver;
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
			if (currentDriver != null) { // TODO: invert
				IDriverUsage currentUsage = currentDriver.getUsage();
				LOGGER.finer("Giving usage information for selected driver");
				// TODO: Can we and should we move the usageMessage() method into this class?
				System.out.println(new AppChooserState().usageMessage(currentUsage,
						"true".equals(options.getArgument("--verbose"))));
			} else {
				LOGGER.finer("No driver selected, so giving choices.");
				System.out.println("Strategic Primer assistive programs suite");
				System.out.println("No app specified; use one of the following invocations:");
				System.out.println();
				final AppChooserState acs = new AppChooserState();
				for (DriverFactory driver : driverCache.values().stream()
						.flatMap(l -> StreamSupport.stream(l.spliterator(), false)).distinct()
						.collect(Collectors.toList())) {
					// TODO: in Java 11+ use String.lines()
					String[] lines = acs.usageMessage(driver.getUsage(), "true".equals(
							options.getArgument("--verbose"))).split(System.lineSeparator());
					String invocationExample = lines[0].replace("Usage: ", "");
					String description = lines.length > 1 ? lines[1].replace(".", "") : "An unknown app";
					System.out.print(description);
					System.out.print(": ");
					System.out.println(invocationExample);
				}
			}
		} else if (currentDriver != null) {
			LOGGER.finer("Starting chosen app.");
			startChosenDriver.accept(currentDriver, currentOptions.copy());
		} else {
			LOGGER.finer("Starting app-chooser.");
			SPOptions currentOptionsTyped = currentOptions.copy();
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
				DriverFactory chosenDriver = cli.chooseFromList(driverCache.values().stream()
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
