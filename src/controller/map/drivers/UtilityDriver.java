package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import model.misc.IDriverModel;

/**
 * An interface for utility drivers, which operate on files rather than a map model.
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
@FunctionalInterface
public interface UtilityDriver extends ISPDriver {
	/**
	 * String to throw if utility driver called with driver model.
	 */
	String ERR_STR = "A utility driver can't operate on a driver model";

	/**
	 * Try to start the driver. This default method always throws, because a utility
	 * driver most often can't operate on a driver model.
	 *
	 * @param cli
	 * @param options
	 * @param model   the driver-model that should be used by the app
	 * @throws DriverFailedException always: a utility driver operates on files directly,
	 *                               not a driver model.
	 */
	@Override
	default void startDriver(final ICLIHelper cli, final SPOptions options,
							 final IDriverModel model)
			throws DriverFailedException {
		throw new DriverFailedException(new IllegalStateException(ERR_STR));
	}
}
