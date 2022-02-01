package drivers.advancement;

import drivers.common.IDriverModel;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.CLIDriver;
import drivers.common.ModelDriver;
import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.IWorkerModel;

import drivers.common.cli.ICLIHelper;

import common.map.Player;
import common.map.IMutableMapNG;

import worker.common.WorkerModel;

import java.util.logging.Level;
import java.util.logging.Logger;

import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.IUnit;

import java.util.ArrayList;
import java.util.List;

import com.google.auto.service.AutoService;

/**
 * A factory for the worker-advancement CLI driver.
 */
@AutoService(DriverFactory.class)
public class AdvancementCLIFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "advance", ParamCount.AtLeastOne,
		"View a player's workers and manage their advancement",
		"View a player's units, the workers in those units, each worker's Jobs, and his or her level in each Skill in each Job.", true, false, "--current-turn=NN", "--allow-expert-mentoring");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
		if (model instanceof IWorkerModel) {
			return new AdvancementCLI(cli, options, (IWorkerModel) model);
		} else {
			return createDriver(cli, options, new WorkerModel(model));
		}
	}

	@Override
	public IDriverModel createModel(IMutableMapNG map) {
		return new WorkerModel(map);
	}
}
