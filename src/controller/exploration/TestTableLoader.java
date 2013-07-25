// $codepro.audit.disable numericLiterals
package controller.exploration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import model.exploration.old.EncounterTable;
import model.map.Point;
import model.map.PointFactory;
import model.map.Tile;
import model.map.TileType;

import org.junit.Test;

/**
 * @author Jonathan Lovelace
 */
//ESCA-JAVA0137:
public final class TestTableLoader {
	/**
	 * "one".
	 */
	private static final String ONE_STRING = "one";
	/**
	 * The string "static-method", used in an annotation on each method.
	 */
	private static final String ST_MET = "static-method";
	/**
	 * Test method for
	 * {@link controller.exploration.TableLoader#loadQuadrantTable(java.io.BufferedReader)}
	 * . .
	 * @throws IOException on I/O error in the test or in cleaning up after it.
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testLoadQuadrantTable() throws IOException {
		final BufferedReader reader = new BufferedReader(new StringReader(
				"quadrant\n2\none\ntwo\nthree\nfour\nfive\nsix"));
		try {
			final EncounterTable result = TableLoader.loadTable(reader);
			final Point point = PointFactory.point(0, 0);
			assertEquals("loading quadrant table", ONE_STRING,
					result.generateEvent(point, new Tile(TileType.Tundra)));
			// TODO: somehow check that it got properly loaded, beyond this
		} finally {
			reader.close();
		}
		final BufferedReader readerTwo = new BufferedReader(new StringReader(
				"quadrant"));
		try {
			TableLoader.loadTable(readerTwo);
			fail("Didn't object to quadrant table without number of rows");
		} catch (final IOException except) {
			assertEquals("Objecting to quadrant table without number of rows",
					"File doesn't start with the number of rows of quadrants",
					except.getMessage());
		} finally {
			readerTwo.close();
		}
	}

	/**
	 * Test method for
	 * {@link controller.exploration.TableLoader#loadRandomTable(java.io.BufferedReader)}
	 * .
	 * @throws IOException on I/O error in the test or in cleaning up after it.
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testLoadRandomTable() throws IOException {
		final BufferedReader reader = new BufferedReader(new StringReader(
				"random\n0 one\n99 two"));
		try {
			final EncounterTable result = TableLoader.loadTable(reader);
			final Point point = PointFactory.point(30, 30);
			// ESCA-JAVA0076:
			assertEquals("loading random table", ONE_STRING,
					result.generateEvent(point, new Tile(TileType.Tundra)));
		} finally {
			reader.close();
		}
	}

	/**
	 * Test method for
	 * {@link controller.exploration.TableLoader#loadTerrainTable(java.io.BufferedReader)}
	 * .
	 * @throws IOException on I/O error in the test or in cleaning up after it.
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testLoadTerrainTable() throws IOException {
		final BufferedReader reader = new BufferedReader(new StringReader(
				"terrain\ntundra one\nplains two\nocean three"));
		try {
			final EncounterTable result = TableLoader.loadTable(reader);
			final Point one = PointFactory.point(30, 30);
			assertEquals("loading terrain table: tundra", ONE_STRING,
					result.generateEvent(one, new Tile(TileType.Tundra)));
			final Point two = PointFactory.point(15, 15);
			assertEquals("loading terrain table: plains", "two",
					result.generateEvent(two, new Tile(TileType.Plains)));
			assertEquals("loading terrain table: ocean", "three",
					result.generateEvent(two, new Tile(TileType.Ocean)));
		} finally {
			reader.close();
		}
	}

	/**
	 * Test method for
	 * {@link controller.exploration.TableLoader#loadConstantTable(java.io.BufferedReader)}
	 * .
	 * @throws IOException on I/O error in the test or in cleaning up after it.
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testLoadConstantTable() throws IOException {
		final BufferedReader one = new BufferedReader(new StringReader(
				"constant\none"));
		try {
			final EncounterTable result = TableLoader.loadTable(one);
			final Point point = PointFactory.point(10, 5);
			assertEquals("loading constant table: first test", ONE_STRING,
					result.generateEvent(point, new Tile(TileType.Plains)));
		} finally {
			one.close();
		}
	}

	/**
	 * Test the bad-input logic in
	 * {@link controller.exploration.TableLoader#loadTable(java.io.BufferedReader)}
	 * .
	 * @throws IOException on I/O error in the test or in cleaning up after it.
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testInvalidInput() throws IOException {
		final BufferedReader one = new BufferedReader(new StringReader(""));
		try {
			TableLoader.loadTable(one);
			fail("Accepted empty input");
		} catch (final IOException except) {
			assertEquals("Objects to empty input",
					"File doesn't start by specifying which kind of table.",
					except.getMessage());
		} finally {
			one.close();
		}
		final BufferedReader two = new BufferedReader(new StringReader(
				"2\ninvaliddata\ninvaliddata"));
		try {
			TableLoader.loadTable(two);
			fail("Accepted table without header");
		} catch (final IllegalArgumentException except) {
			assertEquals("Table without header", "unknown table type",
					except.getMessage());
		} finally {
			two.close();
		}
	}

	/**
	 *
	 * @return a String representation of this class
	 */
	@Override
	public String toString() {
		return "TestTableLoader";
	}
}
