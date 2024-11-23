package drivers.common;

import legacy.map.HasName;

/**
 * An interface for factories for drivers, so one run() method can start
 * different drivers based on user-supplied options, without the drivers having
 * to have no-arg initializers.
 *
 * TODO: Take the driver as a type parameter.
 *
 * TODO: Make ISPDriver implementations no longer public
 *
 * TODO: Investigate possibility of doing something like enumerated subtypes in Java
 */
public interface DriverFactory /*of UtilityDriverFactory|ModelDriverFactory*/ extends HasName {
	/**
	 * An object giving details to describe how the driver should be invoked and used.
	 */
	IDriverUsage getUsage();

	/**
	 * What to call this driver in a CLI list.
	 */
	@Override
	default String getName() {
		return getUsage().getShortDescription();
	}
}
