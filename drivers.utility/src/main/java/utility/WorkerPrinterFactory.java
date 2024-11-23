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

import java.util.EnumSet;

/**
 * A factory for the driver to print a mini-report on workers, suitable for
 * inclusion in a player's results.
 */
@AutoService(DriverFactory.class)
public final class WorkerPrinterFactory implements ModelDriverFactory<IExplorationModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "print-stats",
			ParamCount.One, "Print stats of workers", "Print stats of workers in a unit in a brief list.",
			EnumSet.of(IDriverUsage.DriverMode.CommandLine));

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IExplorationModel model) {
		return new WorkerPrintCLI(cli, model);
	}

	@Override
	public IExplorationModel createModel(final IMutableLegacyMap map) {
		return new ExplorationModel(map);
	}
}
