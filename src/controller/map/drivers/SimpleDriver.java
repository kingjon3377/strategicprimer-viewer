package controller.map.drivers;

import controller.map.misc.FileChooser;
import controller.map.misc.ICLIHelper;
import controller.map.misc.MapReaderAdapter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Consumer;
import model.map.IMutableMapNG;
import model.misc.IMultiMapModel;
import util.EqualsAny;
import util.Pair;
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
 * Foundation; see COPYING or
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
	 *
	 * @throws DriverFailedException on any failure
	 */
	default void startDriver() throws DriverFailedException {
		throw new DriverFailedException("Driver does not support no-arg operation",
											   new IllegalStateException("Driver does " +
																				 "not " +
																				 "support no-arg operation"));
	}

	/**
	 * Run the driver. If the driver is a GUIDriver, this should use
	 * SwingUtilities.invokeLater(); if it's a CLIDriver, that's not necessary. This
	 * default implementation does not write to file after running the driver on the
	 * driver model.
	 *
	 * @param cli the interface for user I/O
	 * @param options command-line options passed in
	 * @param args    any command-line arguments that should be passed to the driver.
	 * @throws DriverFailedException if it's impossible for the driver to start.
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	@Override
	default void startDriver(final ICLIHelper cli, final SPOptions options,
							 final String... args)
			throws DriverFailedException {
		final ParamCount desiderata = usage().getParamsWanted();
		final Consumer<IMutableMapNG> turnFixer;
		if (options.hasOption("--current-turn")) {
			final int currentTurn =
					Integer.parseInt(options.getArgument("--current-turn"));
			turnFixer = map -> map.setCurrentTurn(currentTurn);
		} else {
			turnFixer = map -> {
			};
		}
		if (args.length == 0) {
			if (EqualsAny.equalsAny(desiderata, ParamCount.None,
					ParamCount.AnyNumber)) {
				startDriver(cli, options);
			} else if (EqualsAny.equalsAny(desiderata, ParamCount.Two,
					ParamCount.AtLeastTwo)) {
				final Path masterPath = askUserForFile();
				final Path subPath = askUserForFile();
				final IMultiMapModel mapModel = new MapReaderAdapter()
														.readMultiMapModel(
																Warning.DEFAULT,
																masterPath,
																subPath);
				mapModel.streamAllMaps().map(Pair::first).forEach(turnFixer);
				startDriver(cli, options, mapModel);
			} else {
				final IMultiMapModel mapModel = new MapReaderAdapter()
														.readMultiMapModel(
																Warning.DEFAULT,
																askUserForFile());
				mapModel.streamAllMaps().map(Pair::first).forEach(turnFixer);
				startDriver(cli, options, mapModel);
			}
		} else if (ParamCount.None == desiderata) {
			throw new IncorrectUsageException(usage());
		} else if ((args.length == 1) && EqualsAny.equalsAny(desiderata,
				ParamCount.Two, ParamCount.AtLeastTwo)) {
			final IMultiMapModel mapModel = new MapReaderAdapter()
													.readMultiMapModel(Warning.DEFAULT,
															Paths.get(args[0]),
															askUserForFile());
			mapModel.streamAllMaps().map(Pair::first).forEach(turnFixer);
			startDriver(cli, options, mapModel);
		} else {
			final IMultiMapModel mapModel = new MapReaderAdapter()
													.readMultiMapModel(Warning.DEFAULT,
															Paths.get(args[0]),
															MapReaderAdapter
																	.namesToFiles(true,
																			args));
			mapModel.streamAllMaps().map(Pair::first).forEach(turnFixer);
			startDriver(cli, options, mapModel);
		}
	}

	/**
	 * Ask the user to choose a file.
	 *
	 * @return the file chosen
	 * @throws DriverFailedException if the user fails to choose
	 */
	default Path askUserForFile() throws DriverFailedException {
		try {
			return new FileChooser(Optional.empty()).getFile();
		} catch (final FileChooser.ChoiceInterruptedException except) {
			throw new DriverFailedException("Choice interrupted or user didn't choose",
												   except);
		}
	}
}
