package utility;

import drivers.common.cli.ICLIHelper;

import drivers.common.SPOptions;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.IDriverModel;
import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.ModelDriver;

import legacy.map.IMutableLegacyMap;

import com.google.auto.service.AutoService;

import java.util.EnumSet;

/**
 * A factory for the hackish driver to fix missing content in the map, namely
 * units with "TODO" for their "kind" and aquatic villages with non-aquatic
 * races.
 */
@AutoService(DriverFactory.class)
public final class TodoFixerFactory implements ModelDriverFactory {
	private final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "fix-todos",
			ParamCount.AtLeastOne, "Fix TODOs in maps",
			"Fix TODOs in unit kinds and aquatic villages with non-aquatic races",
			EnumSet.noneOf(IDriverUsage.DriverMode.class));

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		if (model instanceof final UtilityDriverModel udm) {
			return new TodoFixerCLI(cli, udm);
		} else {
			return createDriver(cli, options, new UtilityDriverModel(model));
		}
	}

	@Override
	public IDriverModel createModel(final IMutableLegacyMap map) {
		return new UtilityDriverModel(map);
	}
}
