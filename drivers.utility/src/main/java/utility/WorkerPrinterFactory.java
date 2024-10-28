package utility;

import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.SPOptions;
import drivers.common.IDriverModel;
import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.ModelDriver;

import drivers.common.cli.ICLIHelper;

import exploration.common.IExplorationModel;
import exploration.common.ExplorationModel;

import legacy.map.IMutableLegacyMap;

import com.google.auto.service.AutoService;

/**
 * A factory for the driver to print a mini-report on workers, suitable for
 * inclusion in a player's results.
 */
@AutoService(DriverFactory.class)
public final class WorkerPrinterFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "print-stats",
			ParamCount.One, "Print stats of workers", "Print stats of workers in a unit in a brief list.", true,
			false);

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		if (model instanceof final IExplorationModel em) {
			return new WorkerPrintCLI(cli, em);
		} else {
			return createDriver(cli, options, new ExplorationModel(model));
		}
	}

	@Override
	public IDriverModel createModel(final IMutableLegacyMap map) {
		return new ExplorationModel(map);
	}
}
