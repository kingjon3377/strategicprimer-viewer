package query;

import common.map.IMutableMapNG;

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

/**
 * A factory for the driver to "query" the driver model about various things.
 */
@AutoService(DriverFactory.class)
public class QueryCLIFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "query",
		ParamCount.One, "Answer questions about a map.",
		"Answer questions about a map, such as counting workers or calculating distances.",
		true, false);

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
		return new QueryCLI(cli, model);
	}

	@Override
	public IDriverModel createModel(IMutableMapNG map) {
		return new SimpleDriverModel(map);
	}
}
