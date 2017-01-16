package controller.map.drivers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class to represent usage information for drivers, for use in the AppStarter and in
 * help text.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class DriverUsage implements IDriverUsage {
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
	 * Whether the driver is a GUI.
	 * @return whether the driver is graphical or not.
	 */
	@Override
	public boolean isGraphical() {
		return graphical;
	}

	/**
	 * The short option that invokes this driver.
	 * @return the short option to give to AppStarter to get this driver
	 */
	@Override
	public String getShortOption() {
		return shortOption;
	}

	/**
	 * The long option that invokes this driver.
	 * @return the long option to give to AppStarter to get this driver
	 */
	@Override
	public String getLongOption() {
		return longOption;
	}

	/**
	 * How many parameters this driver wants.
	 * @return how many parameters this driver wants
	 */
	@Override
	public ParamCount getParamsWanted() {
		return paramsWanted;
	}

	/**
	 * A short description of this driver.
	 * @return a short (one-line) description of the driver.
	 */
	@Override
	public String getShortDescription() {
		return shortDescription;
	}

	/**
	 * A longer description of this driver.
	 * @return a long(er) description of the driver.
	 */
	@Override
	public String getLongDescription() {
		return longDescription;
	}

	/**
	 * Delegates to getShortDescription().
	 * @return a description of the driver
	 */
	@Override
	public String toString() {
		return shortDescription;
	}

	/**
	 * A description of the first parameter.
	 * @return a description of the first parameter for use in a usage statement;
	 * defaults to "filename.xml"
	 */
	@Override
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
	 * A description of subsequent parameters.
	 * @return a description of each parameter after the first for use in a usage
	 * statement; defaults to "filename.xml"
	 */
	@Override
	public String getSubsequentParamDesc() {
		return subsequentParamDesc;
	}

	/**
	 * Set the subsequent-parameter description.
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
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	public void addSupportedOption(final String option) {
		supportedOptions.add(option);
	}

	/**
	 * The list of supported options.
	 * @return a list of the options this driver supports, to show the user.
	 */
	@Override
	public Iterable<String> getSupportedOptions() {
		return Collections.unmodifiableList(supportedOptions);
	}
}
