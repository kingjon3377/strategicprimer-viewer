package utility;

import drivers.common.UtilityDriver;
import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.SPOptions;
import drivers.common.UtilityDriverFactory;
import drivers.common.DriverFactory;

import drivers.common.cli.ICLIHelper;

import com.google.auto.service.AutoService;

/**
 * A factory for a driver to check every map file in a list for errors and report the results in a window.
 */
@AutoService(DriverFactory.class)
public final class MapCheckerGUIFactory implements UtilityDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.Graphical, "check",
			ParamCount.AnyNumber, "Check map for errors", "Check a map file for errors, deprecated syntax, etc.", false,
			true);

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public UtilityDriver createDriver(final ICLIHelper cli, final SPOptions options) {
		return new MapCheckerGUI();
	}
}
