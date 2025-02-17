package query;

import drivers.common.SPOptions;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.IDriverModel;
import drivers.common.ModelDriverFactory;
import drivers.common.DriverFactory;
import drivers.common.ModelDriver;

import drivers.common.SimpleDriverModel;
import drivers.common.cli.ICLIHelper;

import com.google.auto.service.AutoService;
import legacy.map.IMutableLegacyMap;

import java.util.EnumSet;

/**
 * A factory for the driver to "query" the driver model about various things.
 */
@AutoService(DriverFactory.class)
public final class QueryCLIFactory implements ModelDriverFactory<IDriverModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "query",
			ParamCount.One, "Answer questions about a map.",
			"Answer questions about a map, such as counting workers or calculating distances.",
			EnumSet.of(IDriverUsage.DriverMode.CommandLine));

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		return new QueryCLI(cli, model);
	}

	@Override
	public IDriverModel createModel(final IMutableLegacyMap map)  {
		return new SimpleDriverModel(map);
	}

	@Override
	public IDriverModel createModel(IDriverModel model) {
		return new SimpleDriverModel(model);
	}
}
