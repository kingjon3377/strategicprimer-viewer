package drivers.common;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DriverUsage implements IDriverUsage {
	/**
	 * Whether this driver is graphical or not.
	 */
	private final DriverMode mode;

	/**
	 * Whether this driver is graphical or not.
	 */
	@Override
	public final DriverMode getMode() {
		return mode;
	}

	/**
	 * The subcomand with which the user can select this driver.
	 */
	private final String invocation;

	/**
	 * The subcomand with which the user can select this driver.
	 */
	@Override
	public final String getInvocation() {
		return invocation;
	}

	/**
	 * How many parameters this driver wants.
	 */
	private final ParamCount paramsWanted;

	/**
	 * How many parameters this driver wants.
	 */
	@Override
	public final ParamCount getParamsWanted() {
		return paramsWanted;
	}

	/**
	 * A short description of the driver
	 */
	private final String shortDescription;

	/**
	 * A short description of the driver
	 */
	@Override
	public final String getShortDescription() {
		return shortDescription;
	}

	/**
	 * A longer description of the driver
	 */
	private final String longDescription;

	/**
	 * A longer description of the driver
	 */
	@Override
	public final String getLongDescription() {
		return longDescription;
	}

	/**
	 * Which app-choosing list(s) to show this app in.
	 */
	private final Set<DriverMode> listsToShowIn;

	/**
	 * A description of the first (non-option) parameter, for use in a usage statement.
	 */
	private final String firstParamDescription;

	/**
	 * A description of the first (non-option) parameter, for use in a usage statement.
	 */
	@Override
	public final String getFirstParamDescription() {
		return firstParamDescription;
	}

	/**
	 * A description of a later parameter, for use in a usage statement.
	 * (We assume that all parameters after the first should be described
	 * similarly.)
	 */
	private final String subsequentParamDescription;

	/**
	 * A description of a later parameter, for use in a usage statement.
	 * (We assume that all parameters after the first should be described
	 * similarly.)
	 */
	@Override
	public final String getSubsequentParamDescription() {
		return subsequentParamDescription;
	}

	/**
	 * The options this driver supports.
	 */
	private final List<String> supportedOptions;

	/**
	 * The options this driver supports.
	 */
	@Override
	public final Iterable<String> getSupportedOptions() {
		return supportedOptions;
	}

	public DriverUsage(final DriverMode mode, final String invocation, final ParamCount paramsWanted,
	                   final String shortDescription, final String longDescription,
	                   final Set<DriverMode> listsToShowIn) {
		this(mode, invocation, paramsWanted, shortDescription, longDescription,
				listsToShowIn, "filename.xml");
	}

	public DriverUsage(final DriverMode mode, final String invocation, final ParamCount paramsWanted,
	                   final String shortDescription, final String longDescription, final Set<DriverMode> listsToShowIn,
	                   final String firstParamDescription) {
		this(mode, invocation, paramsWanted, shortDescription, longDescription,
				listsToShowIn, firstParamDescription, "filename.xml");
	}

	/**
	 * @param mode                       The mode of this driver (graphical or CLI)
	 * @param invocation                 Subcommand with which one can invoke this driver.
	 * @param paramsWanted               How many parameters this driver wants.
	 * @param shortDescription           A short description of the driver
	 * @param longDescription            A longer description of the driver
	 * @param listsToShowIn              Which lists for the user to choose an app from to show this app in
	 * @param firstParamDescription      A description of the first (non-option)
	 *                                   parameter, for use in a usage statement.
	 * @param subsequentParamDescription A description of a later
	 *                                   parameter, for use in a usage statement. (We assume that all
	 *                                   parameters after the first should be described similarly.)
	 * @param supportedOptions           The options this driver supports.
	 */
	public DriverUsage(final DriverMode mode, final String invocation, final ParamCount paramsWanted,
	                   final String shortDescription, final String longDescription, final Set<DriverMode> listsToShowIn,
	                   final String firstParamDescription, final String subsequentParamDescription,
	                   final String... supportedOptions) {
		this.mode = mode;
		this.invocation = invocation;
		this.paramsWanted = paramsWanted;
		this.shortDescription = shortDescription;
		this.longDescription = longDescription;
		this.listsToShowIn = Collections.unmodifiableSet(listsToShowIn);
		this.firstParamDescription = firstParamDescription;
		this.subsequentParamDescription = subsequentParamDescription;
		this.supportedOptions = List.of(supportedOptions);
	}

	@Override
	public final boolean includeInList(final DriverMode mode) {
		return listsToShowIn.contains(mode);
	}
}
