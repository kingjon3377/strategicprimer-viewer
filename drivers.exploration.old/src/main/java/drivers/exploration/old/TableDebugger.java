package drivers.exploration.old;

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import lovelace.util.ThrowingConsumer;
import drivers.common.DriverFailedException;
import drivers.common.UtilityDriver;
import drivers.common.IncorrectUsageException;
import drivers.common.SPOptions;
import drivers.common.EmptyOptions;

import java.util.Collection;
import java.util.Collections;

/**
 * A driver to help debug "exploration tables", which were the second
 * "exploration results" framework I implemented.
 */
public class TableDebugger implements UtilityDriver {
	private final ThrowingConsumer<String, IOException> ostream;

	public TableDebugger(final ThrowingConsumer<String, IOException> ostream) {
		this.ostream = ostream;
	}

	@Override
	public SPOptions getOptions() {
		return EmptyOptions.EMPTY_OPTIONS;
	}

	/**
	 * Print all possible results from a table.
	 *
	 * @param runner    The exploration-runner to use for testing
	 * @param before    The string to print before each result (passed from the calling table)
	 * @param after     The string to print after each result (passed from the calling table)
	 * @param table     The table to debug
	 * @param tableName The name of that table
	 * @param set       The set of tables already on the stack, to prevent infinite recursion
	 */
	private void debugSingleTable(final ExplorationRunner runner, final String before, final String after,
								  final EncounterTable table, final String tableName,
								  final Collection<EncounterTable> set)
			throws IOException, MissingTableException {
		if (set.contains(table)) {
			ostream.accept("table %s is already on the stack, skipping ...".formatted(
					tableName));
			ostream.accept("The cause was: %s#%s#%s".formatted(before, tableName, after));
			return;
		}
		final Collection<EncounterTable> innerState = Stream.concat(set.stream(),
				Stream.of(table)).collect(Collectors.toSet());
		for (final String item : table.getAllEvents()) {
			if (item.contains("#")) {
				final String[] parsed = item.split("#", 3);
				debugSingleTable(runner, "%s%s".formatted(before, parsed[0]),
						(parsed.length >= 3) ? "%s%s".formatted(parsed[2], after) :
								after,
						runner.getTable(parsed[1]), parsed[1], innerState);
			} else {
				ostream.accept("%s%s%s".formatted(before, item, after));
			}
		}
	}

	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length > 0) {
			throw new IncorrectUsageException(TableDebuggerFactory.USAGE);
		} else if (!Files.isDirectory(Paths.get("tables"))) {
			throw new DriverFailedException(new IllegalStateException("Table debugger requires a tables directory"));
		}
		final ExplorationRunner runner = new ExplorationRunner();
		try {
			runner.loadAllTables(Paths.get("tables"));
			runner.verboseGlobalRecursiveCheck(ostream);
			final EncounterTable mainTable = runner.getTable("main");
			debugSingleTable(runner, "", "", mainTable, "main", Collections.emptyList());
		} catch (final IOException | MissingTableException except) {
			throw new DriverFailedException(except);
		}
	}
}
