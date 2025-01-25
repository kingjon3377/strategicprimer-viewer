package utility.subset;

import drivers.common.DriverUsage;
import drivers.common.IDriverModel;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.SimpleMultiMapModel;
import drivers.common.IMultiMapModel;
import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.ModelDriver;

import drivers.common.cli.ICLIHelper;

import legacy.map.IMutableLegacyMap;

import com.google.auto.service.AutoService;
import lovelace.util.LovelaceLogger;

import java.util.EnumSet;

/**
 * A factory for a driver to check whether player maps are subsets of the main map.
 */
@AutoService(DriverFactory.class)
public final class SubsetCLIFactory implements ModelDriverFactory<IMultiMapModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "subset",
			ParamCount.AtLeastTwo, "Check players' maps against master",
			"""
					Check that subordinate maps are subsets of the main map, containing nothing that it does not \
					contain in the same place.""",
			EnumSet.of(IDriverUsage.DriverMode.CommandLine));

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IMultiMapModel model) {
		if (model.streamSubordinateMaps().noneMatch(x -> true)) {
			cli.println("Subset checking does nothing with no subordinate maps");
			LovelaceLogger.warning("Subset checking does nothing with no subordinate maps");
		}
		return new SubsetCLI(cli, model);
	}

	@Override
	public IMultiMapModel createModel(final IMutableLegacyMap map) {
		return new SimpleMultiMapModel(map);
	}

	@Override
	public IMultiMapModel createModel(final IDriverModel model) {
		return new SimpleMultiMapModel(model);
	}
}
