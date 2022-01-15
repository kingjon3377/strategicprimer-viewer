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

import common.map.IMutableMapNG;

import com.google.auto.service.AutoService;

/**
 * A factory for the driver to print a mini-report on workers, suitable for
 * inclusion in a player's results.
 */
@AutoService(DriverFactory.class)
public class WorkerPrinterFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "print-stats", ParamCount.One,
		"Print stats of workers", "Print stats of workers in a unit in a brief list.", true,
		false);

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
		if (model instanceof IExplorationModel) {
			return new WorkerPrintCLI(cli, (IExplorationModel) model);
		} else {
			return createDriver(cli, options, new ExplorationModel(model));
		}
	}

	@Override
	public IDriverModel createModel(IMutableMapNG map) {
		return new ExplorationModel(map);
	}
}
