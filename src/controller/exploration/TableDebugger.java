package controller.exploration;

import controller.map.drivers.DriverFailedException;
import controller.map.drivers.DriverUsage;
import controller.map.drivers.ParamCount;
import controller.map.drivers.SimpleCLIDriver;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import model.exploration.old.EncounterTable;
import model.exploration.old.ExplorationRunner;
import model.exploration.old.MissingTableException;
import model.misc.IDriverModel;
import util.LineEnd;
import util.NullCleaner;
import util.TypesafeLogger;
import view.util.SystemOut;

/**
 * A driver to help debug exploration tables.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class TableDebugger implements SimpleCLIDriver {
	/**
	 * Usage object.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-T", "--table-debug", ParamCount.None,
								   "Debug old-model encounter tables",
								   "See whether old-model encounter tables refer to a " +
										   "nonexistent table",
								   TableDebugger.class);
	/**
	 * @return the usage object
	 */
	@Override
	public DriverUsage usage() {
		return USAGE;
	}
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(TableDebugger.class);
	/**
	 * The exploration runner.
	 */
	private final ExplorationRunner runner;

	/**
	 * Constructor.
	 *
	 * @param explorationRunner an exploration runner loaded with all the tables we want.
	 */
	protected TableDebugger(final ExplorationRunner explorationRunner) {
		runner = explorationRunner;
	}

	/**
	 * Print all possible results from the tables.
	 *
	 * @param ostream the stream to print to.
	 * @throws MissingTableException if a referenced table isn't there
	 * @throws IOException           on error writing to the stream
	 */
	private void debugTables(final Appendable ostream)
			throws MissingTableException, IOException {
		runner.verboseRecursiveCheck(ostream);
		final EncounterTable mainTable = runner.getTable("main");
		debugSingleTable("", "", mainTable, "main", ostream,
				new HashSet<>());
	}

	/**
	 * Print all possible results from a table.
	 *
	 * @param before    the string to print before each result (from the calling table)
	 * @param after     the string to print after each result (from the calling table)
	 * @param table     the table to debug
	 * @param tableName the name of the table
	 * @param ostream   the stream to print to
	 * @param set       the set of tables already on the stack, to prevent infinite
	 *                  recursion
	 * @throws MissingTableException if a table is missing
	 * @throws IOException           on I/O error writing to the stream
	 */
	@SuppressWarnings("StringConcatenationMissingWhitespace")
	private void debugSingleTable(final String before, final String after,
								final EncounterTable table,
								@SuppressWarnings("TypeMayBeWeakened")
								final String tableName, final Appendable ostream,
								final Set<EncounterTable> set)
			throws MissingTableException, IOException {
		if (set.contains(table)) {
			ostream.append("table ");
			ostream.append(tableName);
			ostream.append(" is already on the stack, skipping ...");
			ostream.append(LineEnd.LINE_SEP);
			ostream.append("The cause was: ");
			ostream.append(before);
			ostream.append('#');
			ostream.append(tableName);
			ostream.append('#');
			ostream.append(after);
			ostream.append(LineEnd.LINE_SEP);
			return;
		} // else
		set.add(table);
		for (final String value : table.allEvents()) {
			if (value.contains("#")) {
				final String[] parsed = value.split("#", 3);
				final String callee = NullCleaner.assertNotNull(parsed[1]);
				debugSingleTable(before + parsed[0], parsed[2] + after,
						runner.getTable(callee), callee, ostream, set);
			} else {
				ostream.append(before);
				ostream.append(value);
				ostream.append(after);
			}
		}
		set.remove(table);
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TableDebugger";
	}

	/**
	 * A utility driver method that loads all files in tables/ under the current
	 * directory, then checks to see whether any references a nonexistent table, then
	 * does further tests for debugging purposes.
	 * @throws DriverFailedException on missing table or I/O error
	 */
	@Override
	public void startDriver() throws DriverFailedException {
		final ExplorationRunner localRunner = new ExplorationRunner();
		TableLoader.loadAllTables("tables", localRunner);
		try {
			new TableDebugger(localRunner).debugTables(SystemOut.SYS_OUT);
		} catch (final MissingTableException e) {
			throw new DriverFailedException("Missing table", e);
		} catch (final IOException e) {
			//noinspection HardcodedFileSeparator
			throw new DriverFailedException("I/O error writing to stdout", e);
		}
	}

	/**
	 * @param model ignored
	 * @throws DriverFailedException on missing table or I/O error
	 */
	@Override
	public void startDriver(final IDriverModel model) throws DriverFailedException {
		LOGGER.warning("TableDebugger doesn't need a driver model");
		startDriver();
	}
}
