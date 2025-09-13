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

import java.io.File;
import java.util.EnumSet;

/**
 * A factory for an app to let the user create a map from an image.
 */
@AutoService(DriverFactory.class)
public final class ImporterFactory implements UtilityDriverFactory {
	private static final String SAMPLE_IMAGE = "%spath%sto%simagge.png".formatted(File.separator, File.separator,
			File.separator);

	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "import",
			ParamCount.AtLeastOne, "Import terrain data from a raster image",
			"Import terrain data from a raster image", EnumSet.noneOf(IDriverUsage.DriverMode.class),
			SAMPLE_IMAGE, SAMPLE_IMAGE, "--size=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public UtilityDriver createDriver(final ICLIHelper cli, final SPOptions options) {
		return new ImporterDriver(cli, options);
	}
}
