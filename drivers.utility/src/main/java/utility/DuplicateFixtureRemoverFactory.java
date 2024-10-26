package utility;

import legacy.map.IMutableLegacyMap;

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
public final class DuplicateFixtureRemoverFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "remove-duplicates",
			ParamCount.AtLeastOne, "Remove duplicate fixtures",
			"Remove duplicate fixtures (identical except ID# and on the same tile) from a map.",
			true, false, "--current-turn=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		if (model instanceof final UtilityDriverModel udm) {
			return new DuplicateFixtureRemoverCLI(cli, udm);
		} else {
			return createDriver(cli, options, new UtilityDriverModel(model));
		}
	}

	@Override
	public IDriverModel createModel(final IMutableLegacyMap map) {
		return new UtilityDriverModel(map);
	}
}
