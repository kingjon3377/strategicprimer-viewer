package drivers.common;

/**
 * An interface for the apps in this suite, so a single entry-point can start
 * different apps based on options and common code (e.g. file handling) can be
 * centralized instead of duplicated.
 *
 * TODO: Look into how to emulate enumerated subtypes in pure Java.
 */
public interface ISPDriver /*of UtilityDriver|ModelDriver*/ {
	/**
	 * Options (where supported) controlling the driver's behavior
	 */
	SPOptions getOptions();
}
