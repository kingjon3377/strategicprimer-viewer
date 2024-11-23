package drivers.common;

import drivers.common.cli.ICLIHelper;

public interface ViewerDriverFactory<ModelType extends IDriverModel> extends GUIDriverFactory<ModelType> {
	@Override
	ViewerDriver createDriver(ICLIHelper cli, SPOptions options, ModelType model);
}
