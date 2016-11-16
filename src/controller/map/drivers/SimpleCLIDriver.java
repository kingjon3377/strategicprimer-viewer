package controller.map.drivers;

import controller.map.misc.CLIHelper;
import controller.map.misc.ICLIHelper;
import controller.map.misc.MapReaderAdapter;
import java.nio.file.Paths;
import java.util.stream.StreamSupport;
import model.misc.IMultiMapModel;
import util.Pair;
import util.Warning;

/**
 * An interface for drivers which operate on a map model of some kind and want to write
 * it out again to file when they finish.
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
public interface SimpleCLIDriver extends SimpleDriver {
	/**
	 * Run the driver. If the driver is a GUIDriver, this should use
	 * SwingUtilities.invokeLater(); if it's a CLIDriver, that's not necessary. This
	 * default implementation assumes a CLI driver, and writes the model back to file(s)
	 * after calling startDriver with the model.
	 *
	 *
	 *
	 * @param cli
	 * @param options
	 * @param args any command-line arguments that should be passed to the driver.
	 * @throws DriverFailedException if it's impossible for the driver to start.
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	@Override
	default void startDriver(final ICLIHelper cli, final SPOptions options,
							 final String... args)
			throws DriverFailedException {
		switch (usage().getParamsWanted()) {
		case None:
			if (args.length == 0) {
				startDriver();
				return;
			} else {
				throw new IncorrectUsageException(usage());
			}
		case AnyNumber:
			if (args.length == 0) {
				startDriver();
				return;
			} else {
				break;
			}
		case AtLeastOne: // Fall through
			if (args.length == 0) {
				throw new IncorrectUsageException(usage());
			}
			break;
		case One:
			if (args.length != 1) {
				throw new IncorrectUsageException(usage());
			}
			break;
		case Two:
			if (args.length != 2) {
				throw new IncorrectUsageException(usage());
			}
			break;
		case AtLeastTwo:
			if (args.length < 2) {
				throw new IncorrectUsageException(usage());
			}
			break;
		}
		final MapReaderAdapter reader = new MapReaderAdapter();
		// We declare it as an IMultiMapModel so we can correct the current turn
		// in all maps if needed
		final IMultiMapModel model =
				reader.readMultiMapModel(Warning.Ignore, Paths.get(args[0]),
						MapReaderAdapter.namesToFiles(true, args));
		if (options.hasOption("--current-turn")) {
			final int currentTurn =
					Integer.parseInt(options.getArgument("--current-turn"));
			StreamSupport.stream(model.getAllMaps().spliterator(), false)
					.map(Pair::first).forEach(map -> map.setCurrentTurn(currentTurn));
		}
		startDriver(new CLIHelper(), options, model);
		reader.writeModel(model);
	}
}
