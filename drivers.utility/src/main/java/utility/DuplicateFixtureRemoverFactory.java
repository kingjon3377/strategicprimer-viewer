package utility;

import common.map.IMutableMapNG;

import drivers.common.cli.ICLIHelper;

import drivers.common.SPOptions;
import drivers.common.DriverUsage;
import drivers.common.IDriverUsage;
import drivers.common.IDriverModel;
import drivers.common.ParamCount;
import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.ModelDriver;

import com.google.auto.service.AutoService;

/**
 * A factory for a driver to remove duplicate hills, forests, etc., from the
 * map (to reduce the disk space it takes up and the memory and CPU required to
 * deal with it).
 */
@AutoService(DriverFactory.class)
public class DuplicateFixtureRemoverFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "remove-duplicates",
		ParamCount.AtLeastOne, "Remove duplicate fixtures",
		"Remove duplicate fixtures (identical except ID# and on the same tile) from a map.",
		true, false, "--current-turn=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
		if (model instanceof UtilityDriverModel) {
			return new DuplicateFixtureRemoverCLI(cli, (UtilityDriverModel) model);
		} else {
			return createDriver(cli, options, new UtilityDriverModel(model));
		}
	}

	@Override
	public IDriverModel createModel(IMutableMapNG map) {
		return new UtilityDriverModel(map);
	}
}
