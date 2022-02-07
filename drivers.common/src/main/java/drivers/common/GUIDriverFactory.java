package drivers.common;

import java.nio.file.Path;
import drivers.common.cli.ICLIHelper;

/**
 * An interface for factories producing GUI drivers. This interface exists so
 * the app-chooser can detect such drivers before instantiating them (which
 * requires deserializing possibly-large maps).
 */
public interface GUIDriverFactory extends ModelDriverFactory {
	/**
	 * Create a new instance of the driver with the given environment.
	 *
	 * @param cli The interface to interact with the user, either on the
	 * console or in a window emulating a console
	 * @param options Any (already-processed) command-line options"
	 * @param model The driver-model that should be used by the app.
	 */
	@Override
	GUIDriver createDriver(ICLIHelper cli, SPOptions options, IDriverModel model);

	/**
	 * Ask the user to choose a file or files. (Or do something equivalent to produce a filename.)
	 */
	Iterable<Path> askUserForFiles() throws DriverFailedException;
}
