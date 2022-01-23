package drivers.worker_mgmt;

import java.nio.file.Paths;
import drivers.common.DriverFailedException;
import drivers.common.IDriverModel;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.ReadOnlyDriver;
import drivers.common.ModelDriverFactory;
import drivers.common.DriverFactory;
import drivers.common.ModelDriver;
import drivers.common.IWorkerModel;
import drivers.common.cli.ICLIHelper;
import worker.common.WorkerModel;

import common.map.IMutableMapNG;

import com.google.auto.service.AutoService;

/**
 * A factory for a command-line program to export a proto-strategy for a player from orders in a map.
 */
@AutoService(DriverFactory.class)
public class StrategyExportFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "export-strategy", ParamCount.One,
		"Export a proto-strategy", "Create a proto-strategy using orders stored in the map",
		true, false, "--current-turn=NN", "--print-empty", "--export=filename.txt",
		"--include-unleveled-jobs", "--summarize-large-units", "--results");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
		if (model instanceof IWorkerModel) {
			return new StrategyExportCLI(options, (IWorkerModel) model);
		} else {
			return createDriver(cli, options, new WorkerModel(model));
		}
	}

	@Override
	public IDriverModel createModel(IMutableMapNG map) {
		return new WorkerModel(map);
	}
}