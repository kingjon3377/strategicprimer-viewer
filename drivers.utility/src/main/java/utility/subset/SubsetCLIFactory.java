package utility.subset;

import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.SimpleMultiMapModel;
import drivers.common.IMultiMapModel;
import drivers.common.IDriverModel;
import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.ModelDriver;

import drivers.common.cli.ICLIHelper;

import legacy.map.IMutableLegacyMap;

import com.google.auto.service.AutoService;
import lovelace.util.LovelaceLogger;

/**
 * A factory for a driver to check whether player maps are subsets of the main map.
 */
@AutoService(DriverFactory.class)
public final class SubsetCLIFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "subset", ParamCount.AtLeastTwo,
			"Check players' maps against master",
			"""
					Check that subordinate maps are subsets of the main map, containing nothing that it does not \
					contain in the same place.""",
			true, false);

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		if (model instanceof final IMultiMapModel mmm && mmm.streamSubordinateMaps().anyMatch(x -> true)) {
			return new SubsetCLI(cli, mmm);
		} else {
			cli.println("Subset checking does nothing with no subordinate maps");
			LovelaceLogger.warning("Subset checking does nothing with no subordinate maps");
			return createDriver(cli, options, new SimpleMultiMapModel(model));
		}
	}

	@Override
	public IDriverModel createModel(final IMutableLegacyMap map) {
		return new SimpleMultiMapModel(map);
	}
}
