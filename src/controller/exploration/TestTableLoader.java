package controller.exploration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import model.exploration.QuadrantTable;
import model.exploration.RandomTable;
import model.exploration.TerrainTable;
import model.viewer.Tile;
import model.viewer.TileType;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Jonathan Lovelace
 */
public final class TestTableLoader {
	/**
	 * The object we'll use for the tests
	 */
	private TableLoader loader;
	/**
	 * Constructor: implemented to make a warning go away.
	 */
	public TestTableLoader() {
		setUp();
	}
	/**
	 * Setup method
	 */
	@Before
	public void setUp() {
		loader = new TableLoader();
	}

	/**
	 * Test method for
	 * {@link controller.exploration.TableLoader#loadQuadrantTable(java.io.BufferedReader)}
	 * .
	 */
	@Test
	public void testLoadQuadrantTable() {
		final BufferedReader reader = new BufferedReader(new StringReader(
				"2\none\ntwo\nthree\nfour\nfive\nsix"));
		try {
			final QuadrantTable result = loader.loadQuadrantTable(reader);
			assertEquals("loading quadrant table", "one",
					result.generateEvent(new Tile(0, 0, TileType.Tundra)));
			// TODO: somehow check that it got properly loaded, beyond this
		} catch (IOException e) {
			fail("I/O exception");
		}
	}

	/**
	 * Test method for
	 * {@link controller.exploration.TableLoader#loadRandomTable(java.io.BufferedReader)}
	 * .
	 */
	@Test
	public void testLoadRandomTable() {
		final BufferedReader reader = new BufferedReader(new StringReader("0 one\n99 two"));
		try {
			final RandomTable result = loader.loadRandomTable(reader);
			// ESCA-JAVA0076:
			assertEquals("loading random table", "one", result.generateEvent(new Tile(30, 30, TileType.Tundra)));
		} catch (IOException e) {
			fail("I/O exception");
		}
	}

	/**
	 * Test method for
	 * {@link controller.exploration.TableLoader#loadTerrainTable(java.io.BufferedReader)}
	 * .
	 */
	@Test
	public void testLoadTerrainTable() {
		final BufferedReader reader = new BufferedReader(new StringReader("tundra one\nplains two\nocean three"));
		try {
			final TerrainTable result = loader.loadTerrainTable(reader);
			assertEquals("loading terrain table: tundra", "one", result.generateEvent(new Tile(30, 30, TileType.Tundra)));
			assertEquals("loading terrain table: plains", "two", result.generateEvent(new Tile(15, 15, TileType.Plains)));
			assertEquals("loading terrain table: ocean", "three", result.generateEvent(new Tile(15, 15, TileType.Ocean)));
		} catch (IOException e) {
			fail("I/O exception");
		}
	}

}
