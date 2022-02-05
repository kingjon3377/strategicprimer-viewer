package drivers.common;

import common.map.IMutableMapNG;
import drivers.common.cli.ICLIHelper;

/**
 * An interface for factories for drivers that operate on map models rather
 * than directly on files, which must also produce the models that their
 * drivers consume.
 *
 * TODO: Take a more specific model interface as a type parameter.
 */
public interface ModelDriverFactory extends DriverFactory {
	/**
	 * Create a new instance of the driver with the given environment.
	 * @param cli The interface to interact with the user, either on the
	 * console or in a window emulating a console
	 * @param options Any (already-processed) command-line options
	 * @param model The driver-model that should be used by the app.
	 */
	ModelDriver createDriver(ICLIHelper cli, SPOptions options, IDriverModel model);

	/**
	 * Create a model to pass to {@link createDriver}. The 'modified' flag is set to false.
	 * @param map The map
	 */
	default IDriverModel createModel(final IMutableMapNG map) {
		return new SimpleDriverModel(map);
	}
}
