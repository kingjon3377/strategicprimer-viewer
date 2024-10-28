package utility.subset;

import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.UtilityDriver;
import drivers.common.DriverFactory;
import drivers.common.UtilityDriverFactory;

import drivers.common.cli.ICLIHelper;

import com.google.auto.service.AutoService;

import java.util.EnumSet;

/**
 * A factory for a driver to check whether player maps are subsets of the main
 * map and display the results graphically.
 */
@AutoService(DriverFactory.class)
public final class SubsetGUIFactory implements UtilityDriverFactory {
	public static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.Graphical, "subset",
			ParamCount.AtLeastOne, "Check players' maps against master",
			"""
					Check that subordinate maps are subsets of the main map, containing nothing that it does not \
					contain in the same place.""",
			EnumSet.of(IDriverUsage.DriverMode.Graphical));

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public UtilityDriver createDriver(final ICLIHelper cli, final SPOptions options) {
		return new SubsetGUI(options);
	}
}
