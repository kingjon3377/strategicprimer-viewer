package drivers.common;

import drivers.common.cli.ICLIHelper;

public interface ViewerDriverFactory extends GUIDriverFactory {
	@Override
	ViewerDriver createDriver(ICLIHelper cli, SPOptions options, IDriverModel model);
}
