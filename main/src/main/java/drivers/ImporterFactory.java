package drivers;

import drivers.common.DriverFactory;
import drivers.common.IDriverUsage;
import drivers.common.UtilityDriver;
import drivers.common.SPOptions;
import drivers.common.UtilityDriverFactory;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;

import drivers.common.cli.ICLIHelper;

import com.google.auto.service.AutoService;

/**
 * A factory for an app to let the user create a map from an image.
 */
@AutoService(DriverFactory.class)
public final class ImporterFactory implements UtilityDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "import",
			ParamCount.AtLeastOne, "Import terrain data from a raster image",
			"Import terrain data from a raster image", false, false, "/path/to/image.png",
			"/path/to/image.png", "--size=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public UtilityDriver createDriver(final ICLIHelper cli, final SPOptions options) {
		return new ImporterDriver(cli, options);
	}
}
