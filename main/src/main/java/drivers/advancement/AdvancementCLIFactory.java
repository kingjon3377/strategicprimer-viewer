package drivers.advancement;

import drivers.common.IDriverModel;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.ModelDriver;
import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.IWorkerModel;

import drivers.common.cli.ICLIHelper;

import legacy.map.IMutableLegacyMap;

import worker.common.WorkerModel;

import com.google.auto.service.AutoService;

import java.util.EnumSet;

/**
 * A factory for the worker-advancement CLI driver.
 */
@AutoService(DriverFactory.class)
public final class AdvancementCLIFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "advance",
			ParamCount.AtLeastOne, "View a player's workers and manage their advancement",
			"View a player's units, workers in those units, each worker's Jobs, and Skill levels in each Job.",
			EnumSet.of(IDriverUsage.DriverMode.CommandLine), "--current-turn=NN", "--allow-expert-mentoring");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		if (model instanceof final IWorkerModel wm) {
			return new AdvancementCLI(cli, options, wm);
		} else {
			return createDriver(cli, options, new WorkerModel(model));
		}
	}

	@Override
	public IDriverModel createModel(final IMutableLegacyMap map) {
		return new WorkerModel(map);
	}
}
