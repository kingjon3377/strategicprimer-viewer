package controller.map.drivers;

/**
 * An interface for objects representing usage information for drivers, for use in the
 * AppStarter and in help text.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface IDriverUsage {
	/**
	 * Whether the driver is a GUI.
	 * @return whether the driver is graphical or not.
	 */
	boolean isGraphical();

	/**
	 * The short option to select this driver.
	 * @return the short option to give to AppStarter to get this driver
	 */
	String getShortOption();

	/**
	 * The long option to select this driver.
	 * @return the long option to give to AppStarter to get this driver
	 */
	String getLongOption();

	/**
	 * How many non-option parameters this driver wants.
	 * @return how many parameters this driver wants
	 */
	ParamCount getParamsWanted();

	/**
	 * A short description of the driver.
	 * @return a short (one-line) description of the driver.
	 */
	String getShortDescription();

	/**
	 * A long(er) description of the driver.
	 * @return a long(er) description of the driver.
	 */
	String getLongDescription();

	/**
	 * A description of the first parameter.
	 * @return a description of the first parameter for use in a usage statement;
	 * defaults to "filename.xml"
	 */
	String getFirstParamDesc();

	/**
	 * A description of parameters other than the first.
	 * @return a description of each parameter after the first for use in a usage
	 * statement; defaults to "filename.xml"
	 */
	String getSubsequentParamDesc();

	/**
	 * A list of options this driver supports.
	 * @return a list of the options this driver supports, to show the user.
	 */
	Iterable<String> getSupportedOptions();
}
