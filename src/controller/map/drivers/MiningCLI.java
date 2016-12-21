package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.text.ParseException;
import model.map.Point;
import model.map.PointFactory;
import model.mining.LodeStatus;
import model.mining.MiningModel;

/**
 * A driver to create a spreadsheet model of a mine.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2014-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("UtilityClassCanBeEnum")
public final class MiningCLI implements UtilityDriver {
	/**
	 * An object describing how to use the driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-i", "--mining", ParamCount.Two,
								   "Create a model of a mine",
								   "Create a CSV spreadsheet representing a mine's area");

	static {
		USAGE.addSupportedOption("--seed=NN");
		USAGE.setFirstParamDesc("output.csv");
		USAGE.setSubsequentParamDesc("status");
	}

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public IDriverUsage usage() {
		return USAGE;
	}

	/**
	 * @param cli the interface for user I/O
	 * @param options any options passed to the driver
	 * @param args    Arg 0 is the name of a file to write the CSV to; Arg 1 is the value
	 *                of the top center (as an index into the LodeStatus values array)
	 * @throws DriverFailedException on incorrect usage
	 */
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final String... args)
			throws DriverFailedException {
		if (args.length != 2) {
			throw new IncorrectUsageException(USAGE);
		}
		final int index;
		try {
			index = NumberFormat.getIntegerInstance().parse(args[1]).intValue();
		} catch (final ParseException e) {
			throw new DriverFailedException("non-numeric status index", e);
		}
		final long seed;
		if (options.hasOption("--seed")) {
			try {
				seed = Long.parseLong(options.getArgument("--seed"));
			} catch (final NumberFormatException except) {
				throw new DriverFailedException("non-numeric seed", except);
			}
		} else {
			seed = System.currentTimeMillis();
		}
		final int actualIndex;
		final MiningModel.MineKind mineKind;
		if (index >= 0) {
			actualIndex = index;
			mineKind = MiningModel.MineKind.Normal;
		} else {
			actualIndex = -index;
			mineKind = MiningModel.MineKind.Banded;
		}
		final LodeStatus initial = LodeStatus.values()[actualIndex];
		final MiningModel model = new MiningModel(initial, seed, mineKind);
		final Point lowerRight = model.getMaxPoint();
		final int maxCol = lowerRight.getCol() + 1;
		final int maxRow = lowerRight.getRow() + 1;
		final String out = args[0];
		try (final PrintWriter ostream = new PrintWriter(new FileWriter(out))) {
			for (int row = 0; row < maxRow; row++) {
				for (int col = 0; col < maxCol; col++) {
					ostream.print(
							model.statusAt(PointFactory.point(row, col)).getRatio());
					ostream.print(',');
				}
				ostream.println();
			}
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			throw new DriverFailedException("I/O error writing to file", except);
		}
	}

}
