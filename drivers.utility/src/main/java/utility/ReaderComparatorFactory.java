package utility;

import drivers.common.UtilityDriver;
import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.SPOptions;
import drivers.common.DriverFactory;
import drivers.common.UtilityDriverFactory;

import drivers.common.cli.ICLIHelper;

import com.google.auto.service.AutoService;

/**
 * A factory for a driver to compare the performance and results of the two map reading implementations.
 */
@AutoService(DriverFactory.class)
public class ReaderComparatorFactory implements UtilityDriverFactory {
	private final IDriverUsage USAGE = new DriverUsage(false, "compare-readers", ParamCount.AtLeastOne,
			"Test map readers",
			"Test map-reading implementations by comparing their results on the same file.",
			true, false);

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public UtilityDriver createDriver(final ICLIHelper cli, final SPOptions options) {
		return new ReaderComparator(cli);
	}
}
