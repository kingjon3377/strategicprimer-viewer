package utility;

import drivers.common.IDriverModel;
import legacy.map.IMutableLegacyMap;

import drivers.common.cli.ICLIHelper;

import drivers.common.SPOptions;
import drivers.common.DriverUsage;
import drivers.common.IDriverUsage;
import drivers.common.ParamCount;
import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.ModelDriver;

import com.google.auto.service.AutoService;

import java.util.EnumSet;

/**
 * A factory for a driver to remove duplicate hills, forests, etc., from the
 * map (to reduce the disk space it takes up and the memory and CPU required to
 * deal with it).
 */
@AutoService(DriverFactory.class)
public final class DuplicateFixtureRemoverFactory implements ModelDriverFactory<UtilityDriverModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "remove-duplicates",
			ParamCount.AtLeastOne, "Remove duplicate fixtures",
			"Remove duplicate fixtures (identical except ID# and on the same tile) from a map.",
			EnumSet.of(IDriverUsage.DriverMode.CommandLine), "--current-turn=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final UtilityDriverModel model) {
		return new DuplicateFixtureRemoverCLI(cli, model);
	}

	@Override
	public UtilityDriverModel createModel(final IMutableLegacyMap map) {
		return new UtilityDriverModel(map);
	}

	@Override
	public UtilityDriverModel createModel(final IDriverModel model) {
		return new UtilityDriverModel(model);
	}
}
