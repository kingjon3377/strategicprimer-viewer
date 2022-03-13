package drivers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.IOException;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static lovelace.util.ShowErrorDialog.showErrorDialog;

import lovelace.util.MalformedXMLException;

import drivers.common.IDriverUsage;
import drivers.common.DriverFactory;

import com.apple.eawt.AppEvent;

import com.pump.window.WindowList;

import drivers.gui.common.SPFrame;

import common.xmlio.SPFormatException;

import lovelace.util.MissingFileException;

import lovelace.util.ResourceInputStream;

/* package */ final class AppChooserState {
	// FIXME: Move methods back into AppStarter, unless that would break something.
	private static final Logger LOGGER = Logger.getLogger(AppChooserState.class.getName());
	// TODO: Define and register a custom log formatter? We had to in
	// Ceylon to get log messags to show at all, but in Java I dropped this
	// as j.u.l includes one. Or TODO: define my own central logging method?

	/**
	 * Create the cache of driver objects.
	 */
	public static Map<String, Iterable<DriverFactory>> createCache() {
		// TODO: Use a multimap?
		final Map<String, List<DriverFactory>> cache = new HashMap<>();
		final Map<String, List<DriverFactory>> conflicts = new HashMap<>();
		for (final DriverFactory factory : ServiceLoader.load(DriverFactory.class)) {
			final String command = factory.getUsage().getInvocation();
			if (command.startsWith("-")) {
				LOGGER.severe(String.format(
					"An app wants to register an option, %s, not a subcommand", command));
			} else if (conflicts.containsKey(command)) {
				LOGGER.warning(String.format("Additional conflict for %s: %s", command,
					factory.getUsage().getShortDescription()));
				conflicts.get(command).add(factory);
			} else if (cache.containsKey(command) &&
					cache.get(command).stream().anyMatch(f -> f.getUsage().isGraphical() ==
						factory.getUsage().isGraphical())) {
				final DriverFactory existing = cache.get(command).stream()
					.filter(f -> f.getUsage().isGraphical() ==
						factory.getUsage().isGraphical()).findAny().orElse(null);
				LOGGER.warning(String.format("Invocation command conflict for %s between %s and %s",
					command, factory.getUsage().getShortDescription(),
					Optional.ofNullable(existing).map(DriverFactory::getUsage)
							.map(IDriverUsage::getShortDescription).orElse("a null factory")));
				conflicts.put(command, new ArrayList<>(Arrays.asList(factory, existing)));
				final List<DriverFactory> existingList = cache.get(command);
				existingList.remove(existing);
				if (existingList.isEmpty()) {
					cache.remove(command);
				}
			} else {
				final List<DriverFactory> list = new ArrayList<>();
				list.add(factory);
				cache.put(command, list);
			}
		}
		return new HashMap<>(cache);
	}

	/**
	 * Create the usage message for a particular driver.
	 */
	public static String usageMessage(final IDriverUsage usage, final boolean verbose) {
		final StringBuilder builder = new StringBuilder();
		builder.append("Usage: ");
		String mainInvocation;
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(
				new ResourceInputStream("invocation", AppChooserState.class)))) {
			mainInvocation = reader.lines().collect(Collectors.joining(System.lineSeparator())).trim();
		} catch (final FileNotFoundException | NoSuchFileException except) {
			LOGGER.warning("Invocation file not found");
			LOGGER.log(Level.FINER, "Stack trace for invocation-not-found", except);
			mainInvocation = "java -jar viewer-VERSION.jar";
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O error reading invocation file", except);
			mainInvocation = "java -jar viewer-VERSION.jar";
		}
		builder.append(mainInvocation);
		if (usage.isGraphical()) {
			builder.append(" [-g|--gui] ");
		} else {
			builder.append(" -c|--cli "); // FIXME For apps with no GUI counterpart this is actually optional
		}
		builder.append(usage.getInvocation());
		for (final String option : usage.getSupportedOptions()) {
			builder.append(" [").append(option).append("]");
		}
		switch (usage.getParamsWanted()) {
			case None:
				// Nothing to show
				break;
			case One:
				builder.append(" ").append(usage.getFirstParamDescription());
				break;
			case AtLeastOne:
				builder.append(" ").append(usage.getFirstParamDescription()).append(" [")
					.append(usage.getSubsequentParamDescription()).append(" ...]");
				break;
			case Two:
				builder.append(" ").append(usage.getFirstParamDescription()).append(" ")
					.append(usage.getSubsequentParamDescription());
				break;
			case AtLeastTwo:
				builder.append(" ").append(usage.getFirstParamDescription()).append(" ")
					.append(usage.getSubsequentParamDescription()).append(" [")
					.append(usage.getSubsequentParamDescription()).append(" ...]");
				break;
			case AnyNumber:
				builder.append(" [").append(usage.getSubsequentParamDescription()).append(" ...]");
				break;
			default:
				throw new IllegalStateException("Exhaustive switch wasn't");
			}
			builder.append(System.lineSeparator());
			if (verbose) {
				builder.append(usage.getLongDescription());
			} else {
				builder.append(usage.getShortDescription());
			}
			builder.append(System.lineSeparator());
			return builder.toString();
	}

	public static void handleDroppedFiles(final AppEvent.OpenFilesEvent openFilesEvent) {
		final SPFrame topWindow = Stream.of(WindowList.getWindows(true, false)).filter(SPFrame.class::isInstance)
			.map(SPFrame.class::cast).reduce((first, second) -> second).orElse(null);
		if (topWindow != null) {
			for (final File file : openFilesEvent.getFiles()) {
				try {
					topWindow.acceptDroppedFile(file.toPath());
				} catch (final SPFormatException except) {
					showErrorDialog(topWindow, "Strategic Primer Map Format Error", except.getMessage());
					LOGGER.severe(except.getMessage());
				} catch (final FileNotFoundException|NoSuchFileException|MissingFileException except) {
					showErrorDialog(topWindow, "File Not Found", except.getMessage());
					LOGGER.log(Level.SEVERE, "Dropped file not found", except);
				} catch (final IOException except) {
					showErrorDialog(topWindow, "I/O Error", except.getMessage());
					LOGGER.log(Level.SEVERE, "I/O error reading dropped file", except.getMessage());
				} catch (final MalformedXMLException except) {
					showErrorDialog(topWindow, "Strategic Primer Map Format Error", except.getMessage());
					LOGGER.log(Level.SEVERE, "Malformed XML in " + file, except);
				}
			}
		}
	}
}

