package drivers.common;

/**
 * An interface for objects representing usage information for drivers, for use
 * in the app starter and in help text.
 */
public interface IDriverUsage {
	public enum DriverMode {
		Graphical, CommandLine
	}
	/**
	 * The mode of the driver.
	 * <p>
	 * TODO: We'd like to get rid of this as a general thing, and make it
	 * instead an option that the most important drivers respect.
	 */
	DriverMode getMode();

	/**
	 * The subcomand with which the user can select this driver.
	 */
	String getInvocation();

	/**
	 * How many non-option parameters this driver wants.
	 */
	ParamCount getParamsWanted();

	/**
	 * A short (one-line at most) description of the driver.
	 */
	String getShortDescription();

	/**
	 * A long(er) description of the driver.
	 */
	String getLongDescription();

	/**
	 * A description of the first parameter for use in a usage statement.
	 */
	String getFirstParamDescription();

	/**
	 * A description of parameters other than the first for use in a usage statement.
	 */
	String getSubsequentParamDescription();

	/**
	 * Options this driver supports. (To show the user, so "=NN" to mean a
	 * numeric option is reasonable.)
	 */
	Iterable<String> getSupportedOptions();

	/**
	 * Whether this driver should be included in the list presented for the
	 * user to choose from.
	 *
	 * @param mode the mode corresponding to the list being shown
	 */
	boolean includeInList(DriverMode mode);
}
