package drivers.map_viewer;

import com.google.auto.service.AutoService;

import drivers.common.SPOptions;
import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.UtilityDriver;
import drivers.common.DriverFactory;
import drivers.common.UtilityDriverFactory;

import drivers.common.cli.ICLIHelper;

import java.util.EnumSet;

/**
 * A factory for a driver to compare the performance of TileDrawHelpers.
 */
@AutoService(DriverFactory.class)
public final class DrawHelperComparatorFactory implements UtilityDriverFactory {
	public static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.Graphical, "drawing-performance",
			ParamCount.AtLeastOne, "Test drawing performance.",
			"Test the performance of map-rendering implementations using a variety of automated tests.",
			EnumSet.of(IDriverUsage.DriverMode.CommandLine), "--report=out.csv");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public UtilityDriver createDriver(final ICLIHelper cli, final SPOptions options) {
		return new DrawHelperComparator(cli, options);
	}
}
