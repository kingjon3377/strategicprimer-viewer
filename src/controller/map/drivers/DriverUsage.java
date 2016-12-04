package controller.map.drivers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class to represent usage information for drivers, for use in the AppStarter and in
 * help text.
 *
 * @author Jonathan Lovelace
 */
public class DriverUsage {
	/**
	 * Whether the driver is graphical or not.
	 */
	private final boolean graphical;
	/**
	 * The short option to give to AppStarter to get this driver.
	 */
	private final String shortOption;
	/**
	 * The long option to give to AppStarter to get this driver.
	 */
	private final String longOption;
	/**
	 * How many parameters this driver wants.
	 */
	private final ParamCount paramsWanted;
	/**
	 * A short description of the driver.
	 */
	private final String shortDescription;
	/**
	 * A longer description of the driver.
	 */
	private final String longDescription;
	/**
	 * The options this driver supports.
	 */
	private final List<String> supportedOptions = new ArrayList<>();
	/**
	 * A description of the first (non-option) parameter, for use in a usage statement.
	 * Defaults to "filename.xml"
	 */
	private String firstParamDesc = "filename.xml";
	/**
	 * A description of a later parameter, for use in a usage statement. (We assume that
	 * all parameters after the first should be described similarly.) Defaults to
	 * "filename.xml"
	 */
	private String subsequentParamDesc = "filename.xml";

	/**
	 * Constructor.
	 *
	 * @param graph     whether this driver is graphical or not
	 * @param shortOpt  the short (generally one character) option to give to AppStarter
	 *                  to get this driver
	 * @param longOpt   the long option to give to AppStarter to get this driver
	 * @param params    how many parameters the driver wants
	 * @param shortDesc a short description of the driver
	 * @param longDesc  a longer description of the driver.
	 */
	public DriverUsage(final boolean graph, final String shortOpt,
					   final String longOpt, final ParamCount params,
					   final String shortDesc, final String longDesc) {
		graphical = graph;
		shortOption = shortOpt;
		longOption = longOpt;
		paramsWanted = params;
		shortDescription = shortDesc;
		longDescription = longDesc;
	}

	/**
	 * @return whether the driver is graphical or not.
	 */
	public boolean isGraphical() {
		return graphical;
	}

	/**
	 * @return the short option to give to AppStarter to get this driver
	 */
	public String getShortOption() {
		return shortOption;
	}

	/**
	 * @return the long option to give to AppStarter to get this driver
	 */
	public String getLongOption() {
		return longOption;
	}

	/**
	 * @return how many parameters this driver wants
	 */
	public ParamCount getParamsWanted() {
		return paramsWanted;
	}

	/**
	 * @return a short (one-line) description of the driver.
	 */
	public String getShortDescription() {
		return shortDescription;
	}

	/**
	 * @return a long(er) description of the driver.
	 */
	public String getLongDescription() {
		return longDescription;
	}

	/**
	 * @return a description of the driver
	 */
	@Override
	public String toString() {
		return shortDescription;
	}

	/**
	 * @return a description of the first parameter for use in a usage statement;
	 * defaults
	 * to "filename.xml"
	 */
	public String getFirstParamDesc() {
		return firstParamDesc;
	}

	/**
	 * Note that this is ignored by its users if getParamsWanted() returns AnyNumber.
	 *
	 * @param newDesc a new description for the first parameter
	 */
	public void setFirstParamDesc(final String newDesc) {
		firstParamDesc = newDesc;
	}

	/**
	 * @return a description of each parameter after the first for use in a usage
	 * statement; defaults to "filename.xml"
	 */
	public String getSubsequentParamDesc() {
		return subsequentParamDesc;
	}

	/**
	 * @param newDesc a new description for each parameter after the first
	 */
	public void setSubsequentParamDesc(final String newDesc) {
		subsequentParamDesc = newDesc;
	}

	/**
	 * This is only used to tell the user about the option, so describe it accordingly.
	 *
	 * @param option an option to add to the list of supported options.
	 */
	public void addSupportedOption(final String option) {
		supportedOptions.add(option);
	}

	/**
	 * @return a list of the options this driver supports, to show the user.
	 */
	public List<String> getSupportedOptions() {
		return Collections.unmodifiableList(supportedOptions);
	}
}
