package utility;

import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.ParamCount;
import drivers.common.DriverUsage;
import drivers.common.IDriverUsage;
import drivers.common.ModelDriver;
import drivers.common.SPOptions;
import drivers.common.cli.ICLIHelper;
import legacy.map.IMutableLegacyMap;

import com.google.auto.service.AutoService;

import java.util.EnumSet;

/**
 * An app to produce a difference between two maps, to aid understanding what
 * an explorer has found. This modifies non-main maps in place; only run on
 * copies or under version control!
 */
@AutoService(DriverFactory.class)
public final class SubtractFactory implements ModelDriverFactory<UtilityDriverModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "subtract",
			ParamCount.AtLeastTwo, "Subtract one map from another",
			"Remove everything known in a base map from submaps for easier comparison",
			EnumSet.noneOf(IDriverUsage.DriverMode.class), "baseMap.xml", "operand.xml");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final UtilityDriverModel model) {
		return new SubtractCLI(model);
	}

	@Override
	public UtilityDriverModel createModel(final IMutableLegacyMap map) {
		return new UtilityDriverModel(map);
	}
}
