package controller.map.drivers;

import controller.map.misc.FileChooser;
import controller.map.misc.FileChooser.ChoiceInterruptedException;
import controller.map.misc.MapReaderAdapter;
import java.io.File;
import util.EqualsAny;
import util.Warning;

/**
 * An interface for drivers which operate on a map model of some kind.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@FunctionalInterface
public interface SimpleDriver extends ISPDriver {
	/**
	 * (Try to) run the driver. If the driver does not need arguments, it should
	 * override this default method to support that; otherwise, this will throw,
	 * because nearly all drivers do need arguments.
	 */
	default void startDriver() throws DriverFailedException {
		throw new DriverFailedException("Driver does not support no-arg operation",
											new IllegalStateException("Driver does not " +
															"support no-arg operation"));
	}
	/**
	 * Run the driver. If the driver is a GUIDriver, this should use
	 * SwingUtilities.invokeLater(); if it's a CLIDriver, that's not necessary. This
	 * default implementation does not write to file after running the driver on the
	 * driver model.
	 *
	 * @param args any command-line arguments that should be passed to the driver.
	 * @throws DriverFailedException if it's impossible for the driver to start.
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	@Override
	default void startDriver(final String... args) throws DriverFailedException {
		if (args.length == 0) {
			if (EqualsAny.equalsAny(usage().getParamsWanted(), ParamCount.None,
					ParamCount.AnyNumber)) {
				startDriver();
			} else {
				startDriver(new MapReaderAdapter().readMultiMapModel(Warning.DEFAULT,
						askUserForFile()));
			}
		} else if (ParamCount.None == usage().getParamsWanted()) {
			throw new IncorrectUsageException(usage());
		} else if ((args.length == 1) && EqualsAny.equalsAny(usage().getParamsWanted(),
				ParamCount.Two, ParamCount.AtLeastTwo)) {
			startDriver(new MapReaderAdapter()
								.readMultiMapModel(Warning.DEFAULT, new File(args[0]),
										askUserForFile()));
		} else {
			startDriver(new MapReaderAdapter()
								.readMultiMapModel(Warning.DEFAULT, new File(args[0]),
										MapReaderAdapter.namesToFiles(true, args)));
		}
	}
	/**
	 * Ask the user to choose a file.
	 * @return the file chosen
	 * @throws DriverFailedException if the user fails to choose
	 */
	default File askUserForFile() throws DriverFailedException {
		try {
			return new FileChooser(new File("")).getFile();
		} catch (final ChoiceInterruptedException except) {
			throw new DriverFailedException("Choice interrupted or user didn't choose",
												except);
		}
	}
}
