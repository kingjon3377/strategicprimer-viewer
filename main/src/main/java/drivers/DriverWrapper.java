package drivers;

import common.xmlio.SPFormatException;
import common.xmlio.Warning;
import drivers.common.CLIDriver;
import drivers.common.DriverFactory;
import drivers.common.DriverFailedException;
import drivers.common.GUIDriverFactory;
import drivers.common.IDriverModel;
import drivers.common.IMultiMapModel;
import drivers.common.IncorrectUsageException;
import drivers.common.ModelDriver;
import drivers.common.ModelDriverFactory;
import drivers.common.ParamCount;
import drivers.common.SPOptions;
import drivers.common.UtilityDriverFactory;
import drivers.common.cli.ICLIHelper;
import legacy.xmlio.MapIOHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import lovelace.util.LovelaceLogger;

/* package */ final class DriverWrapper {
	private final DriverFactory factory;

	public DriverWrapper(final DriverFactory factory) {
		this.factory = factory;
	}

	private boolean enoughArguments(final int argc) {
		if (argc < 0) {
			throw new IllegalArgumentException("Negative arg counts don't make sense");
		}
		return switch (factory.getUsage().getParamsWanted()) {
			case None, AnyNumber -> true;
			case One, AtLeastOne -> argc >= 1;
			case Two, AtLeastTwo -> argc >= 2;
		};
	}

	private boolean tooManyArguments(final int argc) {
		if (argc < 0) {
			throw new IllegalArgumentException("Negative arg counts don't make sense");
		}
		return switch (factory.getUsage().getParamsWanted()) {
			case AnyNumber, AtLeastOne, AtLeastTwo -> false;
			case None -> argc > 0;
			case One -> argc > 1;
			case Two -> argc > 2;
		};
	}

	private void checkArguments(final String... args) throws IncorrectUsageException {
		if (!enoughArguments(args.length) || tooManyArguments(args.length)) {
			throw new IncorrectUsageException(factory.getUsage());
		}
	}

	private List<Path> extendArguments(final String... args) throws IncorrectUsageException {
		if (factory instanceof final GUIDriverFactory gdf) {
			final List<Path> files = new ArrayList<>();
			if (args.length > 0) {
				files.addAll(MapIOHelper.namesToFiles(args));
			}
			if (tooManyArguments(files.size())) {
				throw new IncorrectUsageException(factory.getUsage());
			}
			while (!enoughArguments(files.size()) &&
					!tooManyArguments(files.size() + 1)) {
				final List<Path> requested;
				try {
					requested = gdf.askUserForFiles();
				} catch (final DriverFailedException except) {
					LovelaceLogger.warning(except, "User presumably canceled");
					throw new IncorrectUsageException(factory.getUsage());
				}
				if (requested.isEmpty() || tooManyArguments(files.size() + requested.size())) {
					throw new IncorrectUsageException(factory.getUsage());
				} else {
					files.addAll(requested);
				}
			}
			return files;
		} else if (args.length > 0) {
			return MapIOHelper.namesToFiles(args);
		} else {
			return Collections.emptyList();
		}
	}

	private static void fixCurrentTurn(final SPOptions options, final IDriverModel model) {
		if (options.hasOption("--current-turn")) {
			try {
				model.setCurrentTurn(Integer.parseInt(options.getArgument("--current-turn")));
			} catch (final NumberFormatException except) {
				LovelaceLogger.warning("Non-numeric current turn argument");
			}
		}
	}

	public void startCatchingErrors(final ICLIHelper cli, final SPOptions options, final String... args) {
		try {
			switch (factory) {
				case final UtilityDriverFactory udf -> {
					checkArguments(args);
					udf.createDriver(cli, options).startDriver(args);
				}
				case final GUIDriverFactory gdf -> {
					if (ParamCount.One == gdf.getUsage().getParamsWanted() && args.length > 1) {
						for (final String arg : args) {
							startCatchingErrors(cli, options, arg);
						}
					} else {
						// FIXME: What if paramsWanted is None or Any, and args is empty?
						final List<Path> files = extendArguments(args);
						// TODO: Make MapReaderAdapter just take args directly, not split, to reduce inconvenience here
						final IMultiMapModel model = MapReaderAdapter.readMultiMapModel(Warning.WARN,
								files.getFirst(), files.stream().skip(1).toArray(Path[]::new));
						fixCurrentTurn(options, model);
						gdf.createDriver(cli, options, model).startDriver();
					}
				}
				case final ModelDriverFactory mdf -> {
					checkArguments(args);
					// FIXME: What if a model driver had paramsWanted as None or Any, and args is empty?
					// In Ceylon we asserted args was nonempty, but didn't address this case
					final List<Path> files = MapIOHelper.namesToFiles(args);
					final IMultiMapModel model = MapReaderAdapter.readMultiMapModel(Warning.WARN,
							files.getFirst(), files.stream().skip(1).toArray(Path[]::new));
					fixCurrentTurn(options, model);
					final ModelDriver driver = mdf.createDriver(cli, options, model);
					driver.startDriver();
					if (driver instanceof CLIDriver) {
						MapReaderAdapter.writeModel(model);
					}  // TODO: refactor to avoid successive instanceof tests
				}
				default -> LovelaceLogger.error("Unhandled driver class",
						new IllegalStateException("Unhandled driver class"));
			}
		} catch (final IncorrectUsageException except) {
			cli.println(AppChooserState.usageMessage(except.getCorrectUsage(),
					"true".equals(options.getArgument("--verbose")) ? AppChooserState.UsageVerbosity.Verbose :
							AppChooserState.UsageVerbosity.Terse));
		} catch (final DriverFailedException except) {
			final Throwable cause = except.getCause();
			if (cause instanceof SPFormatException) {
				LovelaceLogger.error(cause.getMessage());
			} else {
				LovelaceLogger.error(Objects.requireNonNullElse(cause, except), "Driver failed:");
			}
		} catch (final Exception except) {
			LovelaceLogger.error(except, except.getMessage());
		}
	}
}
