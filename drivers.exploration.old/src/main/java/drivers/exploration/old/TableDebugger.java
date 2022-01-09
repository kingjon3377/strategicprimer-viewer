package drivers.exploration.old;

import java.io.IOException;
import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import lovelace.util.IOConsumer;
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
	private final IOConsumer<String> ostream;
	public TableDebugger(IOConsumer<String> ostream) {
		this.ostream = ostream;
	}

	@Override
	public SPOptions getOptions() {
		return EmptyOptions.EMPTY_OPTIONS;
	}

	/**
	 * Print all possible results from a table.
	 * @param runner The exploration-runner to use for testing
	 * @param before The string to print before each result (passed from the calling table)
	 * @param after The string to print after each result (passed from the calling table)
	 * @param table The table to debug
	 * @param tableName The name of that table
	 * @param set The set of tables already on the stack, to prevent infinite recursion
	 */
	private void debugSingleTable(ExplorationRunner runner, String before, String after,
			EncounterTable table, String tableName, Collection<EncounterTable> set)
			throws IOException, MissingTableException {
		if (set.contains(table)) {
			ostream.accept(String.format("table %s is already on the stack, skipping ...",
				tableName));
			ostream.accept(String.format("The cause was: %s#%s#%s", before, tableName, after));
			return;
		}
		Collection<EncounterTable> innerState = Stream.concat(set.stream(),
			Stream.of(table)).collect(Collectors.toSet());
		for (String item : table.getAllEvents()) {
			if (item.contains("#")) {
				String[] parsed = item.split("#", 3);
				debugSingleTable(runner, String.format("%s%s", before, parsed[0]),
					(parsed.length >= 3) ? String.format("%s%s", parsed[2], after) :
						after,
					runner.getTable(parsed[1]), parsed[1], innerState);
			} else {
				ostream.accept(String.format("%s%s%s", before, item, after));
			}
		}
	}

	@Override
	public void startDriver(String... args) throws DriverFailedException {
		if (args.length > 0) {
			throw new IncorrectUsageException(TableDebuggerFactory.USAGE);
		} else if (!Files.isDirectory(Paths.get("tables"))) {
			throw new DriverFailedException(new IllegalStateException("Table debugger requires a tables directory"));
		}
		ExplorationRunner runner = new ExplorationRunner();
		try {
			runner.loadAllTables(Paths.get("tables"));
			runner.verboseGlobalRecursiveCheck(ostream);
			EncounterTable mainTable = runner.getTable("main");
			debugSingleTable(runner, "", "", mainTable, "main", Collections.emptyList());
		} catch (IOException|MissingTableException except) {
			throw new DriverFailedException(except);
		}
	}
}