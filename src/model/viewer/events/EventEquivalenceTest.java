package model.viewer.events;

import static org.junit.Assert.assertEquals;
import model.exploration.LegacyTable;
import model.viewer.Tile;
import model.viewer.TileType;

import org.junit.Before;
import org.junit.Test;

import controller.exploration.TableLoader;
import controller.map.EventConverter;

/**
 * Test that the Event objects produce the same results text as the LegacyTable.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class EventEquivalenceTest {
	/**
	 * The loader to load the table.
	 */
	private final TableLoader loader = new TableLoader();
	/**
	 * The table that we compare to.
	 */
	private LegacyTable table;
	/**
	 * The converter we use.
	 */
	private EventConverter converter;

	/**
	 * Set up the table.
	 * 
	 * @throws Exception
	 *             specified by spec
	 */
	@Before
	public void setUp() throws Exception {
		table = (LegacyTable) loader
				.loadTable("/home/kingjon/strategic_primer/interim_2009/tables/legacy");
		converter = new EventConverter(table);
	}

	/**
	 * Do the test.
	 */
	@Test
	public void testEquivalence() {
		for (int i = 0; i < 256; i++) {
			Tile tile = new Tile(0, 0, TileType.NotVisible, i);
			assertEquals("object should produce same message as legacy table",
					converter.convertEvent(tile).getText(),
					table.generateEvent(tile));
		}
	}
}
