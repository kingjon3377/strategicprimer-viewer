package controller.map.drivers;

import java.nio.file.Paths;
import java.util.stream.Stream;
import view.map.misc.MapCheckerFrame;

/**
 * A driver to check every map file in a list for errors and report the results in a
 * window.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class MapCheckerGUI implements UtilityDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(true, "-k", "--check", ParamCount.AtLeastOne,
								"Check map for errors",
								"Check a map file for errors, deprecated syntax, etc.",
								MapCheckerGUI.class);

	/**
	 * Run the driver.
	 *
	 * @param options
	 * @param args command-line arguments
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	@Override
	public void startDriver(final SPOptions options, final String... args) {
		final MapCheckerFrame window = new MapCheckerFrame();
		window.setVisible(true);
		Stream.of(args).map(Paths::get).forEach(window::check);
	}

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE;
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "MapCheckerGUI";
	}
}
