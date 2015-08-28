package controller.map.drivers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.text.ParseException;

import model.map.Point;
import model.map.PointFactory;
import model.mining.LodeStatus;
import model.mining.MiningModel;
import util.NullCleaner;

/**
 * A driver to create a spreadsheet model of a mine.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2014-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class MiningCLI {
	/**
	 * Do not instantiate.
	 */
	private MiningCLI() {
		// Do nothing
	}
	/**
	 * @param args
	 *            Arg 0 is the name of a file to write the CSV to; Arg 1 is the
	 *            value of the top center (as an index into the LodeStatus
	 *            values array); Arg 2 (optional) is a value to seed the RNG
	 *            with.
	 */
	public static void main(final String[] args) {
		if (args.length < 2) {
			System.out.println("Usage: MiningCLI output.csv status [seed]");
			System.exit(1);
			return;
		}
		int index;
		try {
			index = NumberFormat.getIntegerInstance().parse(args[1]).intValue();
		} catch (ParseException e) {
			System.err.println("Non-numeric status index");
			System.exit(2);
			return;
		}
		final LodeStatus initial =
				NullCleaner.assertNotNull(LodeStatus.values()[index]);
		final long seed;
		if (args.length >= 3) {
			seed = Long.parseLong(args[2]);
		} else {
			seed = System.currentTimeMillis();
		}
		final MiningModel model = new MiningModel(initial, seed);
		final Point lowerRight = model.getMaxPoint();
		final int maxCol = lowerRight.col + 1;
		final int maxRow = lowerRight.row + 1;
		final String out = args[0];
		try (final PrintWriter ostream = new PrintWriter(new FileWriter(out))) {
			for (int row = 0; row < maxRow; row++) {
				for (int col = 0; col < maxCol; col++) {
					ostream.print(model.statusAt(PointFactory.point(row, col))
							.getRatio());
					ostream.print(',');
				}
				ostream.println();
			}
		} catch (IOException except) {
			System.out.println("I/O error writing to file");
			System.exit(2);
		}
	}

}
