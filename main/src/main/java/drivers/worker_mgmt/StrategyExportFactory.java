package drivers.worker_mgmt;

import drivers.common.DriverUsage;
import drivers.common.IDriverModel;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.ModelDriverFactory;
import drivers.common.DriverFactory;
import drivers.common.ModelDriver;
import drivers.common.IWorkerModel;
import drivers.common.cli.ICLIHelper;
import worker.common.WorkerModel;

import legacy.map.IMutableLegacyMap;

import com.google.auto.service.AutoService;

import java.util.EnumSet;

/**
 * A factory for a command-line program to export a proto-strategy for a player from orders in a map.
 */
@AutoService(DriverFactory.class)
public final class StrategyExportFactory implements ModelDriverFactory<IWorkerModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "export-strategy",
			ParamCount.One, "Export a proto-strategy", "Create a proto-strategy using orders stored in the map",
			EnumSet.of(IDriverUsage.DriverMode.CommandLine), "--current-turn=NN", "--print-empty",
			"--export=filename.txt", "--include-unleveled-jobs", "--summarize-large-units", "--results");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IWorkerModel model) {
		return new StrategyExportCLI(options, model);
	}

	@Override
	public IWorkerModel createModel(final IMutableLegacyMap map) {
		return new WorkerModel(map);
	}

	@Override
	public IWorkerModel createModel(IDriverModel model) {
		return new WorkerModel(model);
	}
}
