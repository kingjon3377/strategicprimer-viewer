// $codepro.audit.disable numericLiterals
package controller.exploration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import model.exploration.EncounterTable;
import model.map.Tile;
import model.map.TileType;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Jonathan Lovelace
 */
public final class TestTableLoader {
	/**
	 * Error message for IOExceptions.
	 */
	private static final String IO_ERROR_MESSAGE = "I/O exception";
	/**
	 * "one".
	 */
	private static final String ONE_STRING = "one";
	/**
	 * The object we'll use for the tests.
	 */
	private TableLoader loader;

	/**
	 * Constructor: implemented to make a warning go away.
	 */
	public TestTableLoader() {
		setUp();
	}

	/**
	 * Setup method.
	 */
	@Before
	public void setUp() {
		loader = new TableLoader();
	}

	/**
	 * Test method for
	 * {@link controller.exploration.TableLoader#loadQuadrantTable(java.io.BufferedReader)}
	 * . .
	 */
	@Test
	public void testLoadQuadrantTable() {
		final BufferedReader reader = new BufferedReader(new StringReader(
				"quadrant\n2\none\ntwo\nthree\nfour\nfive\nsix"));
		try {
			final EncounterTable result = loader.loadTable(reader);
			assertEquals("loading quadrant table", ONE_STRING,
					result.generateEvent(new Tile(0, 0, TileType.Tundra)));
			// TODO: somehow check that it got properly loaded, beyond this
		} catch (final IOException e) {
			fail(IO_ERROR_MESSAGE);
		} finally {
			try {
				reader.close();
			} catch (final IOException e) {
				fail("I/O error cleaning up after the test");
			}
		}
		final BufferedReader readerTwo = new BufferedReader(new StringReader(
				"quadrant"));
		try {
			loader.loadTable(readerTwo);
			fail("Didn't object to quadrant table without number of rows");
		} catch (final IOException except) {
			assertEquals("Objecting to quadrant table without number of rows",
					"File doesn't start with the number of rows of quadrants",
					except.getMessage());
		} finally {
			try {
				readerTwo.close();
			} catch (final IOException e) {
				fail("I/O error cleaning up after the test");
			}
		}
	}

	/**
	 * Test method for
	 * {@link controller.exploration.TableLoader#loadRandomTable(java.io.BufferedReader)}
	 * .
	 */
	@Test
	public void testLoadRandomTable() {
		final BufferedReader reader = new BufferedReader(new StringReader(
				"random\n0 one\n99 two"));
		try {
			final EncounterTable result = loader.loadTable(reader);
			// ESCA-JAVA0076:
			assertEquals("loading random table", ONE_STRING,
					result.generateEvent(new Tile(30, 30, TileType.Tundra)));
		} catch (final IOException e) {
			fail(IO_ERROR_MESSAGE);
		} finally {
			try {
				reader.close();
			} catch (final IOException e) {
				fail(IO_ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Test method for
	 * {@link controller.exploration.TableLoader#loadTerrainTable(java.io.BufferedReader)}
	 * .
	 */
	@Test
	public void testLoadTerrainTable() {
		final BufferedReader reader = new BufferedReader(new StringReader(
				"terrain\ntundra one\nplains two\nocean three"));
		try {
			final EncounterTable result = loader.loadTable(reader);
			assertEquals("loading terrain table: tundra", ONE_STRING,
					result.generateEvent(new Tile(30, 30, TileType.Tundra)));
			assertEquals("loading terrain table: plains", "two",
					result.generateEvent(new Tile(15, 15, TileType.Plains)));
			assertEquals("loading terrain table: ocean", "three",
					result.generateEvent(new Tile(15, 15, TileType.Ocean)));
		} catch (final IOException e) {
			fail(IO_ERROR_MESSAGE);
		} finally {
			try {
				reader.close();
			} catch (final IOException e) {
				fail(IO_ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Test method for
	 * {@link controller.exploration.TableLoader#loadConstantTable(java.io.BufferedReader)}
	 * .
	 */
	@Test
	public void testLoadConstantTable() {
		final BufferedReader one = new BufferedReader(new StringReader(
				"constant\none"));
		try {
			final EncounterTable result = loader.loadTable(one);
			assertEquals("loading constant table: first test", ONE_STRING,
					result.generateEvent(new Tile(10, 5, TileType.Plains)));
		} catch (final IOException e) {
			fail(IO_ERROR_MESSAGE);
		} finally {
			try {
				one.close();
			} catch (final IOException e) {
				fail(IO_ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Test the bad-input logic in
	 * {@link controller.exploration.TableLoader#loadTable(java.io.BufferedReader)}
	 * .
	 */
	@Test
	public void testInvalidInput() {
		final BufferedReader one = new BufferedReader(new StringReader(""));
		try {
			loader.loadTable(one);
			fail("Accepted empty input");
		} catch (final IOException except) {
			assertEquals("Objects to empty input",
					"File doesn't start by specifying which kind of table.",
					except.getMessage());
		} finally {
			try {
				one.close();
			} catch (final IOException e) {
				fail("I/O error while closing the stream");
			}
		}
		final BufferedReader two = new BufferedReader(new StringReader(
				"2\ninvaliddata\ninvaliddata"));
		try {
			loader.loadTable(two);
			fail("Accepted table without header");
		} catch (final IllegalArgumentException except) {
			assertEquals("Table without header", "unknown table type",
					except.getMessage());
		} catch (final IOException except) {
			fail("I/O error when testing for rejection of headerless tables");
		} finally {
			try {
				two.close();
			} catch (final IOException e) {
				fail("I/O error while closing the stream");
			}
		}
	}

	/**
	 * @return a String representation of this class
	 */
	@Override
	public String toString() {
		return "TestTableLoader";
	}
}
