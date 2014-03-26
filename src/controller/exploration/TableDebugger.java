package controller.exploration;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.exploration.old.EncounterTable;
import model.exploration.old.ExplorationRunner;
import model.exploration.old.MissingTableException;
import util.TypesafeLogger;

import com.sun.istack.internal.NotNull;

/**
 * A driver to help debug exploration tables.
 *
 * @author Jonathan Lovelace
 *
 */
public class TableDebugger {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(TableDebugger.class);
	/**
	 * The exploration runner.
	 */
	private final ExplorationRunner runner;

	/**
	 * A utility driver method that loads all files in tables/ under the current
	 * directory, then checks to see whether any references a nonexistent table,
	 * then does further tests for debugging purposes.
	 *
	 * @param args ignored
	 */
	public static void main(final String[] args) {
		final ExplorationRunner runner = new ExplorationRunner();
		TableLoader.loadAllTables("tables", runner);
		try {
			final PrintStream out = System.out;
			assert out != null;
			new TableDebugger(runner).debugTables(out);
		} catch (final MissingTableException e) {
			LOGGER.log(Level.SEVERE, "Missing table", e);
			System.exit(1);
		}
	}

	/**
	 * Constructor.
	 *
	 * @param explRunner an exploration runner loaded with all the tables we
	 *        want.
	 */
	public TableDebugger(@NotNull final ExplorationRunner explRunner) {
		runner = explRunner;
	}

	/**
	 * Print all possible results from the tables.
	 *
	 * @param ostream the stream to print to.
	 * @throws MissingTableException if a referenced table isn't there
	 */
	public void debugTables(@NotNull final PrintStream ostream)
			throws MissingTableException {
		runner.verboseRecursiveCheck(ostream);
		final EncounterTable mainTable = runner.getTable("main");
		debugTable("", "", mainTable, "main", ostream,
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
	 * @param ostream the stream to print to
	 * @param set the set of tables already on the stack, to prevent infinite
	 *        recursion
	 * @throws MissingTableException if a table is missing
	 */
	private void debugTable(@NotNull final String before,
			@NotNull final String after, @NotNull final EncounterTable table,
			@NotNull final String tableName, @NotNull final PrintStream ostream,
			@NotNull final Set<EncounterTable> set)
			throws MissingTableException {
		if (set.contains(table)) {
			ostream.print("table ");
			ostream.print(tableName);
			ostream.println(" is already on the stack, skipping ...");
			ostream.print("The cause was: ");
			ostream.print(before);
			ostream.print('#');
			ostream.print(tableName);
			ostream.print('#');
			ostream.println(after);
			return;
		} else {
			set.add(table);
		}
		for (final String value : table.allEvents()) {
			if (value.contains("#")) {
				final String[] parsed = value.split("#", 3);
				final String callee = parsed[1];
				assert callee != null;
				debugTable(before + parsed[0], parsed[2] + after,
						runner.getTable(callee), callee, ostream, set);
			} else {
				ostream.print(before);
				ostream.print(value);
				ostream.println(after);
			}
		}
		set.remove(table);
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TableDebugger";
	}
}
