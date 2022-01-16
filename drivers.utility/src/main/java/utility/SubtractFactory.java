package utility;

import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.ParamCount;
import drivers.common.DriverUsage;
import drivers.common.IDriverUsage;
import drivers.common.ModelDriver;
import drivers.common.SPOptions;
import drivers.common.IDriverModel;
import drivers.common.cli.ICLIHelper;
import common.map.IMutableMapNG;

import com.google.auto.service.AutoService;

/**
 * An app to produce a difference between two maps, to aid understanding what
 * an explorer has found. This modifies non-main maps in place; only run on
 * copies or under version control!
 */
@AutoService(DriverFactory.class)
public class SubtractFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "subtract", ParamCount.AtLeastTwo,
		"Subtract one map from another",
		"Remove everything known in a base map from submaps for easier comparison", false, false,
		"baseMap.xml", "operand.xml");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
		if (model instanceof UtilityDriverModel) {
			return new SubtractCLI((UtilityDriverModel) model);
		} else {
			return createDriver(cli, options, new UtilityDriverModel(model));
		}
	}

	@Override
	public IDriverModel createModel(IMutableMapNG map) {
		return new UtilityDriverModel(map);
	}
}
