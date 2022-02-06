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
import impl.xmlio.MapIOHelper;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/* package */ class DriverWrapper {
	private static final Logger LOGGER = Logger.getLogger(DriverWrapper.class.getName());
	private final DriverFactory factory;
	public DriverWrapper(final DriverFactory factory) {
		this.factory = factory;
	}

	private boolean enoughArguments(final int argc) {
		if (argc < 0) {
			throw new IllegalArgumentException("Negative arg counts don't make sense");
		}
		switch (factory.getUsage().getParamsWanted()) {
		case None: case AnyNumber:
			return true;
		case One: case AtLeastOne:
			return argc >= 1;
		case Two: case AtLeastTwo:
			return argc >= 2;
		default:
			throw new IllegalStateException("Exhaustive switch wasn't");
		}
	}

	private boolean tooManyArguments(final int argc) {
		if (argc < 0) {
			throw new IllegalArgumentException("Negative arg counts don't make sense");
		}
		switch (factory.getUsage().getParamsWanted()) {
		case AnyNumber: case AtLeastOne: case AtLeastTwo:
			return false;
		case None:
			return argc > 0;
		case One:
			return argc > 1;
		case Two:
			return argc > 2;
		default:
			throw new IllegalStateException("Exhaustive switch wasn't");
		}
	}

	private void checkArguments(final String... args) throws IncorrectUsageException {
		if (!enoughArguments(args.length) || tooManyArguments(args.length)) {
			throw new IncorrectUsageException(factory.getUsage());
		}
	}

	private List<Path> extendArguments(final String... args) throws IncorrectUsageException {
		if (factory instanceof GUIDriverFactory) {
			final List<Path> files = new ArrayList<>();
			if (args.length > 0) {
				// TODO: Make namesToFiles() return List
				MapIOHelper.namesToFiles(args).forEach(files::add);
			}
			if (tooManyArguments(files.size())) {
				throw new IncorrectUsageException(factory.getUsage());
			}
			while (!enoughArguments(files.size()) &&
					!tooManyArguments(files.size() + 1)) {
				// TODO: change signature of askUserForFiles() to Collection or List
				List<Path> requested = StreamSupport.stream(((GUIDriverFactory) factory).askUserForFiles().spliterator(), false)
					.collect(Collectors.toList());
				if (requested.isEmpty() || tooManyArguments(files.size() + requested.size())) {
					throw new IncorrectUsageException(factory.getUsage());
				} else {
					files.addAll(requested);
				}
			}
			return files;
		} else if (args.length > 0) {
			return StreamSupport.stream(MapIOHelper.namesToFiles(args).spliterator(), false)
				.collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}

	private static void fixCurrentTurn(final SPOptions options, final IDriverModel model) {
		if (options.hasOption("--current-turn")) {
			try {
				model.setCurrentTurn(Integer.parseInt(options.getArgument("--current-turn")));
			} catch (final NumberFormatException except) {
				LOGGER.warning("Non-numeric current turn argument");
			}
		}
	}

	public void startCatchingErrors(final ICLIHelper cli, final SPOptions options, final String... args) {
		try {
			if (factory instanceof UtilityDriverFactory) {
				checkArguments(args);
				((UtilityDriverFactory) factory).createDriver(cli, options).startDriver(args);
			} else if (factory instanceof ModelDriverFactory) { // TODO: refactor to avoid successive instanceof tests
				if (factory instanceof GUIDriverFactory) {
					if (ParamCount.One.equals(factory.getUsage().getParamsWanted()) && args.length > 1) {
						for (String arg : args) {
							startCatchingErrors(cli, options, arg);
						}
					} else {
						// FIXME: What if paramsWanted is None or Any, and args is empty?
						List<Path> files = extendArguments(args);
						// TODO: Make MapReaderAdapter just take args directly, not split, to reduce inconvenience here
						IMultiMapModel model = MapReaderAdapter.readMultiMapModel(Warning.WARN,
							files.get(0), files.stream().skip(1).toArray(Path[]::new));
						fixCurrentTurn(options, model);
						((GUIDriverFactory) factory).createDriver(cli, options, model).startDriver();
					}
				} else {
					checkArguments(args);
					// FIXME: What if a model driver had paramsWanted as None or Any, and args is empty?
					// In Ceylon we asserted args was nonempty, but didn't address this case
					List<Path> files = StreamSupport.stream(MapIOHelper.namesToFiles(args).spliterator(), false)
							                   .collect(Collectors.toList());
					IMultiMapModel model = MapReaderAdapter.readMultiMapModel(Warning.WARN,
						files.get(0), files.stream().skip(1).toArray(Path[]::new));
					fixCurrentTurn(options, model);
					ModelDriver driver = ((ModelDriverFactory) factory).createDriver(cli, options, model);
					driver.startDriver();
					if (driver instanceof CLIDriver) {
						MapReaderAdapter.writeModel(model);
					}
				}
			} else {
				throw new DriverFailedException(new IllegalStateException("Unhandled driver class"));
			}
		} catch (final IncorrectUsageException except) {
			cli.println(new AppChooserState().usageMessage(except.getCorrectUsage(),
				options.getArgument("--verbose") == "true"));
		} catch (final DriverFailedException except) {
			Throwable cause = except.getCause();
			if (cause instanceof SPFormatException) {
				LOGGER.severe(cause.getMessage());
			} else if (cause != null) {
				LOGGER.log(Level.SEVERE, "Driver failed:", cause);
			} else {
				LOGGER.log(Level.SEVERE, "Driver failed:", except);
			}
		} catch (final Exception except) {
			LOGGER.log(Level.SEVERE, except.getMessage(), except);
		}
	}
}