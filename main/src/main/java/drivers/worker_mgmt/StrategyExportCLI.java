package drivers.worker_mgmt;

import java.io.IOException;
import java.util.Collections;
import java.nio.file.Paths;
import drivers.common.DriverFailedException;
import drivers.common.SPOptions;
import drivers.common.ReadOnlyDriver;
import drivers.common.IWorkerModel;

/**
 * A command-line program to export a proto-strategy for a player from orders in a map.
 */
public class StrategyExportCLI implements ReadOnlyDriver {
	public StrategyExportCLI(final SPOptions options, final IWorkerModel model) {
		this.options = options;
		this.model = model;
	}

	private final SPOptions options;
	private final IWorkerModel model;

	@Override
	public SPOptions getOptions() {
		return options;
	}

	@Override
	public IWorkerModel getModel() {
		return model;
	}

	@Override
	public void startDriver() throws DriverFailedException {
		if (options.hasOption("--export")) {
			try {
				new StrategyExporter(model, options).writeStrategy(Paths.get(
					options.getArgument("--export")), Collections.emptyList());
			} catch (final IOException except) {
				throw new DriverFailedException(except, "I/O error writing to file");
			}
		} else {
			throw DriverFailedException.illegalState("--export option is required");
		}
	}
}
