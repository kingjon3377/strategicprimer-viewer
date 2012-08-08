package controller.map.drivers;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import model.exploration.EncounterTable;
import model.exploration.ExplorationRunner;
import model.exploration.MissingTableException;
import view.util.SystemOut;
import controller.exploration.TableLoader;

/**
 * A driver to help debug exploration tables.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class TableDebugger {

	/**
	 * @param args ignored
	 */
	public static void main(final String[] args) {
		final ExplorationRunner runner = new ExplorationRunner();
		new TableLoader().loadAllTables("tables", runner);
		try {
			new TableDebugger(runner).debugTables(System.out);
		} catch (final MissingTableException e) {
			SystemOut.SYS_OUT.println("Missing table");
			System.exit(1);
		}
	}

	/**
	 * The exploration runner.
	 */
	private final ExplorationRunner runner;

	/**
	 * Constructor.
	 * 
	 * @param explRunner an exploration runner loaded with all the tables we
	 *        want.
	 */
	public TableDebugger(final ExplorationRunner explRunner) {
		runner = explRunner;
	}

	/**
	 * Print all possible results from the tables.
	 * 
	 * @param out the stream to print to.
	 * @throws MissingTableException if a referenced table isn't there
	 */
	public void debugTables(final PrintStream out) throws MissingTableException {
		final EncounterTable mainTable = runner.getTable("main");
		debugTable("", "", mainTable, "main", out,
				new HashSet<EncounterTable>());
	}

	/**
	 * Print all possible results from a table.
	 * 
	 * @param before the string to print before each result (from the calling
	 *        table)
	 * @param after the string to print after each result (from the calling
	 *        table)
	 * @param table the table to debug
	 * @param tableName the name of the table
	 * @param out the stream to print to
	 * @param set the set of tables already on the stack, to prevent infinite
	 *        recursion
	 * @throws MissingTableException if a table is missing
	 */
	private void debugTable(final String before, final String after,
			final EncounterTable table, final String tableName,
			final PrintStream out, final Set<EncounterTable> set)
			throws MissingTableException {
		if (set.contains(table)) {
			out.println("table " + tableName
					+ " is already on the stack, skipping ...");
			out.println("The cause was: " + before + "#" + tableName + "#"
					+ after);
			return;
		} else {
			set.add(table);
		}
		for (final String value : table.allEvents()) {
			if (value.contains("#")) {
				final String[] parsed = value.split("#", 3);
				debugTable(before + parsed[0], parsed[2] + after,
						runner.getTable(parsed[1]), parsed[1], out, set);
			} else {
				out.print(before);
				out.print(value);
				out.println(after);
			}
		}
		set.remove(table);
	}
}
