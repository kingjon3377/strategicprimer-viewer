package drivers.common;

import drivers.common.cli.ICLIHelper;

/**
 * An interface for factories for drivers that operate on files rather than pre-parsed maps.
 */
public interface UtilityDriverFactory extends DriverFactory {
	/**
	 * The driver.
	 *
	 * @param cli     The interface to interact with the user, either on the
	 *                console or in a window emulating a console
	 * @param options Any (already-processed) command-line options
	 */
	UtilityDriver createDriver(ICLIHelper cli, SPOptions options);
}
