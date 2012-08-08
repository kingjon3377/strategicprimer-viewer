package controller.map.drivers;

import model.exploration.ExplorationRunner;
import controller.exploration.TableLoader;

/**
 * A driver to check that a directory of encounter tables is entirely formatted
 * correctly.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class TablesCheckDriver {
	/**
	 * A utility driver method that loads all files in tables/ under the current
	 * directory, then checks to see whether any references a nonexistent table.
	 * 
	 * @param args ignore
	 */
	public static void main(final String[] args) {
		final ExplorationRunner runner = new ExplorationRunner();
		new TableLoader().loadAllTables("tables", runner);
		// ESCA-JAVA0266:
		runner.verboseRecursiveCheck(System.out);
	}

	/**
	 * Do not instantiate.
	 */
	private TablesCheckDriver() {
		// Do nothing.
	}
}
