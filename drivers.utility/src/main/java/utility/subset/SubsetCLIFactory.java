package utility.subset;

import java.util.logging.Logger;

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

import common.map.IMutableMapNG;

import com.google.auto.service.AutoService;

/**
 * A factory for a driver to check whether player maps are subsets of the main map.
 */
@AutoService(DriverFactory.class)
public class SubsetCLIFactory implements ModelDriverFactory {
	/**
	 * A logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(SubsetCLIFactory.class.getName());

	private static final IDriverUsage USAGE = new DriverUsage(false, "subset", ParamCount.AtLeastTwo,
		"Check players' maps against master",
		"Check that subordinate maps are subsets of the main map, containing nothing that it does not contain in the same place.",
		true, false);

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		if (model instanceof IMultiMapModel) {
			return new SubsetCLI(cli, (IMultiMapModel) model);
		} else {
			LOGGER.warning("Subset checking does nothing with no subordinate maps"); // TODO: Should probably warn on the provided ICLIHelper
			return createDriver(cli, options, new SimpleMultiMapModel(model));
		}
	}

	@Override
	public IDriverModel createModel(final IMutableMapNG map) {
		return new SimpleMultiMapModel(map);
	}
}
